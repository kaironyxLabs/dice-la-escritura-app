package com.example.lampara.feature.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.lampara.core.flags.FeatureFlags
import com.example.lampara.core.flags.modulosHabilitados
import com.example.lampara.core.navigation.Destino
import com.example.lampara.data.datastore.AjustesRepository
import com.example.lampara.data.db.TipoSesion
import com.example.lampara.data.repository.PreguntaRepository
import com.example.lampara.data.repository.ProgresoRepository
import com.example.lampara.data.repository.RetoDiarioRepository
import com.example.lampara.data.repository.SesionTerminada
import com.example.lampara.domain.model.Categoria
import com.example.lampara.domain.model.Nivel
import com.example.lampara.domain.model.Pregunta
import com.example.lampara.domain.usecase.FiltroJuego
import com.example.lampara.domain.usecase.GestorNiveles
import com.example.lampara.domain.usecase.Respuesta
import com.example.lampara.domain.usecase.ResultadoRespuesta
import com.example.lampara.domain.usecase.ResumenSesion
import com.example.lampara.domain.usecase.SelectorPreguntas
import com.example.lampara.domain.usecase.SesionJuego
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

enum class MotivoSinPreguntas { SIN_PREGUNTAS, EXAMEN_INSUFICIENTE, CATEGORIA_BLOQUEADA, RETO_COMPLETADO }

enum class ModoJuego { CATEGORIA, EXAMEN, RETO_DIARIO }

sealed interface PartidaUiState {
    data object Cargando : PartidaUiState

    data class SinPreguntas(val motivo: MotivoSinPreguntas) : PartidaUiState

    data class EnCurso(
        val numero: Int,
        val total: Int,
        val modo: ModoJuego,
        val pregunta: Pregunta,
        /** Opciones u elementos barajados; vacío en verdadero/falso. */
        val disposicion: List<String>,
        /** Elementos ya elegidos, en orden, en las preguntas de ordenar. */
        val orden: List<String>,
        val respuestaDada: Respuesta?,
        val resultado: ResultadoRespuesta?,
        val esUltima: Boolean,
    ) : PartidaUiState

    data class Terminada(
        /** Con el XP que de verdad entró en la barra. */
        val resumen: ResumenSesion,
        /** XP que no se sumó porque la barra del nivel ya estaba llena. */
        val xpPerdido: Int,
        val modo: ModoJuego,
        val aprobado: Boolean?,
        val nivelNuevo: Nivel?,
        val porcentajeAprobacion: Int,
    ) : PartidaUiState
}

