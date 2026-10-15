package com.kaironyx.diceescritura.feature.niveles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaironyx.diceescritura.data.repository.ProgresoRepository
import com.kaironyx.diceescritura.domain.model.Nivel
import com.kaironyx.diceescritura.domain.usecase.GestorNiveles
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class NivelesUiState(val niveles: List<Nivel>, val actual: Int, val examenDisponible: Boolean)

@HiltViewModel
class NivelesViewModel @Inject constructor(
    progreso: ProgresoRepository,
    private val gestor: GestorNiveles,
) : ViewModel() {

    /** null mientras carga. */
    val estado: StateFlow<NivelesUiState?> = progreso.perfil().map { perfil ->
        NivelesUiState(
            niveles = gestor.config.niveles,
            actual = perfil.nivel,
            examenDisponible = gestor.examenDisponible(perfil),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
