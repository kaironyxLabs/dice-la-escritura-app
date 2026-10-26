package com.kaironyx.diceescritura.feature.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaironyx.diceescritura.R
import com.kaironyx.diceescritura.domain.usecase.Logro
import com.kaironyx.diceescritura.domain.usecase.EstadoLogro
import com.kaironyx.diceescritura.core.ui.Pergamino
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import com.kaironyx.diceescritura.core.ui.TarjetaPergamino
import com.kaironyx.diceescritura.core.ui.EstadoCargando
import com.kaironyx.diceescritura.domain.usecase.PrecisionCategoria
import com.kaironyx.diceescritura.domain.usecase.ResumenHistorial
import com.kaironyx.diceescritura.feature.nombreRes
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun ProgresoScreen(viewModel: ProgresoViewModel = hiltViewModel()) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.titulo_progreso), style = MaterialTheme.typography.headlineMedium)
        val e = estado
        if (e == null) {
            EstadoCargando()
            return@Column
        }
        ResumenNivel(e)
        Text(stringResource(R.string.progreso_precision_titulo), style = MaterialTheme.typography.titleMedium)
        e.precision.forEach { FilaPrecision(it) }
        Text(stringResource(R.string.progreso_historial_titulo), style = MaterialTheme.typography.titleMedium)
        Historial(e.historial)
        SeccionLogros(e.logros)
    }
}

@Composable
private fun ResumenNivel(e: ProgresoUiState) {
    TarjetaPergamino(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.nivel_numero, e.nivel.nivel.numero) + " · " + e.nivel.nivel.nombre,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = if (e.nivel.xpDelNivel == null) {
                stringResource(R.string.nivel_maximo)
            } else {
                stringResource(R.string.xp_progreso, e.nivel.xpEnNivel, e.nivel.xpDelNivel)
            },
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            pluralStringResource(R.plurals.racha_dias, e.racha.actual, e.racha.actual),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            stringResource(R.string.progreso_mejor_racha, e.racha.mejor),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
}
}

