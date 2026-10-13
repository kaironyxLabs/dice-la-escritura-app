package com.example.lampara.domain.model

/**
 * Pasaje bíblico: "Romanos 3:23", "Juan 3:16-18", "Génesis 1:1-2:3", "Salmos 23" o "Judas 3".
 * Un versículo nulo significa que la referencia abarca el capítulo completo.
 */
data class Referencia(
    val libro: Libro,
    val capituloInicio: Int,
    val versiculoInicio: Int?,
    val capituloFin: Int,
    val versiculoFin: Int?,
)

sealed interface ResultadoReferencia {
    data class Valida(val referencia: Referencia) : ResultadoReferencia
    data class Invalida(val motivo: String) : ResultadoReferencia
}
