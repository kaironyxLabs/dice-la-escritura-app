package com.example.lampara.data.content

import android.content.Context
import com.example.lampara.domain.model.Referencia
import com.example.lampara.domain.model.ResultadoReferencia
import com.example.lampara.domain.usecase.ParserReferencias
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/** Pasaje clave a memorizar: solo la referencia y etiquetas; el texto sale del texto bíblico instalado. */
data class Pasaje(
    val id: String,
    val referenciaTexto: String,
    val referencia: Referencia,
    val etiquetas: List<String>,
    val revisado: Boolean,
)

@Serializable
private data class PasajeDto(
    val id: String,
    val reference: String,
    val tags: List<String> = emptyList(),
    val reviewed: Boolean = false,
)

@Serializable
private data class ArchivoPasajesDto(val contentVersion: Int, val passages: List<PasajeDto>)

data class InformePasajes(val pasajes: List<Pasaje>, val errores: List<String>)

/** Lee y valida assets/content/memorize.json. */
object LectorPasajes {
    const val ARCHIVO = "memorize.json"

    private val formatoId = Regex("""^mem-\d{3}$""")
    private val json = Json { ignoreUnknownKeys = false }

    fun leer(texto: String): InformePasajes {
        val archivo = try {
            json.decodeFromString<ArchivoPasajesDto>(texto)
        } catch (e: Exception) {
            return InformePasajes(emptyList(), listOf("$ARCHIVO: ${e.message?.lineSequence()?.firstOrNull()}"))
        }
        val errores = mutableListOf<String>()
        val pasajes = mutableListOf<Pasaje>()
        val vistos = mutableSetOf<String>()
        archivo.passages.forEach { dto ->
            val problemas = mutableListOf<String>()
            if (!formatoId.matches(dto.id)) problemas += "id con formato inválido (esperado mem-001)"
            if (!vistos.add(dto.id)) problemas += "id duplicado"
            if (dto.tags.any { it.isBlank() }) problemas += "etiqueta vacía"
            val referencia = when (val r = ParserReferencias.analizar(dto.reference)) {
                is ResultadoReferencia.Valida -> r.referencia
                is ResultadoReferencia.Invalida -> {
                    problemas += "referencia \"${dto.reference}\": ${r.motivo}"
                    null
                }
            }
            if (referencia != null) {
                val unSoloCapitulo = referencia.capituloInicio == referencia.capituloFin
                if (!unSoloCapitulo || referencia.versiculoInicio == null || referencia.versiculoFin == null) {
                    problemas += "debe ser un pasaje de un solo capítulo con versículos explícitos"
                }
            }
            if (problemas.isEmpty() && referencia != null) {
                pasajes += Pasaje(dto.id, dto.reference, referencia, dto.tags, dto.reviewed)
            } else {
                errores += problemas.map { "${dto.id}: $it" }
            }
        }
        return InformePasajes(pasajes, errores)
    }
}

@Singleton
class PasajesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val informe: InformePasajes by lazy {
        val texto = context.assets.open("content/${LectorPasajes.ARCHIVO}").bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
        LectorPasajes.leer(texto)
    }

    val pasajes: List<Pasaje> get() = informe.pasajes

    fun porId(id: String): Pasaje? = informe.pasajes.firstOrNull { it.id == id }
}
