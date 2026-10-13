package com.example.lampara.domain

import com.example.lampara.data.content.LectorNiveles
import com.example.lampara.domain.model.Categoria
import com.example.lampara.domain.model.Contenido
import com.example.lampara.domain.model.Modulo
import com.example.lampara.domain.model.Pregunta
import com.example.lampara.domain.usecase.FiltroJuego
import com.example.lampara.domain.usecase.GestorNiveles
import com.example.lampara.domain.usecase.Respuesta
import com.example.lampara.domain.usecase.SeguimientoPregunta
import com.example.lampara.domain.usecase.SelectorPreguntas
import com.example.lampara.domain.usecase.SesionJuego
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate
import kotlin.random.Random

class RetoDiarioTest {

    private val hoy = LocalDate.of(2026, 10, 13)

    private val gestor = GestorNiveles(
        LectorNiveles.leer(File("src/main/assets/content/levels.json").readText(Charsets.UTF_8)),
    )

    private fun pregunta(id: String, modulo: Modulo = Modulo.CORE, categoria: Categoria = Categoria.LIBROS) = Pregunta(
        id = id, modulo = modulo, categoria = categoria, dificultad = 1, enunciado = "Pregunta de prueba",
        contenido = Contenido.VerdaderoFalso(true), referencias = listOf("Génesis 1:1"),
        explicacion = "Explicación de prueba", etiquetas = emptyList(), revisada = false,
    )

    private val filtro = FiltroJuego(
        modulosHabilitados = setOf(Modulo.CORE),
        soloRevisadas = false,
        dificultadMaxima = 5,
        categoriasPermitidas = Categoria.entries.toSet(),
    )

    private fun vencida(dias: Long, caja: Int = 2) = SeguimientoPregunta(caja, hoy.minusDays(dias))
    private fun vigente(dias: Long) = SeguimientoPregunta(3, hoy.plusDays(dias))

    @Test
    fun `mezcla hasta la mitad de repaso vencido con preguntas nuevas`() {
        val banco = (1..20).map { pregunta("v$it") } + (1..20).map { pregunta("n$it") }
        val seguimiento = (1..20).associate { "v$it" to vencida(it.toLong()) }
        val reto = SelectorPreguntas(Random(4)).reto(banco, filtro, seguimiento, hoy, 10)
        assertEquals(10, reto.size)
        assertEquals(10, reto.map { it.id }.toSet().size)
        assertEquals(5, reto.count { it.id.startsWith("v") })
        assertEquals(5, reto.count { it.id.startsWith("n") })
        // El repaso elegido es el más atrasado (los mayores días de retraso).
        val repasoElegido = reto.filter { it.id.startsWith("v") }.map { it.id.drop(1).toInt() }.toSet()
        assertEquals((16..20).toSet(), repasoElegido)
    }

    @Test
    fun `si faltan nuevas completa con mas repaso y luego con lo aun no vencido`() {
        val banco = (1..4).map { pregunta("v$it") } + (1..3).map { pregunta("f$it") } + listOf(pregunta("n1"))
        val seguimiento = (1..4).associate { "v$it" to vencida(it.toLong()) } +
            (1..3).associate { "f$it" to vigente(it.toLong()) }
        val reto = SelectorPreguntas(Random(1)).reto(banco, filtro, seguimiento, hoy, 8)
        assertEquals(8, reto.size)
        assertEquals(setOf("v1", "v2", "v3", "v4", "n1", "f1", "f2", "f3"), reto.map { it.id }.toSet())
    }

    @Test
    fun `con poco contenido devuelve las preguntas que haya`() {
        val reto = SelectorPreguntas(Random(1)).reto(listOf(pregunta("a"), pregunta("b")), filtro, emptyMap(), hoy, 10)
        assertEquals(2, reto.size)
    }

    @Test
    fun `el reto respeta el filtro de modulos`() {
        val banco = listOf(
            pregunta("core-1"),
            pregunta("conf-1", Modulo.CONFESION_1689, Categoria.CONFESION),
        )
        val reto = SelectorPreguntas(Random(1)).reto(banco, filtro, emptyMap(), hoy, 10)
        assertEquals(listOf("core-1"), reto.map { it.id })
    }

    @Test
    fun `una sesion retomada cuenta lo ya respondido`() {
        val pendientes = (1..4).map { pregunta("p$it") }
        val sesion = SesionJuego(
            pendientes, esExamen = false, gestor = gestor, random = Random(1),
            respondidasPrevias = 6, aciertosPrevios = 5, xpPrevio = 40,
        )
        assertEquals(10, sesion.total)
        assertEquals(6, sesion.indice)
        assertEquals(5, sesion.resumen.aciertos)
        assertEquals(40, sesion.resumen.xpGanado)

        val xp = sesion.responder(Respuesta.VerdaderoFalso(true)).xp
        assertTrue(xp > 0)
        assertEquals(6, sesion.resumen.aciertos)
        assertEquals(40 + xp, sesion.resumen.xpGanado)
        sesion.avanzar()
        assertEquals(7, sesion.indice)
        repeat(3) { sesion.responder(Respuesta.VerdaderoFalso(false)); sesion.avanzar() }
        assertEquals(9, sesion.indice)
        assertEquals(10, sesion.resumen.total)
    }
}
