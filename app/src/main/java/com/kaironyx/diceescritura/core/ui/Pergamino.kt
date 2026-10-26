package com.kaironyx.diceescritura.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.kaironyx.diceescritura.R
import com.kaironyx.diceescritura.core.theme.Espacio
import com.kaironyx.diceescritura.core.theme.TituloFontFamily
import kotlin.math.sin

/** Paleta de pergamino: fondos de papel, tinta marrón y oro. El texto sobre [Oro] es blanco (AA). */
object Pergamino {
    val Crema = Color(0xFFF6EBCB)
    val Claro = Color(0xFFF0E2BC)
    val Medio = Color(0xFFE7D6AC)
    val Oscuro = Color(0xFFE2CE9F)
    val Borde = Color(0xFFBFA066)
    val Tinta = Color(0xFF4A2F10)
    val TintaSuave = Color(0xFF6B4410)
    val Oro = Color(0xFF8C6414)
    val OroClaro = Color(0xFFD4A63A)

    /** Marrón de quemado del borde del papel. */
    val Quemado = Color(0xFF8A5A24)
}

/** Textura de papel (res/drawable-nodpi/pergamino_textura.webp), espejada para que no se vean costuras. */
@Composable
private fun rememberTextura(): ShaderBrush {
    val imagen: ImageBitmap = ImageBitmap.imageResource(R.drawable.pergamino_textura)
    return remember(imagen) {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader = ImageShader(imagen, TileMode.Mirror, TileMode.Mirror)
        }
    }
}

/** Desviación suave y repetible a lo largo de un borde: [x] es la posición y [paso] el largo de onda. */
private fun irregular(x: Float, paso: Float, semilla: Int): Float =
    0.5f + 0.32f * sin(x / paso * 1.7f + semilla) + 0.18f * sin(x / paso * 4.1f + semilla * 2.3f)

/** Borde irregular, como papel rasgado, solo en el lado de abajo o de arriba. La forma es siempre la misma. */
class BordeRasgadoShape(private val abajo: Boolean, private val amplitud: Dp = 6.dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val a = with(density) { amplitud.toPx() }
        val paso = with(density) { 10.dp.toPx() }
        val camino = Path()
        if (abajo) {
            camino.moveTo(0f, 0f)
            camino.lineTo(size.width, 0f)
            var x = size.width
            while (x > 0f) {
                camino.lineTo(x, size.height - a * irregular(x, paso, 0))
                x -= paso
            }
            camino.lineTo(0f, size.height - a * irregular(0f, paso, 0))
        } else {
            camino.moveTo(0f, size.height)
            camino.lineTo(0f, a * irregular(0f, paso, 0))
            var x = 0f
            while (x < size.width) {
                camino.lineTo(x, a * irregular(x, paso, 0))
                x += paso
            }
            camino.lineTo(size.width, a * irregular(size.width, paso, 0))
            camino.lineTo(size.width, size.height)
        }
        camino.close()
        return Outline.Generic(camino)
    }
}

/** Hoja de papel con los cuatro bordes irregulares, dentro del rectángulo dado. [semilla] cambia el rasgado. */
private fun hojaRasgada(izq: Float, arr: Float, der: Float, aba: Float, amplitud: Float, paso: Float, semilla: Int): Path {
    val camino = Path()
    fun x(t: Float, lado: Int) = amplitud * irregular(t, paso, semilla + lado * 3)
    camino.moveTo(izq + x(izq, 0), arr + x(izq, 0))
    var t = izq
    while (t < der) { camino.lineTo(t, arr + x(t, 0)); t += paso }
    t = arr
    while (t < aba) { camino.lineTo(der - x(t, 1), t); t += paso }
    t = der
    while (t > izq) { camino.lineTo(t, aba - x(t, 2)); t -= paso }
    t = aba
    while (t > arr) { camino.lineTo(izq + x(t, 3), t); t -= paso }
    camino.close()
    return camino
}

/** Oscurece hacia dentro desde el borde del [camino]: el papel viejo se quema por las orillas. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.quemarBorde(camino: Path, grosor: Float, intensidad: Float) {
    clipPath(camino) {
        val capas = 12
        for (i in 0 until capas) {
            val f = (i + 1f) / capas
            drawPath(
                camino,
                color = Pergamino.Quemado.copy(alpha = intensidad * (1f - f) * 0.55f),
                style = Stroke(width = grosor * 2f * f),
            )
        }
    }
}

/**
 * Hoja de pergamino con los cuatro bordes rasgados, textura de papel, orillas quemadas y sombra.
 * Deja un margen alrededor para la sombra, así que el contenido debe sumar ese margen a su relleno.
 */
