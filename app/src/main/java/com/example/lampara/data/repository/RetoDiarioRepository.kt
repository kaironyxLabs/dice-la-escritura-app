package com.example.lampara.data.repository

import androidx.room.withTransaction
import com.example.lampara.data.db.LamparaDatabase
import com.example.lampara.data.db.RetoDiarioEntity
import com.example.lampara.domain.usecase.CalculadoraRacha
import com.example.lampara.domain.usecase.Racha
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** Reto diario de una fecha. [preguntaIds] van en el orden de presentación. */
data class RetoDiario(
    val fecha: LocalDate,
    val preguntaIds: List<String>,
    val respondidas: Int,
    val aciertos: Int,
    val xpGanado: Int,
    val completado: Boolean,
)

/** Reto diario y racha. El día cambia a medianoche local, según el [Clock] inyectado. */
@Singleton
class RetoDiarioRepository @Inject constructor(
    private val db: LamparaDatabase,
    private val clock: Clock,
    private val activo: PerfilActivo,
) {
    private val dao = db.retoDiarioDao()

    private fun hoy(): LocalDate = LocalDate.now(clock)

    suspend fun retoDeHoy(): RetoDiario? = dao.obtener(activo.actual, hoy().toEpochDay())?.aDominio()

    fun observarRetoDeHoy(): Flow<RetoDiario?> = activo.observar { dao.observar(it, hoy().toEpochDay()) }.map { it?.aDominio() }

    /** Crea el reto de hoy con estas preguntas. Si ya existía uno, lo conserva y lo devuelve. */
    suspend fun crearRetoDeHoy(preguntaIds: List<String>): RetoDiario = db.withTransaction {
        retoDeHoy() ?: RetoDiario(hoy(), preguntaIds, 0, 0, 0, false).also { dao.guardar(it.aEntidad()) }
    }

    /**
     * [fecha] es el día del reto que se está jugando: si la partida cruza la medianoche, las respuestas
     * y la finalización siguen contando para el reto que se empezó, no para el día nuevo.
     */
    suspend fun registrarRespuesta(acerto: Boolean, xp: Int, fecha: LocalDate = hoy()) = db.withTransaction {
        val actual = dao.obtener(activo.actual, fecha.toEpochDay())?.aDominio() ?: return@withTransaction
        dao.guardar(
            actual.copy(
                respondidas = actual.respondidas + 1,
                aciertos = actual.aciertos + if (acerto) 1 else 0,
                xpGanado = actual.xpGanado + xp,
            ).aEntidad(),
        )
    }

    suspend fun completar(fecha: LocalDate = hoy()) = db.withTransaction {
        val actual = dao.obtener(activo.actual, fecha.toEpochDay())?.aDominio() ?: return@withTransaction
        dao.guardar(actual.copy(completado = true).aEntidad())
    }

    /** Racha vigente calculada con los retos completados y la fecha de hoy. */
    fun observarRacha(): Flow<Racha> = activo.observar { dao.observarFechasCompletadas(it) }.map { dias ->
        CalculadoraRacha.calcular(dias.map { LocalDate.ofEpochDay(it) }, hoy())
    }

    fun observarFechasCompletadas(): Flow<List<LocalDate>> =
        activo.observar { dao.observarFechasCompletadas(it) }.map { dias -> dias.map { LocalDate.ofEpochDay(it) } }

    /** Racha y reto de hoy juntos, para la pantalla de inicio. */
    fun observarEstado(): Flow<Pair<Racha, RetoDiario?>> =
        combine(observarRacha(), observarRetoDeHoy()) { racha, reto -> racha to reto }

    private fun RetoDiarioEntity.aDominio() = RetoDiario(
        fecha = LocalDate.ofEpochDay(fechaDia),
        preguntaIds = if (preguntaIds.isEmpty()) emptyList() else preguntaIds.split(','),
        respondidas = respondidas,
        aciertos = aciertos,
        xpGanado = xpGanado,
        completado = completado,
    )

    private fun RetoDiario.aEntidad() = RetoDiarioEntity(
        perfilId = activo.actual,
        fechaDia = fecha.toEpochDay(),
        preguntaIds = preguntaIds.joinToString(","),
        respondidas = respondidas,
        aciertos = aciertos,
        xpGanado = xpGanado,
        completado = completado,
    )
}
