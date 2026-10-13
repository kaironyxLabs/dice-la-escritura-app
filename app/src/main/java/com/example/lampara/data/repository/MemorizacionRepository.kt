package com.example.lampara.data.repository

import androidx.room.withTransaction
import com.example.lampara.data.db.LamparaDatabase
import com.example.lampara.data.db.ProgresoVersiculoEntity
import com.example.lampara.domain.usecase.EstadoMemoria
import com.example.lampara.domain.usecase.Memorizacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class ProgresoPasaje(
    val memorizando: Boolean,
    val estado: EstadoMemoria,
    val intentos: Int,
    val aciertos: Int,
)

/** Progreso de memorización de los pasajes: cuáles se estudian y su repetición espaciada. */
@Singleton
class MemorizacionRepository @Inject constructor(
    private val db: LamparaDatabase,
    private val clock: Clock,
) {
    private val dao = db.progresoVersiculoDao()

    fun hoy(): LocalDate = LocalDate.now(clock)

    fun observar(): Flow<Map<String, ProgresoPasaje>> = dao.observarTodos().map { filas ->
        filas.associate { it.pasajeId to it.aDominio() }
    }

    suspend fun obtener(pasajeId: String): ProgresoPasaje? = dao.obtener(pasajeId)?.aDominio()

    /** Marca o desmarca un pasaje como "lo estoy memorizando"; conserva el avance si ya lo había practicado. */
    suspend fun establecerMemorizando(pasajeId: String, memorizando: Boolean) = db.withTransaction {
        val actual = dao.obtener(pasajeId)?.aDominio()
        val nuevo = (actual ?: ProgresoPasaje(false, Memorizacion.iniciar(hoy()), 0, 0)).copy(memorizando = memorizando)
        dao.guardar(nuevo.aEntidad(pasajeId))
    }

    /** Registra el resultado de una práctica y reprograma el repaso. */
    suspend fun registrarPractica(pasajeId: String, acierto: Boolean) = db.withTransaction {
        val hoy = hoy()
        val actual = dao.obtener(pasajeId)?.aDominio() ?: ProgresoPasaje(true, Memorizacion.iniciar(hoy), 0, 0)
        val nuevo = actual.copy(
            estado = Memorizacion.registrar(actual.estado, acierto, hoy),
            intentos = actual.intentos + 1,
            aciertos = actual.aciertos + if (acierto) 1 else 0,
        )
        dao.guardar(nuevo.aEntidad(pasajeId))
    }

    private fun ProgresoVersiculoEntity.aDominio() = ProgresoPasaje(
        memorizando = memorizando,
        estado = EstadoMemoria(grado, dominio, LocalDate.ofEpochDay(proximaFechaDia)),
        intentos = intentos,
        aciertos = aciertos,
    )

    private fun ProgresoPasaje.aEntidad(pasajeId: String) = ProgresoVersiculoEntity(
        pasajeId = pasajeId,
        memorizando = memorizando,
        grado = estado.grado,
        dominio = estado.dominio,
        proximaFechaDia = estado.proximaFecha.toEpochDay(),
        intentos = intentos,
        aciertos = aciertos,
    )
}
