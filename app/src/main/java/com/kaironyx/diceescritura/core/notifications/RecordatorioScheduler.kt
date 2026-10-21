package com.kaironyx.diceescritura.core.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kaironyx.diceescritura.domain.usecase.Recordatorio
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Programa el recordatorio diario con WorkManager. Cada disparo agenda el del día siguiente.
 * Cada ocurrencia tiene su propio nombre único (según la fecha), de modo que reprogramar desde el propio
 * worker no cancela el trabajo en ejecución y programar dos veces el mismo día no duplica nada.
 */
@Singleton
class RecordatorioScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clock: Clock,
) {
    private val workManager get() = WorkManager.getInstance(context)

    /** Agenda el próximo recordatorio a la hora local indicada (minutos desde las 00:00). */
    fun programar(minutosDelDia: Int) {
        val ahora = ZonedDateTime.now(clock)
        val disparo = Recordatorio.proximoDisparo(ahora, minutosDelDia)
        val retrasoMs = disparo.toInstant().toEpochMilli() - ahora.toInstant().toEpochMilli()
        val peticion = OneTimeWorkRequestBuilder<RecordatorioWorker>()
            .setInitialDelay(retrasoMs, TimeUnit.MILLISECONDS)
            .addTag(ETIQUETA)
            .build()
        workManager.enqueueUniqueWork(
            "$ETIQUETA-${disparo.toLocalDate().toEpochDay()}",
            ExistingWorkPolicy.KEEP,
            peticion,
        )
    }

    /** Quita cualquier recordatorio pendiente (al desactivarlo o antes de cambiar la hora). */
    fun cancelar() {
        workManager.cancelAllWorkByTag(ETIQUETA)
    }

    /** Cambia la hora: descarta lo agendado y agenda de nuevo. */
    fun reprogramar(minutosDelDia: Int) {
        cancelar()
        programar(minutosDelDia)
    }

    private companion object {
        const val ETIQUETA = "recordatorio"
    }
}
