package com.kaironyx.diceescritura.data.repository

import com.kaironyx.diceescritura.data.db.PerfilUsuarioEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Perfil local con el que se está estudiando. Hoy solo hay uno ([PerfilUsuarioEntity.ID_INICIAL]);
 * los repositorios ya piden el id aquí para que añadir más perfiles no cambie sus consultas.
 */
@Singleton
class PerfilActivo @Inject constructor() {
    private val _id = MutableStateFlow(PerfilUsuarioEntity.ID_INICIAL)
    val id: StateFlow<Long> = _id

    /** Id vigente en este momento. */
    val actual: Long get() = _id.value

    fun cambiarA(id: Long) {
        _id.value = id
    }

    /** Re-emite la consulta cada vez que cambia el perfil activo. */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T> observar(consulta: (Long) -> Flow<T>): Flow<T> = _id.flatMapLatest(consulta)
}
