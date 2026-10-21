package com.kaironyx.diceescritura.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.kaironyx.diceescritura.core.flags.FeatureFlag
import com.kaironyx.diceescritura.core.flags.FeatureFlags
import com.kaironyx.diceescritura.domain.usecase.Recordatorio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Ajustes del usuario y banderas de función, respaldados por DataStore (Preferences). */
@Singleton
class AjustesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : FeatureFlags {

    private val claveSoloRevisadas = booleanPreferencesKey("solo_revisadas")
    private val claveOnboardingVisto = booleanPreferencesKey("onboarding_visto")
    private val claveRecordatorioActivo = booleanPreferencesKey("recordatorio_activo")
    private val claveRecordatorioMinutos = intPreferencesKey("recordatorio_minutos")

    override fun habilitada(flag: FeatureFlag): Flow<Boolean> =
        dataStore.data.map { it[booleanPreferencesKey(flag.clave)] ?: flag.valorPorDefecto }.distinctUntilChanged()

    override suspend fun establecer(flag: FeatureFlag, valor: Boolean) {
        dataStore.edit { it[booleanPreferencesKey(flag.clave)] = valor }
    }

    /** Apagado por defecto mientras el contenido esté pendiente de revisión. */
    val soloRevisadas: Flow<Boolean> =
        dataStore.data.map { it[claveSoloRevisadas] ?: false }.distinctUntilChanged()

    suspend fun establecerSoloRevisadas(valor: Boolean) {
        dataStore.edit { it[claveSoloRevisadas] = valor }
    }

    val recordatorioActivo: Flow<Boolean> =
        dataStore.data.map { it[claveRecordatorioActivo] ?: false }.distinctUntilChanged()

    /** Hora del recordatorio en minutos desde las 00:00 (20:00 por defecto). */
    val recordatorioMinutos: Flow<Int> =
        dataStore.data.map { it[claveRecordatorioMinutos] ?: Recordatorio.MINUTOS_POR_DEFECTO }.distinctUntilChanged()

    suspend fun establecerRecordatorio(activo: Boolean, minutos: Int) {
        dataStore.edit {
            it[claveRecordatorioActivo] = activo
            it[claveRecordatorioMinutos] = minutos
        }
    }

    val onboardingVisto: Flow<Boolean> =
        dataStore.data.map { it[claveOnboardingVisto] ?: false }.distinctUntilChanged()

    suspend fun marcarOnboardingVisto() {
        dataStore.edit { it[claveOnboardingVisto] = true }
    }
}
