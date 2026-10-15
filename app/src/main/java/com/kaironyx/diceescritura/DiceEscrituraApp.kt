package com.kaironyx.diceescritura

import android.app.Application
import com.kaironyx.diceescritura.core.notifications.Notificaciones
import com.kaironyx.diceescritura.core.notifications.RecordatorioScheduler
import com.kaironyx.diceescritura.data.datastore.AjustesRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class DiceEscrituraApp : Application() {

    @Inject lateinit var ajustes: AjustesRepository
    @Inject lateinit var recordatorios: RecordatorioScheduler

    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        Notificaciones.crearCanales(this)
        // Si el recordatorio está activo, garantiza que haya uno agendado (no duplica si ya existe).
        alcance.launch {
            if (ajustes.recordatorioActivo.first()) recordatorios.programar(ajustes.recordatorioMinutos.first())
        }
    }
}
