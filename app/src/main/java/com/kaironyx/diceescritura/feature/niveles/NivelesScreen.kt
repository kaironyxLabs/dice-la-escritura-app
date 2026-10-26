package com.kaironyx.diceescritura.feature.niveles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaironyx.diceescritura.R
import com.kaironyx.diceescritura.core.theme.Espacio
import com.kaironyx.diceescritura.core.ui.BotonSecundario
import com.kaironyx.diceescritura.core.ui.EstadoCargando
import com.kaironyx.diceescritura.core.ui.PlantaNivel
import com.kaironyx.diceescritura.domain.model.Categoria
import com.kaironyx.diceescritura.domain.model.Nivel
import com.kaironyx.diceescritura.core.ui.TarjetaPergamino
import com.kaironyx.diceescritura.feature.nombreRes

/** Mapa de los diez niveles: cada uno con su planta, lo que desbloquea y si ya se alcanzó. */
@Composable
fun NivelesScreen(alVolver: () -> Unit, viewModel: NivelesViewModel = hiltViewModel()) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val actual = estado
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            stringResource(R.string.niveles_titulo),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = Espacio.l, end = Espacio.l, top = Espacio.m, bottom = Espacio.s),
        )
        if (actual == null) {
            EstadoCargando()
            return@Column
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Espacio.l, vertical = Espacio.s),
            verticalArrangement = Arrangement.spacedBy(Espacio.m),
        ) {
            items(actual.niveles, key = { it.numero }) { nivel ->
                FilaNivel(nivel, alcanzado = nivel.numero <= actual.actual, esActual = nivel.numero == actual.actual)
            }
        }
        BotonSecundario(
            stringResource(R.string.volver),
            onClick = alVolver,
            modifier = Modifier.padding(horizontal = Espacio.l, vertical = Espacio.m),
        )
    }
}

@Composable
private fun FilaNivel(nivel: Nivel, alcanzado: Boolean, esActual: Boolean) {
    val estadoTexto = when {
        esActual -> stringResource(R.string.niveles_actual)
        alcanzado -> stringResource(R.string.niveles_superado)
        else -> stringResource(R.string.niveles_bloqueado)
    }
    val descripcion = stringResource(R.string.niveles_descripcion_planta, nivel.numero, nivel.nombre, estadoTexto)
    TarjetaPergamino(modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {}, semilla = nivel.numero) {
        Row(horizontalArrangement = Arrangement.spacedBy(Espacio.m), verticalAlignment = Alignment.CenterVertically) {
            PlantaNivel(nivel.numero, tamano = 72.dp, descripcion = descripcion, bloqueada = !alcanzado)
            Column(verticalArrangement = Arrangement.spacedBy(Espacio.xs)) {
                Text(
                    stringResource(R.string.nivel_numero, nivel.numero),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(nivel.nombre, style = MaterialTheme.typography.titleLarge)
                Text(estadoTexto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                if (!alcanzado) {
                    Text(
                        stringResource(R.string.niveles_requisito, nivel.umbralXp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val categorias = nivel.categorias.filter { it != Categoria.CONFESION }
                if (categorias.isNotEmpty()) {
                    val nombres = categorias.map { stringResource(it.nombreRes()) }
                    Text(
                        stringResource(R.string.niveles_desbloquea, nombres.joinToString(", ")),
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    Text(
                        stringResource(R.string.niveles_dificultad, nivel.dificultadMaxima),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
