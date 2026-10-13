package com.example.lampara.data.bible

import com.example.lampara.domain.model.CapituloTexto
import com.example.lampara.domain.model.ParaleloTexto
import com.example.lampara.domain.model.Libro
import com.example.lampara.domain.model.SeccionTexto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Formato de assets/bible/libros/<ID>.json, documentado en docs/TEXTO_BIBLICO.md.

@Serializable
private data class SeccionDto(val antesDelVersiculo: Int, val titulo: String)

@Serializable
private data class ParaleloDto(val antesDelVersiculo: Int, val texto: String)

@Serializable
private data class CapituloDto(
    val numero: Int,
    val encabezado: String? = null,
    val versiculos: List<String>,
    val secciones: List<SeccionDto> = emptyList(),
    val paralelos: List<ParaleloDto> = emptyList(),
    val pendientes: List<Int> = emptyList(),
)

@Serializable
private data class LibroDto(val id: String, val capitulos: List<CapituloDto>)

/** El archivo del texto bíblico no tiene el formato esperado. */
class TextoBiblicoInvalidoException(mensaje: String) : Exception(mensaje)

object LectorBiblia {
    private val json = Json { ignoreUnknownKeys = false }

    /** Lee el JSON de un libro y comprueba que sea ese libro y que tenga todos sus capítulos en orden. */
    fun leerLibro(texto: String, libro: Libro): List<CapituloTexto> {
        val dto = try {
            json.decodeFromString<LibroDto>(texto)
        } catch (e: Exception) {
            throw TextoBiblicoInvalidoException("Formato inválido: ${e.message?.lineSequence()?.firstOrNull()}")
        }
        if (dto.id != libro.id) throw TextoBiblicoInvalidoException("Se esperaba ${libro.id} y es ${dto.id}")
        if (dto.capitulos.map { it.numero } != (1..libro.capitulos).toList()) {
            throw TextoBiblicoInvalidoException("Capítulos de ${libro.id} incompletos o desordenados")
        }
        return dto.capitulos.map {
            CapituloTexto(
                numero = it.numero,
                encabezado = it.encabezado,
                versiculos = it.versiculos,
                secciones = it.secciones.map { s -> SeccionTexto(s.antesDelVersiculo, s.titulo) },
                paralelos = it.paralelos.map { p -> ParaleloTexto(p.antesDelVersiculo, p.texto) },
                pendientes = it.pendientes.toSet(),
            )
        }
    }
}
