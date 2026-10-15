package com.kaironyx.diceescritura.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Espaciado sobre una cuadrícula de 4/8 dp. */
object Espacio {
    val xs = 4.dp
    val s = 8.dp
    val m = 16.dp
    val l = 24.dp
    val xl = 32.dp
}

/** Tamaño mínimo táctil (accesibilidad) y alto de los botones grandes. */
object Tamano {
    val tactil = 48.dp
    val boton = 52.dp
}

internal val DiceEscrituraShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
