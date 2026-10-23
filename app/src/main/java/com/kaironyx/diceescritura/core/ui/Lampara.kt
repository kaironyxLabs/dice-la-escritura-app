package com.kaironyx.diceescritura.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val LlamaExterior = Color(0xFFE8A317)
private val LlamaInterior = Color(0xFFFFE08A)
private val CuerpoLampara = Color(0xFF8A5A24)
private val BrilloCuerpo = Color(0xFFC08A3E)

/**
 * Lámpara de aceite (símbolo de la racha): encendida si hoy se completó el reto, apagada si no.
 * Con [celebrar] la llama se enciende creciendo y suelta destellos; encendida, la llama titila un poco.
 * Sin animaciones del sistema todo queda quieto. Es decorativa salvo que se pase [descripcion].
 */
@Composable
fun Lampara(
    encendida: Boolean,
    modifier: Modifier = Modifier,
    tamano: Dp = 72.dp,
    celebrar: Boolean = false,
    descripcion: String? = null,
) {
    val animar = rememberAnimacionesActivas()
    val encendido = remember { Animatable(if (encendida && !(celebrar && animar)) 1f else 0f) }
    LaunchedEffect(encendida) {
        if (!encendida) {
            encendido.snapTo(0f)
        } else if (animar && celebrar) {
            encendido.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        } else {
            encendido.snapTo(1f)
        }
    }
    val titileo = if (animar && encendida) {
        rememberInfiniteTransition(label = "titileo").animateFloat(
            initialValue = 0.94f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
            label = "llama",
        ).value
    } else {
        1f
    }
    val semantica = if (descripcion != null) Modifier.semantics { contentDescription = descripcion } else Modifier
    Box(modifier.size(tamano).then(semantica), contentAlignment = Alignment.Center) {
        if (celebrar && animar && encendida) {
            Destellos(Modifier.requiredSize(tamano * 2.6f))
        }
        Canvas(Modifier.size(tamano)) {
            val k = size.minDimension / 100f
            dibujarCuerpo(k)
            val f = encendido.value
            if (f > 0f) dibujarLlama(k, f * titileo, f)
        }
    }
}

private fun DrawScope.dibujarCuerpo(k: Float) {
    // Asa a la izquierda.
    drawArc(
        CuerpoLampara, startAngle = 90f, sweepAngle = 180f, useCenter = false,
        topLeft = Offset(8f * k, 60f * k), size = Size(18f * k, 22f * k), style = Stroke(5f * k),
    )
    // Pico a la derecha.
    val pico = Path().apply {
        moveTo(66f * k, 66f * k)
        lineTo(90f * k, 62f * k)
        lineTo(90f * k, 70f * k)
        lineTo(70f * k, 80f * k)
        close()
    }
    drawPath(pico, CuerpoLampara)
    // Cuerpo ovalado con un brillo.
    drawOval(CuerpoLampara, Offset(16f * k, 62f * k), Size(58f * k, 30f * k))
    drawOval(BrilloCuerpo, Offset(24f * k, 66f * k), Size(34f * k, 8f * k))
    // Base.
    drawRoundRect(CuerpoLampara, Offset(30f * k, 88f * k), Size(28f * k, 6f * k), androidx.compose.ui.geometry.CornerRadius(3f * k))
}

/** [escala] mueve el tamaño de la llama (con titileo); [luz] el brillo (0 a 1). */
private fun DrawScope.dibujarLlama(k: Float, escala: Float, luz: Float) {
    val base = Offset(90f * k, 62f * k)
    drawCircle(
        Brush.radialGradient(
            listOf(LlamaExterior.copy(alpha = 0.55f * luz), Color.Transparent),
            center = Offset(base.x - 2f * k, base.y - 14f * k * escala),
            radius = 34f * k,
        ),
        radius = 34f * k,
        center = Offset(base.x - 2f * k, base.y - 14f * k * escala),
    )
    fun gota(ancho: Float, alto: Float, color: Color) {
        val a = ancho * k * luz
        val h = alto * k * escala * luz
        val p = Path().apply {
            moveTo(base.x, base.y)
            cubicTo(base.x - a, base.y - h * 0.35f, base.x - a * 0.5f, base.y - h * 0.75f, base.x, base.y - h)
            cubicTo(base.x + a * 0.5f, base.y - h * 0.75f, base.x + a, base.y - h * 0.35f, base.x, base.y)
            close()
        }
        drawPath(p, color)
    }
    gota(ancho = 11f, alto = 38f, color = LlamaExterior)
    gota(ancho = 6f, alto = 23f, color = LlamaInterior)
}
