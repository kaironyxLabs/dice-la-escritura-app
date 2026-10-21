package com.kaironyx.diceescritura.domain

import com.kaironyx.diceescritura.domain.model.Categoria
import com.kaironyx.diceescritura.domain.model.Contenido
import com.kaironyx.diceescritura.domain.model.Modulo
import com.kaironyx.diceescritura.domain.model.Pregunta
import com.kaironyx.diceescritura.domain.usecase.ConteoPregunta
import com.kaironyx.diceescritura.domain.usecase.Estadisticas
import com.kaironyx.diceescritura.domain.usecase.SesionResumida
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class EstadisticasTest {

    private val hoy = LocalDate.of(2026, 10, 13)

    private fun pregunta(id: String, categoria: Categoria) = Pregunta(
        id = id, modulo = categoria.modulo, categoria = categoria, dificultad = 1, enunciado = "Pregunta de prueba",
        contenido = Contenido.VerdaderoFalso(true), referencias = listOf("Génesis 1:1"),
        explicacion = "Explicación de prueba", etiquetas = emptyList(), revisada = false,
    )

    @Test
    fun `precision por categoria con respuestas y sin ellas`() {
        val banco = listOf(
            pregunta("a", Categoria.LIBROS), pregunta("b", Categoria.LIBROS), pregunta("c", Categoria.HISTORIA),
        )
        val conteos = mapOf("a" to ConteoPregunta(3, 1), "b" to ConteoPregunta(1, 1))
        val r = Estadisticas.precisionPorCategoria(conteos, banco, listOf(Categoria.LIBROS, Categoria.HISTORIA))
        assertEquals(4, r[0].aciertos)
        assertEquals(2, r[0].fallos)
        assertEquals(67, r[0].porcentaje)
        assertNull(r[1].porcentaje) // sin respuestas todavía
    }

    @Test
    fun `el progreso de un modulo apagado se conserva pero no cuenta ni se muestra`() {
        val base = pregunta("base", Categoria.LIBROS)
        val confesion = pregunta("conf", Categoria.CONFESION)
        val conteos = mapOf("base" to ConteoPregunta(2, 0), "conf" to ConteoPregunta(5, 5))
        // Apagado: el banco filtrado solo trae las preguntas base y la categoría ni se lista.
        val apagado = Estadisticas.precisionPorCategoria(conteos, listOf(base), listOf(Categoria.LIBROS))
        assertEquals(listOf(Categoria.LIBROS), apagado.map { it.categoria })
        assertEquals(2, apagado.sumOf { it.respondidas })
        // Encendido: reaparece con su avance intacto.
        val encendido = Estadisticas.precisionPorCategoria(
            conteos, listOf(base, confesion), listOf(Categoria.LIBROS, Categoria.CONFESION),
        )
        assertEquals(50, encendido.first { it.categoria == Categoria.CONFESION }.porcentaje)
        assertEquals(Modulo.CONFESION_1689, Categoria.CONFESION.modulo)
    }

    @Test
    fun `el historial cubre 30 dias, rellena los vacios y suma por dia`() {
        val sesiones = listOf(
            SesionResumida(hoy, aciertos = 8, total = 10),
            SesionResumida(hoy, aciertos = 4, total = 5),
            SesionResumida(hoy.minusDays(29), aciertos = 1, total = 2),
            SesionResumida(hoy.minusDays(30), aciertos = 9, total = 9), // fuera de la ventana
            SesionResumida(hoy.plusDays(1), aciertos = 9, total = 9), // futuro
        )
        val h = Estadisticas.historial(sesiones, hoy)
        assertEquals(30, h.dias.size)
        assertEquals(hoy.minusDays(29), h.dias.first().fecha)
        assertEquals(hoy, h.dias.last().fecha)
        assertEquals(15, h.dias.last().respondidas)
        assertEquals(12, h.dias.last().aciertos)
        assertEquals(17, h.respondidas)
        assertEquals(2, h.diasConActividad)
        assertEquals(15, h.maximoDiario)
        assertEquals(76, h.porcentajeAciertos) // 13 de 17
    }

    @Test
    fun `historial vacio`() {
        val h = Estadisticas.historial(emptyList(), hoy)
        assertEquals(0, h.respondidas)
        assertEquals(0, h.diasConActividad)
        assertNull(h.porcentajeAciertos)
    }
}
