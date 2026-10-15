package com.example.lampara.data.export

import com.example.lampara.data.db.TipoSesion
import com.example.lampara.domain.model.Categoria
import com.example.lampara.domain.usecase.NombrePerfil
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Copia del progreso del jugador en un archivo JSON, para cambiar de teléfono o hacer respaldo. */
@Serializable
data class ProgresoExportado(
    val formato: String = FORMATO,
    val version: Int = VERSION,
    /** Instante de la exportación (ISO-8601). Solo informativo. */
    val exportadoEn: String,
    /** Quién es la persona (versión 2). Los archivos de la versión 1 no lo traen. */
    val usuario: UsuarioExp? = null,
    val perfil: PerfilExp,
    val preguntas: List<PreguntaExp> = emptyList(),
    val sesiones: List<SesionExp> = emptyList(),
    val retos: List<RetoExp> = emptyList(),
    val versiculos: List<VersiculoExp> = emptyList(),
    val ajustes: AjustesExp,
) {
    companion object {
        const val FORMATO = "lampara-progreso"
        const val VERSION = 2

        /** Versión más antigua que todavía se sabe leer. */
        const val VERSION_MINIMA = 1
    }
}

/** Nombre, avatar y fecha de creación del perfil local. */
@Serializable
data class UsuarioExp(val nombre: String, val avatar: String, val creadoEnMs: Long)

/** Nivel y XP. */
@Serializable
data class PerfilExp(val nivel: Int, val xp: Int)

/** Seguimiento Leitner de una pregunta (las fechas son días desde la época). */
@Serializable
data class PreguntaExp(
    val preguntaId: String,
    val caja: Int,
    val proximaFechaDia: Long,
    val aciertos: Int,
    val fallos: Int,
    val ultimaFechaDia: Long,
)

@Serializable
data class SesionExp(
    val tipo: String,
    val categoria: String? = null,
    val fechaDia: Long,
    val creadaEnMs: Long,
    val aciertos: Int,
    val total: Int,
    val xpGanado: Int,
    val aprobado: Boolean? = null,
)

@Serializable
data class RetoExp(
    val fechaDia: Long,
    val preguntaIds: List<String>,
    val respondidas: Int,
    val aciertos: Int,
    val xpGanado: Int,
    val completado: Boolean,
)

@Serializable
data class VersiculoExp(
    val pasajeId: String,
    val memorizando: Boolean,
    val grado: Int,
    val dominio: Int,
    val proximaFechaDia: Long,
    val intentos: Int,
    val aciertos: Int,
)

/** Ajustes que viajan con el progreso. La bienvenida no: en el teléfono nuevo se vuelve a ver. */
@Serializable
data class AjustesExp(
    val confesionActiva: Boolean,
    val soloRevisadas: Boolean,
    val recordatorioActivo: Boolean,
    val recordatorioMinutos: Int,
)

sealed interface LecturaProgreso {
    data class Valido(val progreso: ProgresoExportado) : LecturaProgreso

    data class Invalido(val errores: List<String>) : LecturaProgreso
}

object CodecProgreso {
    private val json = Json {
        ignoreUnknownKeys = false
        encodeDefaults = true
    }

    fun aJson(progreso: ProgresoExportado): String = json.encodeToString(ProgresoExportado.serializer(), progreso)

    /** Lee y valida. Un archivo inválido nunca debe tocar el progreso actual. */
    fun leer(texto: String): LecturaProgreso {
        val progreso = try {
            json.decodeFromString(ProgresoExportado.serializer(), texto)
        } catch (e: Exception) {
            return LecturaProgreso.Invalido(listOf("No es un archivo de progreso de Lámpara válido."))
        }
        val errores = validar(progreso)
        return if (errores.isEmpty()) LecturaProgreso.Valido(progreso) else LecturaProgreso.Invalido(errores)
    }

    fun validar(p: ProgresoExportado): List<String> {
        val e = mutableListOf<String>()
        if (p.formato != ProgresoExportado.FORMATO) e += "Formato desconocido: \"${p.formato}\"."
        if (p.version !in ProgresoExportado.VERSION_MINIMA..ProgresoExportado.VERSION) {
            e += "Versión ${p.version} no compatible (esta app lee de la ${ProgresoExportado.VERSION_MINIMA} a la ${ProgresoExportado.VERSION})."
        }
        if (p.version < 2 && p.usuario != null) e += "Un archivo de la versión 1 no puede traer usuario."
        p.usuario?.let {
            if (NombrePerfil.normalizar(it.nombre) == null || it.nombre.length > NombrePerfil.MAX || it.creadoEnMs < 0) {
                e += "Nombre de usuario inválido."
            }
            if (NombrePerfil.avatarValido(it.avatar) != it.avatar) e += "Avatar desconocido: \"${it.avatar}\"."
        }
        if (p.perfil.nivel < 1 || p.perfil.xp < 0) e += "Perfil inválido (nivel ${p.perfil.nivel}, XP ${p.perfil.xp})."
        if (p.preguntas.map { it.preguntaId }.toSet().size != p.preguntas.size) e += "Hay preguntas repetidas."
        p.preguntas.forEach {
            if (it.preguntaId.isBlank() || it.caja !in 1..5 || it.aciertos < 0 || it.fallos < 0) {
                e += "Seguimiento inválido en la pregunta \"${it.preguntaId}\"."
            }
        }
        val tiposValidos = TipoSesion.entries.map { it.id }.toSet()
        p.sesiones.forEachIndexed { i, s ->
            val categoriaValida = s.categoria == null || Categoria.desdeId(s.categoria) != null
            if (s.tipo !in tiposValidos || !categoriaValida || s.total < 0 || s.aciertos !in 0..s.total || s.xpGanado < 0) {
                e += "Sesión ${i + 1} inválida."
            }
        }
        if (p.retos.map { it.fechaDia }.toSet().size != p.retos.size) e += "Hay retos diarios repetidos."
        p.retos.forEach {
            val idsValidos = it.preguntaIds.none { id -> id.isBlank() || ',' in id }
            if (it.respondidas !in 0..it.preguntaIds.size || it.aciertos !in 0..it.respondidas || it.xpGanado < 0 || !idsValidos) {
                e += "Reto del día ${it.fechaDia} inválido."
            }
        }
        if (p.versiculos.map { it.pasajeId }.toSet().size != p.versiculos.size) e += "Hay pasajes repetidos."
        p.versiculos.forEach {
            if (it.pasajeId.isBlank() || it.grado !in 0..3 || it.dominio < 0 || it.intentos < 0 || it.aciertos !in 0..it.intentos) {
                e += "Progreso inválido en el pasaje \"${it.pasajeId}\"."
            }
        }
        if (p.ajustes.recordatorioMinutos !in 0 until 24 * 60) e += "Hora de recordatorio inválida."
        return e
    }
}
