package com.kaironyx.diceescritura.feature.memorize

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaironyx.diceescritura.R
import com.kaironyx.diceescritura.core.ui.EstadoCargando
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun MemorizarScreen(
    onPracticar: (String) -> Unit,
    viewModel: MemorizarViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var soloMemorizando by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            stringResource(R.string.titulo_memorizar),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
        )
        when (val e = estado) {
            MemorizarUiState.Cargando -> EstadoCargando()
            is MemorizarUiState.Listo -> {
                if (e.texto != EstadoTexto.INSTALADO) AvisoTextoFaltante(e.texto)
                FilterChip(
                    selected = soloMemorizando,
                    onClick = { soloMemorizando = !soloMemorizando },
                    label = { Text(stringResource(R.string.memorizar_solo_mis_pasajes)) },
                    modifier = Modifier.padding(horizontal = 24.dp).heightIn(min = 48.dp),
                )
                val visibles = if (soloMemorizando) e.items.filter { it.memorizando } else e.items
                if (visibles.isEmpty()) {
                    Text(stringResource(R.string.memorizar_sin_pasajes), modifier = Modifier.padding(24.dp))
                }
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(visibles, key = { it.pasaje.id }) { item ->
                        TarjetaPasaje(
                            item = item,
                            onAlternar = { viewModel.alternarMemorizando(item.pasaje.id, it) },
                            onPracticar = { onPracticar(item.pasaje.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvisoTextoFaltante(texto: EstadoTexto) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.memorizar_texto_faltante_titulo),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Text(
                stringResource(
                    if (texto == EstadoTexto.INVALIDO) R.string.memorizar_texto_invalido else R.string.memorizar_texto_faltante,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
private fun TarjetaPasaje(item: ItemPasaje, onAlternar: (Boolean) -> Unit, onPracticar: () -> Unit) {
    val disponible = item.disponibilidad == Disponibilidad.DISPONIBLE
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(item.pasaje.referenciaTexto, style = MaterialTheme.typography.titleMedium)
            if (item.pasaje.etiquetas.isNotEmpty()) {
                Text(
                    item.pasaje.etiquetas.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (!disponible) {
                Text(
                    stringResource(
                        when (item.disponibilidad) {
                            Disponibilidad.PENDIENTE -> R.string.memorizar_no_disponible_pendiente
                            Disponibilidad.INEXISTENTE -> R.string.memorizar_no_disponible_inexistente
                            else -> R.string.memorizar_no_disponible_sin_texto
                        },
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .toggleable(value = item.memorizando, role = Role.Switch, onValueChange = onAlternar),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(stringResource(R.string.memorizar_estoy_memorizando), style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = item.memorizando, onCheckedChange = null)
                }
                if (item.memorizando) {
                    EstadoDeRepaso(item)
                    OutlinedButton(
                        onClick = onPracticar,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) { Text(stringResource(R.string.memorizar_practicar)) }
                }
            }
        }
    }
}

@Composable
private fun EstadoDeRepaso(item: ItemPasaje) {
    val texto = when {
        item.vencido -> stringResource(R.string.memorizar_repaso_pendiente)
        item.proximoRepaso != null -> stringResource(R.string.memorizar_proximo_repaso, formatear(item.proximoRepaso))
        else -> return
    }
    Text(texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
}

internal fun formatear(fecha: LocalDate): String =
    fecha.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
