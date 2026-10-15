package com.kaironyx.diceescritura.domain

import com.kaironyx.diceescritura.domain.usecase.CalculadoraRacha
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Calendario de referencia: el 13 de octubre de 2026 es martes; la semana del 12 al 18 y la del 5 al 11. */
class RachaTest {

    private fun d(dia: Int) = LocalDate.of(2026, 10, dia)

    @Test
    fun `sin historial no hay racha`() {
        val r = CalculadoraRacha.calcular(emptyList(), d(13))
        assertEquals(0, r.actual)
        assertEquals(0, r.mejor)
        assertFalse(r.completadoHoy)
        assertTrue(r.graciaDisponibleEstaSemana)
    }

    @Test
    fun `dias consecutivos incluyendo hoy`() {
        val r = CalculadoraRacha.calcular(listOf(d(11), d(12), d(13)), d(13))
        assertEquals(3, r.actual)
        assertEquals(3, r.mejor)
        assertTrue(r.completadoHoy)
    }

    @Test
    fun `hoy sin completar todavia no rompe la racha`() {
        val r = CalculadoraRacha.calcular(listOf(d(10), d(11), d(12)), d(13))
        assertEquals(3, r.actual)
        assertFalse(r.completadoHoy)
    }

    @Test
    fun `un dia de gracia por semana mantiene la racha`() {
        // Lunes 5 y miércoles 7: se salta el martes 6.
        val r = CalculadoraRacha.calcular(listOf(d(5), d(7)), d(7))
        assertEquals(2, r.actual)
    }

    @Test
    fun `dos saltos en la misma semana rompen la racha`() {
        // Lunes 5, miércoles 7 (salta el 6) y viernes 9 (salta el 8): el segundo salto de la semana la rompe.
        val r = CalculadoraRacha.calcular(listOf(d(5), d(7), d(9)), d(10))
        assertEquals(1, r.actual)
        assertEquals(2, r.mejor)
    }

    @Test
    fun `la gracia se renueva cada semana`() {
        // Salta el sábado 10 (semana del 5) y el lunes 12 (semana del 12): un salto por semana, la racha sigue.
        val r = CalculadoraRacha.calcular(listOf(d(9), d(11), d(13)), d(13))
        assertEquals(3, r.actual)
        assertEquals(3, r.mejor)
    }

    @Test
    fun `dos dias seguidos sin reto rompen la racha`() {
        val r = CalculadoraRacha.calcular(listOf(d(5), d(6), d(9)), d(9))
        assertEquals(1, r.actual)
        assertEquals(2, r.mejor)
    }

    @Test
    fun `una racha vieja se pierde pero la mejor se conserva`() {
        val r = CalculadoraRacha.calcular(listOf(d(5), d(6), d(7)), d(13))
        assertEquals(0, r.actual)
        assertEquals(3, r.mejor)
    }

    @Test
    fun `ayer saltado con gracia disponible mantiene la racha viva y la gasta`() {
        // Último reto el domingo 11; hoy martes 13 (el lunes 12 quedó sin reto, en la semana de hoy).
        val r = CalculadoraRacha.calcular(listOf(d(9), d(10), d(11)), d(13))
        assertEquals(3, r.actual)
        assertFalse(r.graciaDisponibleEstaSemana)
    }

    @Test
    fun `sin gracia usada la semana de hoy la tiene disponible`() {
        val r = CalculadoraRacha.calcular(listOf(d(12), d(13)), d(13))
        assertTrue(r.graciaDisponibleEstaSemana)
    }

    @Test
    fun `las fechas futuras y repetidas no cuentan`() {
        val r = CalculadoraRacha.calcular(listOf(d(13), d(13), d(20)), d(13))
        assertEquals(1, r.actual)
    }
}
