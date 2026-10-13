package com.example.lampara.data.repository

import androidx.room.withTransaction
import com.example.lampara.data.db.LamparaDatabase
import com.example.lampara.data.db.PerfilEntity
import com.example.lampara.data.db.ProgresoPreguntaEntity
import com.example.lampara.data.db.SesionEntity
import com.example.lampara.data.db.TipoSesion
import com.example.lampara.domain.model.Categoria
import com.example.lampara.domain.model.Perfil
import com.example.lampara.domain.usecase.GestorNiveles
import com.example.lampara.domain.usecase.Leitner
import com.example.lampara.domain.usecase.SeguimientoPregunta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** Datos de una sesión terminada, tal como los entrega el juego. */
data class SesionTerminada(
    val tipo: TipoSesion,
    val categoria: Categoria?,
    val aciertos: Int,
    val total: Int,
    val xpGanado: Int,
    val aprobado: Boolean? = null,
)

/** Progreso del jugador: perfil, seguimiento Leitner por pregunta y sesiones. */
@Singleton
class ProgresoRepository @Inject constructor(
    private val db: LamparaDatabase,
    private val gestor: GestorNiveles,
    private val clock: Clock,
) {
    private val perfilDao = db.perfilDao()
    private val progresoDao = db.progresoPreguntaDao()
    private val sesionDao = db.sesionDao()

    fun hoy(): LocalDate = LocalDate.now(clock)

    fun perfil(): Flow<Perfil> = perfilDao.observar().map { it?.let { e -> Perfil(e.nivel, e.xp) } ?: Perfil.INICIAL }

    suspend fun perfilActual(): Perfil = perfilDao.obtener()?.let { Perfil(it.nivel, it.xp) } ?: Perfil.INICIAL

    suspend fun seguimiento(): Map<String, SeguimientoPregunta> = progresoDao.todos().associate {
        it.preguntaId to SeguimientoPregunta(it.caja, LocalDate.ofEpochDay(it.proximaFechaDia))
    }

    /**
     * Registra una respuesta al instante (así no se pierde nada si el usuario sale a mitad de partida):
     * actualiza la caja Leitner de la pregunta y suma el XP ganado respetando el tope del nivel.
     */
    suspend fun registrarRespuesta(preguntaId: String, acerto: Boolean, xp: Int) = db.withTransaction {
        val hoy = hoy()
        val previo = progresoDao.obtener(preguntaId)
        val estado = Leitner.responder(previo?.caja, acerto, hoy)
        progresoDao.guardar(
            ProgresoPreguntaEntity(
                preguntaId = preguntaId,
                caja = estado.caja,
                proximaFechaDia = estado.proximaFecha.toEpochDay(),
                aciertos = (previo?.aciertos ?: 0) + if (acerto) 1 else 0,
                fallos = (previo?.fallos ?: 0) + if (acerto) 0 else 1,
                ultimaFechaDia = hoy.toEpochDay(),
            ),
        )
        if (xp > 0) guardarPerfil(gestor.sumarXp(perfilActual(), xp))
    }

    /** Sube de nivel si la barra está llena. Devuelve el perfil resultante. */
    suspend fun ascender(): Perfil = db.withTransaction {
        val nuevo = gestor.ascender(perfilActual())
        guardarPerfil(nuevo)
        nuevo
    }

    suspend fun guardarSesion(sesion: SesionTerminada) {
        sesionDao.insertar(
            SesionEntity(
                tipo = sesion.tipo.id,
                categoria = sesion.categoria?.id,
                fechaDia = hoy().toEpochDay(),
                creadaEnMs = clock.millis(),
                aciertos = sesion.aciertos,
                total = sesion.total,
                xpGanado = sesion.xpGanado,
                aprobado = sesion.aprobado,
            ),
        )
    }

    private suspend fun guardarPerfil(perfil: Perfil) {
        perfilDao.guardar(PerfilEntity(nivel = perfil.nivel, xp = perfil.xp))
    }
}
