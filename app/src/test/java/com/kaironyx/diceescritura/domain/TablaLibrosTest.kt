package com.kaironyx.diceescritura.domain

import com.kaironyx.diceescritura.domain.model.TablaLibros
import com.kaironyx.diceescritura.domain.model.Testamento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TablaLibrosTest {

    @Test
    fun `hay 66 libros y 1189 capitulos`() {
        assertEquals(66, TablaLibros.todos.size)
        assertEquals(1189, TablaLibros.todos.sumOf { it.capitulos })
    }

    @Test
    fun `39 del Antiguo Testamento y 27 del Nuevo`() {
        assertEquals(39, TablaLibros.todos.count { it.testamento == Testamento.ANTIGUO })
        assertEquals(27, TablaLibros.todos.count { it.testamento == Testamento.NUEVO })
        // El Antiguo va primero y el orden es consecutivo de 1 a 66.
        assertEquals((1..66).toList(), TablaLibros.todos.map { it.orden })
        assertEquals(Testamento.ANTIGUO, TablaLibros.todos[38].testamento)
        assertEquals(Testamento.NUEVO, TablaLibros.todos[39].testamento)
    }

    @Test
    fun `ids de tres caracteres y unicos`() {
        assertEquals(66, TablaLibros.todos.map { it.id }.toSet().size)
        assertEquals(true, TablaLibros.todos.all { it.id.length == 3 })
    }

    @Test
    fun `nombres y abreviaturas normalizados no colisionan`() {
        val claves = TablaLibros.todos.flatMap {
            listOf(TablaLibros.normalizar(it.nombre), TablaLibros.normalizar(it.abreviatura))
        }
        // Job tiene la misma abreviatura que su nombre, por eso se excluye ese par.
        val esperadas = 66 * 2 - 1
        assertEquals(esperadas, claves.toSet().size)
    }

    @Test
    fun `capitulos de libros de referencia`() {
        assertEquals(150, TablaLibros.porId.getValue("PSA").capitulos)
        assertEquals(1, TablaLibros.porId.getValue("PHM").capitulos)
        assertEquals(22, TablaLibros.porId.getValue("REV").capitulos)
        assertEquals(31, TablaLibros.porId.getValue("1SA").capitulos)
    }

    @Test
    fun `busqueda por nombre, abreviatura y alias`() {
        assertEquals("SNG", TablaLibros.buscar("Cantares")?.id)
        assertEquals("SNG", TablaLibros.buscar("Cantar de los Cantares")?.id)
        assertEquals("PSA", TablaLibros.buscar("Salmo")?.id)
        assertEquals("PSA", TablaLibros.buscar("sal.")?.id)
        assertEquals("GEN", TablaLibros.buscar("genesis")?.id)
        assertEquals("1CO", TablaLibros.buscar("1Corintios")?.id)
        assertEquals("1CO", TablaLibros.buscar("1 Co.")?.id)
        assertEquals("REV", TablaLibros.buscar("Apocalipsis")?.id)
        assertNotNull(TablaLibros.buscar("Éxodo"))
        assertNull(TablaLibros.buscar("Tobías"))
    }
}
