package com.kaironyx.diceescritura.domain

import com.kaironyx.diceescritura.domain.usecase.Leitner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LeitnerTest {

    private val hoy = LocalDate.of(2026, 10, 13)

    @Test
    fun `intervalos de 1, 2, 4, 8 y 16 dias`() {
        assertEquals(listOf(1, 2, 4, 8, 16), Leitner.INTERVALOS_DIAS)
    }

    @Test
    fun `una pregunta nueva acertada pasa a la caja 2 con repaso en 2 dias`() {
        val e = Leitner.responder(cajaActual = null, acerto = true, hoy = hoy)
        assertEquals(2, e.caja)
        assertEquals(hoy.plusDays(2), e.proximaFecha)
    }

    @Test
    fun `una pregunta nueva fallada queda en la caja 1 con repaso al dia siguiente`() {
        val e = Leitner.responder(null, false, hoy)
        assertEquals(1, e.caja)
        assertEquals(hoy.plusDays(1), e.proximaFecha)
    }

    @Test
    fun `cada acierto sube una caja hasta la 5`() {
        var caja: Int? = null
        val dias = mutableListOf<Long>()
        repeat(6) {
            val e = Leitner.responder(caja, true, hoy)
            caja = e.caja
            dias += java.time.temporal.ChronoUnit.DAYS.between(hoy, e.proximaFecha)
        }
        assertEquals(5, caja)
        assertEquals(listOf(2L, 4L, 8L, 16L, 16L, 16L), dias)
    }

    @Test
    fun `un fallo devuelve a la caja 1 desde cualquier caja`() {
        for (caja in 1..5) {
            val e = Leitner.responder(caja, false, hoy)
            assertEquals(1, e.caja)
            assertEquals(hoy.plusDays(1), e.proximaFecha)
        }
    }

    @Test
    fun `vencimiento`() {
        assertTrue(Leitner.estaVencida(hoy, hoy))
        assertTrue(Leitner.estaVencida(hoy.minusDays(3), hoy))
        assertFalse(Leitner.estaVencida(hoy.plusDays(1), hoy))
    }
}
