package com.example.lampara.data

import com.example.lampara.data.bible.BibleTextSource
import com.example.lampara.data.bible.CargaTexto
import com.example.lampara.data.bible.LectorBiblia
import com.example.lampara.domain.model.TablaLibros

const val TEXTO_FALSO = "Texto de prueba, no es RVR1960."

/**
 * Texto bíblico FALSO para las pruebas: la estructura de la RVR1960 (66 libros, 1.189 capítulos) con el
 * mismo texto de relleno en cada versículo. Nunca se usa texto de la RVR1960 en las fuentes de test.
 */
object TextoDePrueba {
    const val VERSICULOS_POR_CAPITULO = 180

    /**
     * [pendientes] indica, por "ID capítulo", los versículos pendientes (ej. "JHN 3" a [16]).
     * [modificar] permite alterar el JSON generado para probar formatos inválidos.
     */
    fun json(pendientes: Map<String, List<Int>> = emptyMap(), omitirLibro: String? = null): String {
        val libros = TablaLibros.todos.filter { it.id != omitirLibro }.joinToString(",") { libro ->
            val capitulos = (1..libro.capitulos).joinToString(",") { numero ->
                val versiculos = (1..VERSICULOS_POR_CAPITULO).joinToString(",") { v ->
                    "\"$TEXTO_FALSO ${libro.id} $numero:$v\""
                }
                val pend = pendientes["${libro.id} $numero"].orEmpty().joinToString(",")
                """{"numero":$numero,"encabezado":null,"versiculos":[$versiculos],"secciones":[],"paralelos":[],"pendientes":[$pend]}"""
            }
            """{"id":"${libro.id}","capitulos":[$capitulos]}"""
        }
        return """{"traduccion":"RVR1960","libros":[$libros]}"""
    }

    fun texto(pendientes: Map<String, List<Int>> = emptyMap()) = LectorBiblia.leer(json(pendientes))
}

class FuenteDeTextoFalsa(private val carga: CargaTexto) : BibleTextSource {
    override suspend fun cargar(): CargaTexto = carga
}
