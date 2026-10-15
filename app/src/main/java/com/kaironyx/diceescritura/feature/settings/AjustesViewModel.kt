package com.kaironyx.diceescritura.feature.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaironyx.diceescritura.core.flags.FeatureFlag
import com.kaironyx.diceescritura.core.flags.FeatureFlags
import com.kaironyx.diceescritura.core.notifications.RecordatorioScheduler
import com.kaironyx.diceescritura.data.datastore.AjustesRepository
import com.kaironyx.diceescritura.data.repository.RespaldoRepository
import com.kaironyx.diceescritura.data.repository.ResultadoImportacion
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AjustesUiState(
    val recordatorioActivo: Boolean,
    /** Minutos desde las 00:00. */
    val recordatorioMinutos: Int,
    val confesionActiva: Boolean,
    val soloRevisadas: Boolean,
)

/** Resultado de exportar o importar, para mostrarlo al usuario. */
sealed interface MensajeRespaldo {
    data object Exportado : MensajeRespaldo
    data object Importado : MensajeRespaldo
    data object ErrorAlExportar : MensajeRespaldo
    data object ErrorAlLeer : MensajeRespaldo
    data class Rechazado(val motivo: String) : MensajeRespaldo
}

@HiltViewModel
class AjustesViewModel @Inject constructor(
    @ApplicationContext private val contexto: Context,
    private val ajustes: AjustesRepository,
    private val flags: FeatureFlags,
    private val recordatorios: RecordatorioScheduler,
    private val respaldo: RespaldoRepository,
) : ViewModel() {

    private val _mensaje = MutableStateFlow<MensajeRespaldo?>(null)
    val mensaje: StateFlow<MensajeRespaldo?> = _mensaje.asStateFlow()

    fun descartarMensaje() {
        _mensaje.value = null
    }

    /** Escribe la copia del progreso en el archivo que eligió el usuario (Storage Access Framework). */
    fun exportarA(destino: Uri) {
        viewModelScope.launch {
            _mensaje.value = try {
                val json = respaldo.exportarJson()
                withContext(Dispatchers.IO) {
                    contexto.contentResolver.openOutputStream(destino, "wt")!!.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                }
                MensajeRespaldo.Exportado
            } catch (e: Exception) {
                MensajeRespaldo.ErrorAlExportar
            }
        }
    }

    /** Lee el archivo elegido y, si es válido, reemplaza el progreso actual. */
    fun importarDesde(origen: Uri) {
        viewModelScope.launch {
            val texto = try {
                withContext(Dispatchers.IO) {
                    contexto.contentResolver.openInputStream(origen)!!.use { it.readBytes().toString(Charsets.UTF_8) }
                }
            } catch (e: Exception) {
                _mensaje.value = MensajeRespaldo.ErrorAlLeer
                return@launch
            }
            _mensaje.value = when (val r = respaldo.importarJson(texto)) {
                ResultadoImportacion.Correcta -> MensajeRespaldo.Importado
                is ResultadoImportacion.Rechazada -> MensajeRespaldo.Rechazado(r.errores.first())
            }
        }
    }

    val estado: StateFlow<AjustesUiState?> = combine(
        ajustes.recordatorioActivo,
        ajustes.recordatorioMinutos,
        flags.habilitada(FeatureFlag.CONFESION_1689),
        ajustes.soloRevisadas,
    ) { activo, minutos, confesion, revisadas -> AjustesUiState(activo, minutos, confesion, revisadas) }
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

    fun activarConfesion(activa: Boolean) {
        viewModelScope.launch { flags.establecer(FeatureFlag.CONFESION_1689, activa) }
    }

    fun establecerSoloRevisadas(valor: Boolean) {
        viewModelScope.launch { ajustes.establecerSoloRevisadas(valor) }
    }
}
