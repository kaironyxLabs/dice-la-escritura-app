package com.example.lampara.data

import com.example.lampara.core.flags.FeatureFlag
import com.example.lampara.core.flags.FeatureFlags
import com.example.lampara.core.flags.modulosHabilitados
import com.example.lampara.data.content.FuenteContenido
import com.example.lampara.data.content.FuenteContenidoAssets
import com.example.lampara.data.content.PreguntaDto
import com.example.lampara.data.content.ValidadorContenido
import com.example.lampara.data.repository.PreguntaRepository
import com.example.lampara.domain.model.Categoria
import com.example.lampara.domain.model.Modulo
import com.example.lampara.domain.usecase.FiltroJuego
import com.example.lampara.domain.usecase.SelectorPreguntas
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate
import kotlin.random.Random

class ConfesionTest {

    private val carpeta = File("src/main/assets/content")

    private val repositorio = PreguntaRepository(
        object : FuenteContenido {
            override fun leerArchivos(): Map<String, String> = carpeta.listFiles().orEmpty()
                .filter { FuenteContenidoAssets.esArchivoDePreguntas(it.name) }
                .sortedBy { it.name }
                .associate { it.name to it.readText(Charsets.UTF_8) }
        },
    )

    private val soloBase = setOf(Modulo.CORE)
    private val ambos = setOf(Modulo.CORE, Modulo.CONFESION_1689)

    /** Banderas en memoria para probar la lógica sin DataStore. */
    private class BanderasEnMemoria : FeatureFlags {
        private val valores = mutableMapOf<FeatureFlag, MutableStateFlow<Boolean>>()
        private fun flujo(flag: FeatureFlag) = valores.getOrPut(flag) { MutableStateFlow(flag.valorPorDefecto) }
        override fun habilitada(flag: FeatureFlag): Flow<Boolean> = flujo(flag).map { it }
        override suspend fun establecer(flag: FeatureFlag, valor: Boolean) { flujo(flag).value = valor }
    }

    // --- Bandera ---

    @Test
    fun `la bandera de la Confesion esta apagada por defecto`() = runTest {
        assertFalse(FeatureFlag.CONFESION_1689.valorPorDefecto)
        val banderas = BanderasEnMemoria()
        assertEquals(soloBase, banderas.modulosHabilitados().first())
    }

    @Test
    fun `al encender y apagar la bandera cambian los modulos habilitados`() = runTest {
        val banderas = BanderasEnMemoria()
        banderas.establecer(FeatureFlag.CONFESION_1689, true)
        assertEquals(ambos, banderas.modulosHabilitados().first())
        banderas.establecer(FeatureFlag.CONFESION_1689, false)
        assertEquals(soloBase, banderas.modulosHabilitados().first())
    }

    // --- Contenido real ---

    @Test
    fun `hay preguntas de la Confesion y todas llevan su referencia a la Confesion`() {
        val confesion = repositorio.preguntas(ambos).filter { it.modulo == Modulo.CONFESION_1689 }
        assertTrue("Preguntas de la Confesión: ${confesion.size}", confesion.size >= 10)
        assertTrue(confesion.all { it.categoria == Categoria.CONFESION })
        assertTrue(confesion.all { !it.confesionRef.isNullOrBlank() })
    }

    @Test
    fun `con la bandera apagada jamas sale una pregunta de la Confesion por ninguna via`() {
        val banco = repositorio.preguntas(soloBase)
        assertTrue(banco.none { it.modulo == Modulo.CONFESION_1689 || it.categoria == Categoria.CONFESION })
        assertEquals(null, repositorio.porId("confesion1689-confesion-0001", soloBase))

        // Aun si el selector recibiera todo el banco, el filtro de módulos lo excluye.
        val todo = repositorio.preguntas(ambos)
        val filtro = FiltroJuego(
            modulosHabilitados = soloBase,
            soloRevisadas = false,
            dificultadMaxima = 5,
            categoriasPermitidas = Categoria.entries.toSet(),
        )
        val selector = SelectorPreguntas(Random(11))
        val hoy = LocalDate.of(2026, 10, 13)
        repeat(30) {
            assertTrue(selector.partida(todo, filtro, emptyMap(), hoy, 50).none { it.modulo == Modulo.CONFESION_1689 })
            assertTrue(selector.reto(todo, filtro, emptyMap(), hoy, 50).none { it.modulo == Modulo.CONFESION_1689 })
            assertTrue(selector.examen(todo, filtro, 50).none { it.modulo == Modulo.CONFESION_1689 })
        }
        // Con la bandera encendida sí entran.
        val conModulo = selector.partida(todo, filtro.copy(modulosHabilitados = ambos), emptyMap(), hoy, 100)
        assertTrue(conModulo.any { it.modulo == Modulo.CONFESION_1689 })
    }

    @Test
    fun `la tabla de capitulos de la Confesion tiene 32 capitulos consecutivos con titulo`() {
        val capitulos = Json.parseToJsonElement(File("src/main/assets/content/confesion-capitulos.json").readText(Charsets.UTF_8))
            .jsonObject.getValue("capitulos").jsonArray.map { it.jsonObject }
        assertEquals(ValidadorContenido.CAPITULOS_CONFESION, capitulos.size)
        assertEquals((1..32).toList(), capitulos.map { it.getValue("numero").jsonPrimitive.int })
        assertTrue(capitulos.all { it.getValue("titulo").jsonPrimitive.content.isNotBlank() })
    }

    @Test
    fun `la tabla de capitulos no se confunde con un archivo de preguntas`() {
        assertFalse(FuenteContenidoAssets.esArchivoDePreguntas("confesion-capitulos.json"))
        assertTrue(FuenteContenidoAssets.esArchivoDePreguntas("confesion1689-confesion.json"))
    }

    // --- Formato de confessionRef ---

    private fun pregunta(ref: String?) = PreguntaDto(
        id = "confesion1689-confesion-0001", module = "confesion1689", category = "confesion",
        type = PreguntaDto.TIPO_VERDADERO_FALSO, difficulty = 1, question = "Pregunta de prueba",
        answer = true, references = listOf("Génesis 1:1"), explanation = "Explicación de prueba",
        confessionRef = ref,
    )

    @Test
    fun `acepta los formatos validos de confessionRef`() {
        for (ref in listOf("cap. 1", "cap. 1, párr. 1", "cap. 32, párr. 3", "cap. 1-5", "cap. 26, párr. 15")) {
            val errores = ValidadorContenido.validar(listOf(pregunta(ref))).errores
            assertTrue("$ref: $errores", errores.isEmpty())
        }
    }

    @Test
    fun `rechaza formatos y capitulos invalidos de confessionRef`() {
        for (ref in listOf("capítulo 1", "cap 1", "cap. 0", "cap. 33", "cap. 5-3", "cap. 1, par. 1", "1, párr. 2", "cap. 1-40")) {
            assertFalse("$ref debería ser inválido", ValidadorContenido.validar(listOf(pregunta(ref))).esValido)
        }
    }
}
