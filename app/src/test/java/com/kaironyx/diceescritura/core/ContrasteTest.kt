package com.kaironyx.diceescritura.core

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.kaironyx.diceescritura.core.theme.AciertoClaro
import com.kaironyx.diceescritura.core.theme.AciertoOscuro
import com.kaironyx.diceescritura.core.theme.DarkColors
import com.kaironyx.diceescritura.core.theme.LightColors
import org.junit.Assert.assertTrue
import org.junit.Test

/** Contraste mínimo AA (4,5:1) en los pares texto/fondo de la paleta, en claro y oscuro. */
class ContrasteTest {

    private fun ratio(a: Color, b: Color): Double {
        val l1 = a.luminance().toDouble() + 0.05
        val l2 = b.luminance().toDouble() + 0.05
        return maxOf(l1, l2) / minOf(l1, l2)
    }

    private fun pares(c: ColorScheme) = listOf(
        "onPrimary/primary" to (c.onPrimary to c.primary),
        "onPrimaryContainer/primaryContainer" to (c.onPrimaryContainer to c.primaryContainer),
        "onSecondary/secondary" to (c.onSecondary to c.secondary),
        "onSecondaryContainer/secondaryContainer" to (c.onSecondaryContainer to c.secondaryContainer),
        "onTertiary/tertiary" to (c.onTertiary to c.tertiary),
        "onTertiaryContainer/tertiaryContainer" to (c.onTertiaryContainer to c.tertiaryContainer),
        "onBackground/background" to (c.onBackground to c.background),
        "onSurface/surface" to (c.onSurface to c.surface),
        "onSurface/surfaceContainerLow" to (c.onSurface to c.surfaceContainerLow),
        "onSurface/surfaceContainerHighest" to (c.onSurface to c.surfaceContainerHighest),
        "onSurfaceVariant/surface" to (c.onSurfaceVariant to c.surface),
        "onSurfaceVariant/surfaceContainerLow" to (c.onSurfaceVariant to c.surfaceContainerLow),
        "onSurfaceVariant/surfaceVariant" to (c.onSurfaceVariant to c.surfaceVariant),
        "onError/error" to (c.onError to c.error),
        "onErrorContainer/errorContainer" to (c.onErrorContainer to c.errorContainer),
        "primary/surface" to (c.primary to c.surface),
        "error/surface" to (c.error to c.surface),
    )

    private fun verificar(nombre: String, c: ColorScheme) {
        val fallos = pares(c).filter { (_, p) -> ratio(p.first, p.second) < 4.5 }
            .map { (n, p) -> "$nombre $n = ${"%.2f".format(ratio(p.first, p.second))}" }
        assertTrue("Contraste insuficiente: $fallos", fallos.isEmpty())
    }

    @Test fun `la paleta clara cumple AA`() = verificar("claro", LightColors)

    @Test fun `la paleta oscura cumple AA`() = verificar("oscuro", DarkColors)

    @Test fun `el color de acierto se distingue sobre su fondo`() {
        assertTrue(ratio(AciertoClaro, LightColors.primaryContainer) >= 3.0)
        assertTrue(ratio(AciertoOscuro, DarkColors.primaryContainer) >= 3.0)
    }
}
