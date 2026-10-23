package com.kaironyx.diceescritura.feature.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaironyx.diceescritura.R
import com.kaironyx.diceescritura.core.theme.Espacio
import com.kaironyx.diceescritura.core.ui.ChipCita
import com.kaironyx.diceescritura.core.ui.ContenedorRetroalimentacion
import com.kaironyx.diceescritura.core.ui.EstadoCargando
import com.kaironyx.diceescritura.core.ui.PlantaNivel
import com.kaironyx.diceescritura.core.ui.TarjetaContenido
import com.kaironyx.diceescritura.core.ui.rememberAnimacionesActivas
import com.kaironyx.diceescritura.domain.model.Categoria
import com.kaironyx.diceescritura.domain.model.Contenido
import com.kaironyx.diceescritura.domain.model.Pregunta
import com.kaironyx.diceescritura.domain.model.ResultadoReferencia
import com.kaironyx.diceescritura.domain.usecase.ParserReferencias
import com.kaironyx.diceescritura.domain.usecase.Respuesta

@Composable
fun PartidaScreen(
    onSalir: () -> Unit,
    viewModel: PartidaViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    when (val e = estado) {
        PartidaUiState.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { EstadoCargando() }
        is PartidaUiState.SinPreguntas -> SinPreguntas(e.motivo, onSalir)
        is PartidaUiState.EnCurso -> PreguntaEnCurso(
            estado = e,
            onSalir = onSalir,
            onResponder = viewModel::responder,
            onTocarElemento = viewModel::tocarElemento,
            onComprobarOrden = viewModel::comprobarOrden,
            onSiguiente = viewModel::siguiente,
        )
        is PartidaUiState.Terminada -> Resultado(e, onSalir)
    }
}

@Composable
private fun SinPreguntas(motivo: MotivoSinPreguntas, onSalir: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(
                when (motivo) {
                    MotivoSinPreguntas.SIN_PREGUNTAS -> R.string.sin_preguntas
                    MotivoSinPreguntas.EXAMEN_INSUFICIENTE -> R.string.examen_sin_preguntas
                    MotivoSinPreguntas.CATEGORIA_BLOQUEADA -> R.string.categoria_bloqueada_aviso
                    MotivoSinPreguntas.RETO_COMPLETADO -> R.string.reto_ya_completado_aviso
                },
            ),
            style = MaterialTheme.typography.bodyLarge,
        )
        OutlinedButton(onClick = onSalir) { Text(stringResource(R.string.volver)) }
    }
}

@Composable
private fun PreguntaEnCurso(
    estado: PartidaUiState.EnCurso,
    onSalir: () -> Unit,
    onResponder: (Respuesta) -> Unit,
    onTocarElemento: (String) -> Unit,
    onComprobarOrden: () -> Unit,
    onSiguiente: () -> Unit,
) {
    // En horizontal (pantalla baja) el botón fijo abajo se comería casi toda la altura: va dentro del desplazable.
    val altoDp = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() }
    val pantallaBaja = altoDp < ALTURA_PANTALLA_BAJA_DP.dp
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                if (estado.modo != ModoJuego.CATEGORIA) {
                    Text(
                        stringResource(if (estado.modo == ModoJuego.EXAMEN) R.string.titulo_examen else R.string.reto_titulo),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Text(
                    stringResource(R.string.pregunta_n_de_total, estado.numero, estado.total),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            IconButton(onClick = onSalir) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.salir_partida))
            }
        }
        LinearProgressIndicator(
            progress = { (estado.numero - if (estado.resultado == null) 1 else 0).toFloat() / estado.total },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TarjetaContenido(modifier = Modifier.fillMaxWidth()) {
                Text(estado.pregunta.enunciado, style = MaterialTheme.typography.titleLarge)
                pistaCapitulo(estado.pregunta)?.let {
                    Text(
                        stringResource(R.string.pista_capitulo, it),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            when (val contenido = estado.pregunta.contenido) {
                is Contenido.OpcionMultiple -> OpcionesMultiples(estado, contenido, onResponder)
                is Contenido.VerdaderoFalso -> BotonesVerdaderoFalso(estado, contenido, onResponder)
                is Contenido.Ordenar -> Ordenar(estado, contenido, onTocarElemento, onComprobarOrden)
            }
            estado.resultado?.let { Retroalimentacion(estado, it) }
            if (estado.resultado != null && pantallaBaja) BotonSiguiente(estado, onSiguiente, Modifier)
        }
        if (estado.resultado != null && !pantallaBaja) {
            BotonSiguiente(estado, onSiguiente, Modifier.padding(horizontal = 24.dp, vertical = 12.dp))
        }
    }
}

private const val ALTURA_PANTALLA_BAJA_DP = 480

@Composable
private fun BotonSiguiente(estado: PartidaUiState.EnCurso, onSiguiente: () -> Unit, modifier: Modifier) {
    Button(
        onClick = onSiguiente,
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp),
    ) {
        Text(stringResource(if (estado.esUltima) R.string.ver_resultado else R.string.siguiente))
    }
}

