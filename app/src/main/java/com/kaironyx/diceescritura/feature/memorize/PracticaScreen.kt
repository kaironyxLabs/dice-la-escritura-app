package com.kaironyx.diceescritura.feature.memorize

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaironyx.diceescritura.R
import com.kaironyx.diceescritura.core.theme.EstiloPasaje
import com.kaironyx.diceescritura.domain.usecase.TextoOculto

@Composable
fun PracticaScreen(
    onSalir: () -> Unit,
    viewModel: PracticaViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    when (val e = estado) {
        PracticaUiState.Cargando -> Mensaje(stringResource(R.string.cargando), onSalir, conBoton = false)
        is PracticaUiState.SinTexto -> Mensaje(
            stringResource(
                when (e.motivo) {
                    MotivoSinTexto.ARCHIVO_FALTANTE -> R.string.memorizar_texto_faltante
                    MotivoSinTexto.PASAJE_NO_DISPONIBLE -> R.string.memorizar_no_disponible_pendiente
                    MotivoSinTexto.PASAJE_DESCONOCIDO -> R.string.practica_pasaje_desconocido
                },
            ),
            onSalir,
        )
        is PracticaUiState.Practicando -> Practicando(
            estado = e,
            onEntrada = viewModel::cambiarEntrada,
            onComprobar = viewModel::comprobar,
            onContinuar = viewModel::continuar,
            onSalir = onSalir,
        )
    }
}

@Composable
private fun Mensaje(texto: String, onSalir: () -> Unit, conBoton: Boolean = true) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(texto, style = MaterialTheme.typography.bodyLarge)
        if (conBoton) OutlinedButton(onClick = onSalir) { Text(stringResource(R.string.volver)) }
    }
}

@Composable
private fun Practicando(
    estado: PracticaUiState.Practicando,
    onEntrada: (String) -> Unit,
    onComprobar: () -> Unit,
    onContinuar: () -> Unit,
    onSalir: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.practica_titulo), style = MaterialTheme.typography.labelLarge)
        Text(estado.referencia, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = if (estado.grado.esEscribir) {
                stringResource(R.string.practica_grado_escribir)
            } else {
                stringResource(R.string.practica_grado_ocultar, estado.grado.porcentaje)
            },
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        estado.oculto?.let { TextoConHuecos(it) }
        Text(
            stringResource(
                if (estado.grado.esEscribir) R.string.practica_instruccion_escribir else R.string.practica_instruccion_ocultar,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = estado.entrada,
            onValueChange = onEntrada,
            enabled = estado.resultado == null,
            label = { Text(stringResource(R.string.practica_entrada_etiqueta)) },
            minLines = if (estado.grado.esEscribir) 5 else 2,
            modifier = Modifier.fillMaxWidth(),
        )
        val resultado = estado.resultado
        if (resultado == null) {
            Button(
                onClick = onComprobar,
                enabled = estado.entrada.isNotBlank(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            ) { Text(stringResource(R.string.practica_comprobar)) }
        } else {
            Resultado(estado, resultado, onContinuar, onSalir)
        }
    }
}

/** Pasaje con las palabras ocultas como huecos; para TalkBack se lee "palabra oculta" en su lugar. */
@Composable
private fun TextoConHuecos(oculto: TextoOculto) {
    val colorHueco = MaterialTheme.colorScheme.primary
    val leido = stringResource(R.string.practica_palabra_oculta)
    val descripcion = oculto.tokens.mapIndexed { i, t -> if (i in oculto.ocultas) leido else t.texto }.joinToString("")
    val visible = buildAnnotatedString {
        oculto.tokens.forEachIndexed { i, token ->
            if (i in oculto.ocultas) {
                withStyle(SpanStyle(color = colorHueco)) { append("_".repeat(token.texto.length.coerceAtLeast(3))) }
            } else {
                append(token.texto)
            }
        }
    }
    Text(
        text = visible,
        style = EstiloPasaje,
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = descripcion },
    )
}

@Composable
private fun Resultado(
    estado: PracticaUiState.Practicando,
    resultado: ResultadoPractica,
    onContinuar: () -> Unit,
    onSalir: () -> Unit,
) {
    val aprobado = resultado.resultado.aprobado
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (aprobado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(if (aprobado) R.string.practica_aprobado else R.string.practica_no_aprobado),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                stringResource(R.string.practica_resultado, resultado.resultado.aciertos, resultado.resultado.total),
                style = MaterialTheme.typography.bodyLarge,
            )
            resultado.proximoRepaso?.let {
                Text(stringResource(R.string.practica_dominado, formatear(it)), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    Text(stringResource(R.string.practica_texto_correcto), style = MaterialTheme.typography.titleSmall)
    Text(estado.textoCompleto, style = EstiloPasaje)

    val terminado = resultado.proximoRepaso != null
    if (!terminado) {
        Button(onClick = onContinuar, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
            Text(stringResource(if (aprobado) R.string.practica_siguiente_grado else R.string.practica_reintentar))
        }
    }
    OutlinedButton(onClick = onSalir, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
        Text(stringResource(R.string.practica_terminar))
    }
}
