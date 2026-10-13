package com.example.lampara.core.flags

import com.example.lampara.domain.model.Modulo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Banderas de función. Para agregar una: un valor nuevo aquí, con su clave y valor por defecto. */
enum class FeatureFlag(val clave: String, val valorPorDefecto: Boolean) {
    CONFESION_1689("flag_confesion_1689", false),
}

interface FeatureFlags {
    fun habilitada(flag: FeatureFlag): Flow<Boolean>
    suspend fun establecer(flag: FeatureFlag, valor: Boolean)
}

/** Módulos de contenido activos según las banderas. El módulo base siempre está. */
fun FeatureFlags.modulosHabilitados(): Flow<Set<Modulo>> =
    habilitada(FeatureFlag.CONFESION_1689).map { confesion ->
        if (confesion) setOf(Modulo.CORE, Modulo.CONFESION_1689) else setOf(Modulo.CORE)
    }
