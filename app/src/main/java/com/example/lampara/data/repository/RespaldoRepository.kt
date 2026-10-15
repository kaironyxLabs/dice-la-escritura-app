package com.example.lampara.data.repository

import androidx.room.withTransaction
import com.example.lampara.core.flags.FeatureFlag
import com.example.lampara.core.flags.FeatureFlags
import com.example.lampara.core.notifications.RecordatorioScheduler
import com.example.lampara.data.datastore.AjustesRepository
import com.example.lampara.data.db.LamparaDatabase
import com.example.lampara.data.db.AvanceEntity
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
import com.example.lampara.data.export.UsuarioExp
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
    private val activo: PerfilActivo,
) {
    suspend fun exportarJson(): String = CodecProgreso.aJson(exportar())

    suspend fun exportar(): ProgresoExportado {
        val id = activo.actual
        val perfil = db.avanceDao().obtener(id)
        val usuario = db.perfilUsuarioDao().obtener(id)
        return ProgresoExportado(
            exportadoEn = Instant.now(clock).toString(),
            usuario = usuario?.let { UsuarioExp(it.nombre, it.avatar, it.creadoEnMs) },
            perfil = PerfilExp(perfil?.nivel ?: 1, perfil?.xp ?: 0),
            preguntas = db.progresoPreguntaDao().todos(id).map {
                PreguntaExp(it.preguntaId, it.caja, it.proximaFechaDia, it.aciertos, it.fallos, it.ultimaFechaDia)
            },
            sesiones = db.sesionDao().todas(id).map {
                SesionExp(it.tipo, it.categoria, it.fechaDia, it.creadaEnMs, it.aciertos, it.total, it.xpGanado, it.aprobado)
            },
            retos = db.retoDiarioDao().todos(id).map {
                RetoExp(
                    it.fechaDia,
                    if (it.preguntaIds.isEmpty()) emptyList() else it.preguntaIds.split(','),
                    it.respondidas, it.aciertos, it.xpGanado, it.completado,
                )
            },
            versiculos = db.progresoVersiculoDao().todos(id).map {
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
            val id = activo.actual
            // La identidad solo cambia si el archivo la trae (versión 2); uno de la versión 1 conserva la actual.
            progreso.usuario?.let { u ->
                val actual = db.perfilUsuarioDao().obtener(id)
                if (actual != null) {
                    db.perfilUsuarioDao().guardar(actual.copy(nombre = u.nombre, avatar = u.avatar, creadoEnMs = u.creadoEnMs))
                }
            }
            db.avanceDao().borrar(id)
            db.progresoPreguntaDao().borrarTodos(id)
            db.sesionDao().borrarTodas(id)
            db.retoDiarioDao().borrarTodos(id)
            db.progresoVersiculoDao().borrarTodos(id)

            db.avanceDao().guardar(AvanceEntity(perfilId = id, nivel = progreso.perfil.nivel, xp = progreso.perfil.xp))
            db.progresoPreguntaDao().guardarTodos(progreso.preguntas.map {
                ProgresoPreguntaEntity(id, it.preguntaId, it.caja, it.proximaFechaDia, it.aciertos, it.fallos, it.ultimaFechaDia)
            })
            db.sesionDao().insertarTodas(progreso.sesiones.map {
                SesionEntity(
                    perfilId = id, tipo = it.tipo, categoria = it.categoria, fechaDia = it.fechaDia, creadaEnMs = it.creadaEnMs,
                    aciertos = it.aciertos, total = it.total, xpGanado = it.xpGanado, aprobado = it.aprobado,
                )
            })
            db.retoDiarioDao().guardarTodos(progreso.retos.map {
                RetoDiarioEntity(
                    id, it.fechaDia, it.preguntaIds.joinToString(","), it.respondidas, it.aciertos, it.xpGanado, it.completado,
                )
            })
            db.progresoVersiculoDao().guardarTodos(progreso.versiculos.map {
                ProgresoVersiculoEntity(
                    id, it.pasajeId, it.memorizando, it.grado, it.dominio, it.proximaFechaDia, it.intentos, it.aciertos,
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
