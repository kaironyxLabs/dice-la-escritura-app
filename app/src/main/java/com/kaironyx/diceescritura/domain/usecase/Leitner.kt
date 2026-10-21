package com.kaironyx.diceescritura.domain.usecase

import java.time.LocalDate

/** Estado de repetición espaciada de una pregunta. */
data class EstadoLeitner(val caja: Int, val proximaFecha: LocalDate)

/**
 * Leitner de 5 cajas con repasos a 1, 2, 4, 8 y 16 días.
 * Un acierto sube una caja; un fallo devuelve la pregunta a la caja 1.
 */
object Leitner {
    val INTERVALOS_DIAS = listOf(1, 2, 4, 8, 16)
    const val CAJA_MINIMA = 1
    val CAJA_MAXIMA = INTERVALOS_DIAS.size

    /** [cajaActual] es null si la pregunta nunca se había respondido. */
    fun responder(cajaActual: Int?, acerto: Boolean, hoy: LocalDate): EstadoLeitner {
        // Una pregunta nueva cuenta como caja 1, así que su primer acierto la lleva a la caja 2.
        val nueva = if (acerto) minOf((cajaActual ?: CAJA_MINIMA) + 1, CAJA_MAXIMA) else CAJA_MINIMA
        return EstadoLeitner(nueva, hoy.plusDays(INTERVALOS_DIAS[nueva - 1].toLong()))
    }

    fun estaVencida(proximaFecha: LocalDate, hoy: LocalDate): Boolean = !proximaFecha.isAfter(hoy)
}
