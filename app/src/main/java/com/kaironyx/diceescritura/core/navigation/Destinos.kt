package com.kaironyx.diceescritura.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.kaironyx.diceescritura.R
import kotlinx.serialization.Serializable

/** Rutas con tipos seguros (Navigation Compose + kotlinx.serialization). */
sealed interface Destino {
    @Serializable data object Onboarding : Destino
    @Serializable data object Inicio : Destino
    @Serializable data object Memorizar : Destino
    @Serializable data object Progreso : Destino
    @Serializable data object Ajustes : Destino
    @Serializable data object Categorias : Destino
    @Serializable data object Perfil : Destino
    @Serializable data object Niveles : Destino

    /** Catálogo de componentes de diseño; solo se registra en builds de depuración. */
    @Serializable data object Catalogo : Destino

    /** Práctica de memorización de un pasaje (`pasajeId` de memorize.json). */
    @Serializable data class Practica(val pasajeId: String) : Destino

    /** Partida de una categoría (`categoria` = id de [com.kaironyx.diceescritura.domain.model.Categoria]), examen de ascenso o reto diario. */
    @Serializable data class Partida(
        val categoria: String? = null,
        val examen: Boolean = false,
        val reto: Boolean = false,
    ) : Destino
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
