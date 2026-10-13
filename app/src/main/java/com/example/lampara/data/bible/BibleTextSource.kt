package com.example.lampara.data.bible

import android.content.Context
import com.example.lampara.domain.model.TextoBiblico
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import javax.inject.Inject
import javax.inject.Singleton

sealed interface CargaTexto {
    data class Cargado(val texto: TextoBiblico) : CargaTexto

    /** No está instalado el archivo del texto (ver docs/TEXTO_BIBLICO.md). */
    data object Faltante : CargaTexto

    /** El archivo existe pero no tiene el formato esperado. */
    data class Invalido(val motivo: String) : CargaTexto
}

/** Origen del texto bíblico. Es una interfaz para poder usar un texto de prueba en los tests. */
interface BibleTextSource {
    suspend fun cargar(): CargaTexto
}

/**
 * Lee assets/bible/rvr1960.json. Ese archivo lo genera `tools/importar_biblia` a partir del PDF del usuario
 * y no está en el repositorio; si falta, la app funciona igual y las pantallas que lo necesitan lo avisan.
 */
@Singleton
class BibleTextSourceAssets @Inject constructor(
    @ApplicationContext private val context: Context,
) : BibleTextSource {

    private val candado = Mutex()
    private var carga: CargaTexto? = null

    override suspend fun cargar(): CargaTexto = candado.withLock {
        carga ?: withContext(Dispatchers.IO) { leerAsset() }.also { carga = it }
    }

    private fun leerAsset(): CargaTexto = try {
        val json = context.assets.open(RUTA).bufferedReader(Charsets.UTF_8).use { it.readText() }
        CargaTexto.Cargado(LectorBiblia.leer(json))
    } catch (_: FileNotFoundException) {
        CargaTexto.Faltante
    } catch (e: TextoBiblicoInvalidoException) {
        CargaTexto.Invalido(e.message.orEmpty())
    }

    companion object {
        const val RUTA = "bible/rvr1960.json"
    }
}
