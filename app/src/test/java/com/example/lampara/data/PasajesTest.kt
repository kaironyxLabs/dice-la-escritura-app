package com.example.lampara.data

import com.example.lampara.data.content.LectorPasajes
import com.example.lampara.domain.model.ResultadoTexto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PasajesTest {

    private fun informe(texto: String) = LectorPasajes.leer(texto)

    private fun archivo(vararg pasajes: String) =
        """{"contentVersion":1,"passages":[${pasajes.joinToString(",")}]}"""

    private fun pasaje(id: String = "mem-001", referencia: String = "Juan 3:16", etiquetas: String = "[\"Salvación\"]") =
        """{"id":"$id","reference":"$referencia","tags":$etiquetas,"reviewed":false}"""

    // --- memorize.json real ---

    private val real = informe(File("src/main/assets/content/memorize.json").readText(Charsets.UTF_8))

    @Test
    fun `memorize json real es valido`() {
        assertEquals(emptyList<String>(), real.errores)
    }

    @Test
    fun `hay unos cien pasajes con etiquetas y sin repetir`() {
        assertTrue("Pasajes: ${real.pasajes.size}", real.pasajes.size in 90..130)
        assertEquals(real.pasajes.size, real.pasajes.map { it.id }.toSet().size)
        assertEquals(real.pasajes.size, real.pasajes.map { it.referenciaTexto }.toSet().size)
        assertTrue(real.pasajes.all { it.etiquetas.isNotEmpty() })
    }

    @Test
    fun `todos los pasajes son nuevos y por revisar`() {
        assertTrue(real.pasajes.none { it.revisado })
    }

    @Test
    fun `los versiculos de cada pasaje existen en su capitulo`() {
        // Conteo independiente de versículos por capítulo (docs/versiculos_por_capitulo.json, solo números).
        val conteo = Json.parseToJsonElement(File("../docs/versiculos_por_capitulo.json").readText(Charsets.UTF_8))
            .jsonObject.getValue("libros").jsonObject
        real.pasajes.forEach { pasaje ->
            val r = pasaje.referencia
            val maximo = (conteo.getValue(r.libro.id) as JsonArray)[r.capituloInicio - 1].jsonPrimitive.int
            assertTrue("${pasaje.referenciaTexto}: el capítulo tiene $maximo versículos", r.versiculoFin!! <= maximo)
        }
    }

    @Test
    fun `cada pasaje se resuelve en un texto con la estructura de la Biblia`() {
        val texto = TextoDePrueba.texto()
        real.pasajes.forEach {
            assertTrue(it.referenciaTexto, texto.versiculos(it.referencia) is ResultadoTexto.Disponible)
        }
    }

    // --- Validación del lector ---

    @Test
    fun `lee un pasaje valido`() {
        val r = informe(archivo(pasaje()))
        assertEquals(emptyList<String>(), r.errores)
        assertEquals("Juan 3:16", r.pasajes.single().referenciaTexto)
        assertEquals("JHN", r.pasajes.single().referencia.libro.id)
    }

    @Test
    fun `detecta ids repetidos o mal formados`() {
        assertTrue(informe(archivo(pasaje(), pasaje())).errores.any { "duplicado" in it })
        assertTrue(informe(archivo(pasaje(id = "x1"))).errores.any { "formato" in it })
    }

    @Test
    fun `detecta referencias inexistentes`() {
        assertTrue(informe(archivo(pasaje(referencia = "Juan 99:1"))).errores.any { "capítulo" in it })
        assertTrue(informe(archivo(pasaje(referencia = "Libro Falso 1:1"))).errores.any { "libro" in it })
    }

    @Test
    fun `exige un solo capitulo y versiculos explicitos`() {
        assertTrue(informe(archivo(pasaje(referencia = "Génesis 1:1-2:3"))).errores.any { "un solo capítulo" in it })
        assertTrue(informe(archivo(pasaje(referencia = "Salmos 23"))).errores.any { "un solo capítulo" in it })
    }

    @Test
    fun `detecta etiquetas vacias y json roto`() {
        assertTrue(informe(archivo(pasaje(etiquetas = "[\" \"]"))).errores.any { "etiqueta" in it })
        assertTrue(informe("{no es json").errores.isNotEmpty())
        assertTrue(informe(archivo(pasaje()).replace("\"tags\"", "\"etiquetas\"")).errores.isNotEmpty())
    }
}
