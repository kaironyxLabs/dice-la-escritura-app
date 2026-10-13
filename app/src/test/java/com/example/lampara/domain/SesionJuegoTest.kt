package com.example.lampara.domain

import com.example.lampara.data.content.LectorNiveles
import com.example.lampara.domain.model.Categoria
import com.example.lampara.domain.model.Contenido
import com.example.lampara.domain.model.Modulo
import com.example.lampara.domain.model.Pregunta
import com.example.lampara.domain.usecase.GestorNiveles
import com.example.lampara.domain.usecase.Respuesta
import com.example.lampara.domain.usecase.SesionJuego
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

class SesionJuegoTest {

    private val gestor = GestorNiveles(
        LectorNiveles.leer(File("src/main/assets/content/levels.json").readText(Charsets.UTF_8)),
    )

    private fun vf(id: String, dificultad: Int = 1) = Pregunta(
        id = id, modulo = Modulo.CORE, categoria = Categoria.LIBROS, dificultad = dificultad,
        enunciado = "Pregunta de prueba", contenido = Contenido.VerdaderoFalso(true),
        referencias = listOf("Génesis 1:1"), explicacion = "Explicación de prueba",
        etiquetas = emptyList(), revisada = false,
    )

    private fun sesion(n: Int, examen: Boolean = false, dificultad: Int = 1) =
        SesionJuego((1..n).map { vf("core-libros-%04d".format(it), dificultad) }, examen, gestor, Random(1))

    private val acierto = Respuesta.VerdaderoFalso(true)
    private val fallo = Respuesta.VerdaderoFalso(false)

    @Test
    fun `una partida suma XP por acierto con bonificacion por racha`() {
        val s = sesion(5)
        val base = gestor.config.xpPorDificultad[0]
        val xp = mutableListOf<Int>()
        repeat(5) {
            xp += s.responder(acierto).xp
            s.avanzar()
        }
        assertEquals(listOf(base, base, base + 1, base + 2, base + 3), xp)
        assertEquals(5, s.resumen.aciertos)
        assertEquals(xp.sum(), s.resumen.xpGanado)
    }

    @Test
    fun `un fallo no da XP y corta la racha`() {
        val s = sesion(4)
        val base = gestor.config.xpPorDificultad[0]
        assertEquals(base, s.responder(acierto).xp); s.avanzar()
        assertEquals(base, s.responder(acierto).xp); s.avanzar()
        val malo = s.responder(fallo); s.avanzar()
        assertEquals(0, malo.xp)
        assertEquals(0, malo.aciertosSeguidos)
        // La racha vuelve a empezar: el siguiente acierto no tiene bonificación.
        assertEquals(base, s.responder(acierto).xp)
        assertEquals(3, s.resumen.aciertos)
    }

    @Test
    fun `el examen no otorga XP pero cuenta aciertos`() {
        val s = sesion(3, examen = true)
        repeat(3) { assertEquals(0, s.responder(acierto).xp); s.avanzar() }
        assertEquals(3, s.resumen.aciertos)
        assertEquals(0, s.resumen.xpGanado)
    }

    @Test
    fun `no se puede responder dos veces ni avanzar sin responder`() {
        val s = sesion(2)
        assertThrows(IllegalStateException::class.java) { s.avanzar() }
        s.responder(acierto)
        assertThrows(IllegalStateException::class.java) { s.responder(acierto) }
    }

    @Test
    fun `avanza hasta la ultima pregunta y se detiene`() {
        val s = sesion(2)
        assertFalse(s.esUltima)
        s.responder(acierto)
        assertTrue(s.avanzar())
        assertEquals(1, s.indice)
        assertNull(s.resultadoActual)
        assertTrue(s.esUltima)
        s.responder(fallo)
        assertFalse(s.avanzar())
        assertEquals(1, s.indice)
        assertEquals(1, s.resumen.aciertos)
        assertEquals(2, s.resumen.total)
    }

    @Test
    fun `una sesion sin preguntas es invalida`() {
        assertThrows(IllegalArgumentException::class.java) { SesionJuego(emptyList(), false, gestor) }
    }
}
