package com.kaironyx.diceescritura.feature.quiz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaironyx.diceescritura.R
import com.kaironyx.diceescritura.core.ui.EstadoCargando
import com.kaironyx.diceescritura.domain.model.Categoria
import com.kaironyx.diceescritura.feature.nombreRes

@Composable
fun CategoriasScreen(
    onElegir: (Categoria) -> Unit,
    viewModel: CategoriasViewModel = hiltViewModel(),
) {
    val categorias by viewModel.categorias.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            stringResource(R.string.titulo_categorias),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
        )
        val lista = categorias
        if (lista == null) {
            EstadoCargando()
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(lista, key = { it.categoria.id }) { item ->
                    TarjetaCategoria(item, onClick = { onElegir(item.categoria) })
                }
            }
        }
    }
}

@Composable
private fun TarjetaCategoria(item: ItemCategoria, onClick: () -> Unit) {
    val bloqueada = item.bloqueadaHastaNivel != null
    val habilitada = !bloqueada && item.preguntasDisponibles > 0
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .then(if (habilitada) Modifier.clickable(onClick = onClick) else Modifier),
        colors = if (habilitada) {
            CardDefaults.cardColors()
        } else {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        },
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(item.categoria.nombreRes()), style = MaterialTheme.typography.titleMedium)
            Text(
                text = if (bloqueada) {
                    stringResource(R.string.categoria_bloqueada, item.bloqueadaHastaNivel)
                } else {
                    pluralStringResource(R.plurals.preguntas_disponibles, item.preguntasDisponibles, item.preguntasDisponibles)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
