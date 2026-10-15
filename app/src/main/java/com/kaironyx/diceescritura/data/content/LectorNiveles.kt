package com.kaironyx.diceescritura.data.content

import com.kaironyx.diceescritura.domain.model.Categoria
import com.kaironyx.diceescritura.domain.model.ConfigNiveles
import com.kaironyx.diceescritura.domain.model.Nivel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class NivelDto(
    val numero: Int,
    val nombre: String,
    val umbralXp: Int,
    val dificultadMaxima: Int,
    val categorias: List<String>,
)

@Serializable
private data class ConfigNivelesDto(
    val xpPorDificultad: List<Int>,
    val bonusRachaDesde: Int,
    val bonusRachaMaximo: Int,
    val examenPreguntas: Int,
    val examenAprobacionPorcentaje: Int,
    val niveles: List<NivelDto>,
)

/** Lee assets/content/levels.json. Lanza [IllegalArgumentException] si el archivo es inválido. */
object LectorNiveles {
    const val ARCHIVO = "levels.json"

    private val json = Json { ignoreUnknownKeys = false }

    fun leer(texto: String): ConfigNiveles {
        val dto = json.decodeFromString<ConfigNivelesDto>(texto)
        return ConfigNiveles(
            niveles = dto.niveles.map { nivel ->
                Nivel(
                    numero = nivel.numero,
                    nombre = nivel.nombre,
                    umbralXp = nivel.umbralXp,
                    dificultadMaxima = nivel.dificultadMaxima,
                    categorias = nivel.categorias.map {
                        requireNotNull(Categoria.desdeId(it)) { "Categoría desconocida \"$it\" en levels.json" }
                    }.toSet(),
                )
            },
            xpPorDificultad = dto.xpPorDificultad,
            bonusRachaDesde = dto.bonusRachaDesde,
            bonusRachaMaximo = dto.bonusRachaMaximo,
            examenPreguntas = dto.examenPreguntas,
            examenAprobacionPorcentaje = dto.examenAprobacionPorcentaje,
        )
    }
}
