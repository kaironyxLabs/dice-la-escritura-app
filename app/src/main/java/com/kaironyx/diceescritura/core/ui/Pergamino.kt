package com.kaironyx.diceescritura.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaironyx.diceescritura.core.theme.Espacio
import com.kaironyx.diceescritura.core.theme.TituloFontFamily
import kotlin.math.sin

/** Paleta de pergamino: fondos de papel, tinta marrón y oro. El texto sobre [Oro] es blanco (AA). */
object Pergamino {
    val Crema = Color(0xFFFBF3DC)
    val Claro = Color(0xFFF5E8C8)
    val Medio = Color(0xFFE9D5A4)
    val Oscuro = Color(0xFFD9BE86)
    val Borde = Color(0xFFBFA066)
    val Tinta = Color(0xFF4A2F10)
    val TintaSuave = Color(0xFF6B4410)
    val Oro = Color(0xFF8C6414)
    val OroClaro = Color(0xFFD4A63A)
}

/** Borde irregular, como papel rasgado, en el lado de abajo o de arriba. La forma es siempre la misma. */
class BordeRasgadoShape(private val abajo: Boolean, private val amplitud: Dp = 6.dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val a = with(density) { amplitud.toPx() }
        val paso = with(density) { 10.dp.toPx() }
        val camino = Path()
        fun irregular(x: Float): Float = a * (0.55f + 0.3f * sin(x / paso * 1.7f) + 0.15f * sin(x / paso * 4.3f + 1f))
        if (abajo) {
            camino.moveTo(0f, 0f)
            camino.lineTo(size.width, 0f)
            var x = size.width
            while (x > 0f) {
                camino.lineTo(x, size.height - irregular(x))
                x -= paso
            }
            camino.lineTo(0f, size.height - irregular(0f))
        } else {
            camino.moveTo(0f, size.height)
            camino.lineTo(0f, irregular(0f))
            var x = 0f
            while (x < size.width) {
                camino.lineTo(x, irregular(x))
                x += paso
            }
            camino.lineTo(size.width, irregular(size.width))
            camino.lineTo(size.width, size.height)
        }
        camino.close()
        return Outline.Generic(camino)
    }
}

/** Fondo de papel claro con un leve degradado y manchas suaves en las esquinas. */
fun Modifier.fondoPergamino(): Modifier = this
    .background(Brush.verticalGradient(listOf(Pergamino.Crema, Color(0xFFF8EDD0), Pergamino.Crema)))
    .drawBehind {
        val manchas = listOf(
            Offset(0f, size.height * 0.15f) to 0.55f,
            Offset(size.width, size.height * 0.55f) to 0.6f,
            Offset(size.width * 0.2f, size.height) to 0.5f,
        )
        manchas.forEach { (centro, radio) ->
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Pergamino.Oscuro.copy(alpha = 0.28f), Color.Transparent),
                    center = centro,
                    radius = size.width * radio,
                ),
                radius = size.width * radio,
                center = centro,
            )
        }
    }

private val FondoLosa = Brush.verticalGradient(listOf(Color(0xFFF7EACB), Pergamino.Medio))

/** Losa de pergamino con sombra y borde fino: la tarjeta de la app. Si recibe [onClick], es tocable. */
@Composable
fun TarjetaPergamino(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    val forma = RoundedCornerShape(20.dp)
    val colores = CardDefaults.cardColors(containerColor = Color.Transparent)
    val elevacion = CardDefaults.cardElevation(defaultElevation = 4.dp)
    val borde = BorderStroke(1.dp, Pergamino.Borde)
    val cuerpo: @Composable ColumnScope.() -> Unit = {
        Column(
            modifier = Modifier.background(FondoLosa).padding(Espacio.m + Espacio.xs),
            verticalArrangement = Arrangement.spacedBy(Espacio.s),
            content = contenido,
        )
    }
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = forma, colors = colores, elevation = elevacion, border = borde, content = cuerpo)
    } else {
        Card(modifier = modifier, shape = forma, colors = colores, elevation = elevacion, border = borde, content = cuerpo)
    }
}

/** Botón de acción principal en oro, con forma de píldora. */
@Composable
fun BotonDorado(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = CircleShape,
        color = Pergamino.Oro,
        contentColor = Color.White,
        shadowElevation = 3.dp,
    ) {
        Box(Modifier.padding(horizontal = Espacio.l, vertical = Espacio.s + Espacio.xs), contentAlignment = Alignment.Center) {
            Text(texto, style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold))
        }
    }
}

/**
 * Medallón circular con aro dorado. Por ahora lleva un [icono] sobre un cielo suave; la ilustración
 * de época de cada categoría se pondrá aquí cuando exista.
 */
@Composable
fun Medallon(icono: ImageVector, modifier: Modifier = Modifier, tamano: Dp = 92.dp) {
    Box(
        modifier = modifier
            .size(tamano)
            .border(3.dp, Brush.verticalGradient(listOf(Pergamino.OroClaro, Pergamino.Oro)), CircleShape)
            .padding(4.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color(0xFFC9DDE6), Color(0xFFF1E2B6), Color(0xFFDDBF84)))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icono, contentDescription = null, tint = Pergamino.TintaSuave, modifier = Modifier.size(tamano * 0.46f))
    }
}

/** Cabecera de pergamino con el nombre de la app en tipografía de manuscrito y borde rasgado abajo. */
@Composable
fun BarraTituloPergamino(titulo: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(BordeRasgadoShape(abajo = true))
            .background(Brush.verticalGradient(listOf(Pergamino.Oscuro, Pergamino.Medio, Color(0xFFF2E3BC))))
            .statusBarsPadding()
            .padding(top = Espacio.s + Espacio.xs, bottom = Espacio.l),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            titulo,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = TituloFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
            ),
            color = Pergamino.TintaSuave,
        )
    }
}

/** Fondo de la barra inferior: pergamino con el borde de arriba rasgado. */
fun Modifier.fondoBarraInferior(): Modifier = this
    .clip(BordeRasgadoShape(abajo = false))
    .background(Brush.verticalGradient(listOf(Color(0xFFF2E3BC), Pergamino.Medio, Pergamino.Oscuro)))
