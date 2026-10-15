package com.example.lampara.feature.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lampara.data.repository.PerfilLocal
import com.example.lampara.data.repository.PerfilRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val repositorio: PerfilRepository,
) : ViewModel() {

    /** null mientras carga. */
    val perfil: StateFlow<PerfilLocal?> = repositorio.observarActivo()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun guardar(nombre: String, avatar: String, alTerminar: () -> Unit) {
        viewModelScope.launch {
            repositorio.guardar(nombre, avatar)
            alTerminar()
        }
    }
}
