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

@Composable
fun LamparaNavHost() {
    val navController = rememberNavController()
    val entrada by navController.currentBackStackEntryAsState()
    val actual = entrada?.destination

    Scaffold(
        bottomBar = {
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
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destino.Inicio,
            modifier = Modifier.padding(padding),
        ) {
            composable<Destino.Inicio> { PantallaProvisional(R.string.titulo_inicio) }
            composable<Destino.Memorizar> { PantallaProvisional(R.string.titulo_memorizar) }
            composable<Destino.Progreso> { PantallaProvisional(R.string.titulo_progreso) }
            composable<Destino.Ajustes> { PantallaProvisional(R.string.titulo_ajustes) }
        }
    }
}
