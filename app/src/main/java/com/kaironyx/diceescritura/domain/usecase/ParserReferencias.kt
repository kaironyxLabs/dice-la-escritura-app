package com.kaironyx.diceescritura.domain.usecase

import com.kaironyx.diceescritura.domain.model.Referencia
import com.kaironyx.diceescritura.domain.model.ResultadoReferencia
import com.kaironyx.diceescritura.domain.model.TablaLibros

/**
 * Interpreta una referencia escrita y la valida contra la tabla de 66 libros (libro y capítulos existentes).
 * No valida números de versículo por capítulo: ese conteo viene del texto importado (H4).
 */
object ParserReferencias {

    // libro, capítulo, [:versículo], [-[capítulo:]fin]
    private val patron = Regex("""^(.+?)\s+(\d+)(?::(\d+))?(?:\s*[-–]\s*(?:(\d+):)?(\d+))?$""")

    fun analizar(texto: String): ResultadoReferencia {
        val limpio = texto.trim()
        if (limpio.isEmpty()) return invalida("la referencia está vacía")
        if (limpio.contains(';') || limpio.contains(',')) {
            return invalida("una referencia por elemento, sin ';' ni ','")
        }
        val coincidencia = patron.matchEntire(limpio)
            ?: return invalida("formato no reconocido: \"$limpio\"")
        val (nombre, capTexto, versTexto, finCapTexto, finTexto) = coincidencia.destructured

        val libro = TablaLibros.buscar(nombre) ?: return invalida("libro desconocido: \"$nombre\"")
        val primerNumero = capTexto.toInt()
        val segundoNumero = versTexto.takeIf { it.isNotEmpty() }?.toInt()
        val finCapitulo = finCapTexto.takeIf { it.isNotEmpty() }?.toInt()
        val fin = finTexto.takeIf { it.isNotEmpty() }?.toInt()

        // En libros de un solo capítulo, "Judas 3" o "Judas 3-5" se refiere a versículos.
        val soloVersiculos = libro.capitulos == 1 && segundoNumero == null
        val capituloInicio: Int
        val versiculoInicio: Int?
        val capituloFin: Int
        val versiculoFin: Int?
        when {
            soloVersiculos -> {
                capituloInicio = 1
                versiculoInicio = primerNumero
                capituloFin = 1
                versiculoFin = fin
            }
            segundoNumero == null -> { // "Salmos 23" o "Salmos 23-25": capítulos completos
                capituloInicio = primerNumero
                versiculoInicio = null
                capituloFin = fin ?: primerNumero
                versiculoFin = null
            }
            else -> {
                capituloInicio = primerNumero
                versiculoInicio = segundoNumero
                capituloFin = finCapitulo ?: primerNumero
                versiculoFin = fin ?: segundoNumero
            }
        }

        if (capituloInicio !in 1..libro.capitulos) {
            return invalida("${libro.nombre} no tiene el capítulo $capituloInicio (tiene ${libro.capitulos})")
        }
        if (capituloFin !in capituloInicio..libro.capitulos) {
            return invalida("capítulo final $capituloFin fuera de rango en ${libro.nombre}")
        }
        if (versiculoInicio != null && versiculoInicio < 1) return invalida("versículo inicial inválido")
        if (versiculoFin != null && versiculoFin < 1) return invalida("versículo final inválido")
        if (capituloFin == capituloInicio && versiculoInicio != null && versiculoFin != null &&
            versiculoFin < versiculoInicio
        ) {
            return invalida("el versículo final es menor que el inicial")
        }
        return ResultadoReferencia.Valida(
            Referencia(libro, capituloInicio, versiculoInicio, capituloFin, versiculoFin),
        )
    }

    private fun invalida(motivo: String) = ResultadoReferencia.Invalida(motivo)
}
