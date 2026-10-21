package com.kaironyx.diceescritura.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaironyx.diceescritura.core.theme.AciertoClaro
import com.kaironyx.diceescritura.core.theme.AciertoOscuro
import com.kaironyx.diceescritura.core.theme.Espacio
import com.kaironyx.diceescritura.core.theme.Tamano

// Componentes base del sistema de diseño. No llevan texto propio: quien los usa pasa los textos
// (que viven en strings.xml). Se muestran todos en CatalogoComponentes (solo en debug).

/** Tarjeta de contenido: superficie de pergamino con borde fino. Si recibe [onClick], es tocable. */
@Composable
fun TarjetaContenido(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    val colores = CardDefaults.cardColors(containerColor = Pergamino.Claro)
    val borde = BorderStroke(1.dp, Pergamino.Borde)
    val cuerpo: @Composable ColumnScope.() -> Unit = {
        Column(
            modifier = Modifier.padding(Espacio.m + Espacio.xs),
            verticalArrangement = Arrangement.spacedBy(Espacio.s),
            content = contenido,
        )
    }
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, colors = colores, border = borde, content = cuerpo)
    } else {
        Card(modifier = modifier, colors = colores, border = borde, content = cuerpo)
    }
}

/** Botón de la acción principal de una pantalla. */
@Composable
fun BotonPrincipal(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier, habilitado: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        modifier = modifier.fillMaxWidth().heightIn(min = Tamano.boton),
    ) { Text(texto) }
}

/** Botón de una acción secundaria. */
@Composable
fun BotonSecundario(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier, habilitado: Boolean = true) {
    OutlinedButton(
        onClick = onClick,
        enabled = habilitado,
        modifier = modifier.fillMaxWidth().heightIn(min = Tamano.boton),
    ) { Text(texto) }
}

/** Cita bíblica destacada (por ejemplo «Romanos 3:23»). Es solo informativa. */
@Composable
fun ChipCita(texto: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Text(
            texto,
            modifier = Modifier.padding(horizontal = Espacio.s + Espacio.xs, vertical = Espacio.xs + 2.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

/**
 * Barra de progreso que se llena con suavidad. [descripcion] es lo que lee el lector de pantalla
 * (por ejemplo «Avance del nivel: 40 %»). Con las animaciones del sistema apagadas salta al valor.
 */
@Composable
fun BarraProgresoAnimada(fraccion: Float, descripcion: String, modifier: Modifier = Modifier) {
    val animar = rememberAnimacionesActivas()
    val valor by animateFloatAsState(
        targetValue = fraccion.coerceIn(0f, 1f),
        animationSpec = tween(if (animar) 600 else 0),
        label = "progreso",
    )
    LinearProgressIndicator(
        progress = { valor },
        modifier = modifier.fillMaxWidth().semantics { contentDescription = descripcion },
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

/** Insignia circular con el número de nivel. */
@Composable
fun InsigniaNivel(numero: Int, modifier: Modifier = Modifier, tamano: Dp = 48.dp) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            numero.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

/**
 * Retroalimentación de una respuesta. Comunica acierto o error con icono, texto y color a la vez,
 * nunca solo con color. [titulo] y [cuerpo] vienen de strings.xml o del contenido.
 */
@Composable
fun ContenedorRetroalimentacion(
    acierto: Boolean,
    titulo: String,
    modifier: Modifier = Modifier,
    cuerpo: String? = null,
) {
    val fondo = if (acierto) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val sobreFondo = if (acierto) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
    val marca = if (acierto) {
        if (isSystemInDarkTheme()) AciertoOscuro else AciertoClaro
    } else {
        MaterialTheme.colorScheme.error
    }
    Surface(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = fondo, contentColor = sobreFondo) {
        Row(
            modifier = Modifier.padding(Espacio.m),
            horizontalArrangement = Arrangement.spacedBy(Espacio.m),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = if (acierto) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = null,
                tint = marca,
                modifier = Modifier.size(24.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(Espacio.xs)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                if (cuerpo != null) Text(cuerpo, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** Una fila de puntos que se van llenando (reto diario: un punto por pregunta). Es decorativa. */
@Composable
fun PuntosReto(llenos: Int, total: Int, modifier: Modifier = Modifier) {
    val lleno = MaterialTheme.colorScheme.primary
    val vacio = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = modifier.clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(Espacio.s),
    ) {
        repeat(total) { i ->
            Box(
                Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(if (i < llenos) lleno else vacio),
            )
        }
    }
}
