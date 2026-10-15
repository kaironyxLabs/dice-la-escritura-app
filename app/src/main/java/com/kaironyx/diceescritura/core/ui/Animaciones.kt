package com.kaironyx.diceescritura.core.ui

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Falso si el usuario quitó las animaciones del sistema (escala de duración en 0).
 * Todo movimiento decorativo de la app debe consultarlo.
 */
@Composable
fun rememberAnimacionesActivas(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        runCatching {
            Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
        }.getOrDefault(true)
    }
}
