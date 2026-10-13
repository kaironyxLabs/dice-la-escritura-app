package com.example.lampara.feature.memorize

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lampara.data.bible.BibleRepository
import com.example.lampara.data.bible.CargaTexto
import com.example.lampara.data.content.Pasaje
import com.example.lampara.data.content.PasajesRepository
import com.example.lampara.data.repository.MemorizacionRepository
import com.example.lampara.data.repository.ProgresoPasaje
import com.example.lampara.domain.model.MotivoNoDisponible
import com.example.lampara.domain.model.ResultadoTexto
import com.example.lampara.domain.usecase.Memorizacion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

enum class EstadoTexto { INSTALADO, FALTANTE, INVALIDO }

enum class Disponibilidad {
    DISPONIBLE,

    /** Incluye un versículo que el importador marcó como pendiente de verificación. */
    PENDIENTE,
    INEXISTENTE,
    SIN_TEXTO,
}

data class ItemPasaje(
    val pasaje: Pasaje,
    val memorizando: Boolean,
    val disponibilidad: Disponibilidad,
    /** Hay repaso pendiente hoy. */
    val vencido: Boolean,
    /** Fecha del próximo repaso si ya lo practicó. */
    val proximoRepaso: LocalDate?,
)

sealed interface MemorizarUiState {
    data object Cargando : MemorizarUiState

    data class Listo(val texto: EstadoTexto, val items: List<ItemPasaje>) : MemorizarUiState
}

@HiltViewModel
class MemorizarViewModel @Inject constructor(
    private val pasajes: PasajesRepository,
    private val biblia: BibleRepository,
    private val memorizacion: MemorizacionRepository,
) : ViewModel() {

    private class Base(val texto: EstadoTexto, val disponibilidad: Map<String, Disponibilidad>)

    private val base = MutableStateFlow<Base?>(null)

    init {
        viewModelScope.launch {
            base.value = withContext(Dispatchers.Default) { calcularBase() }
        }
    }

    val estado: StateFlow<MemorizarUiState> = combine(base, memorizacion.observar()) { base, progreso ->
        if (base == null) {
            MemorizarUiState.Cargando
        } else {
            val hoy = memorizacion.hoy()
            MemorizarUiState.Listo(
                texto = base.texto,
                items = pasajes.pasajes.map { pasaje ->
                    val avance: ProgresoPasaje? = progreso[pasaje.id]
                    ItemPasaje(
                        pasaje = pasaje,
                        memorizando = avance?.memorizando == true,
                        disponibilidad = base.disponibilidad.getValue(pasaje.id),
                        vencido = avance != null && Memorizacion.estaVencido(avance.estado, hoy),
                        proximoRepaso = avance?.estado?.proximaFecha,
                    )
                },
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MemorizarUiState.Cargando)

    fun alternarMemorizando(pasajeId: String, memorizando: Boolean) {
        viewModelScope.launch { memorizacion.establecerMemorizando(pasajeId, memorizando) }
    }

    private suspend fun calcularBase(): Base {
        val texto = when (biblia.estado()) {
            is CargaTexto.Cargado -> EstadoTexto.INSTALADO
            CargaTexto.Faltante -> EstadoTexto.FALTANTE
            is CargaTexto.Invalido -> EstadoTexto.INVALIDO
        }
        val disponibilidad = pasajes.pasajes.associate { pasaje ->
            pasaje.id to when (val resultado = biblia.textoDe(pasaje.referencia)) {
                is ResultadoTexto.Disponible -> Disponibilidad.DISPONIBLE
                is ResultadoTexto.NoDisponible -> when (resultado.motivo) {
                    MotivoNoDisponible.PENDIENTE -> Disponibilidad.PENDIENTE
                    MotivoNoDisponible.INEXISTENTE -> Disponibilidad.INEXISTENTE
                }
                ResultadoTexto.ArchivoFaltante -> Disponibilidad.SIN_TEXTO
            }
        }
        return Base(texto, disponibilidad)
    }
}
