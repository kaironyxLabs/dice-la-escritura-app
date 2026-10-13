package com.example.lampara.domain.usecase

import com.example.lampara.domain.model.Categoria
import com.example.lampara.domain.model.Pregunta
import java.time.LocalDate

/** Aciertos y fallos acumulados de una pregunta. */
data class ConteoPregunta(val aciertos: Int, val fallos: Int)

data class PrecisionCategoria(val categoria: Categoria, val aciertos: Int, val fallos: Int) {
    val respondidas: Int get() = aciertos + fallos

    /** Porcentaje de aciertos, o null si aún no hay respuestas. */
    val porcentaje: Int? get() = if (respondidas == 0) null else Math.round(aciertos * 100f / respondidas)
}

/** Una sesión terminada, reducida a lo que necesita el historial. */
data class SesionResumida(
    val fecha: LocalDate,
    val aciertos: Int,
    val total: Int,
    /** Categoría de la sesión si fue de una sola (null en exámenes y retos, que mezclan). */
    val categoria: Categoria? = null,
)

data class DiaHistorial(val fecha: LocalDate, val respondidas: Int, val aciertos: Int)

data class ResumenHistorial(val dias: List<DiaHistorial>) {
    val respondidas: Int get() = dias.sumOf { it.respondidas }
    val aciertos: Int get() = dias.sumOf { it.aciertos }
    val diasConActividad: Int get() = dias.count { it.respondidas > 0 }
    val maximoDiario: Int get() = dias.maxOfOrNull { it.respondidas } ?: 0
    val porcentajeAciertos: Int? get() = if (respondidas == 0) null else Math.round(aciertos * 100f / respondidas)
}

object Estadisticas {

    /**
     * Precisión por categoría. Solo cuenta las preguntas de [banco], que ya viene filtrado por los módulos
     * habilitados: con un módulo apagado su progreso se conserva en la base de datos pero no aparece aquí.
     */
    fun precisionPorCategoria(
        conteos: Map<String, ConteoPregunta>,
        banco: List<Pregunta>,
        categorias: List<Categoria>,
    ): List<PrecisionCategoria> {
        val porCategoria = banco.groupBy { it.categoria }
        return categorias.map { categoria ->
            val preguntas = porCategoria[categoria].orEmpty()
            PrecisionCategoria(
                categoria = categoria,
                aciertos = preguntas.sumOf { conteos[it.id]?.aciertos ?: 0 },
                fallos = preguntas.sumOf { conteos[it.id]?.fallos ?: 0 },
            )
        }
    }

    /** Actividad de los últimos [dias] días (incluido [hoy]), del más antiguo al más reciente. */
    fun historial(sesiones: List<SesionResumida>, hoy: LocalDate, dias: Int = 30): ResumenHistorial {
        require(dias >= 1) { "Debe haber al menos un día" }
        val desde = hoy.minusDays((dias - 1).toLong())
        val porDia = sesiones.filter { !it.fecha.isBefore(desde) && !it.fecha.isAfter(hoy) }.groupBy { it.fecha }
        return ResumenHistorial(
            (0 until dias).map { desplazamiento ->
                val fecha = desde.plusDays(desplazamiento.toLong())
                val delDia = porDia[fecha].orEmpty()
                DiaHistorial(fecha, respondidas = delDia.sumOf { it.total }, aciertos = delDia.sumOf { it.aciertos })
            },
        )
    }
}
