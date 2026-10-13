package com.example.lampara.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lampara.core.flags.FeatureFlag
import com.example.lampara.core.flags.FeatureFlags
import com.example.lampara.data.datastore.AjustesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Decide si se muestra la bienvenida. null mientras se lee el ajuste (la pantalla de inicio espera). */
@HiltViewModel
class RaizViewModel @Inject constructor(
    ajustes: AjustesRepository,
) : ViewModel() {
    val onboardingVisto: StateFlow<Boolean?> = ajustes.onboardingVisto
        .map<Boolean, Boolean?> { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val ajustes: AjustesRepository,
    private val flags: FeatureFlags,
) : ViewModel() {

    /** Termina la bienvenida; si [activarConfesion], enciende el módulo de la Confesión de 1689. */
    fun terminar(activarConfesion: Boolean, alTerminar: () -> Unit) {
        viewModelScope.launch {
            flags.establecer(FeatureFlag.CONFESION_1689, activarConfesion)
            ajustes.marcarOnboardingVisto()
            alTerminar()
        }
    }
}
