package com.kaironyx.diceescritura.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Tema propio, sin colores dinámicos del sistema: la identidad visual es la de la app. La app es de
 * pergamino (claro) siempre; la paleta oscura se conserva para las vistas previas.
 */
@Composable
fun DiceEscrituraTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = DiceEscrituraTypography,
        shapes = DiceEscrituraShapes,
        content = content,
    )
}
