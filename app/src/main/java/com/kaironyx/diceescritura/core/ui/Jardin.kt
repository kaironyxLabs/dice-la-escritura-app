package com.kaironyx.diceescritura.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val Dorado = Color(0xFFC9A227)
private val Crema = Color(0xFFFFF3C4)

/** Cuántas plantas se ven a la vez en el jardín (la del nivel actual y las anteriores). */
private const val PLANTAS_VISIBLES = 3

/**
 * Jardín de los niveles alcanzados: las últimas [PLANTAS_VISIBLES] plantas sobre un suelo de tierra.
 * Si [celebrar], la planta de [nivel] crece desde el suelo y estalla una lluvia de destellos dorados;
 * sin animaciones del sistema aparece ya formada y sin destellos.
 */
@Composable
fun JardinNivel(nivel: Int, celebrar: Boolean, modifier: Modifier = Modifier) {
    val animar = rememberAnimacionesActivas() && celebrar
    val crecimiento = remember { Animatable(if (animar) 0.25f else 1f) }
    LaunchedEffect(Unit) { if (animar) crecimiento.animateTo(1f, tween(1100, easing = FastOutSlowInEasing)) }
    val primero = (nivel - PLANTAS_VISIBLES + 1).coerceAtLeast(1)
    BoxWithConstraints(modifier.fillMaxWidth().height(190.dp), contentAlignment = Alignment.BottomCenter) {
        // Las tres plantas ocupan ~92 % del ancho: la actual el 42 % (máximo 150 dp) y las otras el 60 % de esa.
        val grande = minOf(150.dp, maxWidth * 0.42f)
        val chica = grande * 0.6f
        if (animar) Destellos(Modifier.fillMaxWidth().height(190.dp))
        Box(
            Modifier.fillMaxWidth().height(26.dp).background(
                Brush.verticalGradient(listOf(Color(0xFF6B8E3F).copy(alpha = 0.55f), Color(0xFF7A5A2E).copy(alpha = 0.55f))),
                shape = RoundedCornerShape(50),
            ),
        )
        Row(
            Modifier.fillMaxWidth().padding(bottom = 13.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            for (n in primero..nivel) {
                val actual = n == nivel
                val tamano = if (actual) grande else chica
                // La planta dibuja su línea de suelo al 90 % de su alto: la celda mide justo eso y el resto
                // desborda hacia abajo, así todas las líneas coinciden con el borde inferior de la fila.
                Box(Modifier.height(tamano * 0.9f).wrapContentHeight(align = Alignment.Top, unbounded = true)) {
                    PlantaNivel(
                        numero = n,
                        tamano = tamano,
                        modifier = if (actual) {
                            Modifier.graphicsLayer {
                                scaleX = crecimiento.value
                                scaleY = crecimiento.value
                                transformOrigin = TransformOrigin(0.5f, 0.9f)
                            }
                        } else {
                            Modifier
                        },
                    )
                }
            }
        }
    }
}

/** Destellos que salen del centro y se desvanecen (una sola vez). */
@Composable
private fun Destellos(modifier: Modifier = Modifier) {
    val avance = remember { Animatable(0f) }
    LaunchedEffect(Unit) { avance.animateTo(1f, tween(1600, easing = LinearEasing)) }
    Canvas(modifier) {
        val centro = Offset(size.width / 2f, size.height * 0.55f)
        val alcance = size.width * 0.5f
        val p = avance.value
        val n = 18
        for (i in 0 until n) {
            val angulo = (2 * PI * i / n) + (i % 3) * 0.12
            val distancia = alcance * (0.25f + 0.75f * (p * (0.6f + 0.4f * ((i * 7) % 5) / 4f)))
            val x = centro.x + (cos(angulo) * distancia).toFloat()
            val y = centro.y + (sin(angulo) * distancia * 0.8f).toFloat()
            val alfa = (1f - p).coerceIn(0f, 1f)
            val radio = (if (i % 2 == 0) 5f else 3f) * density
            drawCircle(if (i % 2 == 0) Dorado else Crema, radio, Offset(x, y), alpha = alfa)
        }
    }
}
