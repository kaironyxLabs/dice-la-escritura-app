package com.example.lampara.feature.progress

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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lampara.R
import com.example.lampara.domain.usecase.PrecisionCategoria
import com.example.lampara.domain.usecase.ResumenHistorial
import com.example.lampara.feature.nombreRes
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
            Text(stringResource(R.string.cargando))
            return@Column
        }
        ResumenNivel(e)
        Text(stringResource(R.string.progreso_precision_titulo), style = MaterialTheme.typography.titleMedium)
        e.precision.forEach { FilaPrecision(it) }
        Text(stringResource(R.string.progreso_historial_titulo), style = MaterialTheme.typography.titleMedium)
        Historial(e.historial)
    }
}

@Composable
private fun ResumenNivel(e: ProgresoUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(nombre, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(end = 12.dp))
            Text(valor, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LinearProgressIndicator(progress = { (porcentaje ?: 0) / 100f }, modifier = Modifier.fillMaxWidth())
    }
}

/**
 * Barras finas, una por día, de un solo tono: preguntas respondidas. Un solo eje, sin leyenda (una sola serie).
 * El resumen en texto y la descripción para TalkBack dan los mismos datos sin depender del dibujo.
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
    val barra = MaterialTheme.colorScheme.primary
    val base = MaterialTheme.colorScheme.outlineVariant
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .semantics { contentDescription = descripcion },
    ) {
        val dias = historial.dias
        val hueco = 2.dp.toPx()
        val ancho = (size.width - hueco * (dias.size - 1)) / dias.size
        val maximo = historial.maximoDiario.coerceAtLeast(1)
        val minimo = 3.dp.toPx()
        dias.forEachIndexed { i, dia ->
            val x = i * (ancho + hueco)
            if (dia.respondidas == 0) {
                // Día sin actividad: solo una marca en la línea base.
                drawRoundRect(base, Offset(x, size.height - 2.dp.toPx()), Size(ancho, 2.dp.toPx()), CornerRadius(1.dp.toPx()))
            } else {
                val alto = (size.height * dia.respondidas / maximo).coerceAtLeast(minimo)
                drawRoundRect(barra, Offset(x, size.height - alto), Size(ancho, alto), CornerRadius(ancho / 2f))
            }
        }
    }
    val formato = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(historial.dias.first().fecha.format(formato), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(historial.dias.last().fecha.format(formato), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
