package com.kaironyx.diceescritura.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaironyx.diceescritura.R
import com.kaironyx.diceescritura.core.theme.Espacio

/** Estado de carga común: indicador y texto. Cabe tanto en una columna con scroll como en una pantalla vacía. */
@Composable
fun EstadoCargando(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = Espacio.l),
        horizontalArrangement = Arrangement.spacedBy(Espacio.m, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
        Text(stringResource(R.string.cargando), style = MaterialTheme.typography.bodyLarge)
    }
}

/** Estado vacío común: un título y una explicación de qué hacer. */
@Composable
fun EstadoVacio(titulo: String, mensaje: String, modifier: Modifier = Modifier) {
    TarjetaContenido(modifier = modifier.fillMaxWidth()) {
        Text(titulo, style = MaterialTheme.typography.titleMedium)
        Text(
            mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