@Composable
private fun OpcionesMultiples(
    estado: PartidaUiState.EnCurso,
    contenido: Contenido.OpcionMultiple,
    onResponder: (Respuesta) -> Unit,
) {
    val correcta = contenido.opciones[contenido.indiceCorrecto]
    val elegida = (estado.respuestaDada as? Respuesta.Opcion)?.texto
    val respondida = estado.resultado != null
    estado.disposicion.forEach { opcion ->
        FilaOpcion(
            texto = opcion,
            enabled = !respondida,
            esCorrecta = respondida && opcion == correcta,
            esErronea = respondida && opcion == elegida && opcion != correcta,
            onClick = { onResponder(Respuesta.Opcion(opcion)) },
        )
    }
}

@Composable
private fun BotonesVerdaderoFalso(
    estado: PartidaUiState.EnCurso,
    contenido: Contenido.VerdaderoFalso,
    onResponder: (Respuesta) -> Unit,
) {
    val elegida = (estado.respuestaDada as? Respuesta.VerdaderoFalso)?.valor
    val respondida = estado.resultado != null
    listOf(true to R.string.verdadero, false to R.string.falso).forEach { (valor, etiqueta) ->
        FilaOpcion(
            texto = stringResource(etiqueta),
            enabled = !respondida,
            esCorrecta = respondida && valor == contenido.respuesta,
            esErronea = respondida && valor == elegida && valor != contenido.respuesta,
            onClick = { onResponder(Respuesta.VerdaderoFalso(valor)) },
        )
    }
}

/** Opción de respuesta con marca de icono, de modo que el resultado no dependa solo del color. */
@Composable
private fun FilaOpcion(
    texto: String,
    enabled: Boolean,
    esCorrecta: Boolean,
    esErronea: Boolean,
    onClick: () -> Unit,
) {
    val colores = MaterialTheme.colorScheme
    val borde = when {
        esCorrecta -> BorderStroke(2.dp, colores.primary)
        esErronea -> BorderStroke(2.dp, colores.error)
        else -> BorderStroke(1.dp, colores.outline)
    }
    val relleno = when {
        esCorrecta -> colores.primaryContainer
        esErronea -> colores.errorContainer
        else -> colores.surface
    }
    OutlinedButton(
        onClick = onClick,
        enabled = enabled || esCorrecta || esErronea,
        border = borde,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = relleno,
            disabledContainerColor = relleno,
            disabledContentColor = colores.onSurface,
        ),
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(texto, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            if (esCorrecta) Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.correcto))
            if (esErronea) Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.incorrecto))
        }
    }
}

/**
 * Libro y capítulo del pasaje, como referencia aproximada que no regala la respuesta. Solo en las categorías cuya
 * respuesta no es un libro o un lugar del pasaje (en libros, cartas y «dónde está escrito» el capítulo la delataría).
 */
private fun pistaCapitulo(pregunta: Pregunta): String? {
    if (pregunta.categoria !in CATEGORIAS_CON_PISTA) return null
    return pregunta.referencias
        .mapNotNull { (ParserReferencias.analizar(it) as? ResultadoReferencia.Valida)?.referencia }
        .map { "${it.libro.nombre} ${it.capituloInicio}" }
        .distinct()
        .take(2)
        .joinToString(" · ")
        .ifEmpty { null }
}

private val CATEGORIAS_CON_PISTA = setOf(Categoria.HISTORIA, Categoria.PERSONAJES, Categoria.LUGARES, Categoria.ENSENANZAS)

