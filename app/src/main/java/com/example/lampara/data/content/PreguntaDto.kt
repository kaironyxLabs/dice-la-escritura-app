package com.example.lampara.data.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Archivo de contenido: `core-*.json` o `confesion1689-*.json`. */
@Serializable
data class ArchivoContenidoDto(
    val contentVersion: Int,
    val questions: List<PreguntaDto>,
)

/** Pregunta tal como está en el JSON. Los campos opcionales dependen de [type]. */
@Serializable
data class PreguntaDto(
    val id: String,
    val module: String,
    val category: String,
    val type: String,
    val difficulty: Int,
    val question: String,
    val options: List<String>? = null,
    val answerIndex: Int? = null,
    val answer: Boolean? = null,
    val items: List<String>? = null,
    val references: List<String> = emptyList(),
    val explanation: String,
    val tags: List<String> = emptyList(),
    val reviewed: Boolean = false,
    val confessionRef: String? = null,
) {
    companion object {
        const val TIPO_OPCION_MULTIPLE = "multiple_choice"
        const val TIPO_VERDADERO_FALSO = "true_false"
        const val TIPO_ORDENAR = "ordering"
    }
}
