package com.example.lampara.domain.model

enum class Testamento { ANTIGUO, NUEVO }

enum class SeccionBiblica {
    PENTATEUCO,
    HISTORICOS,
    POETICOS,
    PROFETAS_MAYORES,
    PROFETAS_MENORES,
    EVANGELIOS,
    HECHOS,
    CARTAS_DE_PABLO,
    CARTAS_GENERALES,
    PROFECIA,
}

/**
 * Libro de la Biblia con nombre según la RVR1960.
 * [id] es estable (código de tres caracteres) y es el que enlaza contenido, texto y progreso.
 * [orden] va de 1 a 66.
 */
data class Libro(
    val id: String,
    val orden: Int,
    val nombre: String,
    val abreviatura: String,
    val testamento: Testamento,
    val seccion: SeccionBiblica,
    val capitulos: Int,
)
