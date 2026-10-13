package com.example.lampara.domain

import com.example.lampara.domain.usecase.Recordatorio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

class RecordatorioTest {

    private val utc = ZoneId.of("UTC")
    private val a20h = Recordatorio.minutosDe(20, 0)

    private fun ahora(hora: Int, minuto: Int = 0) = ZonedDateTime.of(2026, 10, 13, hora, minuto, 0, 0, utc)

    @Test
    fun `si la hora aun no llego dispara hoy`() {
        assertEquals(Duration.ofHours(1), Recordatorio.retraso(ahora(19), a20h))
    }

    @Test
    fun `si la hora ya paso dispara manana`() {
        assertEquals(Duration.ofMinutes(22 * 60 + 30), Recordatorio.retraso(ahora(21, 30), a20h))
    }

    @Test
    fun `a la hora exacta programa el dia siguiente`() {
        assertEquals(Duration.ofHours(24), Recordatorio.retraso(ahora(20), a20h))
    }

    @Test
    fun `respeta el cambio de hora de verano`() {
        val ny = ZoneId.of("America/New_York")
        // El 8 de marzo de 2026 se adelantan los relojes: ese día dura 23 horas.
        val ahora = ZonedDateTime.of(2026, 3, 7, 21, 0, 0, 0, ny)
        val disparo = Recordatorio.proximoDisparo(ahora, a20h)
        assertEquals(8, disparo.dayOfMonth)
        assertEquals(20, disparo.hour)
        assertEquals(Duration.ofHours(22), Duration.between(ahora, disparo))
    }

    @Test
    fun `valida horas y minutos`() {
        assertEquals(0, Recordatorio.minutosDe(0, 0))
        assertEquals(23 * 60 + 59, Recordatorio.minutosDe(23, 59))
        assertThrows(IllegalArgumentException::class.java) { Recordatorio.minutosDe(24, 0) }
        assertThrows(IllegalArgumentException::class.java) { Recordatorio.minutosDe(1, 60) }
        assertThrows(IllegalArgumentException::class.java) { Recordatorio.proximoDisparo(ahora(1), 24 * 60) }
    }
}
