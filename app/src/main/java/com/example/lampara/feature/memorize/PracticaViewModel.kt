package com.example.lampara.feature.memorize

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.lampara.core.navigation.Destino
import com.example.lampara.data.bible.BibleRepository
import com.example.lampara.data.content.Pasaje
import com.example.lampara.data.content.PasajesRepository
import com.example.lampara.data.repository.MemorizacionRepository
import com.example.lampara.domain.model.ResultadoTexto
import com.example.lampara.domain.usecase.EvaluadorMemoria
import com.example.lampara.domain.usecase.GradoMemoria
import com.example.lampara.domain.usecase.Ocultador
import com.example.lampara.domain.usecase.ResultadoMemoria
import com.example.lampara.domain.usecase.TextoOculto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class MotivoSinTexto { ARCHIVO_FALTANTE, PASAJE_NO_DISPONIBLE, PASAJE_DESCONOCIDO }

data class ResultadoPractica(
    val resultado: ResultadoMemoria,
    /** Grado al que pasa la siguiente práctica (sube si acertó, baja si falló). */
    val gradoSiguiente: GradoMemoria,
    /** Próximo repaso, solo cuando se acertó escribiendo el pasaje completo. */
    val proximoRepaso: LocalDate?,
)

sealed interface PracticaUiState {
    data object Cargando : PracticaUiState

    data class SinTexto(val motivo: MotivoSinTexto) : PracticaUiState

    data class Practicando(
        val referencia: String,
        val grado: GradoMemoria,
        /** null al escribir el pasaje completo (no se muestra nada del texto). */
        val oculto: TextoOculto?,
        val entrada: String,
        val textoCompleto: String,
        val resultado: ResultadoPractica?,
    ) : PracticaUiState
}

@HiltViewModel
class PracticaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    pasajes: PasajesRepository,
    private val biblia: BibleRepository,
    private val memorizacion: MemorizacionRepository,
) : ViewModel() {

    private val pasaje: Pasaje? = pasajes.porId(savedStateHandle.toRoute<Destino.Practica>().pasajeId)

    private val _estado = MutableStateFlow<PracticaUiState>(PracticaUiState.Cargando)
    val estado: StateFlow<PracticaUiState> = _estado.asStateFlow()

    private var textoCompleto = ""
    private var intento = 0

    init {
        viewModelScope.launch { iniciar() }
    }

    private suspend fun iniciar() {
        val pasaje = pasaje ?: run {
            _estado.value = PracticaUiState.SinTexto(MotivoSinTexto.PASAJE_DESCONOCIDO)
            return
        }
        when (val texto = biblia.textoDe(pasaje.referencia)) {
            ResultadoTexto.ArchivoFaltante ->
                _estado.value = PracticaUiState.SinTexto(MotivoSinTexto.ARCHIVO_FALTANTE)
            is ResultadoTexto.NoDisponible ->
                _estado.value = PracticaUiState.SinTexto(MotivoSinTexto.PASAJE_NO_DISPONIBLE)
            is ResultadoTexto.Disponible -> {
                textoCompleto = texto.texto
                val grado = memorizacion.obtener(pasaje.id)?.estado?.grado ?: 0
                memorizacion.establecerMemorizando(pasaje.id, true)
                empezar(GradoMemoria.desdeIndice(grado))
            }
        }
    }

    private fun empezar(grado: GradoMemoria) {
        val pasaje = pasaje ?: return
        intento++
        _estado.value = PracticaUiState.Practicando(
            referencia = pasaje.referenciaTexto,
            grado = grado,
            oculto = if (grado.esEscribir) null else {
                Ocultador.ocultar(textoCompleto, grado.porcentaje, semilla = pasaje.id.hashCode() * 31L + intento)
            },
            entrada = "",
            textoCompleto = textoCompleto,
            resultado = null,
        )
    }

    fun cambiarEntrada(texto: String) {
        val actual = _estado.value as? PracticaUiState.Practicando ?: return
        if (actual.resultado == null) _estado.value = actual.copy(entrada = texto)
    }

    fun comprobar() {
        val actual = _estado.value as? PracticaUiState.Practicando ?: return
        if (actual.resultado != null || actual.entrada.isBlank()) return
        val esperadas = actual.oculto?.palabrasOcultas ?: EvaluadorMemoria.palabras(actual.textoCompleto)
        val resultado = EvaluadorMemoria.evaluar(esperadas, actual.entrada)
        viewModelScope.launch {
            val id = pasaje?.id ?: return@launch
            memorizacion.registrarPractica(id, resultado.aprobado)
            val estado = memorizacion.obtener(id)?.estado
            _estado.value = actual.copy(
                resultado = ResultadoPractica(
                    resultado = resultado,
                    gradoSiguiente = GradoMemoria.desdeIndice(estado?.grado ?: 0),
                    proximoRepaso = if (resultado.aprobado && actual.grado.esEscribir) estado?.proximaFecha else null,
                ),
            )
        }
    }

    /** Pasa al grado que corresponde tras el resultado (el siguiente si acertó, el anterior si falló). */
    fun continuar() {
        val actual = _estado.value as? PracticaUiState.Practicando ?: return
        val resultado = actual.resultado ?: return
        empezar(resultado.gradoSiguiente)
    }
}
