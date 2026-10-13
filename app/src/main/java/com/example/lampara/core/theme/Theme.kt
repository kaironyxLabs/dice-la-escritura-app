package com.example.lampara.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/** Tema propio, sin colores dinámicos del sistema: la identidad visual es la de la app. */
@Composable
fun LamparaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = LamparaTypography,
        content = content,
    )
}
