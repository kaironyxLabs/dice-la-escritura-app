package com.example.lampara.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lampara.core.notifications.RecordatorioScheduler
import com.example.lampara.data.datastore.AjustesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AjustesUiState(
    val recordatorioActivo: Boolean,
    /** Minutos desde las 00:00. */
    val recordatorioMinutos: Int,
)

@HiltViewModel
class AjustesViewModel @Inject constructor(
    private val ajustes: AjustesRepository,
    private val recordatorios: RecordatorioScheduler,
) : ViewModel() {

    val estado: StateFlow<AjustesUiState?> = combine(
        ajustes.recordatorioActivo,
        ajustes.recordatorioMinutos,
    ) { activo, minutos -> AjustesUiState(activo, minutos) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun activarRecordatorio(activo: Boolean) {
        viewModelScope.launch {
            val minutos = ajustes.recordatorioMinutos.first()
            ajustes.establecerRecordatorio(activo, minutos)
            if (activo) recordatorios.reprogramar(minutos) else recordatorios.cancelar()
        }
    }

    fun cambiarHoraRecordatorio(minutos: Int) {
        viewModelScope.launch {
            val activo = ajustes.recordatorioActivo.first()
            ajustes.establecerRecordatorio(activo, minutos)
            if (activo) recordatorios.reprogramar(minutos)
        }
    }
}
