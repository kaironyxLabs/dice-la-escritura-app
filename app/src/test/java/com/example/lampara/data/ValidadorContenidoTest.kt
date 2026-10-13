package com.example.lampara.data

import com.example.lampara.data.content.PreguntaDto
import com.example.lampara.data.content.ValidadorContenido
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorContenidoTest {

    private fun opcionMultiple(
        id: String = "core-libros-0001",
        modify: PreguntaDto.() -> PreguntaDto = { this },
    ) = PreguntaDto(
        id = id,
        module = "core",
        category = "libros",
        type = PreguntaDto.TIPO_OPCION_MULTIPLE,
        difficulty = 1,
        question = "Pregunta de prueba",
        options = listOf("A", "B", "C"),
        answerIndex = 1,
        references = listOf("Génesis 1:1"),
        explanation = "Explicación de prueba",
    ).modify()

    private fun errores(vararg dtos: PreguntaDto) = ValidadorContenido.validar(dtos.toList()).errores

    @Test
    fun `una pregunta correcta no da errores`() {
        val informe = ValidadorContenido.validar(listOf(opcionMultiple()))
        assertTrue(informe.errores.toString(), informe.esValido)
        assertEquals(1, informe.preguntas.size)
    }

    @Test
    fun `detecta ids duplicados`() {
        val e = errores(opcionMultiple(), opcionMultiple())
        assertTrue(e.any { "duplicado" in it })
    }

    @Test
    fun `detecta id mal formado o que no coincide con modulo y categoria`() {
        assertTrue(errores(opcionMultiple(id = "libros-1")).any { "formato" in it })
        assertTrue(errores(opcionMultiple { copy(category = "historia") }).any { "no contiene la categoría" in it })
    }

    @Test
    fun `detecta answerIndex fuera de rango`() {
        assertTrue(errores(opcionMultiple { copy(answerIndex = 3) }).any { "answerIndex" in it })
        assertTrue(errores(opcionMultiple { copy(answerIndex = null) }).any { "answerIndex" in it })
    }

    @Test
    fun `detecta opciones repetidas o insuficientes`() {
        assertTrue(errores(opcionMultiple { copy(options = listOf("A", "A", "B")) }).any { "repetidas" in it })
        assertTrue(errores(opcionMultiple { copy(options = listOf("A", "B")) }).any { "opciones" in it })
    }

    @Test
    fun `detecta explicacion vacia y dificultad invalida`() {
        assertTrue(errores(opcionMultiple { copy(explanation = " ") }).any { "explicación" in it })
        assertTrue(errores(opcionMultiple { copy(difficulty = 6) }).any { "dificultad" in it })
        assertTrue(errores(opcionMultiple { copy(difficulty = 0) }).any { "dificultad" in it })
    }

    @Test
    fun `detecta referencias inexistentes o ausentes`() {
        assertTrue(errores(opcionMultiple { copy(references = listOf("Génesis 99:1")) }).any { "referencia" in it })
        assertTrue(errores(opcionMultiple { copy(references = listOf("Libro Falso 1:1")) }).any { "referencia" in it })
        assertTrue(errores(opcionMultiple { copy(references = emptyList()) }).any { "sin referencias" in it })
    }

    @Test
    fun `verdadero o falso requiere answer`() {
        val vf = opcionMultiple { copy(type = PreguntaDto.TIPO_VERDADERO_FALSO, options = null, answerIndex = null) }
        assertTrue(errores(vf).any { "answer" in it })
        val correcta = vf.copy(answer = true)
        assertTrue(ValidadorContenido.validar(listOf(correcta)).esValido)
    }

    @Test
    fun `ordenar requiere items distintos`() {
        val base = opcionMultiple {
            copy(type = PreguntaDto.TIPO_ORDENAR, options = null, answerIndex = null, items = listOf("A", "B", "C"))
        }
        assertTrue(ValidadorContenido.validar(listOf(base)).esValido)
        assertTrue(errores(base.copy(items = listOf("A", "A", "B"))).any { "repetidos" in it })
        assertTrue(errores(base.copy(items = listOf("A", "B"))).any { "items" in it })
    }

    @Test
    fun `rechaza mezclar campos de otro tipo`() {
        assertTrue(errores(opcionMultiple { copy(answer = true) }).any { "otro tipo" in it })
    }

    @Test
    fun `modulo 1689 exige confessionRef y categoria propia`() {
        val confesion = opcionMultiple(id = "confesion1689-confesion-0001") {
            copy(module = "confesion1689", category = "confesion")
        }
        assertTrue(errores(confesion).any { "confessionRef" in it })
        assertTrue(ValidadorContenido.validar(listOf(confesion.copy(confessionRef = "cap. 1, párr. 1"))).esValido)
        // Categoría de un módulo dentro de otro.
        val cruzada = opcionMultiple { copy(category = "confesion") }
        assertFalse(errores(cruzada).isEmpty())
    }

    @Test
    fun `modulo core no admite confessionRef`() {
        assertTrue(errores(opcionMultiple { copy(confessionRef = "cap. 1") }).any { "confessionRef" in it })
    }
}
