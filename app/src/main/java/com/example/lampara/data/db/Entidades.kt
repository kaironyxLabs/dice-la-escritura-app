package com.example.lampara.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Fila única (id = 1) con el nivel y el XP total. */
@Entity(tableName = "perfil")
data class PerfilEntity(
    @PrimaryKey val id: Int = ID_UNICO,
    val nivel: Int,
    val xp: Int,
) {
    companion object {
        const val ID_UNICO = 1
    }
}

/** Seguimiento Leitner de una pregunta. Las fechas son días desde la época (LocalDate.toEpochDay). */
@Entity(tableName = "progreso_pregunta")
data class ProgresoPreguntaEntity(
    @PrimaryKey val preguntaId: String,
    val caja: Int,
    val proximaFechaDia: Long,
    val aciertos: Int,
    val fallos: Int,
    val ultimaFechaDia: Long,
)

/** Una partida, un examen de ascenso o (H3) un reto diario terminados. */
@Entity(tableName = "sesion")
data class SesionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
@Entity(tableName = "reto_diario")
data class RetoDiarioEntity(
    @PrimaryKey val fechaDia: Long,
    /** Ids de las preguntas separados por coma, en el orden en que se presentan. */
    val preguntaIds: String,
    val respondidas: Int,
    val aciertos: Int,
    val xpGanado: Int,
    val completado: Boolean,
)

enum class TipoSesion(val id: String) {
    PARTIDA("partida"),
    EXAMEN("examen"),
    RETO_DIARIO("reto_diario"),
}
