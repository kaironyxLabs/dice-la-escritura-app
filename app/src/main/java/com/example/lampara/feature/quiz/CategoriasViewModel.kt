package com.example.lampara.feature.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lampara.core.flags.FeatureFlags
import com.example.lampara.core.flags.modulosHabilitados
import com.example.lampara.data.datastore.AjustesRepository
import com.example.lampara.data.repository.PreguntaRepository
import com.example.lampara.data.repository.ProgresoRepository
import com.example.lampara.domain.model.Categoria
import com.example.lampara.domain.usecase.GestorNiveles
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ItemCategoria(
    val categoria: Categoria,
    /** Nivel en que se desbloquea; null si ya está disponible. */
    val bloqueadaHastaNivel: Int?,
    val preguntasDisponibles: Int,
)

@HiltViewModel
class CategoriasViewModel @Inject constructor(
    preguntas: PreguntaRepository,
    progreso: ProgresoRepository,
    gestor: GestorNiveles,
    ajustes: AjustesRepository,
    flags: FeatureFlags,
) : ViewModel() {

    val categorias: StateFlow<List<ItemCategoria>?> = combine(
        progreso.perfil(),
        flags.modulosHabilitados(),
        ajustes.soloRevisadas,
    ) { perfil, modulos, soloRevisadas ->
        val nivel = gestor.nivel(perfil.nivel)
        val desbloqueadas = gestor.categoriasDesbloqueadas(perfil.nivel)
        val banco = preguntas.preguntas(modulos, soloRevisadas)
        Categoria.entries.filter { it.modulo in modulos }.map { categoria ->
            ItemCategoria(
                categoria = categoria,
                bloqueadaHastaNivel = if (categoria in desbloqueadas) null else gestor.nivelQueDesbloquea(categoria)?.numero,
                preguntasDisponibles = banco.count { it.categoria == categoria && it.dificultad <= nivel.dificultadMaxima },
            )
        }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
