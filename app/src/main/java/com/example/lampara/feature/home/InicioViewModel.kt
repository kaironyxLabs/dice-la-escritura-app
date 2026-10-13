package com.example.lampara.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lampara.data.repository.ProgresoRepository
import com.example.lampara.domain.usecase.EstadoNivel
import com.example.lampara.domain.usecase.GestorNiveles
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class InicioViewModel @Inject constructor(
    progreso: ProgresoRepository,
    gestor: GestorNiveles,
) : ViewModel() {

    /** null mientras carga el perfil. */
    val estado: StateFlow<EstadoNivel?> = progreso.perfil()
        .map { gestor.estado(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
