package com.kaironyx.diceescritura.domain

import com.kaironyx.diceescritura.domain.model.Referencia
import com.kaironyx.diceescritura.domain.model.ResultadoReferencia
import com.kaironyx.diceescritura.domain.usecase.ParserReferencias
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ParserReferenciasTest {

    private fun valida(texto: String): Referencia {
        val resultado = ParserReferencias.analizar(texto)
        assertTrue("$texto debería ser válida: $resultado", resultado is ResultadoReferencia.Valida)
        return (resultado as ResultadoReferencia.Valida).referencia
    }

    private fun invalida(texto: String) {
        val resultado = ParserReferencias.analizar(texto)
        assertTrue("$texto debería ser inválida", resultado is ResultadoReferencia.Invalida)
    }

    @Test
    fun `versiculo suelto`() {
        val r = valida("Romanos 3:23")
        assertEquals("ROM", r.libro.id)
        assertEquals(3, r.capituloInicio)
        assertEquals(23, r.versiculoInicio)
        assertEquals(3, r.capituloFin)
        assertEquals(23, r.versiculoFin)
    }

    @Test
    fun `rango de versiculos en el mismo capitulo`() {
        val r = valida("Juan 3:16-18")
        assertEquals(16, r.versiculoInicio)
        assertEquals(18, r.versiculoFin)
        assertEquals(3, r.capituloFin)
    }

    @Test
    fun `rango entre capitulos`() {
        val r = valida("Génesis 1:1-2:3")
        assertEquals(1, r.capituloInicio)
        assertEquals(2, r.capituloFin)
        assertEquals(3, r.versiculoFin)
    }

    @Test
    fun `capitulo completo y rango de capitulos`() {
        val c = valida("Salmos 23")
        assertEquals(23, c.capituloInicio)
        assertEquals(null, c.versiculoInicio)
        val rango = valida("Salmo 23-25")
        assertEquals(25, rango.capituloFin)
        assertEquals(null, rango.versiculoFin)
    }

    @Test
    fun `libros con numero y escritura con abreviatura`() {
        assertEquals("1CO", valida("1 Corintios 13:4-7").libro.id)
        assertEquals("1JN", valida("1 Juan 4:8").libro.id)
        assertEquals("1CO", valida("1 Co. 13:4").libro.id)
        assertEquals("2TI", valida("2 Timoteo 3:16").libro.id)
    }

    @Test
    fun `libros de un solo capitulo interpretan el numero como versiculo`() {
        val r = valida("Judas 3")
        assertEquals(1, r.capituloInicio)
        assertEquals(3, r.versiculoInicio)
        val rango = valida("Filemón 10-12")
        assertEquals(10, rango.versiculoInicio)
        assertEquals(12, rango.versiculoFin)
        val explicito = valida("Filemón 1:10-12")
        assertEquals(10, explicito.versiculoInicio)
    }

    @Test
    fun `rechaza capitulos inexistentes`() {
        invalida("Génesis 51:1")
        invalida("Salmos 151")
        invalida("Rut 5:1")
        invalida("Juan 0:1")
        invalida("Génesis 3:1-60:2")
    }

    @Test
    fun `rechaza libros desconocidos y formatos raros`() {
        invalida("Tobías 1:1")
        invalida("")
        invalida("Juan")
        invalida("3:16")
        invalida("Juan 3:16; Romanos 3:23")
        invalida("Juan 3:18-16")
    }
}
