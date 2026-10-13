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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class MotivoSinPreguntas { SIN_PREGUNTAS, EXAMEN_INSUFICIENTE, CATEGORIA_BLOQUEADA }

sealed interface PartidaUiState {
    data object Cargando : PartidaUiState

    data class SinPreguntas(val motivo: MotivoSinPreguntas) : PartidaUiState

    data class EnCurso(
        val numero: Int,
        val total: Int,
        val esExamen: Boolean,
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
        val resumen: ResumenSesion,
        val esExamen: Boolean,
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
    private val ajustes: AjustesRepository,
    private val flags: FeatureFlags,
    private val gestor: GestorNiveles,
) : ViewModel() {

    private val destino = savedStateHandle.toRoute<Destino.Partida>()
    private val categoria: Categoria? = destino.categoria?.let { Categoria.desdeId(it) }
    private val esExamen: Boolean = destino.examen

    private val _estado = MutableStateFlow<PartidaUiState>(PartidaUiState.Cargando)
    val estado: StateFlow<PartidaUiState> = _estado.asStateFlow()

    private var sesion: SesionJuego? = null
    private var orden: List<String> = emptyList()
    private var respuestaDada: Respuesta? = null
    private var terminando = false

    init {
        viewModelScope.launch { iniciar() }
    }

    private suspend fun iniciar() {
        val perfil = progreso.perfilActual()
        val modulos = flags.modulosHabilitados().first()
        val soloRevisadas = ajustes.soloRevisadas.first()
        val desbloqueadas = gestor.categoriasDesbloqueadas(perfil.nivel)
        if (!esExamen && (categoria == null || categoria !in desbloqueadas)) {
            _estado.value = PartidaUiState.SinPreguntas(MotivoSinPreguntas.CATEGORIA_BLOQUEADA)
            return
        }
        val seguimiento = progreso.seguimiento()
        val filtro = FiltroJuego(
            modulosHabilitados = modulos,
            soloRevisadas = soloRevisadas,
            dificultadMaxima = gestor.nivel(perfil.nivel).dificultadMaxima,
            categoriasPermitidas = desbloqueadas,
            categoria = if (esExamen) null else categoria,
        )
        val elegidas = withContext(Dispatchers.Default) {
            val banco = preguntas.preguntas(modulos, soloRevisadas)
            val selector = SelectorPreguntas()
            if (esExamen) {
                selector.examen(banco, filtro, gestor.config.examenPreguntas)
            } else {
                selector.partida(banco, filtro, seguimiento, progreso.hoy(), PREGUNTAS_POR_PARTIDA)
            }
        }
        val minimo = if (esExamen) minOf(gestor.config.examenPreguntas, MINIMO_EXAMEN) else 1
        if (elegidas.size < minimo) {
            _estado.value = PartidaUiState.SinPreguntas(
                if (esExamen) MotivoSinPreguntas.EXAMEN_INSUFICIENTE else MotivoSinPreguntas.SIN_PREGUNTAS,
            )
            return
        }
        sesion = SesionJuego(elegidas, esExamen, gestor)
        publicar()
    }

    fun responder(respuesta: Respuesta) {
        val s = sesion ?: return
        if (s.resultadoActual != null) return
        val pregunta = s.pregunta
        val resultado = s.responder(respuesta)
        respuestaDada = respuesta
        publicar()
        viewModelScope.launch { progreso.registrarRespuesta(pregunta.id, resultado.correcta, resultado.xp) }
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
        viewModelScope.launch {
            var aprobado: Boolean? = null
            var nivelNuevo: Nivel? = null
            if (esExamen) {
                val resultado = gestor.evaluarExamen(resumen.aciertos, resumen.total)
                aprobado = resultado.aprobado
                if (resultado.aprobado) nivelNuevo = gestor.nivel(progreso.ascender().nivel)
            }
            progreso.guardarSesion(
                SesionTerminada(
                    tipo = if (esExamen) TipoSesion.EXAMEN else TipoSesion.PARTIDA,
                    categoria = if (esExamen) null else categoria,
                    aciertos = resumen.aciertos,
                    total = resumen.total,
                    xpGanado = resumen.xpGanado,
                    aprobado = aprobado,
                ),
            )
            _estado.value = PartidaUiState.Terminada(
                resumen = resumen,
                esExamen = esExamen,
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
            esExamen = esExamen,
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
