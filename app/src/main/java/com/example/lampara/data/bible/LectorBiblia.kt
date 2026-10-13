package com.example.lampara.data.bible

import com.example.lampara.domain.model.CapituloTexto
import com.example.lampara.domain.model.ParaleloTexto
import com.example.lampara.domain.model.SeccionTexto
import com.example.lampara.domain.model.TablaLibros
import com.example.lampara.domain.model.TextoBiblico
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Formato de assets/bible/rvr1960.json, documentado en docs/TEXTO_BIBLICO.md.

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

@Serializable
private data class BibliaDto(val traduccion: String, val libros: List<LibroDto>)

/** El archivo del texto bíblico no tiene el formato esperado. */
class TextoBiblicoInvalidoException(mensaje: String) : Exception(mensaje)

object LectorBiblia {
    private val json = Json { ignoreUnknownKeys = false }

    /** Lee el JSON del texto y comprueba que cada libro de la tabla tenga sus capítulos en orden. */
    fun leer(texto: String): TextoBiblico {
        val dto = try {
            json.decodeFromString<BibliaDto>(texto)
        } catch (e: Exception) {
            throw TextoBiblicoInvalidoException("Formato inválido: ${e.message?.lineSequence()?.firstOrNull()}")
        }
        val porId = dto.libros.associateBy { it.id }
        if (porId.size != dto.libros.size) throw TextoBiblicoInvalidoException("Hay libros repetidos")
        val libros = TablaLibros.todos.associate { libro ->
            val capitulos = porId[libro.id]?.capitulos
                ?: throw TextoBiblicoInvalidoException("Falta el libro ${libro.id}")
            if (capitulos.map { it.numero } != (1..libro.capitulos).toList()) {
                throw TextoBiblicoInvalidoException("Capítulos de ${libro.id} incompletos o desordenados")
            }
            libro.id to capitulos.map {
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
        return TextoBiblico(libros)
    }
}
