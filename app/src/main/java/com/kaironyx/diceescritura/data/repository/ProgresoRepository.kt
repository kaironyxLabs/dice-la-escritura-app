package com.kaironyx.diceescritura.data.repository

import androidx.room.withTransaction
import com.kaironyx.diceescritura.data.db.DiceEscrituraDatabase
import com.kaironyx.diceescritura.data.db.AvanceEntity
import com.kaironyx.diceescritura.data.db.ProgresoPreguntaEntity
import com.kaironyx.diceescritura.data.db.SesionEntity
import com.kaironyx.diceescritura.data.db.TipoSesion
import com.kaironyx.diceescritura.domain.model.Categoria
import com.kaironyx.diceescritura.domain.model.Perfil
import com.kaironyx.diceescritura.domain.usecase.ConteoPregunta
import com.kaironyx.diceescritura.domain.usecase.GestorNiveles
import com.kaironyx.diceescritura.domain.usecase.Leitner
import com.kaironyx.diceescritura.domain.usecase.SeguimientoPregunta
import com.kaironyx.diceescritura.domain.usecase.SesionResumida
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
    private val db: DiceEscrituraDatabase,
    private val gestor: GestorNiveles,
    private val clock: Clock,
    private val activo: PerfilActivo,
) {
    private val avanceDao = db.avanceDao()
    private val progresoDao = db.progresoPreguntaDao()
    private val sesionDao = db.sesionDao()

    fun hoy(): LocalDate = LocalDate.now(clock)

    fun perfil(): Flow<Perfil> = activo.observar { avanceDao.observar(it) }.map { it?.let { e -> Perfil(e.nivel, e.xp) } ?: Perfil.INICIAL }

    suspend fun perfilActual(): Perfil = avanceDao.obtener(activo.actual)?.let { Perfil(it.nivel, it.xp) } ?: Perfil.INICIAL

    /** Aciertos y fallos acumulados por pregunta (para las estadísticas). */
    fun observarConteos(): Flow<Map<String, ConteoPregunta>> = activo.observar { progresoDao.observarTodos(it) }.map { filas ->
        filas.associate { it.preguntaId to ConteoPregunta(it.aciertos, it.fallos) }
    }

    /** Sesiones de los últimos [dias] días, con su categoría si fueron de una sola. */
    fun observarSesiones(dias: Int = 30): Flow<List<SesionResumida>> =
        activo.observar { sesionDao.desde(it, hoy().minusDays((dias - 1).toLong()).toEpochDay()) }.map { filas ->
            filas.map {
                SesionResumida(LocalDate.ofEpochDay(it.fechaDia), it.aciertos, it.total, it.categoria?.let(Categoria::desdeId))
            }
        }

    suspend fun seguimiento(): Map<String, SeguimientoPregunta> = progresoDao.todos(activo.actual).associate {
        it.preguntaId to SeguimientoPregunta(it.caja, LocalDate.ofEpochDay(it.proximaFechaDia))
    }

    /**
     * Registra una respuesta al instante (así no se pierde nada si el usuario sale a mitad de partida):
     * actualiza la caja Leitner de la pregunta y suma el XP ganado respetando el tope del nivel.
     * Devuelve el XP que de verdad entró en la barra (menos que [xp] si el tope lo recortó).
     */
    suspend fun registrarRespuesta(preguntaId: String, acerto: Boolean, xp: Int): Int = db.withTransaction {
        val hoy = hoy()
        val previo = progresoDao.obtener(activo.actual, preguntaId)
        val estado = Leitner.responder(previo?.caja, acerto, hoy)
        progresoDao.guardar(
            ProgresoPreguntaEntity(
                perfilId = activo.actual,
                preguntaId = preguntaId,
                caja = estado.caja,
                proximaFechaDia = estado.proximaFecha.toEpochDay(),
                aciertos = (previo?.aciertos ?: 0) + if (acerto) 1 else 0,
                fallos = (previo?.fallos ?: 0) + if (acerto) 0 else 1,
                ultimaFechaDia = hoy.toEpochDay(),
            ),
        )
        if (xp > 0) {
            val antes = perfilActual()
            val despues = gestor.sumarXp(antes, xp)
            guardarPerfil(despues)
            despues.xp - antes.xp
        } else {
            0
        }
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
                perfilId = activo.actual,
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
        avanceDao.guardar(AvanceEntity(perfilId = activo.actual, nivel = perfil.nivel, xp = perfil.xp))
    }
}
