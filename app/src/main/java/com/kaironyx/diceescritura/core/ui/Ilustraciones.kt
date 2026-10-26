package com.kaironyx.diceescritura.core.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaironyx.diceescritura.R
import com.kaironyx.diceescritura.domain.model.Categoria
import com.kaironyx.diceescritura.domain.usecase.EscenaHistoria
import com.kaironyx.diceescritura.domain.usecase.Logro

@DrawableRes
fun Categoria.banner(): Int = when (this) {
    Categoria.LIBROS -> R.drawable.banner_libros
    Categoria.HISTORIA -> R.drawable.banner_historia
    Categoria.PERSONAJES -> R.drawable.banner_personajes
    Categoria.LUGARES -> R.drawable.banner_lugares
    Categoria.ENSENANZAS -> R.drawable.banner_ensenanzas
    Categoria.CARTAS -> R.drawable.banner_cartas
    Categoria.DONDE_ESTA_ESCRITO -> R.drawable.banner_donde_escrito
    Categoria.CONFESION -> R.drawable.banner_confesion
}

@DrawableRes
fun EscenaHistoria.ilustracion(): Int = when (this) {
    EscenaHistoria.CREACION -> R.drawable.escena_creacion
    EscenaHistoria.EDEN -> R.drawable.escena_eden
    EscenaHistoria.BABEL -> R.drawable.escena_babel
    EscenaHistoria.ABRAHAM_ESTRELLAS -> R.drawable.escena_abraham_estrellas
    EscenaHistoria.JACOB_ESCALERA -> R.drawable.escena_jacob_escalera
    EscenaHistoria.JOSE_EGIPTO -> R.drawable.escena_jose_egipto
    EscenaHistoria.ZARZA -> R.drawable.escena_zarza
    EscenaHistoria.PASCUA -> R.drawable.escena_pascua
}

@StringRes
fun EscenaHistoria.titulo(): Int = when (this) {
    EscenaHistoria.CREACION -> R.string.escena_creacion
    EscenaHistoria.EDEN -> R.string.escena_eden
    EscenaHistoria.BABEL -> R.string.escena_babel
    EscenaHistoria.ABRAHAM_ESTRELLAS -> R.string.escena_abraham_estrellas
    EscenaHistoria.JACOB_ESCALERA -> R.string.escena_jacob_escalera
    EscenaHistoria.JOSE_EGIPTO -> R.string.escena_jose_egipto
    EscenaHistoria.ZARZA -> R.string.escena_zarza
    EscenaHistoria.PASCUA -> R.string.escena_pascua
}

@DrawableRes
fun Logro.sello(): Int = when (this) {
    Logro.PRIMER_RETO -> R.drawable.sello_01
    Logro.RACHA_7 -> R.drawable.sello_02
    Logro.RACHA_30 -> R.drawable.sello_07
    Logro.NIVEL_2 -> R.drawable.sello_05
    Logro.NIVEL_5 -> R.drawable.sello_06
    Logro.NIVEL_10 -> R.drawable.sello_08
    Logro.CIEN_ACIERTOS -> R.drawable.sello_04
    Logro.PRIMER_PASAJE -> R.drawable.sello_03
    Logro.DIEZ_PASAJES -> R.drawable.sello_09
}

/** Banner panorámico de una categoría; el borde inferior se funde con el pergamino. Es decorativo. */
@Composable
fun BannerCategoria(categoria: Categoria, modifier: Modifier = Modifier) {
    // En horizontal el ancho sobra: el alto se limita y el banner queda centrado.
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
    Image(
        painter = painterResource(categoria.banner()),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .aspectRatio(3.2f)
            .heightIn(max = 140.dp)
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawRect(
                    Brush.verticalGradient(0.55f to Color.Black, 1f to Color.Transparent),
                    blendMode = BlendMode.DstIn,
                )
            },
    )
    }
}

/** Escena ilustrada de una historia con su pie, marco dorado y esquinas suaves. */
@Composable
fun EscenaIlustrada(escena: EscenaHistoria, modifier: Modifier = Modifier) {
    val titulo = stringResource(escena.titulo())
    TarjetaPergamino(modifier = modifier.fillMaxWidth(), semilla = 3) {
        Image(
            painter = painterResource(escena.ilustracion()),
            contentDescription = titulo,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .aspectRatio(1f)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(12.dp)),
        )
        Text(
            titulo,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        )
    }
}