@Composable
private fun Ordenar(
    estado: PartidaUiState.EnCurso,
    contenido: Contenido.Ordenar,
    onTocarElemento: (String) -> Unit,
    onComprobar: () -> Unit,
) {
    val respondida = estado.resultado != null
    Text(
        stringResource(R.string.ordenar_instruccion),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (!respondida) {
        Text(stringResource(R.string.por_ordenar), style = MaterialTheme.typography.titleSmall)
        estado.disposicion.filter { it !in estado.orden }.forEach { elemento ->
            FilledTonalButton(
                onClick = { onTocarElemento(elemento) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            ) { Text(elemento) }
        }
    }
    Text(stringResource(R.string.tu_orden), style = MaterialTheme.typography.titleSmall)
    val mostrado = if (respondida) {
        (estado.respuestaDada as? Respuesta.Orden)?.elementos.orEmpty()
    } else {
        estado.orden
    }
    mostrado.forEachIndexed { indice, elemento ->
        val acierto = respondida && contenido.elementos.getOrNull(indice) == elemento
        val fallo = respondida && !acierto
        FilaOpcion(
            texto = stringResource(R.string.posicion_elemento, indice + 1, elemento),
            enabled = !respondida,
            esCorrecta = acierto,
            esErronea = fallo,
            onClick = { onTocarElemento(elemento) },
        )
    }
    if (!respondida) {
        Button(
            onClick = onComprobar,
            enabled = estado.orden.size == estado.disposicion.size,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        ) { Text(stringResource(R.string.comprobar)) }
    }
}

@Composable
private fun Retroalimentacion(estado: PartidaUiState.EnCurso, resultado: com.kaironyx.diceescritura.domain.usecase.ResultadoRespuesta) {
    val pregunta = estado.pregunta
    val textoCorrecto = when (val c = pregunta.contenido) {
        is Contenido.OpcionMultiple -> stringResource(R.string.respuesta_correcta_era, c.opciones[c.indiceCorrecto])
        is Contenido.VerdaderoFalso -> stringResource(
            R.string.respuesta_correcta_era,
            stringResource(if (c.respuesta) R.string.verdadero else R.string.falso),
        )
        is Contenido.Ordenar -> stringResource(R.string.orden_correcto_era, c.elementos.joinToString(", "))
    }
    val titulo = stringResource(if (resultado.correcta) R.string.correcto else R.string.incorrecto) +
        if (resultado.xp > 0) " · " + stringResource(R.string.xp_ganado_aviso, resultado.xp) else ""
    Column(verticalArrangement = Arrangement.spacedBy(Espacio.m)) {
        ContenedorRetroalimentacion(acierto = resultado.correcta, titulo = titulo, cuerpo = textoCorrecto)
        TarjetaContenido(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.justificacion_titulo), style = MaterialTheme.typography.titleSmall)
            Text(pregunta.explicacion, style = MaterialTheme.typography.bodyMedium)
            val porLeer = estado.citas.filter { it.texto == null }
            estado.citas.filter { it.texto != null }.forEach { cita ->
                ChipCita(cita.referencia)
                Text(cita.texto.orEmpty(), style = MaterialTheme.typography.bodyMedium)
            }
            if (porLeer.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(Espacio.s), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    porLeer.forEach { ChipCita(it.referencia) }
                }
            }
            pregunta.confesionRef?.let {
                Text(stringResource(R.string.confesion_aviso, it), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun Resultado(estado: PartidaUiState.Terminada, onSalir: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.resultado_titulo), style = MaterialTheme.typography.headlineMedium)
        Text(
            pluralStringResource(R.plurals.resultado_aciertos, estado.resumen.aciertos, estado.resumen.aciertos, estado.resumen.total),
            style = MaterialTheme.typography.titleLarge,
        )
        if (estado.modo != ModoJuego.EXAMEN) {
            Text(stringResource(R.string.resultado_xp, estado.resumen.xpGanado), style = MaterialTheme.typography.bodyLarge)
            if (estado.xpPerdido > 0) {
                Text(
                    stringResource(R.string.resultado_xp_tope, estado.xpPerdido),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (estado.modo == ModoJuego.EXAMEN) {
            val nivel = estado.nivelNuevo
            if (estado.aprobado == true && nivel != null) PlantaCreciendo(nivel.numero)
            Text(
                text = if (estado.aprobado == true && nivel != null) {
                    stringResource(R.string.examen_aprobado, nivel.numero, nivel.nombre)
                } else {
                    stringResource(R.string.examen_no_aprobado, estado.porcentajeAprobacion)
                },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Button(onClick = onSalir, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
            Text(stringResource(R.string.volver_inicio))
        }
    }
}

/** La planta del nivel nuevo aparece creciendo desde el suelo; sin animaciones del sistema, aparece ya formada. */
@Composable
private fun PlantaCreciendo(numero: Int) {
    val animar = rememberAnimacionesActivas()
    val escala = remember { Animatable(if (animar) 0.3f else 1f) }
    LaunchedEffect(Unit) { if (animar) escala.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        PlantaNivel(
            numero = numero,
            tamano = 160.dp,
            modifier = Modifier.graphicsLayer {
                scaleX = escala.value
                scaleY = escala.value
                transformOrigin = TransformOrigin(0.5f, 0.9f)
            },
        )
    }
}
