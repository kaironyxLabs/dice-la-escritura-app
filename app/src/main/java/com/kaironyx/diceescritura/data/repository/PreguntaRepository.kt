package com.kaironyx.diceescritura.data.repository

import com.kaironyx.diceescritura.data.content.FuenteContenido
import com.kaironyx.diceescritura.data.content.InformeValidacion
import com.kaironyx.diceescritura.data.content.LectorContenido
import com.kaironyx.diceescritura.data.content.PreguntaDto
import com.kaironyx.diceescritura.data.content.ValidadorContenido
import com.kaironyx.diceescritura.domain.model.Modulo
import com.kaironyx.diceescritura.domain.model.Pregunta
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Banco de preguntas. El filtro por módulo se aplica aquí y no en la interfaz:
 * con un módulo deshabilitado, sus preguntas no salen de este repositorio por ninguna vía.
 */
@Singleton
class PreguntaRepository @Inject constructor(
    private val fuente: FuenteContenido,
) {
    private class Cargado(val informe: InformeValidacion, val version: Int)

    private val cargado: Cargado by lazy { cargar() }

    /** Versión del contenido: la mayor entre los archivos, para actualizar sin perder progreso. */
    val versionContenido: Int get() = cargado.version

    /** Problemas hallados al cargar; vacío si todo el contenido es válido. */
    val erroresDeCarga: List<String> get() = cargado.informe.errores

    /** Preguntas de los [modulosHabilitados]. Un módulo ausente del conjunto no devuelve nada. */
    fun preguntas(modulosHabilitados: Set<Modulo>, soloRevisadas: Boolean = false): List<Pregunta> =
        cargado.informe.preguntas.filter {
            it.modulo in modulosHabilitados && (!soloRevisadas || it.revisada)
        }

    fun porId(id: String, modulosHabilitados: Set<Modulo>): Pregunta? =
        cargado.informe.preguntas.firstOrNull { it.id == id && it.modulo in modulosHabilitados }

    private fun cargar(): Cargado {
        val dtos = mutableListOf<PreguntaDto>()
        val errores = mutableListOf<String>()
        var version = 0
        fuente.leerArchivos().forEach { (nombre, texto) ->
            try {
                val archivo = LectorContenido.leer(texto)
                version = maxOf(version, archivo.contentVersion)
                dtos += archivo.questions
            } catch (e: Exception) {
                errores += "$nombre: no se pudo leer (${e.message?.lineSequence()?.firstOrNull()})"
            }
        }
        val validacion = ValidadorContenido.validar(dtos)
        return Cargado(InformeValidacion(validacion.preguntas, errores + validacion.errores), version)
    }
}
