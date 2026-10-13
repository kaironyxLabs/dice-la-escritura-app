package com.example.lampara

import android.app.Application
import com.example.lampara.core.notifications.Notificaciones
import com.example.lampara.core.notifications.RecordatorioScheduler
import com.example.lampara.data.datastore.AjustesRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class LamparaApp : Application() {

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
