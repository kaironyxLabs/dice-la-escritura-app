package com.example.lampara.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lampara.core.flags.FeatureFlags
import com.example.lampara.core.flags.modulosHabilitados
import com.example.lampara.data.repository.PreguntaRepository
import com.example.lampara.data.repository.ProgresoRepository
import com.example.lampara.data.repository.RetoDiarioRepository
import com.example.lampara.domain.model.Categoria
import com.example.lampara.domain.usecase.Estadisticas
import com.example.lampara.domain.usecase.EstadoNivel
import com.example.lampara.domain.usecase.GestorNiveles
import com.example.lampara.domain.usecase.PrecisionCategoria
import com.example.lampara.domain.usecase.Racha
import com.example.lampara.domain.usecase.ResumenHistorial
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ProgresoUiState(
    val nivel: EstadoNivel,
    val racha: Racha,
    val precision: List<PrecisionCategoria>,
    val historial: ResumenHistorial,
)

@HiltViewModel
class ProgresoViewModel @Inject constructor(
    preguntas: PreguntaRepository,
    progreso: ProgresoRepository,
    retos: RetoDiarioRepository,
    gestor: GestorNiveles,
    flags: FeatureFlags,
) : ViewModel() {

    /** null mientras carga. */
    val estado: StateFlow<ProgresoUiState?> = combine(
        progreso.perfil(),
        retos.observarRacha(),
        progreso.observarConteos(),
        progreso.observarSesiones(),
        flags.modulosHabilitados(),
    ) { perfil, racha, conteos, sesiones, modulos ->
        // Con un módulo apagado, su progreso se conserva pero no cuenta ni se muestra.
        val banco = preguntas.preguntas(modulos)
        val categorias = Categoria.entries.filter { it.modulo in modulos }
        ProgresoUiState(
            nivel = gestor.estado(perfil),
            racha = racha,
            precision = Estadisticas.precisionPorCategoria(conteos, banco, categorias),
            historial = Estadisticas.historial(
                sesiones.filter { it.categoria == null || it.categoria.modulo in modulos },
                progreso.hoy(),
            ),
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