@HiltViewModel
class PartidaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val preguntas: PreguntaRepository,
    private val progreso: ProgresoRepository,
    private val retos: RetoDiarioRepository,
    private val ajustes: AjustesRepository,
    private val flags: FeatureFlags,
    private val gestor: GestorNiveles,
) : ViewModel() {

    private val destino = savedStateHandle.toRoute<Destino.Partida>()
    private val categoria: Categoria? = destino.categoria?.let { Categoria.desdeId(it) }
    private val modo = when {
        destino.reto -> ModoJuego.RETO_DIARIO
        destino.examen -> ModoJuego.EXAMEN
        else -> ModoJuego.CATEGORIA
    }

    private val _estado = MutableStateFlow<PartidaUiState>(PartidaUiState.Cargando)
    val estado: StateFlow<PartidaUiState> = _estado.asStateFlow()

    private var sesion: SesionJuego? = null
    private var orden: List<String> = emptyList()
    private var respuestaDada: Respuesta? = null
    private var terminando = false

    /** Último guardado de una respuesta; los guardados se encadenan para terminar con el XP ya contado. */
    private var registro: Job? = null

    /** XP ganado que no entró en la barra porque el nivel estaba lleno (espera al examen de ascenso). */
    private var xpPerdido = 0

    /** Día del reto en juego; no cambia aunque la partida cruce la medianoche. */
    private var fechaReto: LocalDate? = null

    init {
        viewModelScope.launch { iniciar() }
    }

    private suspend fun iniciar() {
        val perfil = progreso.perfilActual()
        val modulos = flags.modulosHabilitados().first()
        val soloRevisadas = ajustes.soloRevisadas.first()
        val desbloqueadas = gestor.categoriasDesbloqueadas(perfil.nivel)
        if (modo == ModoJuego.CATEGORIA && (categoria == null || categoria !in desbloqueadas)) {
            _estado.value = PartidaUiState.SinPreguntas(MotivoSinPreguntas.CATEGORIA_BLOQUEADA)
            return
        }
        val seguimiento = progreso.seguimiento()
        val filtro = FiltroJuego(
            modulosHabilitados = modulos,
            soloRevisadas = soloRevisadas,
            dificultadMaxima = gestor.nivel(perfil.nivel).dificultadMaxima,
            categoriasPermitidas = desbloqueadas,
            categoria = if (modo == ModoJuego.CATEGORIA) categoria else null,
        )
        val banco = withContext(Dispatchers.Default) { preguntas.preguntas(modulos, soloRevisadas) }

        if (modo == ModoJuego.RETO_DIARIO) {
            iniciarReto(banco, filtro, seguimiento)
            return
        }

        val elegidas = withContext(Dispatchers.Default) {
            val selector = SelectorPreguntas()
            if (modo == ModoJuego.EXAMEN) {
                selector.examen(banco, filtro, gestor.config.examenPreguntas)
            } else {
                selector.partida(banco, filtro, seguimiento, progreso.hoy(), PREGUNTAS_POR_PARTIDA)
            }
        }
        val minimo = if (modo == ModoJuego.EXAMEN) minOf(gestor.config.examenPreguntas, MINIMO_EXAMEN) else 1
        if (elegidas.size < minimo) {
            _estado.value = PartidaUiState.SinPreguntas(
                if (modo == ModoJuego.EXAMEN) MotivoSinPreguntas.EXAMEN_INSUFICIENTE else MotivoSinPreguntas.SIN_PREGUNTAS,
            )
            return
        }
        sesion = SesionJuego(elegidas, esExamen = modo == ModoJuego.EXAMEN, gestor = gestor)
        publicar()
    }

    /** Retoma el reto de hoy o lo crea. Las preguntas quedan fijadas al crearlo. */
    private suspend fun iniciarReto(
        banco: List<Pregunta>,
        filtro: FiltroJuego,
        seguimiento: Map<String, com.example.lampara.domain.usecase.SeguimientoPregunta>,
    ) {
        val reto = retos.retoDeHoy() ?: run {
            val ids = withContext(Dispatchers.Default) {
                SelectorPreguntas().reto(banco, filtro, seguimiento, progreso.hoy(), PREGUNTAS_POR_PARTIDA)
            }.map { it.id }
            if (ids.isEmpty()) {
                _estado.value = PartidaUiState.SinPreguntas(MotivoSinPreguntas.SIN_PREGUNTAS)
                return
            }
            retos.crearRetoDeHoy(ids)
        }
        fechaReto = reto.fecha
        if (reto.completado) {
            _estado.value = PartidaUiState.SinPreguntas(MotivoSinPreguntas.RETO_COMPLETADO)
            return
        }
        val porId = banco.associateBy { it.id }
        val pendientes = reto.preguntaIds.drop(reto.respondidas).mapNotNull { porId[it] }
        if (pendientes.isEmpty()) {
            // Lo que faltaba ya no está disponible (por ejemplo, se apagó un módulo): se da por terminado.
            retos.completar(reto.fecha)
            _estado.value = PartidaUiState.SinPreguntas(MotivoSinPreguntas.RETO_COMPLETADO)
            return
        }
        sesion = SesionJuego(
            preguntas = pendientes,
            esExamen = false,
            gestor = gestor,
            respondidasPrevias = reto.respondidas,
            aciertosPrevios = reto.aciertos,
            xpPrevio = reto.xpGanado,
        )
        publicar()
    }

    fun responder(respuesta: Respuesta) {
        val s = sesion ?: return
        if (s.resultadoActual != null) return
        val pregunta = s.pregunta
        val resultado = s.responder(respuesta)
        respuestaDada = respuesta
        publicar()
        val fecha = fechaReto
        val anterior = registro
        registro = viewModelScope.launch {
            anterior?.join()
            val aplicado = progreso.registrarRespuesta(pregunta.id, resultado.correcta, resultado.xp)
            xpPerdido += resultado.xp - aplicado
            if (modo == ModoJuego.RETO_DIARIO && fecha != null) {
                retos.registrarRespuesta(resultado.correcta, aplicado, fecha)
            }
        }
    }

    /** En ordenar: añade el elemento a la lista del jugador o, si ya estaba, lo quita. */
    fun tocarElemento(elemento: String) {
        val s = sesion ?: return
        if (s.resultadoActual != null) return
        orden = if (elemento in orden) orden - elemento else orden + elemento
        publicar()
    }

    fun comprobarOrden() {
        val s = sesion ?: return
        if (orden.size == s.disposicionActual.size) responder(Respuesta.Orden(orden))
    }

    fun siguiente() {
        val s = sesion ?: return
        if (s.resultadoActual == null) return
        if (s.avanzar()) {
            orden = emptyList()
            respuestaDada = null
            publicar()
        } else {
            terminar(s)
        }
    }

    private fun terminar(s: SesionJuego) {
        if (terminando) return
        terminando = true
        val resumen = s.resumen
        val fecha = fechaReto
        val pendiente = registro
        viewModelScope.launch {
            pendiente?.join()
            val perdido = xpPerdido
            var aprobado: Boolean? = null
            var nivelNuevo: Nivel? = null
            if (modo == ModoJuego.EXAMEN) {
                val resultado = gestor.evaluarExamen(resumen.aciertos, resumen.total)
                aprobado = resultado.aprobado
                if (resultado.aprobado) nivelNuevo = gestor.nivel(progreso.ascender().nivel)
            }
            if (modo == ModoJuego.RETO_DIARIO && fecha != null) retos.completar(fecha)
            progreso.guardarSesion(
                SesionTerminada(
                    tipo = when (modo) {
                        ModoJuego.EXAMEN -> TipoSesion.EXAMEN
                        ModoJuego.RETO_DIARIO -> TipoSesion.RETO_DIARIO
                        ModoJuego.CATEGORIA -> TipoSesion.PARTIDA
                    },
                    categoria = if (modo == ModoJuego.CATEGORIA) categoria else null,
                    aciertos = resumen.aciertos,
                    total = resumen.total,
                    xpGanado = resumen.xpGanado - perdido,
                    aprobado = aprobado,
                ),
            )
            _estado.value = PartidaUiState.Terminada(
                resumen = resumen.copy(xpGanado = resumen.xpGanado - perdido),
                xpPerdido = perdido,
                modo = modo,
                aprobado = aprobado,
                nivelNuevo = nivelNuevo,
                porcentajeAprobacion = gestor.config.examenAprobacionPorcentaje,
            )
        }
    }

    private fun publicar() {
        val s = sesion ?: return
        _estado.value = PartidaUiState.EnCurso(
            numero = s.indice + 1,
            total = s.total,
            modo = modo,
            pregunta = s.pregunta,
            disposicion = s.disposicionActual,
            orden = orden,
            respuestaDada = respuestaDada,
            resultado = s.resultadoActual,
            esUltima = s.esUltima,
        )
    }

    private companion object {
        const val PREGUNTAS_POR_PARTIDA = 10
        const val MINIMO_EXAMEN = 5
    }
}
