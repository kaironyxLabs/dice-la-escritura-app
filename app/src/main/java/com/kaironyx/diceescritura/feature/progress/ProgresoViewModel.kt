package com.kaironyx.diceescritura.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaironyx.diceescritura.core.flags.FeatureFlags
import com.kaironyx.diceescritura.core.flags.modulosHabilitados
import com.kaironyx.diceescritura.data.repository.PreguntaRepository
import com.kaironyx.diceescritura.data.repository.ProgresoRepository
import com.kaironyx.diceescritura.data.repository.RetoDiarioRepository
import com.kaironyx.diceescritura.domain.model.Categoria
import com.kaironyx.diceescritura.domain.usecase.Estadisticas
import com.kaironyx.diceescritura.domain.usecase.EstadoNivel
import com.kaironyx.diceescritura.domain.usecase.GestorNiveles
import com.kaironyx.diceescritura.domain.usecase.PrecisionCategoria
import com.kaironyx.diceescritura.domain.usecase.Racha
import com.kaironyx.diceescritura.domain.usecase.ResumenHistorial
import com.kaironyx.diceescritura.data.repository.MemorizacionRepository
import com.kaironyx.diceescritura.domain.usecase.DatosLogros
import com.kaironyx.diceescritura.domain.usecase.EstadoLogro
import com.kaironyx.diceescritura.domain.usecase.Logros
import kotlinx.coroutines.flow.Flow
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
    val logros: List<EstadoLogro> = emptyList(),
)

@HiltViewModel
class ProgresoViewModel @Inject constructor(
    preguntas: PreguntaRepository,
    progreso: ProgresoRepository,
    retos: RetoDiarioRepository,
    memorizacion: MemorizacionRepository,
    gestor: GestorNiveles,
    flags: FeatureFlags,
) : ViewModel() {

    private val base: Flow<ProgresoUiState> = combine(
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

    /** Lo que alimenta los logros y no sale de las demás fuentes: retos completados y pasajes dominados. */
    private val extras: Flow<Pair<Int, Int>> = combine(
        retos.observarFechasCompletadas(),
        memorizacion.observar(),
    ) { fechas, pasajes -> fechas.size to pasajes.values.count { it.estado.dominio >= 1 } }

    /** null mientras carga. */
    val estado: StateFlow<ProgresoUiState?> = combine(base, extras, progreso.observarConteos()) { b, (retosHechos, dominados), conteos ->
        b.copy(
            logros = Logros.evaluar(
                DatosLogros(
                    retosCompletados = retosHechos,
                    mejorRacha = b.racha.mejor,
                    nivel = b.nivel.nivel.numero,
                    aciertosTotales = conteos.values.sumOf { it.aciertos },
                    pasajesDominados = dominados,
                ),
            ),
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
