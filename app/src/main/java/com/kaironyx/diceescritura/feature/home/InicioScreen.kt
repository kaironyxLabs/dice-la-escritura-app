package com.kaironyx.diceescritura.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.kaironyx.diceescritura.core.theme.PasajeFontFamily
import com.kaironyx.diceescritura.core.ui.IconoLibro
import com.kaironyx.diceescritura.core.ui.Medallon
import com.kaironyx.diceescritura.core.ui.Pergamino
import com.kaironyx.diceescritura.feature.nombreRes
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaironyx.diceescritura.R
import com.kaironyx.diceescritura.core.ui.BarraProgresoAnimada
import com.kaironyx.diceescritura.core.ui.BotonDorado
import com.kaironyx.diceescritura.core.ui.PlantaNivel
import com.kaironyx.diceescritura.core.ui.PuntosReto
import com.kaironyx.diceescritura.core.ui.TarjetaPergamino
import com.kaironyx.diceescritura.core.ui.EstadoCargando
import com.kaironyx.diceescritura.data.repository.PerfilLocal
import com.kaironyx.diceescritura.domain.model.Categoria
import com.kaironyx.diceescritura.feature.perfil.AvatarCircular
import com.kaironyx.diceescritura.feature.quiz.CategoriasViewModel
import com.kaironyx.diceescritura.feature.quiz.ItemCategoria
import com.kaironyx.diceescritura.domain.usecase.EstadoNivel
import com.kaironyx.diceescritura.domain.usecase.Racha

@Composable
fun InicioScreen(
    onElegirCategoria: (Categoria) -> Unit,
    onExamen: () -> Unit,
    onReto: () -> Unit,
    onPerfil: () -> Unit,
    onNiveles: () -> Unit,
    viewModel: InicioViewModel = hiltViewModel(),
    categoriasViewModel: CategoriasViewModel = hiltViewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val categorias by categoriasViewModel.categorias.collectAsStateWithLifecycle()
    InicioContenido(estado, categorias, onElegirCategoria, onExamen, onReto, onPerfil, onNiveles)
}

@Composable
fun InicioContenido(
    estado: InicioUiState?,
    categorias: List<ItemCategoria>?,
    onElegirCategoria: (Categoria) -> Unit,
    onExamen: () -> Unit,
    onReto: () -> Unit,
    onPerfil: () -> Unit,
    onNiveles: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (estado == null) {
            Text(stringResource(R.string.titulo_inicio), style = MaterialTheme.typography.headlineMedium)
            EstadoCargando()
            return@Column
        }
        Saludo(estado.persona, onPerfil)
        TarjetaNivel(estado.nivel, onNiveles)
        TarjetaReto(estado, onReto)
        if (estado.nivel.examenDisponible) {
            Text(
                stringResource(R.string.examen_disponible),
                style = MaterialTheme.typography.bodyLarge,
            )
            BotonDorado(stringResource(R.string.boton_examen), onClick = onExamen)
        }
        Text(stringResource(R.string.boton_jugar_categoria), style = MaterialTheme.typography.headlineSmall)
        if (categorias == null) EstadoCargando() else CuadriculaCategorias(categorias, onElegirCategoria)
    }
}

/** Saludo con el avatar; tocarlo abre el perfil. */
@Composable
private fun Saludo(persona: PerfilLocal?, onPerfil: () -> Unit) {
    if (persona == null) {
        Text(stringResource(R.string.titulo_inicio), style = MaterialTheme.typography.headlineMedium)
        return
    }
    val descripcion = stringResource(R.string.inicio_abrir_perfil)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPerfil)
            .semantics(mergeDescendants = true) { contentDescription = descripcion },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AvatarCircular(persona.nombre, persona.avatar, tamano = 56.dp)
        Text(
            stringResource(R.string.inicio_saludo, persona.nombre),
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}

