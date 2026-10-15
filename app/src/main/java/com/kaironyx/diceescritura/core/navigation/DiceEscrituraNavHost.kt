package com.kaironyx.diceescritura.core.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kaironyx.diceescritura.BuildConfig
import com.kaironyx.diceescritura.core.ui.CatalogoComponentes
import com.kaironyx.diceescritura.core.ui.rememberAnimacionesActivas
import com.kaironyx.diceescritura.feature.home.InicioScreen
import com.kaironyx.diceescritura.feature.memorize.MemorizarScreen
import com.kaironyx.diceescritura.feature.memorize.PracticaScreen
import com.kaironyx.diceescritura.feature.onboarding.OnboardingScreen
import com.kaironyx.diceescritura.feature.onboarding.RaizViewModel
import com.kaironyx.diceescritura.feature.perfil.PerfilScreen
import com.kaironyx.diceescritura.feature.progress.ProgresoScreen
import com.kaironyx.diceescritura.feature.quiz.CategoriasScreen
import com.kaironyx.diceescritura.feature.quiz.PartidaScreen
import com.kaironyx.diceescritura.feature.settings.AjustesScreen

/** Pantallas a pantalla completa: sin barra inferior. */
private fun NavDestination?.sinBarra(): Boolean =
    this != null && (
        hasRoute(Destino.Partida::class) ||
            hasRoute(Destino.Practica::class) ||
            hasRoute(Destino.Onboarding::class)
        )

@Composable
fun DiceEscrituraNavHost(raiz: RaizViewModel = hiltViewModel()) {
    val visto by raiz.onboardingVisto.collectAsStateWithLifecycle()
    if (visto == null) {
        // La pantalla de inicio del sistema cubre esta espera; solo evita decidir la ruta de partida a ciegas.
        Box(Modifier.fillMaxSize())
        return
    }
    // La ruta de partida se decide una sola vez: al terminar la bienvenida se navega a mano.
    val inicio: Destino = remember { if (visto == true) Destino.Inicio else Destino.Onboarding }

    val navController = rememberNavController()
    val entrada by navController.currentBackStackEntryAsState()
    val actual = entrada?.destination

    Scaffold(
        bottomBar = {
            if (!actual.sinBarra()) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    DestinoPrincipal.entries.forEach { item ->
                        val etiqueta = stringResource(item.etiqueta)
                        NavigationBarItem(
                            selected = actual?.hasRoute(item.destino::class) == true,
                            onClick = {
                                navController.navigate(item.destino) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icono, contentDescription = null) },
                            label = { Text(etiqueta) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        val animar = rememberAnimacionesActivas()
        NavHost(
            navController = navController,
            startDestination = inicio,
            modifier = Modifier.padding(padding),
            enterTransition = { if (animar) fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 12 } else EnterTransition.None },
            exitTransition = { if (animar) fadeOut(tween(160)) else ExitTransition.None },
            popEnterTransition = { if (animar) fadeIn(tween(220)) + slideInHorizontally(tween(220)) { -it / 12 } else EnterTransition.None },
            popExitTransition = { if (animar) fadeOut(tween(160)) + slideOutHorizontally(tween(220)) { it / 12 } else ExitTransition.None },
        ) {
            composable<Destino.Onboarding> {
                OnboardingScreen(
                    alTerminar = {
                        navController.navigate(Destino.Inicio) {
                            popUpTo(Destino.Onboarding) { inclusive = true }
                        }
                    },
                )
            }
            composable<Destino.Inicio> {
                InicioScreen(
                    onJugarPorCategoria = { navController.navigate(Destino.Categorias) },
                    onExamen = { navController.navigate(Destino.Partida(examen = true)) },
                    onReto = { navController.navigate(Destino.Partida(reto = true)) },
                    onPerfil = { navController.navigate(Destino.Perfil) },
                )
            }
            composable<Destino.Perfil> {
                PerfilScreen(alVolver = { navController.popBackStack() })
            }
            composable<Destino.Categorias> {
                CategoriasScreen(onElegir = { navController.navigate(Destino.Partida(categoria = it.id)) })
            }
            composable<Destino.Partida> {
                PartidaScreen(onSalir = { navController.popBackStack(Destino.Inicio, inclusive = false) })
            }
            composable<Destino.Memorizar> {
                MemorizarScreen(onPracticar = { navController.navigate(Destino.Practica(it)) })
            }
            composable<Destino.Practica> {
                PracticaScreen(onSalir = { navController.popBackStack() })
            }
            composable<Destino.Progreso> { ProgresoScreen() }
            composable<Destino.Ajustes> {
                AjustesScreen(
                    onCatalogo = if (BuildConfig.DEBUG) ({ navController.navigate(Destino.Catalogo) }) else null,
                )
            }
            if (BuildConfig.DEBUG) {
                composable<Destino.Catalogo> { CatalogoComponentes() }
            }
        }
    }
}
