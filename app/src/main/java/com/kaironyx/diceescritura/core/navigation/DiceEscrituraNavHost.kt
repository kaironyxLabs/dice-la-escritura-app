package com.kaironyx.diceescritura.core.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.kaironyx.diceescritura.R
import com.kaironyx.diceescritura.core.ui.BarraTituloPergamino
import com.kaironyx.diceescritura.core.ui.Pergamino
import com.kaironyx.diceescritura.core.ui.fondoBarraInferior
import com.kaironyx.diceescritura.core.ui.fondoPergamino
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
import com.kaironyx.diceescritura.feature.niveles.NivelesScreen
import com.kaironyx.diceescritura.feature.onboarding.OnboardingScreen
import com.kaironyx.diceescritura.feature.onboarding.RaizViewModel
import com.kaironyx.diceescritura.feature.perfil.PerfilScreen
import com.kaironyx.diceescritura.feature.progress.ProgresoScreen
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

    val esPrincipal = actual != null && DestinoPrincipal.entries.any { actual.hasRoute(it.destino::class) }
    // En horizontal el alto sobra poco: sin título y con la navegación en un riel lateral.
    val compacto = LocalConfiguration.current.screenHeightDp < 480
    val navegar: (DestinoPrincipal) -> Unit = { item ->
        navController.navigate(item.destino) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    Scaffold(
        modifier = Modifier.fondoPergamino(),
        containerColor = Color.Transparent,
        topBar = {
            if (esPrincipal && !compacto) BarraTituloPergamino(stringResource(R.string.app_name))
        },
        bottomBar = {
            if (!compacto && !actual.sinBarra()) {
                Box(Modifier.fondoBarraInferior()) {
                    NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp) {
                        DestinoPrincipal.entries.forEach { item ->
                            val etiqueta = stringResource(item.etiqueta)
                            NavigationBarItem(
                                selected = actual?.hasRoute(item.destino::class) == true,
                                onClick = { navegar(item) },
                                icon = { Icon(item.icono, contentDescription = null, modifier = Modifier.size(28.dp)) },
                                label = {
                                    // Con fuente muy grande la etiqueta no se parte: la barra limita su escala en 1,3.
                                    val d = LocalDensity.current
                                    CompositionLocalProvider(LocalDensity provides Density(d.density, d.fontScale.coerceAtMost(1.3f))) {
                                        Text(
                                            etiqueta,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            maxLines = 1,
                                            softWrap = false,
                                        )
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Pergamino.OroClaro.copy(alpha = 0.35f),
                                    selectedIconColor = Pergamino.Oro,
                                    selectedTextColor = Pergamino.Oro,
                                    unselectedIconColor = Pergamino.TintaSuave,
                                    unselectedTextColor = Pergamino.TintaSuave,
                                ),
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        val animar = rememberAnimacionesActivas()
        Row(Modifier.padding(padding).fillMaxSize()) {
            if (compacto && !actual.sinBarra()) {
                NavigationRail(containerColor = Color.Transparent) {
                    Spacer(Modifier.weight(1f))
                    DestinoPrincipal.entries.forEach { item ->
                        NavigationRailItem(
                            selected = actual?.hasRoute(item.destino::class) == true,
                            onClick = { navegar(item) },
                            icon = { Icon(item.icono, contentDescription = null, modifier = Modifier.size(24.dp)) },
                            label = { Text(stringResource(item.etiqueta), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, softWrap = false) },
                            colors = NavigationRailItemDefaults.colors(
                                indicatorColor = Pergamino.OroClaro.copy(alpha = 0.35f),
                                selectedIconColor = Pergamino.Oro,
                                selectedTextColor = Pergamino.Oro,
                                unselectedIconColor = Pergamino.TintaSuave,
                                unselectedTextColor = Pergamino.TintaSuave,
                            ),
                        )
                    }
                    Spacer(Modifier.weight(1f))
                }
            }
            NavHost(
                navController = navController,
                startDestination = inicio,
                modifier = Modifier.weight(1f).fillMaxHeight(),
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
                        onElegirCategoria = { navController.navigate(Destino.Partida(categoria = it.id)) },
                        onExamen = { navController.navigate(Destino.Partida(examen = true)) },
                        onReto = { navController.navigate(Destino.Partida(reto = true)) },
                        onPerfil = { navController.navigate(Destino.Perfil) },
                        onNiveles = { navController.navigate(Destino.Niveles) },
                    )
                }
                composable<Destino.Niveles> {
                    NivelesScreen(alVolver = { navController.popBackStack() })
                }
                composable<Destino.Perfil> {
                    PerfilScreen(alVolver = { navController.popBackStack() })
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
}
