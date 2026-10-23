package com.kaironyx.diceescritura.domain

import com.kaironyx.diceescritura.domain.usecase.DatosLogros
import com.kaironyx.diceescritura.domain.usecase.Logro
import com.kaironyx.diceescritura.domain.usecase.Logros
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogrosTest {

    private fun datos(
        retos: Int = 0,
        racha: Int = 0,
        nivel: Int = 1,
        aciertos: Int = 0,
        pasajes: Int = 0,
    ) = DatosLogros(retos, racha, nivel, aciertos, pasajes)

    private fun conseguidos(d: DatosLogros) = Logros.evaluar(d).filter { it.conseguido }.map { it.logro }.toSet()

    @Test
    fun `sin actividad no hay logros`() {
        assertEquals(emptySet<Logro>(), conseguidos(datos()))
    }

    @Test
    fun `cada logro se consigue al llegar a su meta y no antes`() {
        assertTrue(Logro.RACHA_7 in conseguidos(datos(racha = 7)))
        assertFalse(Logro.RACHA_7 in conseguidos(datos(racha = 6)))
        assertTrue(Logro.NIVEL_5 in conseguidos(datos(nivel = 5)))
        assertTrue(Logro.NIVEL_2 in conseguidos(datos(nivel = 5)))
        assertFalse(Logro.NIVEL_10 in conseguidos(datos(nivel = 9)))
        assertTrue(Logro.CIEN_ACIERTOS in conseguidos(datos(aciertos = 100)))
        assertFalse(Logro.DIEZ_PASAJES in conseguidos(datos(pasajes = 9)))
        assertTrue(Logro.PRIMER_PASAJE in conseguidos(datos(pasajes = 9)))
    }

    @Test
    fun `el avance no pasa de la meta`() {
        val estado = Logros.evaluar(datos(racha = 50)).first { it.logro == Logro.RACHA_7 }
        assertEquals(7, estado.avance)
        assertEquals(50, estado.actual)
    }

    @Test
    fun `hay un estado por cada logro, en orden`() {
        assertEquals(Logro.entries, Logros.evaluar(datos()).map { it.logro })
    }
}
