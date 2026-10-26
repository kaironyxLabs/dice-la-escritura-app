package com.kaironyx.diceescritura.domain.usecase

import com.kaironyx.diceescritura.domain.model.Referencia
import com.kaironyx.diceescritura.domain.model.ResultadoReferencia

/**
 * Escenas ilustradas de las historias. Una pregunta muestra la escena cuando su pasaje se cruza con el
 * tramo (capítulo y versículo) que la escena dibuja. Las que aún no tienen ilustración no aparecen.
 */
enum class EscenaHistoria(private val libro: String, private val desde: Int, private val hasta: Int) {
    CREACION("GEN", pos(1, 1), pos(2, 3)),
    EDEN("GEN", pos(2, 4), pos(3, 24)),
    BABEL("GEN", pos(11, 1), pos(11, 9)),
    ABRAHAM_ESTRELLAS("GEN", pos(15, 1), pos(15, 6)),
    JACOB_ESCALERA("GEN", pos(28, 10), pos(28, 22)),
    JOSE_EGIPTO("GEN", pos(37, 1), pos(47, 31)),
    ZARZA("EXO", pos(3, 1), pos(3, 6)),
    PASCUA("EXO", pos(12, 1), pos(12, 36)),
    ;

    private fun abarca(r: Referencia): Boolean {
        if (r.libro.id != libro) return false
        val inicio = pos(r.capituloInicio, r.versiculoInicio ?: 1)
        val fin = pos(r.capituloFin, r.versiculoFin ?: r.versiculoInicio ?: VERSICULO_MAXIMO)
        return inicio <= hasta && fin >= desde
    }

    companion object {
        /** La primera escena que corresponde a alguna de las [referencias] de la pregunta, o nulo. */
        fun para(referencias: List<String>): EscenaHistoria? {
            for (texto in referencias) {
                val r = (ParserReferencias.analizar(texto) as? ResultadoReferencia.Valida)?.referencia ?: continue
                entries.firstOrNull { it.abarca(r) }?.let { return it }
            }
            return null
        }
    }
}

private const val VERSICULO_MAXIMO = 999

private fun pos(capitulo: Int, versiculo: Int) = capitulo * 1000 + versiculo
