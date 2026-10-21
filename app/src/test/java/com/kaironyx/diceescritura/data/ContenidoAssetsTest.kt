package com.kaironyx.diceescritura.data

import com.kaironyx.diceescritura.data.content.FuenteContenido
import com.kaironyx.diceescritura.data.content.FuenteContenidoAssets
import com.kaironyx.diceescritura.data.repository.PreguntaRepository
import com.kaironyx.diceescritura.domain.model.Categoria
import com.kaironyx.diceescritura.domain.model.Modulo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Valida los archivos reales de app/src/main/assets/content, los que se empaquetan en el APK. */
class ContenidoAssetsTest {

    private val carpeta = File("src/main/assets/content")

    private val repositorio = PreguntaRepository(
        object : FuenteContenido {
            override fun leerArchivos(): Map<String, String> = carpeta.listFiles().orEmpty()
                .filter { FuenteContenidoAssets.esArchivoDePreguntas(it.name) }
                .sortedBy { it.name }
                .associate { it.name to it.readText(Charsets.UTF_8) }
        },
    )

    @Test
    fun `el contenido real es valido`() {
        assertTrue("No se encontró ${carpeta.absolutePath}", carpeta.isDirectory)
        assertEquals(emptyList<String>(), repositorio.erroresDeCarga)
    }

    @Test
    fun `hay preguntas base de todas las categorias del modulo core`() {
        val base = repositorio.preguntas(setOf(Modulo.CORE))
        assertTrue("Faltan preguntas base: ${base.size}", base.size >= 30)
        Categoria.entries.filter { it.modulo == Modulo.CORE }.forEach { categoria ->
            assertTrue("Sin preguntas en ${categoria.id}", base.any { it.categoria == categoria })
        }
    }

    @Test
    fun `las dificultades cubren un rango amplio`() {
        val dificultades = repositorio.preguntas(setOf(Modulo.CORE)).map { it.dificultad }.toSet()
        assertTrue("Dificultades presentes: $dificultades", dificultades.size >= 4)
    }

    @Test
    fun `con el modulo 1689 apagado el contenido real no incluye nada de 1689`() {
        assertTrue(repositorio.preguntas(setOf(Modulo.CORE)).none { it.modulo == Modulo.CONFESION_1689 })
    }

    @Test
    fun `ninguna explicacion ni enunciado esta en blanco`() {
        repositorio.preguntas(Modulo.entries.toSet()).forEach {
            assertTrue(it.id, it.enunciado.isNotBlank() && it.explicacion.isNotBlank())
        }
    }
}
