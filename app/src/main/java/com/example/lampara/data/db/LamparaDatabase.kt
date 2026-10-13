package com.example.lampara.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Base de datos del progreso. El esquema se exporta a app/schemas.
 * Mientras la app no se haya instalado en ningún teléfono, el esquema v1 puede seguir cambiando;
 * desde la primera instalación, cada cambio exige subir la versión y escribir su migración.
 */
@Database(
    entities = [
        PerfilEntity::class,
        ProgresoPreguntaEntity::class,
        SesionEntity::class,
        RetoDiarioEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class LamparaDatabase : RoomDatabase() {
    abstract fun perfilDao(): PerfilDao
    abstract fun progresoPreguntaDao(): ProgresoPreguntaDao
    abstract fun retoDiarioDao(): RetoDiarioDao
    abstract fun sesionDao(): SesionDao

    companion object {
        const val NOMBRE = "lampara.db"
    }
}
