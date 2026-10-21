package com.kaironyx.diceescritura.core.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Paleta de tono bíblico: pergamino (fondos), tinta (texto), verde olivo (acción principal),
// cuero (secundario) y oro antiguo (acento). Pares texto/fondo verificados para contraste AA
// (>= 4.5:1) en ContrasteTest.

internal val LightColors = lightColorScheme(
    primary = Color(0xFF3B5B2C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD7E8C6),
    onPrimaryContainer = Color(0xFF0F2104),
    secondary = Color(0xFF6B5638),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF0E1C6),
    onSecondaryContainer = Color(0xFF2A1D06),
    tertiary = Color(0xFF7A5200),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDFA0),
    onTertiaryContainer = Color(0xFF281900),
    background = Color(0xFFFBF6EA),
    onBackground = Color(0xFF1F1B13),
    surface = Color(0xFFFBF6EA),
    onSurface = Color(0xFF1F1B13),
    surfaceVariant = Color(0xFFEBE2CD),
    onSurfaceVariant = Color(0xFF4B4538),
    surfaceContainerLowest = Color(0xFFFFFCF5),
    surfaceContainerLow = Color(0xFFF6F0E2),
    surfaceContainer = Color(0xFFF1EADA),
    surfaceContainerHigh = Color(0xFFEBE3D1),
    surfaceContainerHighest = Color(0xFFE5DCC8),
    outline = Color(0xFF7D7461),
    outlineVariant = Color(0xFFD0C7B0),
    error = Color(0xFF9F2B21),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD5),
    onErrorContainer = Color(0xFF410000),
)

internal val DarkColors = darkColorScheme(
    primary = Color(0xFFB6D2A2),
    onPrimary = Color(0xFF1F3714),
    primaryContainer = Color(0xFF2D4A1E),
    onPrimaryContainer = Color(0xFFD7E8C6),
    secondary = Color(0xFFDAC3A0),
    onSecondary = Color(0xFF3E2E14),
    secondaryContainer = Color(0xFF55422A),
    onSecondaryContainer = Color(0xFFF0E1C6),
    tertiary = Color(0xFFEDC066),
    onTertiary = Color(0xFF412D00),
    tertiaryContainer = Color(0xFF5C4100),
    onTertiaryContainer = Color(0xFFFFDFA0),
    background = Color(0xFF17140D),
    onBackground = Color(0xFFEAE1CF),
    surface = Color(0xFF17140D),
    onSurface = Color(0xFFEAE1CF),
    surfaceVariant = Color(0xFF4B4538),
    onSurfaceVariant = Color(0xFFD0C7B0),
    surfaceContainerLowest = Color(0xFF100E08),
    surfaceContainerLow = Color(0xFF1D1A12),
    surfaceContainer = Color(0xFF221E15),
    surfaceContainerHigh = Color(0xFF2C281E),
    surfaceContainerHighest = Color(0xFF373328),
    outline = Color(0xFF988F7B),
    outlineVariant = Color(0xFF4B4538),
    error = Color(0xFFFFB4AA),
    onError = Color(0xFF690003),
    errorContainer = Color(0xFF8E1912),
    onErrorContainer = Color(0xFFFFDAD5),
)

/** Color de acierto y de error para la retroalimentación (siempre acompañado de icono y texto). */
internal val AciertoClaro = Color(0xFF2E6B2A)
internal val AciertoOscuro = Color(0xFF9ED494)
