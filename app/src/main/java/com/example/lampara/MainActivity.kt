package com.example.lampara

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.lampara.core.navigation.LamparaNavHost
import com.example.lampara.core.theme.LamparaTheme
import com.example.lampara.feature.onboarding.RaizViewModel
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
            LamparaTheme {
                LamparaNavHost()
            }
        }
    }
}
