package com.example.lampara.domain.usecase

import java.time.Duration
import java.time.ZonedDateTime

/** Cálculo de cuándo toca el recordatorio diario. */
object Recordatorio {
    const val MINUTOS_POR_DEFECTO = 20 * 60

    fun minutosDe(hora: Int, minuto: Int): Int {
        require(hora in 0..23 && minuto in 0..59) { "Hora inválida" }
        return hora * 60 + minuto
    }

    /** Próximo disparo estrictamente posterior a [ahora], a la hora local [minutosDelDia] (minutos desde las 00:00). */
    fun proximoDisparo(ahora: ZonedDateTime, minutosDelDia: Int): ZonedDateTime {
        require(minutosDelDia in 0 until 24 * 60) { "Minutos fuera de rango" }
        val hora = minutosDelDia / 60
        val minuto = minutosDelDia % 60
        val hoy = ahora.toLocalDate().atTime(hora, minuto).atZone(ahora.zone)
        return if (hoy.isAfter(ahora)) hoy else ahora.toLocalDate().plusDays(1).atTime(hora, minuto).atZone(ahora.zone)
    }

    fun retraso(ahora: ZonedDateTime, minutosDelDia: Int): Duration =
        Duration.between(ahora, proximoDisparo(ahora, minutosDelDia))
}
