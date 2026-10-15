package com.kaironyx.diceescritura.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PerfilUsuarioDao {
    @Query("SELECT * FROM perfil_usuario WHERE id = :id")
    fun observar(id: Long): Flow<PerfilUsuarioEntity?>

    @Query("SELECT * FROM perfil_usuario WHERE id = :id")
    suspend fun obtener(id: Long): PerfilUsuarioEntity?

    @Query("SELECT * FROM perfil_usuario ORDER BY id")
    fun observarTodos(): Flow<List<PerfilUsuarioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(perfil: PerfilUsuarioEntity): Long

    @Query("DELETE FROM perfil_usuario WHERE id = :id")
    suspend fun eliminar(id: Long)
}

@Dao
interface AvanceDao {
    @Query("SELECT * FROM avance WHERE perfilId = :perfilId")
    fun observar(perfilId: Long): Flow<AvanceEntity?>

    @Query("SELECT * FROM avance WHERE perfilId = :perfilId")
    suspend fun obtener(perfilId: Long): AvanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(avance: AvanceEntity)

    @Query("DELETE FROM avance WHERE perfilId = :perfilId")
    suspend fun borrar(perfilId: Long)
}

@Dao
interface ProgresoPreguntaDao {
    @Query("SELECT * FROM progreso_pregunta WHERE perfilId = :perfilId")
    suspend fun todos(perfilId: Long): List<ProgresoPreguntaEntity>

    @Query("SELECT * FROM progreso_pregunta WHERE perfilId = :perfilId")
    fun observarTodos(perfilId: Long): Flow<List<ProgresoPreguntaEntity>>

    @Query("SELECT * FROM progreso_pregunta WHERE perfilId = :perfilId AND preguntaId = :id")
    suspend fun obtener(perfilId: Long, id: String): ProgresoPreguntaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(progreso: ProgresoPreguntaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodos(progreso: List<ProgresoPreguntaEntity>)

    @Query("DELETE FROM progreso_pregunta WHERE perfilId = :perfilId")
    suspend fun borrarTodos(perfilId: Long)
}

@Dao
interface RetoDiarioDao {
    @Query("SELECT * FROM reto_diario WHERE perfilId = :perfilId AND fechaDia = :fechaDia")
    suspend fun obtener(perfilId: Long, fechaDia: Long): RetoDiarioEntity?

    @Query("SELECT * FROM reto_diario WHERE perfilId = :perfilId AND fechaDia = :fechaDia")
    fun observar(perfilId: Long, fechaDia: Long): Flow<RetoDiarioEntity?>

    @Query("SELECT fechaDia FROM reto_diario WHERE perfilId = :perfilId AND completado = 1 ORDER BY fechaDia")
    fun observarFechasCompletadas(perfilId: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(reto: RetoDiarioEntity)

    @Query("SELECT * FROM reto_diario WHERE perfilId = :perfilId ORDER BY fechaDia")
    suspend fun todos(perfilId: Long): List<RetoDiarioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodos(retos: List<RetoDiarioEntity>)

    @Query("DELETE FROM reto_diario WHERE perfilId = :perfilId")
    suspend fun borrarTodos(perfilId: Long)
}

@Dao
interface ProgresoVersiculoDao {
    @Query("SELECT * FROM progreso_versiculo WHERE perfilId = :perfilId")
    fun observarTodos(perfilId: Long): Flow<List<ProgresoVersiculoEntity>>

    @Query("SELECT * FROM progreso_versiculo WHERE perfilId = :perfilId AND pasajeId = :id")
    suspend fun obtener(perfilId: Long, id: String): ProgresoVersiculoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(progreso: ProgresoVersiculoEntity)

    @Query("SELECT * FROM progreso_versiculo WHERE perfilId = :perfilId ORDER BY pasajeId")
    suspend fun todos(perfilId: Long): List<ProgresoVersiculoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodos(progreso: List<ProgresoVersiculoEntity>)

    @Query("DELETE FROM progreso_versiculo WHERE perfilId = :perfilId")
    suspend fun borrarTodos(perfilId: Long)
}

@Dao
interface SesionDao {
    @Insert
    suspend fun insertar(sesion: SesionEntity): Long

    @Query("SELECT * FROM sesion WHERE perfilId = :perfilId AND fechaDia >= :desdeDia ORDER BY creadaEnMs DESC")
    fun desde(perfilId: Long, desdeDia: Long): Flow<List<SesionEntity>>

    @Query("SELECT * FROM sesion WHERE perfilId = :perfilId ORDER BY id")
    suspend fun todas(perfilId: Long): List<SesionEntity>

    @Insert
    suspend fun insertarTodas(sesiones: List<SesionEntity>)

    @Query("DELETE FROM sesion WHERE perfilId = :perfilId")
    suspend fun borrarTodas(perfilId: Long)
}
