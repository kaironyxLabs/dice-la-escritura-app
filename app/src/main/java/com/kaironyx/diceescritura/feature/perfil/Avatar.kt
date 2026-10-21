package com.kaironyx.diceescritura.feature.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Colores de fondo de los avatares ("c0".."c7"). Son lo bastante oscuros para que la inicial en blanco
 * tenga contraste de sobra. Las ilustraciones llegan con el rediseño visual.
 */
val coloresAvatar = listOf(
    Color(0xFF1F4E5F), Color(0xFF7A3E2B), Color(0xFF3F6B3A), Color(0xFF5B3F7A),
    Color(0xFF8A5A00), Color(0xFF8A2F4C), Color(0xFF30509A), Color(0xFF4A4A4A),
)

fun colorDeAvatar(id: String): Color = coloresAvatar[id.removePrefix("c").toIntOrNull()?.coerceIn(0, coloresAvatar.lastIndex) ?: 0]

/** Círculo de color con la inicial del nombre. Decorativo: el nombre ya se lee en pantalla junto a él. */
@Composable
fun AvatarCircular(nombre: String, avatar: String, modifier: Modifier = Modifier, tamano: Dp = 48.dp) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(colorDeAvatar(avatar))
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = nombre.trim().take(1).uppercase(),
            color = Color.White,
            fontSize = (tamano.value * 0.45f).sp,
        )
    }
}
