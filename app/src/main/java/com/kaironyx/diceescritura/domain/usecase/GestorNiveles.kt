package com.kaironyx.diceescritura.domain.usecase

import com.kaironyx.diceescritura.domain.model.Categoria
import com.kaironyx.diceescritura.domain.model.ConfigNiveles
import com.kaironyx.diceescritura.domain.model.Nivel
import com.kaironyx.diceescritura.domain.model.Perfil

data class EstadoNivel(
    val nivel: Nivel,
    /** XP dentro del nivel actual. */
    val xpEnNivel: Int,
    /** XP que abarca el nivel actual; null en el último nivel. */
    val xpDelNivel: Int?,
    /** Relleno de la barra de 0 a 1 (siempre 1 en el último nivel). */
    val fraccion: Float,
    val examenDisponible: Boolean,
)

data class ResultadoExamen(val aciertos: Int, val total: Int, val aprobado: Boolean)

/** Reglas de XP, niveles y examen de ascenso. Lógica pura: sin Android. */
class GestorNiveles(val config: ConfigNiveles) {

    fun nivel(numero: Int): Nivel = config.niveles[numero.coerceIn(1, config.nivelMaximo) - 1]

    /** XP con el que se llena la barra del nivel [numero]; null si es el último. */
    fun topeXp(numero: Int): Int? = config.niveles.getOrNull(numero)?.umbralXp

    /** Suma XP respetando el tope: con la barra llena, el XP espera al examen de ascenso. */
    fun sumarXp(perfil: Perfil, ganado: Int): Perfil {
        require(ganado >= 0) { "XP negativo" }
        val tope = topeXp(perfil.nivel)
        val nuevo = perfil.xp + ganado
        return perfil.copy(xp = if (tope == null) nuevo else minOf(nuevo, tope))
    }

    fun examenDisponible(perfil: Perfil): Boolean {
        val tope = topeXp(perfil.nivel) ?: return false
        return perfil.xp >= tope
    }

    fun estado(perfil: Perfil): EstadoNivel {
        val actual = nivel(perfil.nivel)
        val tope = topeXp(perfil.nivel)
        val xpEnNivel = (perfil.xp - actual.umbralXp).coerceAtLeast(0)
        return if (tope == null) {
            EstadoNivel(actual, xpEnNivel, null, 1f, examenDisponible = false)
        } else {
            val ancho = tope - actual.umbralXp
            EstadoNivel(
                nivel = actual,
                xpEnNivel = minOf(xpEnNivel, ancho),
                xpDelNivel = ancho,
                fraccion = (xpEnNivel.toFloat() / ancho).coerceIn(0f, 1f),
                examenDisponible = perfil.xp >= tope,
            )
        }
    }

    fun evaluarExamen(aciertos: Int, total: Int): ResultadoExamen {
        require(total > 0 && aciertos in 0..total) { "Resultado de examen inválido" }
        // Comparación entera para evitar errores de coma flotante: 8/10 con 80 % debe aprobar.
        val aprobado = aciertos * 100 >= config.examenAprobacionPorcentaje * total
        return ResultadoExamen(aciertos, total, aprobado)
    }

    /** Sube un nivel si hay examen disponible; el XP se conserva y la barra del nuevo nivel parte de cero. */
    fun ascender(perfil: Perfil): Perfil =
        if (examenDisponible(perfil)) perfil.copy(nivel = perfil.nivel + 1) else perfil

    /** Categorías disponibles para jugar al nivel [numero]: la unión de lo desbloqueado hasta ahí. */
    fun categoriasDesbloqueadas(numero: Int): Set<Categoria> =
        config.niveles.take(numero.coerceIn(1, config.nivelMaximo)).flatMap { it.categorias }.toSet()

    fun nivelQueDesbloquea(categoria: Categoria): Nivel? =
        config.niveles.firstOrNull { categoria in it.categorias }

    fun xpPorAcierto(dificultad: Int, aciertosSeguidos: Int): Int {
        val base = config.xpPorDificultad[dificultad.coerceIn(1, 5) - 1]
        val bonus = if (aciertosSeguidos >= config.bonusRachaDesde) {
            minOf(aciertosSeguidos - config.bonusRachaDesde + 1, config.bonusRachaMaximo)
        } else {
            0
        }
        return base + bonus
    }
}
