package com.kaironyx.diceescritura.core.splash

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kaironyx.diceescritura.R
import com.kaironyx.diceescritura.core.ui.rememberAnimacionesActivas

private val FondoBase = Color(0xFF0F0A05)

/** Proporción del video (720 x 1450: el original 9:16 se estiró un 13 % en alto para llenar mejor pantallas altas). */
private const val ANCHO_SOBRE_ALTO = 720f / 1450f

/** Alto de los degradados que funden los bordes del video con el fondo oscuro. */
private val AltoFundido = 48.dp

/**
 * Pantalla de apertura: reproduce el video del pergamino (`res/raw/splash_video.mp4`, sin audio) a
 * todo el ancho de la pantalla, sin recortar los lados (se ven todos los personajes). Si sobra alto,
 * los bordes de arriba y abajo se funden con el fondo oscuro. Se salta con un toque. Si el sistema tiene las animaciones
 * apagadas, no se muestra.
 *
 * [iniciar] retrasa la reproducción hasta que el splash del sistema se haya ido; si no, correría
 * sin verse. El video entra con un fundido desde oscuro; al terminar se funde de nuevo a oscuro y
 * se llama a [alTerminar].
 */
@Composable
fun SplashBiblico(alTerminar: () -> Unit, modifier: Modifier = Modifier, iniciar: Boolean = true) {
    val animar = rememberAnimacionesActivas()
    val terminar by rememberUpdatedState(alTerminar)
    // Cubierta oscura sobre el video: 1 = todo oscuro. Baja al empezar y sube al terminar.
    val cubierta = remember { Animatable(1f) }
    var videoVisible by remember { mutableStateOf(true) }
    var reproduciendo by remember { mutableStateOf(false) }
    var fin by remember { mutableStateOf(false) }

    LaunchedEffect(animar) {
        if (!animar) terminar()
    }
    LaunchedEffect(reproduciendo) {
        if (reproduciendo) cubierta.animateTo(0f, tween(300))
    }
    LaunchedEffect(fin) {
        if (!fin) return@LaunchedEffect
        cubierta.animateTo(1f, tween(250))
        videoVisible = false
        terminar()
    }

    val descripcion = stringResource(R.string.splash_descripcion)
    val saltar = stringResource(R.string.splash_saltar)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(FondoBase)
            .clipToBounds()
            .clickable(onClickLabel = saltar) { fin = true }
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center,
    ) {
        if (iniciar && animar && videoVisible) {
            // Tamaño que cabe entero en la pantalla conservando la proporción del video.
            val anchoSiCabeAlto = maxHeight * ANCHO_SOBRE_ALTO
            val (ancho, alto) = if (anchoSiCabeAlto <= maxWidth) {
                anchoSiCabeAlto to maxHeight
            } else {
                maxWidth to maxWidth / ANCHO_SOBRE_ALTO
            }
            AndroidView(
                modifier = Modifier.requiredSize(ancho, alto),
                factory = { contexto ->
                    VideoView(contexto).apply {
                        setVideoURI(Uri.parse("android.resource://${contexto.packageName}/${R.raw.splash_video}"))
                        setOnPreparedListener { reproductor ->
                            reproductor.isLooping = false
                            reproductor.setVolume(0f, 0f)
                            start()
                            reproduciendo = true
                        }
                        setOnCompletionListener { fin = true }
                        setOnErrorListener { _, _, _ ->
                            fin = true
                            true
                        }
                    }
                },
            )
            // Con franjas arriba y abajo, el borde del video se funde con el fondo oscuro.
            if (maxHeight > alto) {
                val franja = (maxHeight - alto) / 2
                Box(
                    Modifier.align(Alignment.TopCenter).offset(y = franja).fillMaxWidth().height(AltoFundido)
                        .background(Brush.verticalGradient(listOf(FondoBase, Color.Transparent))),
                )
                Box(
                    Modifier.align(Alignment.BottomCenter).offset(y = -franja).fillMaxWidth().height(AltoFundido)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, FondoBase))),
                )
            }
        }
        Box(Modifier.fillMaxSize().alpha(cubierta.value).background(FondoBase))
    }
}
