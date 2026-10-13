package com.example.lampara.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lampara.R
import com.example.lampara.domain.usecase.EstadoNivel

@Composable
fun InicioScreen(
    onJugarPorCategoria: () -> Unit,
    onExamen: () -> Unit,
    viewModel: InicioViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    InicioContenido(estado, onJugarPorCategoria, onExamen)
}

@Composable
fun InicioContenido(
    estado: EstadoNivel?,
    onJugarPorCategoria: () -> Unit,
    onExamen: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.titulo_inicio), style = MaterialTheme.typography.headlineMedium)
        if (estado == null) {
            Text(stringResource(R.string.cargando))
            return@Column
        }
        TarjetaNivel(estado)
        if (estado.examenDisponible) {
            Text(
                stringResource(R.string.examen_disponible),
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(onClick = onExamen, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.boton_examen))
            }
            OutlinedButton(onClick = onJugarPorCategoria, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.boton_jugar_categoria))
            }
        } else {
            Button(onClick = onJugarPorCategoria, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.boton_jugar_categoria))
            }
        }
    }
}

@Composable
private fun TarjetaNivel(estado: EstadoNivel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.nivel_numero, estado.nivel.numero),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(estado.nivel.nombre, style = MaterialTheme.typography.headlineSmall)
            val porcentaje = (estado.fraccion * 100).toInt()
            val descripcion = stringResource(R.string.descripcion_barra_xp, porcentaje)
            LinearProgressIndicator(
                progress = { estado.fraccion },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = descripcion },
            )
            Text(
                text = if (estado.xpDelNivel == null) {
                    stringResource(R.string.nivel_maximo)
                } else {
                    stringResource(R.string.xp_progreso, estado.xpEnNivel, estado.xpDelNivel)
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
