package com.example.lampara.data

import com.example.lampara.data.bible.BibleRepository
import com.example.lampara.data.bible.CargaTexto
import com.example.lampara.data.bible.LectorBiblia
import com.example.lampara.data.bible.TextoBiblicoInvalidoException
import com.example.lampara.domain.model.MotivoNoDisponible
import com.example.lampara.domain.model.ResultadoReferencia
import com.example.lampara.domain.model.ResultadoTexto
import com.example.lampara.domain.model.TablaLibros
import com.example.lampara.domain.usecase.ParserReferencias
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TextoBiblicoTest {

    private fun referencia(texto: String) =
        (ParserReferencias.analizar(texto) as ResultadoReferencia.Valida).referencia

    @Test
    fun `lee un texto con los 66 libros y 1189 capitulos`() {
        val texto = TextoDePrueba.texto()
        assertEquals(1189, texto.totalCapitulos)
        assertNotNull(texto.capitulo("PSA", 150))
        assertNull(texto.capitulo("PSA", 151))
        assertNull(texto.capitulo("XXX", 1))
    }

    @Test
    fun `devuelve los versiculos de un pasaje en orden`() {
        val resultado = TextoDePrueba.texto().versiculos(referencia("Juan 3:16-18"))
        resultado as ResultadoTexto.Disponible
        assertEquals(listOf(16, 17, 18), resultado.versiculos.map { it.numero })
        assertTrue(resultado.texto.startsWith(TEXTO_FALSO))
        assertEquals(3, resultado.texto.split(" JHN 3:").size - 1)
    }

    @Test
    fun `un versiculo pendiente vuelve no disponible todo el pasaje`() {
        val texto = TextoDePrueba.texto(pendientes = mapOf("JHN 3" to listOf(17)))
        val resultado = texto.versiculos(referencia("Juan 3:16-18"))
        assertEquals(ResultadoTexto.NoDisponible(MotivoNoDisponible.PENDIENTE, 17), resultado)
        // Fuera del versículo pendiente, el capítulo sigue disponible.
        assertTrue(texto.versiculos(referencia("Juan 3:16")) is ResultadoTexto.Disponible)
        assertTrue(texto.versiculos(referencia("Juan 3:18")) is ResultadoTexto.Disponible)
    }

    @Test
    fun `un versiculo que no existe no esta disponible`() {
        val resultado = TextoDePrueba.texto().versiculos(referencia("Juan 3:179-185"))
        assertEquals(ResultadoTexto.NoDisponible(MotivoNoDisponible.INEXISTENTE, 181), resultado)
    }

    @Test
    fun `un versiculo vacio se trata como pendiente`() {
        val json = TextoDePrueba.json().replaceFirst(
            "\"$TEXTO_FALSO JHN 3:16\"", "\"\"",
        )
        val resultado = LectorBiblia.leer(json).versiculos(referencia("Juan 3:16"))
        assertEquals(ResultadoTexto.NoDisponible(MotivoNoDisponible.PENDIENTE, 16), resultado)
    }

    @Test
    fun `solo admite pasajes de un capitulo con versiculos`() {
        val texto = TextoDePrueba.texto()
        assertThrows(IllegalArgumentException::class.java) { texto.versiculos(referencia("Génesis 1:1-2:3")) }
        assertThrows(IllegalArgumentException::class.java) { texto.versiculos(referencia("Salmos 23")) }
    }

    @Test
    fun `rechaza un archivo con formato invalido`() {
        assertThrows(TextoBiblicoInvalidoException::class.java) { LectorBiblia.leer("{no es json") }
        assertThrows(TextoBiblicoInvalidoException::class.java) { LectorBiblia.leer("{}") }
        // Falta un libro.
        val sinLibro = assertThrows(TextoBiblicoInvalidoException::class.java) {
            LectorBiblia.leer(TextoDePrueba.json(omitirLibro = "RUT"))
        }
        assertTrue(sinLibro.message!!.contains("RUT"))
        // Capítulo desordenado.
        val desordenado = TextoDePrueba.json().replaceFirst("\"numero\":2,", "\"numero\":7,")
        assertThrows(TextoBiblicoInvalidoException::class.java) { LectorBiblia.leer(desordenado) }
        // Campo desconocido.
        val extra = TextoDePrueba.json().replaceFirst("\"traduccion\"", "\"otro\":1,\"traduccion\"")
        assertThrows(TextoBiblicoInvalidoException::class.java) { LectorBiblia.leer(extra) }
    }

    @Test
    fun `el repositorio avisa cuando falta el archivo en lugar de fallar`() = runTest {
        val sinArchivo = BibleRepository(FuenteDeTextoFalsa(CargaTexto.Faltante))
        assertEquals(false, sinArchivo.disponible())
        assertEquals(ResultadoTexto.ArchivoFaltante, sinArchivo.textoDe(referencia("Juan 3:16")))
        assertEquals(CargaTexto.Faltante, sinArchivo.estado())

        val invalido = BibleRepository(FuenteDeTextoFalsa(CargaTexto.Invalido("formato")))
        assertEquals(ResultadoTexto.ArchivoFaltante, invalido.textoDe(referencia("Juan 3:16")))
        assertEquals(false, invalido.disponible())
    }

    @Test
    fun `el repositorio entrega el texto cuando esta instalado`() = runTest {
        val repositorio = BibleRepository(FuenteDeTextoFalsa(CargaTexto.Cargado(TextoDePrueba.texto())))
        assertEquals(true, repositorio.disponible())
        assertTrue(repositorio.textoDe(referencia("Romanos 3:23")) is ResultadoTexto.Disponible)
    }

    @Test
    fun `el texto de prueba cubre cada libro de la tabla`() {
        val texto = TextoDePrueba.texto()
        TablaLibros.todos.forEach { libro ->
            assertNotNull(libro.id, texto.capitulo(libro.id, libro.capitulos))
        }
    }
}
