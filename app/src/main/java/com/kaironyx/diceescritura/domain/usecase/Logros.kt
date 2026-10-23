package com.kaironyx.diceescritura.domain.usecase

/**
 * Logros sobrios: hitos que se calculan con lo que ya se guarda (no hay nada nuevo en la base de datos).
 * Sin presión ni comparación: informan lo conseguido y, de lo pendiente, cuánto falta.
 * [meta] es el valor que hay que alcanzar en la medida de cada logro (ver [Logros.evaluar]).
 */
enum class Logro(val meta: Int) {
    PRIMER_RETO(1),
    RACHA_7(7),
    RACHA_30(30),
    NIVEL_2(2),
    NIVEL_5(5),
    NIVEL_10(10),
    CIEN_ACIERTOS(100),
    PRIMER_PASAJE(1),
    DIEZ_PASAJES(10),
}

data class DatosLogros(
    /** Días con el reto diario completado. */
    val retosCompletados: Int,
    val mejorRacha: Int,
    val nivel: Int,
    /** Respuestas acertadas, de todas las preguntas. */
    val aciertosTotales: Int,
    /** Pasajes con al menos un repaso espaciado superado escribiéndolos completos. */
    val pasajesDominados: Int,
)

data class EstadoLogro(val logro: Logro, val actual: Int) {
    val conseguido: Boolean get() = actual >= logro.meta

    /** Avance acotado a la meta, para mostrar "x de y". */
    val avance: Int get() = actual.coerceIn(0, logro.meta)
}

object Logros {
    fun evaluar(datos: DatosLogros): List<EstadoLogro> = Logro.entries.map { logro ->
        EstadoLogro(
            logro,
            when (logro) {
                Logro.PRIMER_RETO -> datos.retosCompletados
                Logro.RACHA_7, Logro.RACHA_30 -> datos.mejorRacha
                Logro.NIVEL_2, Logro.NIVEL_5, Logro.NIVEL_10 -> datos.nivel
                Logro.CIEN_ACIERTOS -> datos.aciertosTotales
                Logro.PRIMER_PASAJE, Logro.DIEZ_PASAJES -> datos.pasajesDominados
            },
        )
    }
}
