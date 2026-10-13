package com.example.lampara.domain.model

/** Módulo de contenido. El de la Confesión 1689 solo existe con su bandera activa. */
enum class Modulo(val id: String) {
    CORE("core"),
    CONFESION_1689("confesion1689"),
    ;

    companion object {
        fun desdeId(id: String): Modulo? = entries.firstOrNull { it.id == id }
    }
}

enum class Categoria(val id: String, val modulo: Modulo) {
    LIBROS("libros", Modulo.CORE),
    HISTORIA("historia", Modulo.CORE),
    PERSONAJES("personajes", Modulo.CORE),
    LUGARES("lugares", Modulo.CORE),
    ENSENANZAS("ensenanzas", Modulo.CORE),
    CARTAS("cartas", Modulo.CORE),
    DONDE_ESTA_ESCRITO("donde_escrito", Modulo.CORE),
    CONFESION("confesion", Modulo.CONFESION_1689),
    ;

    companion object {
        fun desdeId(id: String): Categoria? = entries.firstOrNull { it.id == id }
    }
}

/** Contenido propio de cada tipo de pregunta. */
sealed interface Contenido {
    data class OpcionMultiple(val opciones: List<String>, val indiceCorrecto: Int) : Contenido
    data class VerdaderoFalso(val respuesta: Boolean) : Contenido

    /** [elementos] está en el orden correcto; la partida los baraja al presentarlos. */
    data class Ordenar(val elementos: List<String>) : Contenido
}

/**
 * Pregunta del banco de contenido. El [id] es estable: el progreso se enlaza por él.
 * [dificultad] va de 1 a 5. [referencias] son citas como "Romanos 3:23" (nunca el texto del versículo).
 * [confesionRef] solo aplica al módulo 1689 (ej. "cap. 1, párr. 1").
 */
data class Pregunta(
    val id: String,
    val modulo: Modulo,
    val categoria: Categoria,
    val dificultad: Int,
    val enunciado: String,
    val contenido: Contenido,
    val referencias: List<String>,
    val explicacion: String,
    val etiquetas: List<String>,
    val revisada: Boolean,
    val confesionRef: String? = null,
)
