package com.kaironyx.diceescritura.domain

import com.kaironyx.diceescritura.data.content.LectorNiveles
import com.kaironyx.diceescritura.domain.model.Categoria
import com.kaironyx.diceescritura.domain.model.Perfil
import com.kaironyx.diceescritura.domain.usecase.GestorNiveles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class GestorNivelesTest {

    private val config = LectorNiveles.leer(File("src/main/assets/content/levels.json").readText(Charsets.UTF_8))
    private val gestor = GestorNiveles(config)

    @Test
    fun `levels json real tiene 10 niveles y umbrales crecientes`() {
        assertEquals(10, config.nivelMaximo)
        assertEquals("Semilla", config.niveles.first().nombre)
        assertEquals("Cedro", config.niveles.last().nombre)
        assertEquals(0, config.niveles.first().umbralXp)
    }

    @Test
    fun `cada categoria se desbloquea exactamente una vez`() {
        val desbloqueadas = config.niveles.flatMap { it.categorias }
        assertEquals(Categoria.entries.toSet(), desbloqueadas.toSet())
        assertEquals(desbloqueadas.size, desbloqueadas.toSet().size)
    }

    @Test
    fun `el XP no pasa del tope mientras el examen esta pendiente`() {
        val tope = gestor.topeXp(1)!!
        val casi = Perfil(nivel = 1, xp = tope - 3)
        assertFalse(gestor.examenDisponible(casi))
        val lleno = gestor.sumarXp(casi, 50)
        assertEquals(tope, lleno.xp)
        assertTrue(gestor.examenDisponible(lleno))
        // Seguir ganando XP con la barra llena no la desborda.
        assertEquals(tope, gestor.sumarXp(lleno, 100).xp)
    }

    @Test
    fun `estado de la barra`() {
        val tope = gestor.topeXp(1)!!
        val mitad = gestor.estado(Perfil(1, tope / 2))
        assertEquals(0.5f, mitad.fraccion, 0.01f)
        assertFalse(mitad.examenDisponible)
        val lleno = gestor.estado(Perfil(1, tope))
        assertEquals(1f, lleno.fraccion, 0f)
        assertTrue(lleno.examenDisponible)
    }

    @Test
    fun `ascender conserva el XP y reinicia la barra del nivel nuevo`() {
        val tope = gestor.topeXp(1)!!
        val ascendido = gestor.ascender(Perfil(1, tope))
        assertEquals(2, ascendido.nivel)
        assertEquals(tope, ascendido.xp)
        assertEquals(0, gestor.estado(ascendido).xpEnNivel)
        assertEquals(0f, gestor.estado(ascendido).fraccion, 0f)
    }

    @Test
    fun `no se asciende sin barra llena`() {
        val perfil = Perfil(1, 10)
        assertEquals(perfil, gestor.ascender(perfil))
    }

    @Test
    fun `el ultimo nivel no tiene examen y acumula XP sin tope`() {
        val cedro = Perfil(config.nivelMaximo, config.niveles.last().umbralXp)
        assertNull(gestor.topeXp(cedro.nivel))
        assertFalse(gestor.examenDisponible(cedro))
        assertEquals(cedro.xp + 500, gestor.sumarXp(cedro, 500).xp)
        assertEquals(cedro, gestor.ascender(cedro))
        assertEquals(1f, gestor.estado(cedro).fraccion, 0f)
    }

    @Test
    fun `el examen aprueba con 80 por ciento o mas`() {
        assertTrue(gestor.evaluarExamen(8, 10).aprobado)
        assertTrue(gestor.evaluarExamen(10, 10).aprobado)
        assertFalse(gestor.evaluarExamen(7, 10).aprobado)
        assertFalse(gestor.evaluarExamen(0, 10).aprobado)
        // Con menos preguntas disponibles: 4 de 5 es exactamente 80 %.
        assertTrue(gestor.evaluarExamen(4, 5).aprobado)
        assertFalse(gestor.evaluarExamen(3, 5).aprobado)
    }

    @Test
    fun `categorias y dificultad se desbloquean con el nivel`() {
        assertEquals(setOf(Categoria.LIBROS, Categoria.HISTORIA, Categoria.PERSONAJES, Categoria.CONFESION),
            gestor.categoriasDesbloqueadas(1))
        assertTrue(Categoria.LUGARES in gestor.categoriasDesbloqueadas(2))
        assertEquals(Categoria.entries.toSet(), gestor.categoriasDesbloqueadas(5))
        assertEquals(1, gestor.nivel(1).dificultadMaxima)
        assertEquals(5, gestor.nivel(10).dificultadMaxima)
        assertEquals(2, gestor.nivelQueDesbloquea(Categoria.LUGARES)?.numero)
    }

    @Test
    fun `xp por acierto segun dificultad y bonificacion por racha`() {
        val base = config.xpPorDificultad
        assertEquals(base[0], gestor.xpPorAcierto(1, aciertosSeguidos = 1))
        assertEquals(base[0], gestor.xpPorAcierto(1, aciertosSeguidos = 2))
        assertEquals(base[0] + 1, gestor.xpPorAcierto(1, aciertosSeguidos = 3))
        assertEquals(base[0] + 2, gestor.xpPorAcierto(1, aciertosSeguidos = 4))
        // La bonificación tiene tope.
        assertEquals(base[4] + config.bonusRachaMaximo, gestor.xpPorAcierto(5, aciertosSeguidos = 50))
        assertTrue(base.zipWithNext().all { (a, b) -> b > a })
    }
}