@Composable
private fun TarjetaNivel(estado: EstadoNivel, onNiveles: () -> Unit) {
    val abrir = stringResource(R.string.inicio_ver_niveles)
    TarjetaPergamino(
        modifier = Modifier.fillMaxWidth().semantics { onClick(label = abrir, action = null) },
        onClick = onNiveles,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            PlantaNivel(
                numero = estado.nivel.numero,
                tamano = 88.dp,
                descripcion = stringResource(R.string.niveles_descripcion_planta_actual, estado.nivel.numero, estado.nivel.nombre),
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.nivel_numero, estado.nivel.numero),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(estado.nivel.nombre, style = MaterialTheme.typography.headlineSmall)
            }
        }
        val porcentaje = (estado.fraccion * 100).toInt()
        BarraProgresoAnimada(estado.fraccion, stringResource(R.string.descripcion_barra_xp, porcentaje))
        Text(
            text = if (estado.xpDelNivel == null) {
                stringResource(R.string.nivel_maximo)
            } else {
                stringResource(R.string.xp_progreso, estado.xpEnNivel, estado.xpDelNivel)
            },
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            stringResource(R.string.inicio_ver_niveles),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun TarjetaReto(estado: InicioUiState, onReto: () -> Unit) {
    TarjetaPergamino(modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.reto_titulo), style = MaterialTheme.typography.titleLarge)
        TextoRacha(estado.racha)
        when (val reto = estado.reto) {
            EstadoReto.SinEmpezar -> {
                Text(stringResource(R.string.reto_sin_empezar), style = MaterialTheme.typography.bodyMedium)
                BotonDorado(stringResource(R.string.reto_boton_empezar), onClick = onReto)
            }
            is EstadoReto.EnCurso -> {
                PuntosReto(reto.respondidas, reto.total)
                Text(
                    pluralStringResource(R.plurals.reto_en_curso, reto.total, reto.respondidas, reto.total),
                    style = MaterialTheme.typography.bodyMedium,
                )
                BotonDorado(stringResource(R.string.reto_boton_continuar), onClick = onReto)
            }
            is EstadoReto.Completado -> {
                PuntosReto(reto.total, reto.total)
                Text(
                    pluralStringResource(R.plurals.reto_completado, reto.aciertos, reto.aciertos, reto.total),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

/** Racha en tono sobrio: informa, no presiona. */
@Composable
private fun TextoRacha(racha: Racha) {
    if (racha.actual == 0) {
        Text(
            stringResource(R.string.racha_vacia),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Text(
        pluralStringResource(R.plurals.racha_dias, racha.actual, racha.actual),
        style = MaterialTheme.typography.titleMedium,
    )
    if (racha.mejor > racha.actual) {
        Text(
            stringResource(R.string.racha_mejor, racha.mejor),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    if (racha.graciaDisponibleEstaSemana) {
        Text(
            stringResource(R.string.racha_gracia),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Categorías en dos columnas, cada una con su medallón. Las bloqueadas se ven atenuadas y no se tocan. */
@Composable
private fun CuadriculaCategorias(categorias: List<ItemCategoria>, onElegir: (Categoria) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        categorias.chunked(2).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                fila.forEach { item ->
                    LosaCategoria(item, onClick = { onElegir(item.categoria) }, modifier = Modifier.weight(1f))
                }
                if (fila.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LosaCategoria(item: ItemCategoria, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bloqueada = item.bloqueadaHastaNivel != null
    val habilitada = !bloqueada && item.preguntasDisponibles > 0
    TarjetaPergamino(
        modifier = modifier.alpha(if (habilitada) 1f else 0.55f),
        onClick = if (habilitada) onClick else null,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Medallon(item.categoria.icono())
            Text(
                stringResource(item.categoria.nombreRes()),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = PasajeFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
                color = Pergamino.Tinta,
                minLines = 2,
            )
            Text(
                text = if (bloqueada) {
                    stringResource(R.string.categoria_bloqueada, item.bloqueadaHastaNivel)
                } else {
                    pluralStringResource(R.plurals.preguntas_disponibles, item.preguntasDisponibles, item.preguntasDisponibles)
                },
                style = MaterialTheme.typography.bodySmall,
                color = Pergamino.TintaSuave,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Icono provisional de cada categoría, hasta que exista su ilustración. */
private fun Categoria.icono(): ImageVector = when (this) {
    Categoria.LIBROS -> IconoLibro
    Categoria.HISTORIA -> Icons.Filled.DateRange
    Categoria.PERSONAJES -> Icons.Filled.Person
    Categoria.LUGARES -> Icons.Filled.Place
    Categoria.ENSENANZAS -> Icons.Filled.Star
    Categoria.CARTAS -> Icons.Filled.Email
    Categoria.DONDE_ESTA_ESCRITO -> Icons.Filled.Search
    Categoria.CONFESION -> Icons.Filled.Info
}
