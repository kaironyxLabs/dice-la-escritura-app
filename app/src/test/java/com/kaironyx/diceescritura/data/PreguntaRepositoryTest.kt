package com.kaironyx.diceescritura.data

import com.kaironyx.diceescritura.data.content.FuenteContenido
import com.kaironyx.diceescritura.data.repository.PreguntaRepository
import com.kaironyx.diceescritura.domain.model.Modulo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreguntaRepositoryTest {

    private fun fuente(vararg archivos: Pair<String, String>) = object : FuenteContenido {
        override fun leerArchivos() = archivos.toMap()
    }

    private val coreJson = """
        {"contentVersion": 3, "questions": [
          {"id": "core-libros-0001", "module": "core", "category": "libros", "type": "true_false",
           "difficulty": 1, "question": "Pregunta falsa de prueba", "answer": true,
           "references": ["Génesis 1:1"], "explanation": "Explicación de prueba", "reviewed": true},
          {"id": "core-libros-0002", "module": "core", "category": "libros", "type": "true_false",
           "difficulty": 1, "question": "Otra pregunta falsa", "answer": false,
           "references": ["Éxodo 1:1"], "explanation": "Explicación de prueba", "reviewed": false}
        ]}
    """.trimIndent()

    private val confesionJson = """
        {"contentVersion": 5, "questions": [
          {"id": "confesion1689-confesion-0001", "module": "confesion1689", "category": "confesion",
           "type": "true_false", "difficulty": 1, "question": "Pregunta falsa del módulo 1689", "answer": true,
           "references": ["Romanos 1:1"], "explanation": "Explicación de prueba",
           "confessionRef": "cap. 1, párr. 1", "reviewed": false}
        ]}
    """.trimIndent()

    private val repositorio = PreguntaRepository(
        fuente("core-libros.json" to coreJson, "confesion1689-confesion.json" to confesionJson),
    )

    @Test
    fun `con el modulo 1689 apagado nunca sale una pregunta de 1689`() {
        val preguntas = repositorio.preguntas(setOf(Modulo.CORE))
        assertEquals(2, preguntas.size)
        assertTrue(preguntas.none { it.modulo == Modulo.CONFESION_1689 })
        assertEquals(null, repositorio.porId("confesion1689-confesion-0001", setOf(Modulo.CORE)))
    }

    @Test
    fun `con el modulo 1689 encendido salen las dos`() {
        val todas = repositorio.preguntas(setOf(Modulo.CORE, Modulo.CONFESION_1689))
        assertEquals(3, todas.size)
        assertEquals(
            "confesion1689-confesion-0001",
            repositorio.porId("confesion1689-confesion-0001", setOf(Modulo.CORE, Modulo.CONFESION_1689))?.id,
        )
    }

    @Test
    fun `sin ningun modulo no devuelve nada`() {
        assertTrue(repositorio.preguntas(emptySet()).isEmpty())
    }

    @Test
    fun `filtra por revisadas`() {
        val revisadas = repositorio.preguntas(setOf(Modulo.CORE), soloRevisadas = true)
        assertEquals(listOf("core-libros-0001"), revisadas.map { it.id })
    }

    @Test
    fun `la version del contenido es la mayor y no hay errores`() {
        assertEquals(5, repositorio.versionContenido)
        assertTrue(repositorio.erroresDeCarga.toString(), repositorio.erroresDeCarga.isEmpty())
    }

    @Test
    fun `un archivo roto se informa y no tumba el resto`() {
        val repo = PreguntaRepository(fuente("core-libros.json" to coreJson, "core-roto.json" to "{no es json"))
        assertEquals(2, repo.preguntas(setOf(Modulo.CORE)).size)
        assertTrue(repo.erroresDeCarga.any { "core-roto.json" in it })
    }

    @Test
    fun `un campo desconocido se considera error de contenido`() {
        val conTypo = coreJson.replace("\"difficulty\"", "\"dificulty\"")
        val repo = PreguntaRepository(fuente("core-libros.json" to conTypo))
        assertTrue(repo.erroresDeCarga.isNotEmpty())
        assertTrue(repo.preguntas(setOf(Modulo.CORE)).isEmpty())
    }
}
