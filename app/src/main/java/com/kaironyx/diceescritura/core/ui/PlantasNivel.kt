package com.kaironyx.diceescritura.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Ilustración vectorial de la planta de un nivel (1 a 10): semilla, brote, retoño, arbusto, vid,
 * olivo, higuera, palmera, encina y cedro. Se dibuja sobre una cuadrícula de 100 x 100 y escala sin
 * perder calidad. Solo plantas y frutos: no hay figuras humanas ni escenas.
 *
 * Es decorativa salvo que se pase [descripcion], que la expone al lector de pantalla.
 * [bloqueada] la dibuja atenuada (nivel aún no alcanzado).
 */
@Composable
fun PlantaNivel(
    numero: Int,
    modifier: Modifier = Modifier,
    tamano: Dp = 96.dp,
    descripcion: String? = null,
    bloqueada: Boolean = false,
) {
    val esquema = MaterialTheme.colorScheme
    val alfa = if (bloqueada) 0.35f else 1f
    val hoja = esquema.primary.copy(alpha = alfa)
    val hojaClara = esquema.primary.copy(alpha = alfa * 0.65f)
    val tronco = esquema.secondary.copy(alpha = alfa)
    val fruto = esquema.tertiary.copy(alpha = alfa)
    val suelo = esquema.outlineVariant
    val semantica = if (descripcion != null) Modifier.semantics { contentDescription = descripcion } else Modifier
    Canvas(modifier = modifier.size(tamano).then(semantica)) {
        val k = size.minDimension / 100f
        drawLine(suelo, Offset(18f * k, 90f * k), Offset(82f * k, 90f * k), strokeWidth = 2f * k, cap = StrokeCap.Round)
        val p = Pincel(this, k)
        when (numero.coerceIn(1, 10)) {
            1 -> p.semilla(tronco)
            2 -> p.brote(tronco, hoja)
            3 -> p.retono(tronco, hoja)
            4 -> p.arbusto(tronco, hoja, hojaClara)
            5 -> p.vid(tronco, hoja, fruto)
            6 -> p.olivo(tronco, hoja, hojaClara, fruto)
            7 -> p.higuera(tronco, hoja, hojaClara, fruto)
            8 -> p.palmera(tronco, hoja, fruto)
            9 -> p.encina(tronco, hoja, hojaClara)
            else -> p.cedro(tronco, hoja, hojaClara)
        }
    }
}

/** Dibuja en la cuadrícula de 100 x 100 (k = píxeles por unidad). */
private class Pincel(private val d: DrawScope, private val k: Float) {
    private fun o(x: Float, y: Float) = Offset(x * k, y * k)

    private fun tallo(color: Color, x1: Float, y1: Float, x2: Float, y2: Float, grosor: Float) =
        d.drawLine(color, o(x1, y1), o(x2, y2), strokeWidth = grosor * k, cap = StrokeCap.Round)

    private fun disco(color: Color, x: Float, y: Float, r: Float) = d.drawCircle(color, r * k, o(x, y))

    private fun hoja(color: Color, x: Float, y: Float, rx: Float, ry: Float, grados: Float) {
        d.rotate(grados, pivot = o(x, y)) {
            drawOval(color, topLeft = o(x - rx, y - ry), size = Size(2 * rx * k, 2 * ry * k))
        }
    }

    fun semilla(tronco: Color) {
        d.rotate(-20f, pivot = o(50f, 80f)) {
            drawOval(tronco, topLeft = o(38f, 72f), size = Size(24f * k, 16f * k))
        }
        d.drawLine(Color.Black.copy(alpha = 0.25f), o(44f, 82f), o(56f, 78f), strokeWidth = 1.5f * k, cap = StrokeCap.Round)
    }

    fun brote(tronco: Color, hoja: Color) {
        tallo(tronco, 50f, 90f, 50f, 58f, 3.5f)
        hoja(hoja, 40f, 58f, 11f, 5.5f, -30f)
        hoja(hoja, 60f, 58f, 11f, 5.5f, 30f)
    }

    fun retono(tronco: Color, hoja: Color) {
        tallo(tronco, 50f, 90f, 50f, 34f, 4f)
        hoja(hoja, 39f, 70f, 11f, 5f, -25f)
        hoja(hoja, 61f, 62f, 11f, 5f, 25f)
        hoja(hoja, 39f, 52f, 11f, 5f, -25f)
        hoja(hoja, 61f, 44f, 11f, 5f, 25f)
        hoja(hoja, 50f, 32f, 5f, 9f, 0f)
    }