@Composable
fun Modifier.hojaPergamino(semilla: Int = 7): Modifier {
    val textura = rememberTextura()
    return this.drawWithCache {
        val margen = 4.dp.toPx()
        val margenAbajo = 10.dp.toPx()
        val camino = hojaRasgada(margen, margen, size.width - margen, size.height - margenAbajo, 3.dp.toPx(), 12.dp.toPx(), semilla)
        onDrawBehind {
            // Sombra: la misma hoja desplazada y repetida con poca opacidad.
            for (i in 4 downTo 1) {
                translateYPath(camino, i * 1.6.dp.toPx(), Color.Black.copy(alpha = 0.05f))
            }
            drawPath(camino, brush = textura)
            quemarBorde(camino, grosor = 9.dp.toPx(), intensidad = 0.25f)
            drawPath(camino, color = Pergamino.Quemado.copy(alpha = 0.3f), style = Stroke(width = 1.dp.toPx()))
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.translateYPath(camino: Path, dy: Float, color: Color) {
    translate(left = 0f, top = dy) { drawPath(camino, color = color) }
}

/** Fondo de papel de toda la pantalla: crema clara con la textura apenas visible. */
@Composable
fun Modifier.fondoPergamino(): Modifier {
    val textura = rememberTextura()
    return this
        .background(Pergamino.Crema)
        .drawBehind { drawRect(textura, alpha = 0.5f) }
}

/** Losa de pergamino: la tarjeta de la app. Si recibe [onClick], es tocable. */
@Composable
fun TarjetaPergamino(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    semilla: Int = 7,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    val tocable = if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier
    Box(modifier = modifier.then(tocable).hojaPergamino(semilla)) {
        Column(
            modifier = Modifier.padding(start = Espacio.m + 6.dp, end = Espacio.m + 6.dp, top = Espacio.m + 6.dp, bottom = Espacio.m + 12.dp),
            verticalArrangement = Arrangement.spacedBy(Espacio.s),
            content = contenido,
        )
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
 * Medallón circular con aro dorado. Lleva un [icono] sobre un cielo suave (se usa en el catálogo de
 * componentes); las categorías usan [MedallonIlustrado].
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

/** Medallón con la ilustración de época de una categoría ([ilustracion] es un drawable cuadrado), recortada en círculo con el aro dorado. */
@Composable
fun MedallonIlustrado(@DrawableRes ilustracion: Int, modifier: Modifier = Modifier, tamano: Dp = 92.dp) {
    Image(
        painter = painterResource(ilustracion),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(tamano)
            .border(3.dp, Brush.verticalGradient(listOf(Pergamino.OroClaro, Pergamino.Oro)), CircleShape)
            .padding(4.dp)
            .clip(CircleShape),
    )
}

/** Cabecera de pergamino con el nombre de la app en tipografía de manuscrito y borde rasgado abajo. */
@Composable
fun BarraTituloPergamino(titulo: String, modifier: Modifier = Modifier) {
    val textura = rememberTextura()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(BordeRasgadoShape(abajo = true, amplitud = 8.dp))
            .background(Pergamino.Oscuro)
            .drawBehind {
                drawRect(textura)
                // Quemado del borde de abajo.
                drawRect(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Pergamino.Quemado.copy(alpha = 0.16f)),
                        startY = size.height - 36.dp.toPx(),
                        endY = size.height,
                    ),
                )
            }
            .statusBarsPadding()
            .padding(top = Espacio.s + Espacio.xs, bottom = Espacio.l),
        contentAlignment = Alignment.Center,
    ) {
        TituloDorado(titulo)
    }
}

/**
 * Título con letras de oro bruñido y contorno marrón, como el del video. Tres capas: sombra, contorno y relleno
 * con degradado metálico (luz arriba, oro oscuro al centro, reflejo abajo).
 */
@Composable
fun TituloDorado(texto: String, modifier: Modifier = Modifier, tamano: TextUnit = 38.sp) {
    val base = MaterialTheme.typography.headlineMedium.copy(
        fontFamily = TituloFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = tamano,
        letterSpacing = 0.8.sp,
        textAlign = TextAlign.Center,
    )
    val oro = Brush.verticalGradient(
        0f to Color(0xFFFFF1B0),
        0.35f to Color(0xFFE9B83C),
        0.6f to Color(0xFFB8801A),
        0.85f to Color(0xFFE7B643),
        1f to Color(0xFFFBE08A),
    )
    Box(modifier = modifier.semantics { contentDescription = texto }, contentAlignment = Alignment.Center) {
        Text(
            texto,
            style = base.copy(
                color = Color(0xFF3A210A),
                shadow = Shadow(Color(0x66301800), Offset(2f, 5f), 6f),
                drawStyle = Stroke(width = 6f, join = StrokeJoin.Round),
            ),
        )
        Text(texto, style = base.copy(brush = oro))
    }
}

/** Fondo de la barra inferior: pergamino con el borde de arriba rasgado y quemado. */
@Composable
fun Modifier.fondoBarraInferior(): Modifier {
    val textura = rememberTextura()
    return this
        .clip(BordeRasgadoShape(abajo = false, amplitud = 8.dp))
        .background(Pergamino.Oscuro)
        .drawBehind {
            drawRect(textura)
            drawRect(
                Brush.verticalGradient(
                    listOf(Pergamino.Quemado.copy(alpha = 0.16f), Color.Transparent),
                    startY = 0f,
                    endY = 36.dp.toPx(),
                ),
            )
        }
}
