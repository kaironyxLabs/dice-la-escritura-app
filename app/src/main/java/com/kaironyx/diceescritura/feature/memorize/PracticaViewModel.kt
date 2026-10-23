package com.kaironyx.diceescritura.feature.memorize

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.kaironyx.diceescritura.core.navigation.Destino
import com.kaironyx.diceescritura.data.bible.BibleRepository
import com.kaironyx.diceescritura.data.content.Pasaje
import com.kaironyx.diceescritura.data.content.PasajesRepository
import com.kaironyx.diceescritura.data.repository.MemorizacionRepository
import com.kaironyx.diceescritura.domain.model.ResultadoTexto
import com.kaironyx.diceescritura.domain.usecase.EvaluadorMemoria
import com.kaironyx.diceescritura.domain.usecase.GradoMemoria
import com.kaironyx.diceescritura.domain.usecase.Ocultador
import com.kaironyx.diceescritura.domain.usecase.ResultadoMemoria
import com.kaironyx.diceescritura.domain.usecase.TextoOculto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.random.Random
import javax.inject.Inject

enum class MotivoSinTexto { ARCHIVO_FALTANTE, PASAJE_NO_DISPONIBLE, PASAJE_DESCONOCIDO }

data class ResultadoPractica(
    val resultado: ResultadoMemoria,
    /** Grado al que pasa la siguiente práctica (sube si acertó, baja si falló). */
    val gradoSiguiente: GradoMemoria,
    /** Próximo repaso, solo cuando se acertó escribiendo el pasaje completo. */
    val proximoRepaso: LocalDate?,
    /** Índice de token del pasaje -> si esa palabra se acertó; solo las palabras que se pedían. */
    val marcas: Map<Int, Boolean>,
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
        /** Ocultar palabras: se eligen de un banco en vez de escribirse. */
        val modoBanco: Boolean = false,
        /** Palabras ocultas barajadas (banco de palabras). */
        val banco: List<String> = emptyList(),
        /** Posiciones del banco ya colocadas, en el orden de los huecos. */
        val usadas: List<Int> = emptyList(),
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

    private fun empezar(grado: GradoMemoria, modoBanco: Boolean = false) {
        val pasaje = pasaje ?: return
        intento++
        val semilla = pasaje.id.hashCode() * 31L + intento
        val oculto = if (grado.esEscribir) null else Ocultador.ocultar(textoCompleto, grado.porcentaje, semilla = semilla)
        _estado.value = PracticaUiState.Practicando(
            referencia = pasaje.referenciaTexto,
            grado = grado,
            oculto = oculto,
            entrada = "",
            textoCompleto = textoCompleto,
            resultado = null,
            modoBanco = modoBanco && oculto != null,
            banco = oculto?.palabrasOcultas?.shuffled(Random(semilla)).orEmpty(),
        )
    }

    /** Cambia entre escribir las palabras y elegirlas de un banco; vuelve a empezar el grado. */
    fun alternarBanco() {
        val actual = _estado.value as? PracticaUiState.Practicando ?: return
        if (actual.resultado != null || actual.oculto == null) return
        _estado.value = actual.copy(modoBanco = !actual.modoBanco, entrada = "", usadas = emptyList())
    }

    fun elegirPalabra(posicion: Int) {
        val actual = _estado.value as? PracticaUiState.Practicando ?: return
        if (actual.resultado != null || !actual.modoBanco) return
        if (posicion !in actual.banco.indices || posicion in actual.usadas) return
        _estado.value = actual.copy(usadas = actual.usadas + posicion)
    }

    fun deshacerPalabra() {
        val actual = _estado.value as? PracticaUiState.Practicando ?: return
        if (actual.resultado != null || actual.usadas.isEmpty()) return
        _estado.value = actual.copy(usadas = actual.usadas.dropLast(1))
    }

    fun cambiarEntrada(texto: String) {
        val actual = _estado.value as? PracticaUiState.Practicando ?: return
        if (actual.resultado == null) _estado.value = actual.copy(entrada = texto)
    }

    fun comprobar() {
        val actual = _estado.value as? PracticaUiState.Practicando ?: return
        val escrito = if (actual.modoBanco) actual.usadas.joinToString(" ") { actual.banco[it] } else actual.entrada
        if (actual.resultado != null || escrito.isBlank()) return
        val esperadas = actual.oculto?.palabrasOcultas ?: EvaluadorMemoria.palabras(actual.textoCompleto)
        val resultado = EvaluadorMemoria.evaluar(esperadas, escrito)
        // Tokens del pasaje que se pedían, en el mismo orden y con el mismo filtro que usa el evaluador.
        val tokens = actual.oculto?.tokens ?: Ocultador.tokenizar(actual.textoCompleto)
        val pedidos = (actual.oculto?.ocultas?.sorted() ?: tokens.indices.filter { tokens[it].esPalabra })
            .filter { EvaluadorMemoria.normalizar(tokens[it].texto).isNotEmpty() }
        val marcas = pedidos.zip(EvaluadorMemoria.marcar(esperadas, escrito)).toMap()
        viewModelScope.launch {
            val id = pasaje?.id ?: return@launch
            memorizacion.registrarPractica(id, resultado.aprobado)
            val estado = memorizacion.obtener(id)?.estado
            _estado.value = actual.copy(
                resultado = ResultadoPractica(
                    resultado = resultado,
                    gradoSiguiente = GradoMemoria.desdeIndice(estado?.grado ?: 0),
                    proximoRepaso = if (resultado.aprobado && actual.grado.esEscribir) estado?.proximaFecha else null,
                    marcas = marcas,
                ),
            )
        }
    }

    /** Pasa al grado que corresponde tras el resultado (el siguiente si acertó, el anterior si falló). */
    fun continuar() {
        val actual = _estado.value as? PracticaUiState.Practicando ?: return
        val resultado = actual.resultado ?: return
        empezar(resultado.gradoSiguiente, modoBanco = actual.modoBanco)
    }
}
