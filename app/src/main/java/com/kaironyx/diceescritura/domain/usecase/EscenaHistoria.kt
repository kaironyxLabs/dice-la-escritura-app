package com.kaironyx.diceescritura.domain.usecase

import com.kaironyx.diceescritura.domain.model.Referencia
import com.kaironyx.diceescritura.domain.model.ResultadoReferencia

/**
 * Escenas ilustradas de las historias. Una pregunta muestra la escena cuando su pasaje cae en el
 * capítulo o capítulos indicados. Las que aún no tienen ilustración simplemente no aparecen.
 */
enum class EscenaHistoria(private val libro: String, private val capitulos: IntRange) {
    CREACION("GEN", 1..1),
    EDEN("GEN", 2..3),
    BABEL("GEN", 11..11),
    ABRAHAM_ESTRELLAS("GEN", 15..15),
    JACOB_ESCALERA("GEN", 28..28),
    JOSE_EGIPTO("GEN", 37..47),
    ZARZA("EXO", 3..3),
    PASCUA("EXO", 12..12),
    ;

    private fun abarca(r: Referencia) =
        r.libro.id == libro && r.capituloInicio <= capitulos.last && r.capituloFin >= capitulos.first

    companion object {
        /** La primera escena que corresponde a alguna de las [referencias] de la pregunta, o nulo. */
        fun para(referencias: List<String>): EscenaHistoria? {
            for (texto in referencias) {
                val r = (ParserReferencias.analizar(texto) as? ResultadoReferencia.Valida)?.referencia
                    ?: continue
                entries.firstOrNull { it.abarca(r) }?.let { return it }
            }
            return null
        }
    }
}
