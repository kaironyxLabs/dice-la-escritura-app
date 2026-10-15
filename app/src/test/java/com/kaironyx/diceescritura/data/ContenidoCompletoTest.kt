package com.kaironyx.diceescritura.data

import com.kaironyx.diceescritura.data.content.FuenteContenido
import com.kaironyx.diceescritura.data.content.FuenteContenidoAssets
import com.kaironyx.diceescritura.data.repository.PreguntaRepository
import com.kaironyx.diceescritura.domain.model.Categoria
import com.kaironyx.diceescritura.domain.model.Contenido
import com.kaironyx.diceescritura.domain.model.Modulo
import com.kaironyx.diceescritura.domain.model.ResultadoReferencia
import com.kaironyx.diceescritura.domain.usecase.ParserReferencias
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Mínimos de contenido de la sección 5 y comprobaciones de calidad sobre los archivos reales. */
class ContenidoCompletoTest {

    private val repositorio = PreguntaRepository(
        object : FuenteContenido {
            override fun leerArchivos(): Map<String, String> = File("src/main/assets/content").listFiles().orEmpty()
                .filter { FuenteContenidoAssets.esArchivoDePreguntas(it.name) }
                .sortedBy { it.name }
                .associate { it.name to it.readText(Charsets.UTF_8) }
        },
    )

    private val todas = repositorio.preguntas(Modulo.entries.toSet())
    private val base = todas.filter { it.modulo == Modulo.CORE }
    private val confesion = todas.filter { it.modulo == Modulo.CONFESION_1689 }

    @Test
    fun `no hay errores de carga en el contenido`() {
        assertEquals(emptyList<String>(), repositorio.erroresDeCarga)
    }

    @Test
    fun `hay al menos 300 preguntas base repartidas por categoria y dificultad`() {
        assertTrue("Preguntas base: ${base.size}", base.size >= 300)
        Categoria.entries.filter { it.modulo == Modulo.CORE }.forEach { categoria ->
            val deLaCategoria = base.filter { it.categoria == categoria }
            assertTrue("${categoria.id}: ${deLaCategoria.size}", deLaCategoria.size >= 25)
            val dificultades = deLaCategoria.map { it.dificultad }.toSet()
            assertTrue("${categoria.id} cubre dificultades $dificultades", dificultades.size >= 3)
        }
        assertEquals((1..5).toSet(), base.map { it.dificultad }.toSet())
    }

    @Test
    fun `hay preguntas de los tres tipos`() {
        assertTrue(base.any { it.contenido is Contenido.OpcionMultiple })
        assertTrue(base.any { it.contenido is Contenido.VerdaderoFalso })
        assertTrue(base.any { it.contenido is Contenido.Ordenar })
    }

    @Test
    fun `el nivel 1 tiene preguntas de sobra para partidas y examen`() {
        // En el nivel 1 solo se desbloquean la dificultad 1 y estas tres categorías.
        for (categoria in listOf(Categoria.LIBROS, Categoria.HISTORIA, Categoria.PERSONAJES)) {
            val faciles = base.count { it.categoria == categoria && it.dificultad == 1 }
            assertTrue("${categoria.id} tiene $faciles preguntas de dificultad 1", faciles >= 14)
        }
    }

    @Test
    fun `hay al menos 96 preguntas de la Confesion, tres por cada uno de los 32 capitulos`() {
        assertTrue("Preguntas de la Confesión: ${confesion.size}", confesion.size >= 96)
        val porCapitulo = (1..32).associateWith { 0 }.toMutableMap()
        val patron = Regex("""^cap\. (\d{1,2})(?:-(\d{1,2}))?""")
        confesion.forEach { pregunta ->
            val m = patron.find(pregunta.confesionRef!!)!!
            val desde = m.groupValues[1].toInt()
            // Una pregunta que abarca varios capítulos ("cap. 1-5") no cuenta para ninguno en particular.
            if (m.groupValues[2].isEmpty()) porCapitulo[desde] = porCapitulo.getValue(desde) + 1
        }
        porCapitulo.forEach { (capitulo, cantidad) ->
            assertTrue("Capítulo $capitulo: $cantidad preguntas", cantidad >= 3)
        }
    }

    @Test
    fun `los versiculos citados existen en su capitulo`() {
        // Conteo independiente de versículos por capítulo (solo números; ver docs/TEXTO_BIBLICO.md).
        val conteo = Json.parseToJsonElement(File("../docs/versiculos_por_capitulo.json").readText(Charsets.UTF_8))
            .jsonObject.getValue("libros").jsonObject
        fun versiculos(libro: String, capitulo: Int) = conteo.getValue(libro).jsonArray[capitulo - 1].jsonPrimitive.int
        todas.forEach { pregunta ->
            pregunta.referencias.forEach { texto ->
                val r = (ParserReferencias.analizar(texto) as ResultadoReferencia.Valida).referencia
                r.versiculoInicio?.let {
                    assertTrue("${pregunta.id}: $texto", it <= versiculos(r.libro.id, r.capituloInicio))
                }
                r.versiculoFin?.let {
                    assertTrue("${pregunta.id}: $texto", it <= versiculos(r.libro.id, r.capituloFin))
                }
            }
        }
    }

    @Test
    fun `no hay enunciados repetidos`() {
        val repetidos = todas.groupBy { it.enunciado.trim().lowercase() }.filterValues { it.size > 1 }
        assertEquals(
            "Enunciados repetidos: ${repetidos.values.map { grupo -> grupo.map { it.id } }}",
            0, repetidos.size,
        )
    }

    @Test
    fun `las preguntas no citan versiculos completos de la RVR1960`() {
        // Las explicaciones parafrasean; ninguna debería ser una cita larga entre comillas.
        val comillas = Regex("[«“\"][^»”\"]{60,}[»”\"]")
        todas.forEach { assertTrue(it.id, !comillas.containsMatchIn(it.explicacion) && !comillas.containsMatchIn(it.enunciado)) }
    }

    @Test
    fun `en opcion multiple la respuesta correcta no esta siempre en el mismo lugar`() {
        val posiciones = base.map { it.contenido }.filterIsInstance<Contenido.OpcionMultiple>()
            .groupingBy { it.indiceCorrecto }.eachCount()
        assertTrue("Posiciones: $posiciones", posiciones.size >= 4 && posiciones.values.all { it > 15 })
    }
}
