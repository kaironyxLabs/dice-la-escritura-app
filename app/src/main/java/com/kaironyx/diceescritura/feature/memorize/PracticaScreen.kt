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
import com.kaironyx.diceescritura.domain.usecase.Token
import com.kaironyx.diceescritura.domain.usecase.Ocultador
import com.kaironyx.diceescritura.core.ui.TarjetaPergamino
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
            onAlternarBanco = viewModel::alternarBanco,
            onElegirPalabra = viewModel::elegirPalabra,
            onDeshacerPalabra = viewModel::deshacerPalabra,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Practicando(
    estado: PracticaUiState.Practicando,
    onEntrada: (String) -> Unit,
    onAlternarBanco: () -> Unit,
    onElegirPalabra: (Int) -> Unit,
    onDeshacerPalabra: () -> Unit,
    onComprobar: () -> Unit,
    onContinuar: () -> Unit,
    onSalir: () -> Unit,
) {
    val sinResultado = estado.resultado == null
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
        estado.oculto?.let { oculto ->
            TarjetaPergamino(modifier = Modifier.fillMaxWidth(), semilla = 4) {
                TextoConHuecos(oculto, estado.banco, if (estado.modoBanco) estado.usadas else emptyList(), estado.modoBanco)
            }
        }
        if (sinResultado && estado.oculto != null) {
            TextButton(onClick = onAlternarBanco) {
                Text(stringResource(if (estado.modoBanco) R.string.practica_escribir_palabras else R.string.practica_elegir_palabras))
            }
        }
        if (estado.modoBanco) {
            if (sinResultado) {
                Text(stringResource(R.string.practica_instruccion_banco), style = MaterialTheme.typography.bodyMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    estado.banco.forEachIndexed { posicion, palabra ->
                        val usada = posicion in estado.usadas
                        OutlinedButton(
                            onClick = { onElegirPalabra(posicion) },
                            enabled = !usada,
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) { Text(palabra) }
                    }
                }
                if (estado.usadas.isNotEmpty()) {
                    TextButton(onClick = onDeshacerPalabra) { Text(stringResource(R.string.practica_deshacer)) }
                }
            }
        } else {
            Text(
                stringResource(
                    if (estado.grado.esEscribir) R.string.practica_instruccion_escribir else R.string.practica_instruccion_ocultar,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedTextField(
                value = estado.entrada,
                onValueChange = onEntrada,
                enabled = sinResultado,
                label = { Text(stringResource(R.string.practica_entrada_etiqueta)) },
                minLines = if (estado.grado.esEscribir) 5 else 2,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        val resultado = estado.resultado
        if (resultado == null) {
            Button(
                onClick = onComprobar,
                enabled = if (estado.modoBanco) estado.usadas.size == estado.banco.size else estado.entrada.isNotBlank(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            ) { Text(stringResource(R.string.practica_comprobar)) }
        } else {
            Resultado(estado, resultado, onContinuar, onSalir)
        }
    }
}

/**
 * Pasaje con las palabras ocultas como huecos; para TalkBack se lee "palabra oculta" en su lugar.
 * En el modo banco, las palabras ya colocadas ([usadas], posiciones de [banco]) llenan los huecos en orden
 * y el siguiente hueco por llenar se resalta.
 */
@Composable
private fun TextoConHuecos(oculto: TextoOculto, banco: List<String>, usadas: List<Int>, modoBanco: Boolean) {
    val colorHueco = MaterialTheme.colorScheme.primary
    val leido = stringResource(R.string.practica_palabra_oculta)
    val ordenHuecos = oculto.ocultas.sorted()
    val descripcion = oculto.tokens.mapIndexed { i, t ->
        val k = ordenHuecos.indexOf(i)
        when {
            k < 0 -> t.texto
            modoBanco && k < usadas.size -> banco[usadas[k]]
            else -> leido
        }
    }.joinToString("")
    val visible = buildAnnotatedString {
        oculto.tokens.forEachIndexed { i, token ->
            val k = ordenHuecos.indexOf(i)
            when {
                k < 0 -> append(token.texto)
                modoBanco && k < usadas.size ->
                    withStyle(SpanStyle(color = colorHueco, fontWeight = FontWeight.Bold)) { append(banco[usadas[k]]) }
                else -> withStyle(
                    SpanStyle(
                        color = colorHueco,
                        fontWeight = if (modoBanco && k == usadas.size) FontWeight.Bold else null,
                        textDecoration = if (modoBanco && k == usadas.size) TextDecoration.Underline else null,
                    ),
                ) { append("_".repeat(token.texto.length.coerceAtLeast(3))) }
            }
        }
    }
    Text(
        text = visible,
        style = EstiloPasaje,
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = descripcion },
    )
}

/** Pasaje completo con las palabras pedidas en verde si se acertaron y en rojo subrayado si se olvidaron. */
@Composable
private fun PasajeResaltado(tokens: List<Token>, marcas: Map<Int, Boolean>) {
    val acierto = MaterialTheme.colorScheme.primary
    val fallo = MaterialTheme.colorScheme.error
    val texto = buildAnnotatedString {
        tokens.forEachIndexed { i, token ->
            when (marcas[i]) {
                true -> withStyle(SpanStyle(color = acierto, fontWeight = FontWeight.Bold)) { append(token.texto) }
                false -> withStyle(SpanStyle(color = fallo, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                    append(token.texto)
                }
                null -> append(token.texto)
            }
        }
    }
    Text(texto, style = EstiloPasaje)
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
    TarjetaPergamino(modifier = Modifier.fillMaxWidth(), semilla = 6) {
        PasajeResaltado(estado.oculto?.tokens ?: Ocultador.tokenizar(estado.textoCompleto), resultado.marcas)
        Text(
            stringResource(R.string.practica_leyenda),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

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
