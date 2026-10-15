package com.kaironyx.diceescritura.core.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Paleta propia: pergamino, tinta y teal profundo, con ámbar de lámpara como acento.
// Pares texto/fondo calculados para contraste AA (>= 4.5:1).

internal val LightColors = lightColorScheme(
    primary = Color(0xFF1F4E5F),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCDE5EE),
    onPrimaryContainer = Color(0xFF0A2630),
    secondary = Color(0xFF5E5340),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE9E0CB),
    onSecondaryContainer = Color(0xFF2A2314),
    tertiary = Color(0xFF7A4F00),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDEA8),
    onTertiaryContainer = Color(0xFF2B1A00),
    background = Color(0xFFFAF7F0),
    onBackground = Color(0xFF1C1B17),
    surface = Color(0xFFFAF7F0),
    onSurface = Color(0xFF1C1B17),
    surfaceVariant = Color(0xFFE8E3D6),
    onSurfaceVariant = Color(0xFF494640),
    outline = Color(0xFF7A766C),
    outlineVariant = Color(0xFFCBC6B8),
    error = Color(0xFFA5271F),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD5),
    onErrorContainer = Color(0xFF410000),
)

internal val DarkColors = darkColorScheme(
    primary = Color(0xFF9CCBDB),
    onPrimary = Color(0xFF00313F),
    primaryContainer = Color(0xFF184656),
    onPrimaryContainer = Color(0xFFCDE5EE),
    secondary = Color(0xFFD0C5AC),
    onSecondary = Color(0xFF362F1E),
    secondaryContainer = Color(0xFF4D4533),
    onSecondaryContainer = Color(0xFFE9E0CB),
    tertiary = Color(0xFFF2BF65),
    onTertiary = Color(0xFF442B00),
    tertiaryContainer = Color(0xFF5E3D00),
    onTertiaryContainer = Color(0xFFFFDEA8),
    background = Color(0xFF14130F),
    onBackground = Color(0xFFE7E2D8),
    surface = Color(0xFF14130F),
    onSurface = Color(0xFFE7E2D8),
    surfaceVariant = Color(0xFF494640),
    onSurfaceVariant = Color(0xFFCBC6B8),
    outline = Color(0xFF959082),
    outlineVariant = Color(0xFF494640),
    error = Color(0xFFFFB4AA),
    onError = Color(0xFF690003),
    errorContainer = Color(0xFF8E1912),
    onErrorContainer = Color(0xFFFFDAD5),
)