    fun arbusto(tronco: Color, hoja: Color, clara: Color) {
        tallo(tronco, 50f, 90f, 30f, 56f, 3f)
        tallo(tronco, 50f, 90f, 50f, 44f, 3f)
        tallo(tronco, 50f, 90f, 70f, 56f, 3f)
        disco(clara, 28f, 52f, 14f)
        disco(clara, 72f, 52f, 14f)
        disco(hoja, 50f, 42f, 17f)
        disco(hoja, 38f, 62f, 13f)
        disco(hoja, 62f, 62f, 13f)
    }

    fun vid(tronco: Color, hoja: Color, fruto: Color) {
        val sarmiento = Path().apply {
            moveTo(30f * k, 90f * k)
            cubicTo(30f * k, 55f * k, 70f * k, 70f * k, 62f * k, 24f * k)
        }
        d.drawPath(sarmiento, tronco, style = Stroke(width = 3.5f * k, cap = StrokeCap.Round))
        hoja(hoja, 40f, 66f, 11f, 7f, -20f)
        hoja(hoja, 70f, 58f, 11f, 7f, 20f)
        hoja(hoja, 50f, 40f, 11f, 7f, -30f)
        hoja(hoja, 72f, 30f, 10f, 6f, 25f)
        listOf(56f to 68f, 63f to 68f, 59f to 75f, 66f to 75f, 62f to 82f).forEach { (x, y) -> disco(fruto, x, y, 4.2f) }
    }

    fun olivo(tronco: Color, hoja: Color, clara: Color, fruto: Color) {
        tallo(tronco, 50f, 90f, 47f, 52f, 9f)
        tallo(tronco, 47f, 62f, 32f, 46f, 4f)
        tallo(tronco, 48f, 58f, 66f, 42f, 4f)
        hoja(clara, 30f, 40f, 20f, 9f, -15f)
        hoja(clara, 68f, 36f, 20f, 9f, 15f)
        hoja(hoja, 50f, 28f, 26f, 11f, 0f)
        hoja(hoja, 48f, 44f, 22f, 8f, 5f)
        listOf(34f to 46f, 58f to 36f, 72f to 42f, 44f to 30f).forEach { (x, y) -> disco(fruto, x, y, 3.2f) }
    }

    fun higuera(tronco: Color, hoja: Color, clara: Color, fruto: Color) {
        tallo(tronco, 50f, 90f, 50f, 56f, 10f)
        d.drawOval(clara, topLeft = o(12f, 22f), size = Size(76f * k, 46f * k))
        d.drawOval(hoja, topLeft = o(22f, 18f), size = Size(56f * k, 38f * k))
        listOf(30f to 52f, 46f to 58f, 64f to 54f, 40f to 36f, 62f to 34f).forEach { (x, y) -> disco(fruto, x, y, 4.5f) }
    }

    fun palmera(tronco: Color, hoja: Color, fruto: Color) {
        val estipite = Path().apply {
            moveTo(48f * k, 90f * k)
            quadraticTo(56f * k, 64f * k, 52f * k, 38f * k)
        }
        d.drawPath(estipite, tronco, style = Stroke(width = 6f * k, cap = StrokeCap.Round))
        listOf(-160f, -125f, -90f, -55f, -20f).forEach { g ->
            hoja(hoja, 52f + 14f * kotlin.math.cos(Math.toRadians(g.toDouble())).toFloat(), 38f + 14f * kotlin.math.sin(Math.toRadians(g.toDouble())).toFloat(), 15f, 4.5f, g)
        }
        disco(fruto, 49f, 42f, 3.5f)
        disco(fruto, 55f, 43f, 3.5f)
    }

    fun encina(tronco: Color, hoja: Color, clara: Color) {
        tallo(tronco, 50f, 90f, 50f, 54f, 13f)
        tallo(tronco, 50f, 62f, 30f, 50f, 5f)
        tallo(tronco, 50f, 62f, 70f, 50f, 5f)
        disco(clara, 26f, 48f, 17f)
        disco(clara, 74f, 48f, 17f)
        disco(hoja, 50f, 34f, 27f)
        disco(hoja, 36f, 46f, 15f)
        disco(hoja, 64f, 46f, 15f)
    }

    fun cedro(tronco: Color, hoja: Color, clara: Color) {
        tallo(tronco, 50f, 90f, 50f, 76f, 7f)
        triangulo(clara, 50f, 38f, 74f, 76f)
        triangulo(hoja, 50f, 22f, 60f, 56f)
        triangulo(clara, 50f, 6f, 46f, 36f)
    }

    /** Triángulo con la cima en (x, yCima) y base de ancho [ancho] centrada en x, a la altura yBase. */
    private fun triangulo(color: Color, x: Float, yCima: Float, ancho: Float, yBase: Float) {
        val t = Path().apply {
            moveTo(x * k, yCima * k)
            lineTo((x - ancho / 2) * k, yBase * k)
            lineTo((x + ancho / 2) * k, yBase * k)
            close()
        }
        d.drawPath(t, color)
    }
}
