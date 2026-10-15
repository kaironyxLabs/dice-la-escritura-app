package com.kaironyx.diceescritura.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Base de datos del progreso. El esquema se exporta a app/schemas.
 * Desde la versión 2 cada cambio sube la versión y lleva su migración (ver [MIGRACION_1_2]).
 */
@Database(
    entities = [
        PerfilUsuarioEntity::class,
        AvanceEntity::class,
        ProgresoPreguntaEntity::class,
        SesionEntity::class,
        RetoDiarioEntity::class,
        ProgresoVersiculoEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class DiceEscrituraDatabase : RoomDatabase() {
    abstract fun perfilUsuarioDao(): PerfilUsuarioDao
    abstract fun avanceDao(): AvanceDao
    abstract fun progresoPreguntaDao(): ProgresoPreguntaDao
    abstract fun retoDiarioDao(): RetoDiarioDao
    abstract fun progresoVersiculoDao(): ProgresoVersiculoDao
    abstract fun sesionDao(): SesionDao

    companion object {
        const val NOMBRE = "lampara.db"
    }
}
