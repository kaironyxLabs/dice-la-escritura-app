package com.example.lampara.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.lampara.R
import kotlinx.serialization.Serializable

/** Rutas con tipos seguros (Navigation Compose + kotlinx.serialization). */
sealed interface Destino {
    @Serializable data object Inicio : Destino
    @Serializable data object Memorizar : Destino
    @Serializable data object Progreso : Destino
    @Serializable data object Ajustes : Destino
    @Serializable data object Categorias : Destino

    /** Partida de una categoría (`categoria` = id de [com.example.lampara.domain.model.Categoria]) o examen de ascenso. */
    @Serializable data class Partida(val categoria: String? = null, val examen: Boolean = false) : Destino
}

/** Destinos de la barra inferior. */
enum class DestinoPrincipal(
    val destino: Destino,
    @StringRes val etiqueta: Int,
    val icono: ImageVector,
) {
    INICIO(Destino.Inicio, R.string.nav_inicio, Icons.Filled.Home),
    MEMORIZAR(Destino.Memorizar, R.string.nav_memorizar, Icons.Filled.Create),
    PROGRESO(Destino.Progreso, R.string.nav_progreso, Icons.Filled.DateRange),
    AJUSTES(Destino.Ajustes, R.string.nav_ajustes, Icons.Filled.Settings),
}
