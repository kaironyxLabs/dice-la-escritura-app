package com.example.lampara.data.db

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Instalación nueva: la base nace ya en la versión 2 y necesita su perfil inicial antes de guardar avance. */
val CREAR_PERFIL_INICIAL = object : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "INSERT INTO `perfil_usuario` (`id`, `nombre`, `avatar`, `creadoEnMs`) VALUES " +
                "(${PerfilUsuarioEntity.ID_INICIAL}, '${PerfilUsuarioEntity.NOMBRE_INICIAL}', '', " +
                "CAST(strftime('%s','now') AS INTEGER) * 1000)",
        )
    }
}

/**
 * Versión 1 → 2: aparece el perfil local y todo el avance pasa a colgar de él.
 * Los datos de la versión 1 se quedan en el perfil [PerfilUsuarioEntity.ID_INICIAL]; no se pierde nada.
 */
val MIGRACION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val fk = "FOREIGN KEY(`perfilId`) REFERENCES `perfil_usuario`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE"
        val id = PerfilUsuarioEntity.ID_INICIAL

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `perfil_usuario` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`nombre` TEXT NOT NULL, `avatar` TEXT NOT NULL, `creadoEnMs` INTEGER NOT NULL)",
        )
        db.execSQL(
            "INSERT INTO `perfil_usuario` (`id`, `nombre`, `avatar`, `creadoEnMs`) VALUES " +
                "($id, '${PerfilUsuarioEntity.NOMBRE_INICIAL}', '', CAST(strftime('%s','now') AS INTEGER) * 1000)",
        )

        // perfil (fila única) -> avance
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `avance` (`perfilId` INTEGER NOT NULL, `nivel` INTEGER NOT NULL, " +
                "`xp` INTEGER NOT NULL, PRIMARY KEY(`perfilId`), $fk)",
        )
        db.execSQL("INSERT INTO `avance` (`perfilId`, `nivel`, `xp`) SELECT $id, `nivel`, `xp` FROM `perfil` LIMIT 1")
        db.execSQL("DROP TABLE `perfil`")

        // progreso_pregunta
        db.execSQL(
            "CREATE TABLE `progreso_pregunta_nueva` (`perfilId` INTEGER NOT NULL, `preguntaId` TEXT NOT NULL, " +
                "`caja` INTEGER NOT NULL, `proximaFechaDia` INTEGER NOT NULL, `aciertos` INTEGER NOT NULL, " +
                "`fallos` INTEGER NOT NULL, `ultimaFechaDia` INTEGER NOT NULL, PRIMARY KEY(`perfilId`, `preguntaId`), $fk)",
        )
        db.execSQL(
            "INSERT INTO `progreso_pregunta_nueva` SELECT $id, `preguntaId`, `caja`, `proximaFechaDia`, " +
                "`aciertos`, `fallos`, `ultimaFechaDia` FROM `progreso_pregunta`",
        )
        db.execSQL("DROP TABLE `progreso_pregunta`")
        db.execSQL("ALTER TABLE `progreso_pregunta_nueva` RENAME TO `progreso_pregunta`")

        // sesion
        db.execSQL(
            "CREATE TABLE `sesion_nueva` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `perfilId` INTEGER NOT NULL, " +
                "`tipo` TEXT NOT NULL, `categoria` TEXT, `fechaDia` INTEGER NOT NULL, `creadaEnMs` INTEGER NOT NULL, " +
                "`aciertos` INTEGER NOT NULL, `total` INTEGER NOT NULL, `xpGanado` INTEGER NOT NULL, `aprobado` INTEGER, $fk)",
        )
        db.execSQL(
            "INSERT INTO `sesion_nueva` SELECT `id`, $id, `tipo`, `categoria`, `fechaDia`, `creadaEnMs`, " +
                "`aciertos`, `total`, `xpGanado`, `aprobado` FROM `sesion`",
        )
        db.execSQL("DROP TABLE `sesion`")
        db.execSQL("ALTER TABLE `sesion_nueva` RENAME TO `sesion`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sesion_perfilId` ON `sesion` (`perfilId`)")

        // reto_diario
        db.execSQL(
            "CREATE TABLE `reto_diario_nueva` (`perfilId` INTEGER NOT NULL, `fechaDia` INTEGER NOT NULL, " +
                "`preguntaIds` TEXT NOT NULL, `respondidas` INTEGER NOT NULL, `aciertos` INTEGER NOT NULL, " +
                "`xpGanado` INTEGER NOT NULL, `completado` INTEGER NOT NULL, PRIMARY KEY(`perfilId`, `fechaDia`), $fk)",
        )
        db.execSQL(
            "INSERT INTO `reto_diario_nueva` SELECT $id, `fechaDia`, `preguntaIds`, `respondidas`, `aciertos`, " +
                "`xpGanado`, `completado` FROM `reto_diario`",
        )
        db.execSQL("DROP TABLE `reto_diario`")
        db.execSQL("ALTER TABLE `reto_diario_nueva` RENAME TO `reto_diario`")

        // progreso_versiculo
        db.execSQL(
            "CREATE TABLE `progreso_versiculo_nueva` (`perfilId` INTEGER NOT NULL, `pasajeId` TEXT NOT NULL, " +
                "`memorizando` INTEGER NOT NULL, `grado` INTEGER NOT NULL, `dominio` INTEGER NOT NULL, " +
                "`proximaFechaDia` INTEGER NOT NULL, `intentos` INTEGER NOT NULL, `aciertos` INTEGER NOT NULL, " +
                "PRIMARY KEY(`perfilId`, `pasajeId`), $fk)",
        )
        db.execSQL(
            "INSERT INTO `progreso_versiculo_nueva` SELECT $id, `pasajeId`, `memorizando`, `grado`, `dominio`, " +
                "`proximaFechaDia`, `intentos`, `aciertos` FROM `progreso_versiculo`",
        )
        db.execSQL("DROP TABLE `progreso_versiculo`")
        db.execSQL("ALTER TABLE `progreso_versiculo_nueva` RENAME TO `progreso_versiculo`")
    }
}
