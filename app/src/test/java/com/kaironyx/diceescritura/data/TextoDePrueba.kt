package com.kaironyx.diceescritura.data

import com.kaironyx.diceescritura.data.bible.BibleTextSource
import com.kaironyx.diceescritura.data.bible.CargaLibro
import com.kaironyx.diceescritura.data.bible.LectorBiblia
import com.kaironyx.diceescritura.domain.model.Libro
import com.kaironyx.diceescritura.domain.model.TablaLibros
import com.kaironyx.diceescritura.domain.model.TextoBiblico

const val TEXTO_FALSO = "Texto de prueba, no es RVR1960."

/**
 * Texto bíblico FALSO para las pruebas: la estructura de la RVR1960 (66 libros, 1.189 capítulos) con el
 * mismo texto de relleno en cada versículo. Nunca se usa texto de la RVR1960 en las fuentes de test.
 */
object TextoDePrueba {
    const val VERSICULOS_POR_CAPITULO = 180

    /**
     * JSON de un libro (formato de assets/bible/libros/<ID>.json). [pendientes] indica, por número de capítulo,
     * los versículos pendientes (ej. 3 a [16]).
     */
    fun jsonLibro(libro: Libro, pendientes: Map<Int, List<Int>> = emptyMap()): String {
        val capitulos = (1..libro.capitulos).joinToString(",") { numero ->
            val versiculos = (1..VERSICULOS_POR_CAPITULO).joinToString(",") { v ->
                "\"$TEXTO_FALSO ${libro.id} $numero:$v\""
            }
            val pend = pendientes[numero].orEmpty().joinToString(",")
            """{"numero":$numero,"encabezado":null,"versiculos":[$versiculos],"secciones":[],"paralelos":[],"pendientes":[$pend]}"""
        }
        return """{"id":"${libro.id}","capitulos":[$capitulos]}"""
    }

    /** Los 66 libros con la estructura de la RVR1960. [pendientes] va por "ID capítulo" (ej. "JHN 3" a [16]). */
    fun texto(pendientes: Map<String, List<Int>> = emptyMap()): TextoBiblico = TextoBiblico(
        TablaLibros.todos.associate { libro ->
            val delLibro = pendientes.entries
                .filter { it.key.startsWith("${libro.id} ") }
                .associate { it.key.substringAfter(" ").toInt() to it.value }
            libro.id to LectorBiblia.leerLibro(jsonLibro(libro, delLibro), libro)
        },
    )
}

/** Fuente de texto falsa: entrega los libros de [texto], o el mismo resultado de error para todos. */
class FuenteDeTextoFalsa private constructor(
    private val respuesta: (String) -> CargaLibro,
) : BibleTextSource {
    constructor(texto: TextoBiblico) : this({ id ->
        texto.libro(id)?.let { CargaLibro.Cargado(it) } ?: CargaLibro.Faltante
    })

    constructor(error: CargaLibro) : this({ error })

    override suspend fun libro(id: String): CargaLibro = respuesta(id)
}
