package com.example.lampara.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.lampara.R

// Fuentes variables empaquetadas (licencia OFL, ver docs/licencias/).
@OptIn(ExperimentalTextApi::class)
private fun variable(resId: Int, weight: FontWeight) = Font(
    resId = resId,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

/** Sans para la interfaz. */
val InterfazFontFamily = FontFamily(
    variable(R.font.inter, FontWeight.Normal),
    variable(R.font.inter, FontWeight.Medium),
    variable(R.font.inter, FontWeight.SemiBold),
    variable(R.font.inter, FontWeight.Bold),
)

/** Serif legible para pasajes bíblicos. */
val PasajeFontFamily = FontFamily(
    variable(R.font.source_serif_4, FontWeight.Normal),
    variable(R.font.source_serif_4, FontWeight.SemiBold),
    variable(R.font.source_serif_4, FontWeight.Bold),
)

/** Estilo del texto de un versículo; escala con el tamaño de fuente del sistema (sp). */
val EstiloPasaje = TextStyle(
    fontFamily = PasajeFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.1.sp,
)

private val base = Typography()

internal val LamparaTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = InterfazFontFamily),
    displayMedium = base.displayMedium.copy(fontFamily = InterfazFontFamily),
    displaySmall = base.displaySmall.copy(fontFamily = InterfazFontFamily),
    headlineLarge = base.headlineLarge.copy(fontFamily = InterfazFontFamily),
    headlineMedium = base.headlineMedium.copy(fontFamily = InterfazFontFamily),
    headlineSmall = base.headlineSmall.copy(fontFamily = InterfazFontFamily),
    titleLarge = base.titleLarge.copy(fontFamily = InterfazFontFamily, fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontFamily = InterfazFontFamily, fontWeight = FontWeight.Medium),
    titleSmall = base.titleSmall.copy(fontFamily = InterfazFontFamily, fontWeight = FontWeight.Medium),
    bodyLarge = base.bodyLarge.copy(fontFamily = InterfazFontFamily),
    bodyMedium = base.bodyMedium.copy(fontFamily = InterfazFontFamily),
    bodySmall = base.bodySmall.copy(fontFamily = InterfazFontFamily),
    labelLarge = base.labelLarge.copy(fontFamily = InterfazFontFamily),
    labelMedium = base.labelMedium.copy(fontFamily = InterfazFontFamily),
    labelSmall = base.labelSmall.copy(fontFamily = InterfazFontFamily),
)
