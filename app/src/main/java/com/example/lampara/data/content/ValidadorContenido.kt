package com.example.lampara.data.content

import com.example.lampara.domain.model.Categoria
import com.example.lampara.domain.model.Contenido
import com.example.lampara.domain.model.Modulo
import com.example.lampara.domain.model.Pregunta
import com.example.lampara.domain.model.ResultadoReferencia
import com.example.lampara.domain.usecase.ParserReferencias

/** Preguntas válidas ya convertidas al modelo de dominio y los problemas encontrados en el resto. */
data class InformeValidacion(
    val preguntas: List<Pregunta>,
    val errores: List<String>,
) {
    val esValido: Boolean get() = errores.isEmpty()
}

/**
 * Valida el contenido: ids únicos y bien formados, campos obligatorios según el tipo,
 * respuesta correcta válida, referencias que existen en la tabla de 66 libros y explicación no vacía.
 */
object ValidadorContenido {

    private val formatoId = Regex("""^(core|confesion1689)-[a-z_]+-\d{4}$""")

    fun validar(dtos: List<PreguntaDto>): InformeValidacion {
        val errores = mutableListOf<String>()
        val preguntas = mutableListOf<Pregunta>()

        dtos.groupBy { it.id }.filterValues { it.size > 1 }.keys.forEach { id ->
            errores += "$id: id duplicado"
        }

        val vistos = mutableSetOf<String>()
        dtos.forEach { dto ->
            if (!vistos.add(dto.id)) return@forEach // el duplicado ya se informó
            val problemas = validarUna(dto)
            if (problemas.isEmpty()) {
                preguntas += convertir(dto)
            } else {
                errores += problemas.map { "${dto.id}: $it" }
            }
        }
        return InformeValidacion(preguntas, errores)
    }

    private fun validarUna(dto: PreguntaDto): List<String> {
        val e = mutableListOf<String>()

        if (!formatoId.matches(dto.id)) e += "id con formato inválido (esperado modulo-categoria-0001)"
        val modulo = Modulo.desdeId(dto.module)
        val categoria = Categoria.desdeId(dto.category)
        if (modulo == null) e += "módulo desconocido \"${dto.module}\""
        if (categoria == null) e += "categoría desconocida \"${dto.category}\""
        if (modulo != null && categoria != null && categoria.modulo != modulo) {
            e += "la categoría \"${dto.category}\" no pertenece al módulo \"${dto.module}\""
        }
        if (modulo != null && !dto.id.startsWith("${modulo.id}-")) e += "el id no empieza por el módulo"
        if (categoria != null && !dto.id.contains("-${categoria.id}-")) e += "el id no contiene la categoría"
        if (dto.difficulty !in 1..5) e += "dificultad ${dto.difficulty} fuera de 1..5"
        if (dto.question.isBlank()) e += "enunciado vacío"
        if (dto.explanation.isBlank()) e += "explicación vacía"
        if (dto.tags.any { it.isBlank() }) e += "etiqueta vacía"

        if (dto.references.isEmpty()) e += "sin referencias bíblicas"
        dto.references.forEach { texto ->
            val resultado = ParserReferencias.analizar(texto)
            if (resultado is ResultadoReferencia.Invalida) e += "referencia \"$texto\": ${resultado.motivo}"
        }

        if (modulo == Modulo.CONFESION_1689 && dto.confessionRef.isNullOrBlank()) {
            e += "falta confessionRef (módulo 1689)"
        }
        if (modulo == Modulo.CORE && dto.confessionRef != null) e += "confessionRef solo aplica al módulo 1689"

        when (dto.type) {
            PreguntaDto.TIPO_OPCION_MULTIPLE -> {
                val opciones = dto.options
                if (opciones == null || opciones.size !in 3..6) {
                    e += "multiple_choice requiere entre 3 y 6 opciones"
                } else {
                    if (opciones.any { it.isBlank() }) e += "opción vacía"
                    if (opciones.toSet().size != opciones.size) e += "opciones repetidas"
                    val indice = dto.answerIndex
                    if (indice == null || indice !in opciones.indices) e += "answerIndex inválido"
                }
                if (dto.answer != null || dto.items != null) e += "campos de otro tipo de pregunta"
            }
            PreguntaDto.TIPO_VERDADERO_FALSO -> {
                if (dto.answer == null) e += "true_false requiere answer"
                if (dto.options != null || dto.answerIndex != null || dto.items != null) {
                    e += "campos de otro tipo de pregunta"
                }
            }
            PreguntaDto.TIPO_ORDENAR -> {
                val items = dto.items
                if (items == null || items.size !in 3..8) {
                    e += "ordering requiere entre 3 y 8 items"
                } else {
                    if (items.any { it.isBlank() }) e += "item vacío"
                    if (items.toSet().size != items.size) e += "items repetidos"
                }
                if (dto.options != null || dto.answerIndex != null || dto.answer != null) {
                    e += "campos de otro tipo de pregunta"
                }
            }
            else -> e += "tipo desconocido \"${dto.type}\""
        }
        return e
    }

    /** Solo se llama con un [dto] que ya pasó [validarUna]. */
    private fun convertir(dto: PreguntaDto): Pregunta {
        val contenido = when (dto.type) {
            PreguntaDto.TIPO_OPCION_MULTIPLE -> Contenido.OpcionMultiple(dto.options!!, dto.answerIndex!!)
            PreguntaDto.TIPO_VERDADERO_FALSO -> Contenido.VerdaderoFalso(dto.answer!!)
            else -> Contenido.Ordenar(dto.items!!)
        }
        return Pregunta(
            id = dto.id,
            modulo = Modulo.desdeId(dto.module)!!,
            categoria = Categoria.desdeId(dto.category)!!,
            dificultad = dto.difficulty,
            enunciado = dto.question,
            contenido = contenido,
            referencias = dto.references,
            explicacion = dto.explanation,
            etiquetas = dto.tags,
            revisada = dto.reviewed,
            confesionRef = dto.confessionRef,
        )
    }
}
