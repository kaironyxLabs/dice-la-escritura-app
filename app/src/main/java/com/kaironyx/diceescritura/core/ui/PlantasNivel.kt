package com.kaironyx.diceescritura.core.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaironyx.diceescritura.R

/** Ilustración de época de cada nivel (1 a 10), de la semilla al árbol de vida. */
@DrawableRes
private fun ilustracionNivel(numero: Int): Int = when (numero.coerceIn(1, 10)) {
    1 -> R.drawable.nivel_01
    2 -> R.drawable.nivel_02
    3 -> R.drawable.nivel_03
    4 -> R.drawable.nivel_04
    5 -> R.drawable.nivel_05
    6 -> R.drawable.nivel_06
    7 -> R.drawable.nivel_07
    8 -> R.drawable.nivel_08
    9 -> R.drawable.nivel_09
    else -> R.drawable.nivel_10
}

/**
 * Ilustración del nivel [numero] recortada en círculo con aro dorado. Solo plantas: no hay figuras
 * humanas. Es decorativa salvo que se pase [descripcion], que la expone al lector de pantalla.
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
    val semantica = if (descripcion != null) Modifier.semantics { contentDescription = descripcion } else Modifier
    Image(
        painter = painterResource(ilustracionNivel(numero)),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(tamano)
            .alpha(if (bloqueada) 0.4f else 1f)
            .border(2.dp, Brush.verticalGradient(listOf(Pergamino.OroClaro, Pergamino.Oro)), CircleShape)
            .padding(3.dp)
            .clip(CircleShape)
            .then(semantica),
    )
}
