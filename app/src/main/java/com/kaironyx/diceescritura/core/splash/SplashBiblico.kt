package com.kaironyx.diceescritura.core.splash

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kaironyx.diceescritura.R
import com.kaironyx.diceescritura.core.ui.rememberAnimacionesActivas

// Colores de los bordes del video (medidos en sus cuadros): rellenan las franjas que quedan arriba
// y abajo en pantallas más altas que 9:16 para que se fundan con el video.
private val FondoBase = Color(0xFF0F0A05)
private val BordeSuperior = Color(0xFF2C2927)
private val BordeInferior = Color(0xFF252321)

/** Proporción del video (720 x 1280). */
private const val ANCHO_SOBRE_ALTO = 720f / 1280f

/**
 * Pantalla de apertura: reproduce el video del pergamino (`res/raw/splash_video.mp4`, sin audio).
 * Se salta con un toque. Si el sistema tiene las animaciones apagadas, no se muestra.
 * [iniciar] retrasa la reproducción hasta que el splash del sistema se haya ido; si no, correría
 * sin verse. Al terminar, el video se funde a oscuro y se llama a [alTerminar].
 */
@Composable
fun SplashBiblico(alTerminar: () -> Unit, modifier: Modifier = Modifier, iniciar: Boolean = true) {
    val animar = rememberAnimacionesActivas()
    val terminar by rememberUpdatedState(alTerminar)
    val velo = remember { Animatable(0f) }
    val franjas = remember { Animatable(0f) }
    var videoVisible by remember { mutableStateOf(true) }
    var reproduciendo by remember { mutableStateOf(false) }
    var fin by remember { mutableStateOf(false) }

    LaunchedEffect(animar) {
        if (!animar) terminar()
    }
    LaunchedEffect(reproduciendo) {
        if (reproduciendo) franjas.animateTo(1f, tween(200))
    }
    LaunchedEffect(fin) {
        if (!fin) return@LaunchedEffect
        velo.animateTo(1f, tween(250))
        videoVisible = false
        terminar()
    }

    val descripcion = stringResource(R.string.splash_descripcion)
    val saltar = stringResource(R.string.splash_saltar)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FondoBase)
            .clickable(onClickLabel = saltar) { fin = true }
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center,
    ) {
        // Franjas: mitad superior e inferior de los colores de borde; el video centrado las tapa.
        Box(
            Modifier
                .fillMaxSize()
                .alpha(franjas.value)
                .background(
                    Brush.verticalGradient(
                        0f to BordeSuperior, 0.5f to BordeSuperior,
                        0.5f to BordeInferior, 1f to BordeInferior,
                    ),
                ),
        )
        if (iniciar && animar && videoVisible) {
            Box(Modifier.fillMaxWidth().aspectRatio(ANCHO_SOBRE_ALTO)) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
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
                // Difumina los bordes del video para que no se vea la línea donde empiezan las franjas.
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(96.dp)
                        .background(Brush.verticalGradient(listOf(BordeSuperior, Color.Transparent))),
                )
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(96.dp)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, BordeInferior))),
                )
            }
        }
        Box(Modifier.fillMaxSize().alpha(velo.value).background(FondoBase))
    }
}