@Composable
private fun FilaPrecision(p: PrecisionCategoria) {
    val porcentaje = p.porcentaje
    val valor = if (porcentaje == null) {
        stringResource(R.string.progreso_precision_vacia)
    } else {
        stringResource(R.string.progreso_precision_valor, porcentaje, p.aciertos, p.respondidas)
    }
    val nombre = stringResource(p.categoria.nombreRes())
    Column(
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) { contentDescription = "$nombre: $valor" },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (LocalDensity.current.fontScale > 1.3f) {
            // Con fuente muy grande el nombre y el valor no caben en una fila.
            Text(nombre, style = MaterialTheme.typography.bodyLarge)
            Text(valor, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(nombre, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(end = 12.dp))
                Text(valor, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        LinearProgressIndicator(progress = { (porcentaje ?: 0) / 100f }, modifier = Modifier.fillMaxWidth())
    }
}

/**
 * Calendario de actividad: una columna por semana (lunes arriba) y un cuadro por día de los últimos 30 días;
 * cuanto más oscuro, más preguntas respondidas ese día. Los días sin actividad quedan en un tono tenue y los
 * futuros de la última semana no se dibujan. El resumen en texto y la descripción para TalkBack dan los mismos
 * datos sin depender del dibujo.
 */
@Composable
private fun Historial(historial: ResumenHistorial) {
    val resumen = if (historial.respondidas == 0) {
        stringResource(R.string.progreso_historial_vacio)
    } else {
        stringResource(
            R.string.progreso_historial_resumen,
            historial.respondidas, historial.diasConActividad, historial.porcentajeAciertos ?: 0,
        )
    }
    Text(resumen, style = MaterialTheme.typography.bodyMedium)
    val descripcion = stringResource(
        R.string.progreso_historial_descripcion,
        if (historial.respondidas == 0) resumen else resumen + " " + stringResource(R.string.progreso_historial_maximo, historial.maximoDiario),
    )
    val intenso = MaterialTheme.colorScheme.primary
    val vacio = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    val dias = historial.dias
    // Fila 0 = lunes. El primer día del historial cae en la fila de su día de la semana.
    val desfase = dias.first().fecha.dayOfWeek.value - 1
    val columnas = (desfase + dias.size + 6) / 7
    val maximo = historial.maximoDiario.coerceAtLeast(1)
    TarjetaPergamino(modifier = Modifier.fillMaxWidth(), semilla = 9) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(196.dp)
                .semantics { contentDescription = descripcion },
        ) {
            val hueco = 4.dp.toPx()
            val lado = minOf((size.width - hueco * (columnas - 1)) / columnas, (size.height - hueco * 6) / 7)
            val inicioX = (size.width - (lado * columnas + hueco * (columnas - 1))) / 2f
            dias.forEachIndexed { i, dia ->
                val celda = desfase + i
                val x = inicioX + (celda / 7) * (lado + hueco)
                val y = (celda % 7) * (lado + hueco)
                val color = if (dia.respondidas == 0) {
                    vacio
                } else {
                    // Cuatro niveles de intensidad sobre el máximo del periodo.
                    val nivel = ((dia.respondidas * 4f) / maximo).coerceIn(1f, 4f).let { Math.ceil(it.toDouble()).toInt() }
                    intenso.copy(alpha = 0.25f + 0.25f * (nivel - 1))
                }
                drawRoundRect(color, Offset(x, y), Size(lado, lado), CornerRadius(lado * 0.2f))
            }
        }
        val formato = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(dias.first().fecha.format(formato), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(dias.last().fecha.format(formato), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Logros: un renglón por logro, con un círculo dorado y una marca si ya se consiguió, o "x/y" si falta. */
@Composable
private fun SeccionLogros(logros: List<EstadoLogro>) {
    val conseguidos = logros.count { it.conseguido }
    Text(stringResource(R.string.logros_titulo), style = MaterialTheme.typography.titleMedium)
    Text(
        stringResource(R.string.logros_resumen, conseguidos, logros.size),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    TarjetaPergamino(modifier = Modifier.fillMaxWidth(), semilla = 11) {
        logros.forEach { FilaLogro(it) }
    }
}

@Composable
private fun FilaLogro(estado: EstadoLogro) {
    val titulo = stringResource(estado.logro.titulo())
    val descripcion = stringResource(estado.logro.descripcion())
    val estadoTexto = if (estado.conseguido) {
        stringResource(R.string.logro_conseguido)
    } else {
        stringResource(R.string.logro_avance, estado.avance, estado.logro.meta)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .semantics(mergeDescendants = true) { contentDescription = "$titulo. $descripcion. $estadoTexto" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val oro = Brush.verticalGradient(listOf(Pergamino.OroClaro, Pergamino.Oro))
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .then(
                    if (estado.conseguido) {
                        Modifier.background(oro)
                    } else {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (estado.conseguido) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Pergamino.TintaSuave)
            } else {
                Text(
                    stringResource(R.string.logro_avance_corto, estado.avance, estado.logro.meta),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                titulo,
                style = MaterialTheme.typography.titleSmall,
                color = if (estado.conseguido) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun Logro.titulo(): Int = when (this) {
    Logro.PRIMER_RETO -> R.string.logro_primer_reto
    Logro.RACHA_7 -> R.string.logro_racha_7
    Logro.RACHA_30 -> R.string.logro_racha_30
    Logro.NIVEL_2 -> R.string.logro_nivel_2
    Logro.NIVEL_5 -> R.string.logro_nivel_5
    Logro.NIVEL_10 -> R.string.logro_nivel_10
    Logro.CIEN_ACIERTOS -> R.string.logro_cien_aciertos
    Logro.PRIMER_PASAJE -> R.string.logro_primer_pasaje
    Logro.DIEZ_PASAJES -> R.string.logro_diez_pasajes
}

private fun Logro.descripcion(): Int = when (this) {
    Logro.PRIMER_RETO -> R.string.logro_primer_reto_desc
    Logro.RACHA_7 -> R.string.logro_racha_7_desc
    Logro.RACHA_30 -> R.string.logro_racha_30_desc
    Logro.NIVEL_2 -> R.string.logro_nivel_2_desc
    Logro.NIVEL_5 -> R.string.logro_nivel_5_desc
    Logro.NIVEL_10 -> R.string.logro_nivel_10_desc
    Logro.CIEN_ACIERTOS -> R.string.logro_cien_aciertos_desc
    Logro.PRIMER_PASAJE -> R.string.logro_primer_pasaje_desc
    Logro.DIEZ_PASAJES -> R.string.logro_diez_pasajes_desc
}
