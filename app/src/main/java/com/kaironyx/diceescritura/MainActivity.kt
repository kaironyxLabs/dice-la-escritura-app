package com.kaironyx.diceescritura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kaironyx.diceescritura.core.navigation.DiceEscrituraNavHost
import com.kaironyx.diceescritura.core.splash.SplashBiblico
import com.kaironyx.diceescritura.core.theme.DiceEscrituraTheme
import com.kaironyx.diceescritura.feature.onboarding.RaizViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val raiz: RaizViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // La pantalla de inicio se mantiene hasta saber si hay que mostrar la bienvenida.
        installSplashScreen().setKeepOnScreenCondition { raiz.onboardingVisto.value == null }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DiceEscrituraTheme {
                // La app se compone debajo del splash para que cargue mientras se anima.
                var mostrarSplash by rememberSaveable { mutableStateOf(true) }
                var videoEnMarcha by remember { mutableStateOf(false) }
                val visto by raiz.onboardingVisto.collectAsStateWithLifecycle()
                Box(Modifier.fillMaxSize()) {
                    // La app (pesada de componer) espera a que el video ya esté en marcha; así el splash no se retrasa.
                    if (visto != null && (videoEnMarcha || !mostrarSplash)) DiceEscrituraNavHost()
                    AnimatedVisibility(visible = mostrarSplash, exit = fadeOut(tween(350))) {
                        SplashBiblico(alTerminar = { mostrarSplash = false }, iniciar = visto != null, alArrancar = { videoEnMarcha = true })
                    }
                }
            }
        }
    }
}
