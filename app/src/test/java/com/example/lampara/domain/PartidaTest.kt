package com.example.lampara.domain

import com.example.lampara.domain.model.Categoria
import com.example.lampara.domain.model.Contenido
import com.example.lampara.domain.model.Modulo
import com.example.lampara.domain.model.Pregunta
import com.example.lampara.domain.usecase.Disposicion
import com.example.lampara.domain.usecase.Evaluador
import com.example.lampara.domain.usecase.FiltroJuego
import com.example.lampara.domain.usecase.Respuesta
import com.example.lampara.domain.usecase.SeguimientoPregunta
import com.example.lampara.domain.usecase.SelectorPreguntas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import kotlin.random.Random

class PartidaTest {

    private val hoy = LocalDate.of(2026, 10, 13)

    private fun pregunta(
        id: String,
        categoria: Categoria = Categoria.LIBROS,
        dificultad: Int = 1,
        modulo: Modulo = categoria.modulo,
        revisada: Boolean = false,
        contenido: Contenido = Contenido.VerdaderoFalso(true),
    ) = Pregunta(
        id = id, modulo = modulo, categoria = categoria, dificultad = dificultad, enunciado = "Pregunta de prueba",
        contenido = contenido, referencias = listOf("Génesis 1:1"), explicacion = "Explicación de prueba",
        etiquetas = emptyList(), revisada = revisada,
    )

    private val filtroBase = FiltroJuego(
        modulosHabilitados = setOf(Modulo.CORE),
        soloRevisadas = false,
        dificultadMaxima = 5,
        categoriasPermitidas = Categoria.entries.toSet(),
    )

    @Test
    fun `evalua opcion multiple por texto`() {
        val p = pregunta("a", contenido = Contenido.OpcionMultiple(listOf("A", "B", "C"), 1))
        assertTrue(Evaluador.esCorrecta(p, Respuesta.Opcion("B")))
        assertFalse(Evaluador.esCorrecta(p, Respuesta.Opcion("A")))
        assertFalse(Evaluador.esCorrecta(p, Respuesta.VerdaderoFalso(true)))
    }

    @Test
    fun `evalua verdadero o falso y orden`() {
        val vf = pregunta("a", contenido = Contenido.VerdaderoFalso(false))
        assertTrue(Evaluador.esCorrecta(vf, Respuesta.VerdaderoFalso(false)))
        assertFalse(Evaluador.esCorrecta(vf, Respuesta.VerdaderoFalso(true)))
        val orden = pregunta("b", contenido = Contenido.Ordenar(listOf("1", "2", "3")))
        assertTrue(Evaluador.esCorrecta(orden, Respuesta.Orden(listOf("1", "2", "3"))))
        assertFalse(Evaluador.esCorrecta(orden, Respuesta.Orden(listOf("1", "3", "2"))))
        assertFalse(Evaluador.esCorrecta(orden, Respuesta.Orden(listOf("1", "2"))))
    }

    @Test
    fun `barajar conserva los elementos y nunca deja el orden correcto`() {
        val orden = pregunta("b", contenido = Contenido.Ordenar(listOf("1", "2", "3")))
        repeat(200) { semilla ->
            val mezcla = Disposicion.barajar(orden, Random(semilla))
            assertEquals(setOf("1", "2", "3"), mezcla.toSet())
            assertNotEquals(listOf("1", "2", "3"), mezcla)
        }
        val mc = pregunta("a", contenido = Contenido.OpcionMultiple(listOf("A", "B", "C", "D"), 2))
        assertEquals(setOf("A", "B", "C", "D"), Disposicion.barajar(mc, Random(1)).toSet())
        assertTrue(Disposicion.barajar(pregunta("c"), Random(1)).isEmpty())
    }

    @Test
    fun `la partida respeta categoria, dificultad, modulo y revisadas`() {
        val banco = listOf(
            pregunta("p1", Categoria.LIBROS, 1),
            pregunta("p2", Categoria.LIBROS, 3),
            pregunta("p3", Categoria.HISTORIA, 1),
            pregunta("p4", Categoria.LIBROS, 1, revisada = true),
            pregunta("c1", Categoria.CONFESION, 1),
        )
        val selector = SelectorPreguntas(Random(7))
        val libros = selector.partida(
            banco, filtroBase.copy(categoria = Categoria.LIBROS, dificultadMaxima = 2), emptyMap(), hoy, 10,
        )
        assertEquals(setOf("p1", "p4"), libros.map { it.id }.toSet())

        val revisadas = selector.partida(banco, filtroBase.copy(soloRevisadas = true), emptyMap(), hoy, 10)
        assertEquals(listOf("p4"), revisadas.map { it.id })

        val todas = selector.partida(banco, filtroBase, emptyMap(), hoy, 10)
        assertTrue("Con el módulo 1689 apagado no sale c1", todas.none { it.id == "c1" })
    }

    @Test
    fun `la categoria bloqueada por nivel no entra`() {
        val banco = listOf(pregunta("l", Categoria.LUGARES), pregunta("b", Categoria.LIBROS))
        val filtro = filtroBase.copy(categoriasPermitidas = setOf(Categoria.LIBROS))
        val ids = SelectorPreguntas(Random(1)).partida(banco, filtro, emptyMap(), hoy, 10).map { it.id }
        assertEquals(listOf("b"), ids)
    }

    @Test
    fun `limita la cantidad y no repite preguntas`() {
        val banco = (1..30).map { pregunta("p$it") }
        val ids = SelectorPreguntas(Random(3)).partida(banco, filtroBase, emptyMap(), hoy, 10).map { it.id }
        assertEquals(10, ids.size)
        assertEquals(10, ids.toSet().size)
    }

    @Test
    fun `prioriza nuevas, luego repaso vencido, luego el resto`() {
        val banco = listOf(pregunta("nueva"), pregunta("vencida"), pregunta("futura"))
        val seguimiento = mapOf(
            "vencida" to SeguimientoPregunta(2, hoy.minusDays(1)),
            "futura" to SeguimientoPregunta(3, hoy.plusDays(4)),
        )
        val selector = SelectorPreguntas(Random(5))
        repeat(20) {
            val dos = selector.partida(banco, filtroBase, seguimiento, hoy, 2).map { it.id }.toSet()
            assertEquals(setOf("nueva", "vencida"), dos)
        }
    }

    @Test
    fun `el examen favorece la dificultad alta del nivel y devuelve menos si no hay suficientes`() {
        val banco = (1..6).map { pregunta("f$it", dificultad = 1) } + (1..6).map { pregunta("d$it", dificultad = 3) }
        val filtro = filtroBase.copy(dificultadMaxima = 3)
        val examen = SelectorPreguntas(Random(2)).examen(banco, filtro, 6)
        assertEquals(6, examen.size)
        assertTrue(examen.all { it.dificultad >= 2 })

        val pocas = SelectorPreguntas(Random(2)).examen(banco.take(3), filtro, 10)
        assertEquals(3, pocas.size)
    }
}
