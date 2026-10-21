package com.kaironyx.diceescritura.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaironyx.diceescritura.data.repository.PerfilLocal
import com.kaironyx.diceescritura.data.repository.PerfilRepository
import com.kaironyx.diceescritura.data.repository.ProgresoRepository
import com.kaironyx.diceescritura.data.repository.RetoDiarioRepository
import com.kaironyx.diceescritura.domain.usecase.EstadoNivel
import com.kaironyx.diceescritura.domain.usecase.GestorNiveles
import com.kaironyx.diceescritura.domain.usecase.Racha
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface EstadoReto {
    data object SinEmpezar : EstadoReto
    data class EnCurso(val respondidas: Int, val total: Int) : EstadoReto
    data class Completado(val aciertos: Int, val total: Int) : EstadoReto
}

data class InicioUiState(
    val persona: PerfilLocal?,
    val nivel: EstadoNivel,
    val racha: Racha,
    val reto: EstadoReto,
)

@HiltViewModel
class InicioViewModel @Inject constructor(
    progreso: ProgresoRepository,
    perfiles: PerfilRepository,
    retos: RetoDiarioRepository,
    gestor: GestorNiveles,
) : ViewModel() {

    /** null mientras carga. */
    val estado: StateFlow<InicioUiState?> = combine(
        progreso.perfil(),
        retos.observarEstado(),
        perfiles.observarActivo(),
    ) { perfil, (racha, reto), persona ->
        InicioUiState(
            persona = persona,
            nivel = gestor.estado(perfil),
            racha = racha,
            reto = when {
                reto == null -> EstadoReto.SinEmpezar
                reto.completado -> EstadoReto.Completado(reto.aciertos, reto.preguntaIds.size)
                else -> EstadoReto.EnCurso(reto.respondidas, reto.preguntaIds.size)
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
