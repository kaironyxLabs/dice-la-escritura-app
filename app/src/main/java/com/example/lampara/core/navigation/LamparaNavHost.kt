package com.example.lampara.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lampara.R
import com.example.lampara.core.ui.PantallaProvisional
import com.example.lampara.feature.home.InicioScreen
import com.example.lampara.feature.quiz.CategoriasScreen
import com.example.lampara.feature.quiz.PartidaScreen

@Composable
fun LamparaNavHost() {
    val navController = rememberNavController()
    val entrada by navController.currentBackStackEntryAsState()
    val actual = entrada?.destination
    // La barra inferior solo acompaña a las pantallas principales, no a una partida en curso.
    val mostrarBarra = actual?.hasRoute(Destino.Partida::class) != true

    Scaffold(
        bottomBar = {
            if (mostrarBarra) {
                NavigationBar {
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
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destino.Inicio,
            modifier = Modifier.padding(padding),
        ) {
            composable<Destino.Inicio> {
                InicioScreen(
                    onJugarPorCategoria = { navController.navigate(Destino.Categorias) },
                    onExamen = { navController.navigate(Destino.Partida(examen = true)) },
                )
            }
            composable<Destino.Categorias> {
                CategoriasScreen(onElegir = { navController.navigate(Destino.Partida(categoria = it.id)) })
            }
            composable<Destino.Partida> {
                PartidaScreen(onSalir = { navController.popBackStack(Destino.Inicio, inclusive = false) })
            }
            composable<Destino.Memorizar> { PantallaProvisional(R.string.titulo_memorizar) }
            composable<Destino.Progreso> { PantallaProvisional(R.string.titulo_progreso) }
            composable<Destino.Ajustes> { PantallaProvisional(R.string.titulo_ajustes) }
        }
    }
}
