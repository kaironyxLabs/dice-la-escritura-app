package com.kaironyx.diceescritura.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Perfil local de una persona (sin login). Todo el avance cuelga de su [id]. */
@Entity(tableName = "perfil_usuario")
data class PerfilUsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    /** Id del avatar elegido (ver C2); vacío hasta que la persona lo escoja. */
    val avatar: String,
    val creadoEnMs: Long,
) {
    companion object {
        /** Perfil que existe desde el primer arranque y que recibe los datos de la versión 1. */
        const val ID_INICIAL = 1L
        const val NOMBRE_INICIAL = "Estudiante"
    }
}

/** Nivel y XP total de un perfil. */
@Entity(
    tableName = "avance",
    foreignKeys = [ForeignKey(PerfilUsuarioEntity::class, ["id"], ["perfilId"], onDelete = ForeignKey.CASCADE)],
)
data class AvanceEntity(
    @PrimaryKey val perfilId: Long,
    val nivel: Int,
    val xp: Int,
)

/** Seguimiento Leitner de una pregunta. Las fechas son días desde la época (LocalDate.toEpochDay). */
@Entity(
    tableName = "progreso_pregunta",
    primaryKeys = ["perfilId", "preguntaId"],
    foreignKeys = [ForeignKey(PerfilUsuarioEntity::class, ["id"], ["perfilId"], onDelete = ForeignKey.CASCADE)],
)
data class ProgresoPreguntaEntity(
    val perfilId: Long,
    val preguntaId: String,
    val caja: Int,
    val proximaFechaDia: Long,
    val aciertos: Int,
    val fallos: Int,
    val ultimaFechaDia: Long,
)

/** Una partida, un examen de ascenso o (H3) un reto diario terminados. */
@Entity(
    tableName = "sesion",
    foreignKeys = [ForeignKey(PerfilUsuarioEntity::class, ["id"], ["perfilId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("perfilId")],
)
data class SesionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val perfilId: Long,
    /** Ver [TipoSesion]. */
    val tipo: String,
    val categoria: String?,
    val fechaDia: Long,
    val creadaEnMs: Long,
    val aciertos: Int,
    val total: Int,
    val xpGanado: Int,
    /** Solo para exámenes: true si se aprobó. */
    val aprobado: Boolean?,
)

/** Reto diario de una fecha. Las preguntas quedan fijadas al crearlo, así que reabrirlo no cambia nada. */
@Entity(
    tableName = "reto_diario",
    primaryKeys = ["perfilId", "fechaDia"],
    foreignKeys = [ForeignKey(PerfilUsuarioEntity::class, ["id"], ["perfilId"], onDelete = ForeignKey.CASCADE)],
)
data class RetoDiarioEntity(
    val perfilId: Long,
    val fechaDia: Long,
    /** Ids de las preguntas separados por coma, en el orden en que se presentan. */
    val preguntaIds: String,
    val respondidas: Int,
    val aciertos: Int,
    val xpGanado: Int,
    val completado: Boolean,
)

/** Progreso de memorización de un pasaje del modo memorizar. Las fechas son días desde la época. */
@Entity(
    tableName = "progreso_versiculo",
    primaryKeys = ["perfilId", "pasajeId"],
    foreignKeys = [ForeignKey(PerfilUsuarioEntity::class, ["id"], ["perfilId"], onDelete = ForeignKey.CASCADE)],
)
data class ProgresoVersiculoEntity(
    val perfilId: Long,
    val pasajeId: String,
    /** El usuario lo marcó como "lo estoy memorizando". */
    val memorizando: Boolean,
    /** Grado actual: 0 = 20 %, 1 = 40 %, 2 = 60 %, 3 = escribir. */
    val grado: Int,
    val dominio: Int,
    val proximaFechaDia: Long,
    val intentos: Int,
    val aciertos: Int,
)

enum class TipoSesion(val id: String) {
    PARTIDA("partida"),
    EXAMEN("examen"),
    RETO_DIARIO("reto_diario"),
}
