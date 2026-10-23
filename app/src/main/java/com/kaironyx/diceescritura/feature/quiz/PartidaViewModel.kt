package com.kaironyx.diceescritura.feature.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kaironyx.diceescritura.core.flags.FeatureFlags
import com.kaironyx.diceescritura.core.flags.modulosHabilitados
import com.kaironyx.diceescritura.core.navigation.Destino
import com.kaironyx.diceescritura.data.bible.BibleRepository
import com.kaironyx.diceescritura.data.datastore.AjustesRepository
import com.kaironyx.diceescritura.data.db.TipoSesion
import com.kaironyx.diceescritura.data.repository.PreguntaRepository
import com.kaironyx.diceescritura.data.repository.ProgresoRepository
import com.kaironyx.diceescritura.data.repository.RetoDiarioRepository
import com.kaironyx.diceescritura.data.repository.SesionTerminada
import com.kaironyx.diceescritura.domain.model.Categoria
import com.kaironyx.diceescritura.domain.model.Nivel
import com.kaironyx.diceescritura.domain.model.Pregunta
import com.kaironyx.diceescritura.domain.model.ResultadoReferencia
import com.kaironyx.diceescritura.domain.model.ResultadoTexto
import com.kaironyx.diceescritura.domain.usecase.ParserReferencias
import com.kaironyx.diceescritura.domain.usecase.FiltroJuego
import com.kaironyx.diceescritura.domain.usecase.GestorNiveles
import com.kaironyx.diceescritura.domain.usecase.Respuesta
import com.kaironyx.diceescritura.domain.usecase.ResultadoRespuesta
import com.kaironyx.diceescritura.domain.usecase.ResumenSesion
import com.kaironyx.diceescritura.domain.usecase.SelectorPreguntas
import com.kaironyx.diceescritura.domain.usecase.SesionJuego
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

/** Pasaje citado por una pregunta; [texto] es el de la RVR1960 local, o nulo si no se pudo leer. */
data class CitaLeida(val referencia: String, val texto: String?)

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
        /** Pasajes de la pregunta ya leídos del texto local; vacío hasta responder. */
        val citas: List<CitaLeida> = emptyList(),
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
        /** Días de racha tras completar el reto diario; nulo en los demás modos. */
        val racha: Int? = null,
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
    private val biblia: BibleRepository,
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
    private var citas: List<CitaLeida> = emptyList()

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
        seguimiento: Map<String, com.kaironyx.diceescritura.domain.usecase.SeguimientoPregunta>,
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
        citas = pregunta.referencias.map { CitaLeida(it, null) }
        publicar()
        viewModelScope.launch {
            val leidas = pregunta.referencias.map { CitaLeida(it, leerPasaje(it)) }
            if (sesion?.pregunta?.id == pregunta.id) {
                citas = leidas
                publicar()
            }
        }
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

    /** Orden que lleva el jugador en ordenar; hasta que mueve algo, el barajado inicial. */
    private fun ordenActual(s: SesionJuego): List<String> = orden.ifEmpty { s.disposicionActual }

    /** En ordenar: mueve el elemento de la posición [desde] a la posición [hacia]. */
    fun moverElemento(desde: Int, hacia: Int) {
        val s = sesion ?: return
        if (s.resultadoActual != null) return
        val lista = ordenActual(s).toMutableList()
        if (desde !in lista.indices || hacia !in lista.indices) return
        lista.add(hacia, lista.removeAt(desde))
        orden = lista
        publicar()
    }

    fun comprobarOrden() {
        val s = sesion ?: return
        responder(Respuesta.Orden(ordenActual(s)))
    }

    fun siguiente() {
        val s = sesion ?: return
        if (s.resultadoActual == null) return
        if (s.avanzar()) {
            orden = emptyList()
            respuestaDada = null
            citas = emptyList()
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
            var racha: Int? = null
            if (modo == ModoJuego.RETO_DIARIO && fecha != null) {
                retos.completar(fecha)
                racha = retos.observarRacha().first().actual
            }
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
                racha = racha,
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
            orden = ordenActual(s),
            respuestaDada = respuestaDada,
            resultado = s.resultadoActual,
            esUltima = s.esUltima,
            citas = if (s.resultadoActual != null) citas else emptyList(),
        )
    }

    /** Texto del pasaje; nulo si la referencia abarca capítulos completos o el texto no está disponible. */
    private suspend fun leerPasaje(texto: String): String? {
        val ref = (ParserReferencias.analizar(texto) as? ResultadoReferencia.Valida)?.referencia ?: return null
        if (ref.versiculoInicio == null || ref.versiculoFin == null || ref.capituloInicio != ref.capituloFin) return null
        return (biblia.textoDe(ref) as? ResultadoTexto.Disponible)?.versiculos
            ?.joinToString(" ") { "${it.numero} ${it.texto}" }
    }

    private companion object {
        const val PREGUNTAS_POR_PARTIDA = 10
        const val MINIMO_EXAMEN = 5
    }
}
