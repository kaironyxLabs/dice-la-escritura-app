package com.kaironyx.diceescritura.core.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaironyx.diceescritura.R
import com.kaironyx.diceescritura.core.theme.PasajeFontFamily
import com.kaironyx.diceescritura.core.ui.rememberAnimacionesActivas
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// Paleta del splash: penumbra cálida, pergamino y oro antiguo (fuera del tema: el splash es siempre oscuro).
private val FondoClaro = Color(0xFF2A1508)
private val FondoOscuro = Color(0xFF0C0703)
private val TintaPrincipal = Color(0xFF2B1406)
private val TintaSecundaria = Color(0xFF6B4524)
private val OroDivino = Color(0xFFE2B755)
private val MaderaOscura = Color(0xFF3E1F08)
private val MaderaDorada = Color(0xFF88541D)

/** Proporción de la ilustración del pergamino (ancho 570, alto 798). */
private const val ALTO_SOBRE_ANCHO = 798f / 570f

/**
 * Pantalla de apertura: un pergamino que se desenrolla, el título que aparece con un destello y
 * partículas doradas. Se salta con un toque. Si el sistema tiene las animaciones apagadas, muestra
 * el estado final un instante. [iniciar] retrasa la animación hasta que el splash del sistema se haya ido.
 */
@Composable
fun SplashBiblico(alTerminar: () -> Unit, modifier: Modifier = Modifier, iniciar: Boolean = true) {
    val animar = rememberAnimacionesActivas()
    val terminar by rememberUpdatedState(alTerminar)
    val desenrollado = remember { Animatable(if (animar) 0.06f else 1f) }
    val contenido = remember { Animatable(if (animar) 0f else 1f) }
    val halo = remember { Animatable(if (animar) 0.5f else 1f) }
    val destello = remember { Animatable(0f) }
    val polvo = remember { Animatable(0f) }

    LaunchedEffect(animar, iniciar) {
        // Espera a que la pantalla de inicio del sistema ya no tape la app: si no, la animación corre sin verse.
        if (!iniciar) return@LaunchedEffect
        if (!animar) {
            delay(1200)
            terminar()
            return@LaunchedEffect
        }
        launch {
            polvo.animateTo(1f, infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart))
        }
        launch { halo.animateTo(1f, tween(1500, easing = FastOutSlowInEasing)) }
        launch { desenrollado.animateTo(1f, tween(1300, easing = EaseOutCubic)) }
        delay(900)
        launch { destello.animateTo(1f, tween(1100, easing = FastOutSlowInEasing)) }
        contenido.animateTo(1f, tween(900))
        delay(1700)
        terminar()
    }

    val descripcion = stringResource(R.string.splash_descripcion)
    val saltar = stringResource(R.string.splash_saltar)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FondoOscuro) // base opaca: la app que carga debajo no debe transparentarse
            .background(Brush.radialGradient(listOf(FondoClaro.copy(alpha = halo.value), FondoOscuro), radius = 1400f))
            .clickable(onClickLabel = saltar) { terminar() }
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center,
    ) {
        PolvoDorado(progreso = polvo.value)
        BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val ancho = maxWidth * 0.88f
            val alto = ancho * ALTO_SOBRE_ANCHO
            // Resplandor dorado detrás del pergamino.
            Box(
                Modifier
                    .size(ancho * 1.25f, alto * 1.1f)
                    .alpha(halo.value * 0.55f)
                    .background(Brush.radialGradient(listOf(OroDivino.copy(alpha = 0.55f), Color.Transparent))),
            )
            Pergamino(
                ancho = ancho,
                alto = alto,
                fraccion = desenrollado.value.coerceIn(0f, 1f),
                contenido = contenido.value,
                destello = destello.value,
            )
        }
    }
}

@Composable
private fun Pergamino(ancho: Dp, alto: Dp, fraccion: Float, contenido: Float, destello: Float) {
    Box(
        modifier = Modifier
            .size(ancho, alto * fraccion)
            .shadow(18.dp, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.splash_pergamino),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .requiredSize(ancho, alto)
                .drawWithContent {
                    drawContent()
                    // Borde envejecido: oscurece las orillas.
                    drawRect(
                        Brush.radialGradient(
                            0.55f to Color.Transparent,
                            1f to Color(0xFF3A1E08).copy(alpha = 0.45f),
                            center = center,
                            radius = size.maxDimension * 0.78f,
                        ),
                    )
                },
        )
        Column(
            modifier = Modifier.requiredSize(ancho, alto).alpha(contenido),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Titulo(destello)
        }
        BarraRodillo(Modifier.align(Alignment.TopCenter))
        BarraRodillo(Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun Titulo(destello: Float) {
    // Una franja dorada recorre el título de izquierda a derecha.
    val barrido = -400f + destello * 1100f
    val pincel = Brush.linearGradient(
        colors = listOf(TintaPrincipal, TintaPrincipal, OroDivino, TintaPrincipal, TintaPrincipal),
        start = Offset(barrido, 0f),
        end = Offset(barrido + 400f, 0f),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.radialGradient(listOf(Color(0xFFFFF3D6).copy(alpha = 0.7f), Color.Transparent))),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.splash_titulo),
            style = TextStyle(
                brush = pincel,
                fontFamily = PasajeFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                lineHeight = 44.sp,
                textAlign = TextAlign.Center,
            ),
        )
        Spacer(Modifier.height(6.dp))
        Floritura()
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.splash_lema),
            fontFamily = PasajeFontFamily,
            fontSize = 14.sp,
            color = TintaSecundaria,
            textAlign = TextAlign.Center,
        )
    }
}

/** Adorno bajo el título: una línea ondulada con un remate en cada extremo. */
@Composable
private fun Floritura() {
    Canvas(modifier = Modifier.size(150.dp, 14.dp).clearAndSetSemantics {}) {
        val w = size.width
        val h = size.height
        val onda = Path().apply {
            moveTo(0f, h / 2)
            cubicTo(w * 0.2f, -h * 0.2f, w * 0.35f, h * 1.2f, w / 2, h / 2)
            cubicTo(w * 0.65f, -h * 0.2f, w * 0.8f, h * 1.2f, w, h / 2)
        }
        drawPath(onda, TintaSecundaria, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
        drawCircle(TintaSecundaria, 2.5.dp.toPx(), Offset(0f, h / 2))
        drawCircle(TintaSecundaria, 2.5.dp.toPx(), Offset(w, h / 2))
    }
}

/** Rodillo de madera con vetas doradas en el borde del pergamino. */
@Composable
private fun BarraRodillo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
            .background(Brush.verticalGradient(listOf(MaderaOscura, MaderaDorada, MaderaOscura)))
            .border(0.5.dp, OroDivino.copy(alpha = 0.6f)),
    )
}

/** Partículas de polvo dorado suspendidas en la luz. */
@Composable
private fun PolvoDorado(progreso: Float) {
    Canvas(modifier = Modifier.fillMaxSize().clearAndSetSemantics {}) {
        val azar = Random(42)
        repeat(28) {
            val x = azar.nextFloat() * size.width
            val y0 = azar.nextFloat() * size.height
            val radio = azar.nextFloat() * 3f + 1f
            val velocidad = azar.nextFloat() * 40f + 20f
            val alfa = azar.nextFloat() * 0.4f + 0.2f
            val y = ((y0 - progreso * velocidad * 8) % size.height).let { if (it < 0) it + size.height else it }
            drawCircle(OroDivino.copy(alpha = alfa), radio.dp.toPx() / 2f, Offset(x, y))
        }
    }
}
