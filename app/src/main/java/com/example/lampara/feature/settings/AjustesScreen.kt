package com.example.lampara.feature.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lampara.R
import com.example.lampara.core.notifications.Notificaciones
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun AjustesScreen(viewModel: AjustesViewModel = hiltViewModel()) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    var permisoDenegado by remember { mutableStateOf(false) }

    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        permisoDenegado = !concedido
        if (concedido) viewModel.activarRecordatorio(true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.titulo_ajustes), style = MaterialTheme.typography.headlineMedium)
        val actual = estado
        if (actual == null) {
            Text(stringResource(R.string.cargando))
            return@Column
        }
        Text(stringResource(R.string.ajustes_contenido_titulo), style = MaterialTheme.typography.titleMedium)
        FilaInterruptor(
            titulo = R.string.ajustes_confesion_titulo,
            descripcion = R.string.ajustes_confesion_descripcion,
            marcado = actual.confesionActiva,
            onCambio = viewModel::activarConfesion,
        )
        FilaInterruptor(
            titulo = R.string.ajustes_revisadas_titulo,
            descripcion = R.string.ajustes_revisadas_descripcion,
            marcado = actual.soloRevisadas,
            onCambio = viewModel::establecerSoloRevisadas,
        )
        SeccionRecordatorio(
            estado = actual,
            permisoDenegado = permisoDenegado,
            onActivar = { activar ->
                if (!activar) {
                    permisoDenegado = false
                    viewModel.activarRecordatorio(false)
                } else if (Notificaciones.tienePermiso(contexto)) {
                    permisoDenegado = false
                    viewModel.activarRecordatorio(true)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onCambiarHora = viewModel::cambiarHoraRecordatorio,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeccionRecordatorio(
    estado: AjustesUiState,
    permisoDenegado: Boolean,
    onActivar: (Boolean) -> Unit,
    onCambiarHora: (Int) -> Unit,
) {
    var mostrarSelector by remember { mutableStateOf(false) }
    val hora = LocalTime.of(estado.recordatorioMinutos / 60, estado.recordatorioMinutos % 60)
        .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

    Text(stringResource(R.string.ajustes_recordatorio_titulo), style = MaterialTheme.typography.titleMedium)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(value = estado.recordatorioActivo, role = Role.Switch, onValueChange = onActivar),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            stringResource(R.string.ajustes_recordatorio_descripcion),
            modifier = Modifier.weight(1f).padding(end = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        Switch(checked = estado.recordatorioActivo, onCheckedChange = null)
    }
    if (permisoDenegado) {
        Text(
            stringResource(R.string.ajustes_recordatorio_permiso_denegado),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
    OutlinedButton(
        onClick = { mostrarSelector = true },
        enabled = estado.recordatorioActivo,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
    ) {
        Text(stringResource(R.string.ajustes_recordatorio_hora, hora))
    }

    if (mostrarSelector) {
        val selector = rememberTimePickerState(
            initialHour = estado.recordatorioMinutos / 60,
            initialMinute = estado.recordatorioMinutos % 60,
            is24Hour = false,
        )
        AlertDialog(
            onDismissRequest = { mostrarSelector = false },
            title = { Text(stringResource(R.string.ajustes_recordatorio_elegir_hora)) },
            text = { TimePicker(state = selector) },
            confirmButton = {
                TextButton(onClick = {
                    onCambiarHora(selector.hour * 60 + selector.minute)
                    mostrarSelector = false
                }) { Text(stringResource(R.string.aceptar)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarSelector = false }) { Text(stringResource(R.string.cancelar)) }
            },
        )
    }
}

/** Fila con título, descripción e interruptor; toda la fila es el objetivo táctil. */
@Composable
private fun FilaInterruptor(
    @androidx.annotation.StringRes titulo: Int,
    @androidx.annotation.StringRes descripcion: Int,
    marcado: Boolean,
    onCambio: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = marcado, role = Role.Switch, onValueChange = onCambio),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(titulo), style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(descripcion),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = marcado, onCheckedChange = null)
    }
}
