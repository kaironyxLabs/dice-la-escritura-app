package com.example.lampara.domain.model

/**
 * Nivel de conocimiento. [umbralXp] es el XP total con el que se llena la barra del nivel anterior
 * (y, por tanto, donde empieza este). [categorias] son las que se desbloquean al alcanzarlo.
 */
data class Nivel(
    val numero: Int,
    val nombre: String,
    val umbralXp: Int,
    val dificultadMaxima: Int,
    val categorias: Set<Categoria>,
)

/** Parámetros del juego leídos de assets/content/levels.json. */
data class ConfigNiveles(
    val niveles: List<Nivel>,
    val xpPorDificultad: List<Int>,
    val bonusRachaDesde: Int,
    val bonusRachaMaximo: Int,
    val examenPreguntas: Int,
    val examenAprobacionPorcentaje: Int,
) {
    init {
        require(niveles.isNotEmpty()) { "Sin niveles" }
        require(niveles.first().umbralXp == 0) { "El primer nivel debe empezar en 0 XP" }
        require(niveles.map { it.numero } == (1..niveles.size).toList()) { "Niveles no consecutivos" }
        require(niveles.zipWithNext().all { (a, b) -> b.umbralXp > a.umbralXp }) { "Umbrales no crecientes" }
        require(niveles.zipWithNext().all { (a, b) -> b.dificultadMaxima >= a.dificultadMaxima }) {
            "La dificultad máxima no puede bajar al subir de nivel"
        }
        require(niveles.all { it.dificultadMaxima in 1..5 }) { "Dificultad máxima fuera de 1..5" }
        require(xpPorDificultad.size == 5 && xpPorDificultad.all { it > 0 }) { "xpPorDificultad requiere 5 valores positivos" }
        require(bonusRachaDesde >= 1 && bonusRachaMaximo >= 0) { "Bonificación de racha inválida" }
        require(examenPreguntas >= 1) { "El examen necesita al menos una pregunta" }
        require(examenAprobacionPorcentaje in 1..100) { "Porcentaje de aprobación inválido" }
    }

    val nivelMaximo: Int get() = niveles.size
}

/** Perfil de juego persistido: nivel actual y XP total acumulado (con tope mientras el examen esté pendiente). */
data class Perfil(val nivel: Int, val xp: Int) {
    companion object {
        val INICIAL = Perfil(nivel = 1, xp = 0)
    }
}
