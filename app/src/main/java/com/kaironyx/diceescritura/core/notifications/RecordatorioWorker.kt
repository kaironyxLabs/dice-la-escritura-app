package com.kaironyx.diceescritura.core.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kaironyx.diceescritura.data.datastore.AjustesRepository
import com.kaironyx.diceescritura.data.repository.RetoDiarioRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

/**
 * Muestra el recordatorio si sigue activado y el reto de hoy no está completo, y agenda el del día siguiente.
 * Usa un punto de entrada de Hilt en lugar de HiltWorker para no cambiar la inicialización de WorkManager.
 */
class RecordatorioWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Dependencias {
        fun ajustes(): AjustesRepository
        fun retos(): RetoDiarioRepository
        fun scheduler(): RecordatorioScheduler
    }

    override suspend fun doWork(): Result {
        val dependencias = EntryPointAccessors.fromApplication(applicationContext, Dependencias::class.java)
        val ajustes = dependencias.ajustes()
        if (!ajustes.recordatorioActivo.first()) return Result.success()

        if (dependencias.retos().retoDeHoy()?.completado != true) {
            Notificaciones.mostrarRecordatorio(applicationContext)
        }
        dependencias.scheduler().programar(ajustes.recordatorioMinutos.first())
        return Result.success()
    }
}
