package com.example.lampara.domain.usecase

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

data class Racha(
    /** Días del reto completados en la cadena vigente (los días de gracia no suman). */
    val actual: Int,
    val mejor: Int,
    val completadoHoy: Boolean,
    /** Queda un día de gracia por usar en la semana (lunes a domingo) de hoy. */
    val graciaDisponibleEstaSemana: Boolean,
)

/**
 * Racha de días consecutivos con el reto diario completado.
 * Un día sin reto no rompe la racha si es el único que se salta en esa semana (lunes a domingo):
 * es el día de gracia. Dos días sin reto seguidos, o un segundo salto en la misma semana, sí la rompen.
 * Hoy aún no cuenta como perdido: la racha sigue viva hasta que termine el día.
 */
object CalculadoraRacha {

    fun calcular(completados: Collection<LocalDate>, hoy: LocalDate): Racha {
        val dias = completados.filter { !it.isAfter(hoy) }.toSortedSet().toList()
        if (dias.isEmpty()) {
            return Racha(0, 0, completadoHoy = false, graciaDisponibleEstaSemana = true)
        }

        val semanasConGracia = mutableSetOf<LocalDate>()
        var longitud = 1
        var mejor = 1
        for ((anterior, dia) in dias.zipWithNext()) {
            val saltados = ChronoUnit.DAYS.between(anterior, dia) - 1
            val semanaDelSalto = lunesDe(anterior.plusDays(1))
            longitud = when {
                saltados == 0L -> longitud + 1
                saltados == 1L && semanaDelSalto !in semanasConGracia -> {
                    semanasConGracia += semanaDelSalto
                    longitud + 1
                }
                else -> 1
            }
            mejor = maxOf(mejor, longitud)
        }

        val ultimo = dias.last()
        val diasSinReto = ChronoUnit.DAYS.between(ultimo, hoy) - 1 // hoy todavía no cuenta
        val viva = when {
            ultimo == hoy || diasSinReto <= 0L -> true
            diasSinReto == 1L -> lunesDe(ultimo.plusDays(1)) !in semanasConGracia
            else -> false
        }
        // Si la cadena vigente solo está viva gracias a un día de gracia pendiente de gastar, ese gasto
        // se cuenta como usado en esta semana.
        val gracia = lunesDe(hoy) !in semanasConGracia &&
            !(viva && diasSinReto == 1L && lunesDe(ultimo.plusDays(1)) == lunesDe(hoy))
        return Racha(
            actual = if (viva) longitud else 0,
            mejor = mejor,
            completadoHoy = ultimo == hoy,
            graciaDisponibleEstaSemana = gracia,
        )
    }

    private fun lunesDe(fecha: LocalDate): LocalDate = fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
}
