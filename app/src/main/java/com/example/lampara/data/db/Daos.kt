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

    @Query("DELETE FROM perfil")
    suspend fun borrar()
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodos(progreso: List<ProgresoPreguntaEntity>)

    @Query("DELETE FROM progreso_pregunta")
    suspend fun borrarTodos()
}

@Dao
interface RetoDiarioDao {
    @Query("SELECT * FROM reto_diario WHERE fechaDia = :fechaDia")
    suspend fun obtener(fechaDia: Long): RetoDiarioEntity?

    @Query("SELECT * FROM reto_diario WHERE fechaDia = :fechaDia")
    fun observar(fechaDia: Long): Flow<RetoDiarioEntity?>

    @Query("SELECT fechaDia FROM reto_diario WHERE completado = 1 ORDER BY fechaDia")
    fun observarFechasCompletadas(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(reto: RetoDiarioEntity)

    @Query("SELECT * FROM reto_diario ORDER BY fechaDia")
    suspend fun todos(): List<RetoDiarioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodos(retos: List<RetoDiarioEntity>)

    @Query("DELETE FROM reto_diario")
    suspend fun borrarTodos()
}

@Dao
interface ProgresoVersiculoDao {
    @Query("SELECT * FROM progreso_versiculo")
    fun observarTodos(): Flow<List<ProgresoVersiculoEntity>>

    @Query("SELECT * FROM progreso_versiculo WHERE pasajeId = :id")
    suspend fun obtener(id: String): ProgresoVersiculoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(progreso: ProgresoVersiculoEntity)

    @Query("SELECT * FROM progreso_versiculo ORDER BY pasajeId")
    suspend fun todos(): List<ProgresoVersiculoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodos(progreso: List<ProgresoVersiculoEntity>)

    @Query("DELETE FROM progreso_versiculo")
    suspend fun borrarTodos()
}

@Dao
interface SesionDao {
    @Insert
    suspend fun insertar(sesion: SesionEntity): Long

    @Query("SELECT * FROM sesion WHERE fechaDia >= :desdeDia ORDER BY creadaEnMs DESC")
    fun desde(desdeDia: Long): Flow<List<SesionEntity>>

    @Query("SELECT * FROM sesion ORDER BY id")
    suspend fun todas(): List<SesionEntity>

    @Insert
    suspend fun insertarTodas(sesiones: List<SesionEntity>)

    @Query("DELETE FROM sesion")
    suspend fun borrarTodas()
}
