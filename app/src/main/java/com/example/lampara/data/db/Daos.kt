package com.example.lampara.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PerfilDao {
    @Query("SELECT * FROM perfil WHERE id = 1")
    fun observar(): Flow<PerfilEntity?>

    @Query("SELECT * FROM perfil WHERE id = 1")
    suspend fun obtener(): PerfilEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(perfil: PerfilEntity)
}

@Dao
interface ProgresoPreguntaDao {
    @Query("SELECT * FROM progreso_pregunta")
    suspend fun todos(): List<ProgresoPreguntaEntity>

    @Query("SELECT * FROM progreso_pregunta")
    fun observarTodos(): Flow<List<ProgresoPreguntaEntity>>

    @Query("SELECT * FROM progreso_pregunta WHERE preguntaId = :id")
    suspend fun obtener(id: String): ProgresoPreguntaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(progreso: ProgresoPreguntaEntity)
}

@Dao
interface SesionDao {
    @Insert
    suspend fun insertar(sesion: SesionEntity): Long

    @Query("SELECT * FROM sesion WHERE fechaDia >= :desdeDia ORDER BY creadaEnMs DESC")
    fun desde(desdeDia: Long): Flow<List<SesionEntity>>
}
