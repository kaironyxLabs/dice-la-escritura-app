package com.example.lampara.data.repository

import androidx.room.withTransaction
import com.example.lampara.core.flags.FeatureFlag
import com.example.lampara.core.flags.FeatureFlags
import com.example.lampara.core.notifications.RecordatorioScheduler
import com.example.lampara.data.datastore.AjustesRepository
import com.example.lampara.data.db.LamparaDatabase
import com.example.lampara.data.db.PerfilEntity
import com.example.lampara.data.db.ProgresoPreguntaEntity
import com.example.lampara.data.db.ProgresoVersiculoEntity
import com.example.lampara.data.db.RetoDiarioEntity
import com.example.lampara.data.db.SesionEntity
import com.example.lampara.data.export.AjustesExp
import com.example.lampara.data.export.CodecProgreso
import com.example.lampara.data.export.LecturaProgreso
import com.example.lampara.data.export.PerfilExp
import com.example.lampara.data.export.PreguntaExp
import com.example.lampara.data.export.ProgresoExportado
import com.example.lampara.data.export.RetoExp
import com.example.lampara.data.export.SesionExp
import com.example.lampara.data.export.VersiculoExp
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

sealed interface ResultadoImportacion {
    data object Correcta : ResultadoImportacion

    data class Rechazada(val errores: List<String>) : ResultadoImportacion
}

/** Exporta e importa el progreso completo como JSON. Importar reemplaza el progreso actual en una transacción. */
@Singleton
class RespaldoRepository @Inject constructor(
    private val db: LamparaDatabase,
    private val ajustes: AjustesRepository,
    private val flags: FeatureFlags,
    private val recordatorios: RecordatorioScheduler,
    private val clock: Clock,
) {
    suspend fun exportarJson(): String = CodecProgreso.aJson(exportar())

    suspend fun exportar(): ProgresoExportado {
        val perfil = db.perfilDao().obtener()
        return ProgresoExportado(
            exportadoEn = Instant.now(clock).toString(),
            perfil = PerfilExp(perfil?.nivel ?: 1, perfil?.xp ?: 0),
            preguntas = db.progresoPreguntaDao().todos().map {
                PreguntaExp(it.preguntaId, it.caja, it.proximaFechaDia, it.aciertos, it.fallos, it.ultimaFechaDia)
            },
            sesiones = db.sesionDao().todas().map {
                SesionExp(it.tipo, it.categoria, it.fechaDia, it.creadaEnMs, it.aciertos, it.total, it.xpGanado, it.aprobado)
            },
            retos = db.retoDiarioDao().todos().map {
                RetoExp(
                    it.fechaDia,
                    if (it.preguntaIds.isEmpty()) emptyList() else it.preguntaIds.split(','),
                    it.respondidas, it.aciertos, it.xpGanado, it.completado,
                )
            },
            versiculos = db.progresoVersiculoDao().todos().map {
                VersiculoExp(it.pasajeId, it.memorizando, it.grado, it.dominio, it.proximaFechaDia, it.intentos, it.aciertos)
            },
            ajustes = AjustesExp(
                confesionActiva = flags.habilitada(FeatureFlag.CONFESION_1689).first(),
                soloRevisadas = ajustes.soloRevisadas.first(),
                recordatorioActivo = ajustes.recordatorioActivo.first(),
                recordatorioMinutos = ajustes.recordatorioMinutos.first(),
            ),
        )
    }

    /** Valida el archivo y, solo si es válido, reemplaza el progreso actual. Un archivo malo no toca nada. */
    suspend fun importarJson(texto: String): ResultadoImportacion {
        val progreso = when (val lectura = CodecProgreso.leer(texto)) {
            is LecturaProgreso.Valido -> lectura.progreso
            is LecturaProgreso.Invalido -> return ResultadoImportacion.Rechazada(lectura.errores)
        }
        db.withTransaction {
            db.perfilDao().borrar()
            db.progresoPreguntaDao().borrarTodos()
            db.sesionDao().borrarTodas()
            db.retoDiarioDao().borrarTodos()
            db.progresoVersiculoDao().borrarTodos()

            db.perfilDao().guardar(PerfilEntity(nivel = progreso.perfil.nivel, xp = progreso.perfil.xp))
            db.progresoPreguntaDao().guardarTodos(progreso.preguntas.map {
                ProgresoPreguntaEntity(it.preguntaId, it.caja, it.proximaFechaDia, it.aciertos, it.fallos, it.ultimaFechaDia)
            })
            db.sesionDao().insertarTodas(progreso.sesiones.map {
                SesionEntity(
                    tipo = it.tipo, categoria = it.categoria, fechaDia = it.fechaDia, creadaEnMs = it.creadaEnMs,
                    aciertos = it.aciertos, total = it.total, xpGanado = it.xpGanado, aprobado = it.aprobado,
                )
            })
            db.retoDiarioDao().guardarTodos(progreso.retos.map {
                RetoDiarioEntity(
                    it.fechaDia, it.preguntaIds.joinToString(","), it.respondidas, it.aciertos, it.xpGanado, it.completado,
                )
            })
            db.progresoVersiculoDao().guardarTodos(progreso.versiculos.map {
                ProgresoVersiculoEntity(
                    it.pasajeId, it.memorizando, it.grado, it.dominio, it.proximaFechaDia, it.intentos, it.aciertos,
                )
            })
        }
        val a = progreso.ajustes
        flags.establecer(FeatureFlag.CONFESION_1689, a.confesionActiva)
        ajustes.establecerSoloRevisadas(a.soloRevisadas)
        ajustes.establecerRecordatorio(a.recordatorioActivo, a.recordatorioMinutos)
        if (a.recordatorioActivo) recordatorios.reprogramar(a.recordatorioMinutos) else recordatorios.cancelar()
        return ResultadoImportacion.Correcta
    }
}
