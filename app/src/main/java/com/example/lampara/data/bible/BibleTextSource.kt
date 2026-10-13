package com.example.lampara.data.bible

import android.content.Context
import com.example.lampara.domain.model.CapituloTexto
import com.example.lampara.domain.model.TablaLibros
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import javax.inject.Inject
import javax.inject.Singleton

/** Estado de la instalación del texto bíblico: presente, faltante o con formato inválido. */
sealed interface CargaTexto {
    data object Cargado : CargaTexto

    /** No está instalado el texto (ver docs/TEXTO_BIBLICO.md). */
    data object Faltante : CargaTexto

    /** Un archivo del texto existe pero no tiene el formato esperado. */
    data class Invalido(val motivo: String) : CargaTexto
}

/** Resultado de leer un libro. */
sealed interface CargaLibro {
    data class Cargado(val capitulos: List<CapituloTexto>) : CargaLibro

    data object Faltante : CargaLibro

    data class Invalido(val motivo: String) : CargaLibro
}

/** Origen del texto bíblico, de a un libro por vez. Es una interfaz para usar un texto de prueba en los tests. */
interface BibleTextSource {
    suspend fun libro(id: String): CargaLibro
}

/**
 * Lee assets/bible/libros/<ID>.json, un archivo por libro (el más grande, Salmos, pesa unos 200 KB), así que solo
 * se lee y analiza lo que se necesita en cada momento. Esos archivos los genera `tools/importar_biblia` a partir
 * del PDF del usuario y no están en el repositorio; si faltan, la app funciona igual y las pantallas que los
 * necesitan lo avisan.
 */
@Singleton
class BibleTextSourceAssets @Inject constructor(
    @ApplicationContext private val context: Context,
) : BibleTextSource {

    private val candado = Mutex()
    private val leidos = mutableMapOf<String, CargaLibro>()

    override suspend fun libro(id: String): CargaLibro = candado.withLock {
        leidos[id] ?: withContext(Dispatchers.IO) { leerAsset(id) }.also { leidos[id] = it }
    }

    private fun leerAsset(id: String): CargaLibro {
        val libro = TablaLibros.porId[id] ?: return CargaLibro.Invalido("Libro desconocido: $id")
        return try {
            val json = context.assets.open("$CARPETA/$id.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
            CargaLibro.Cargado(LectorBiblia.leerLibro(json, libro))
        } catch (_: FileNotFoundException) {
            CargaLibro.Faltante
        } catch (e: TextoBiblicoInvalidoException) {
            CargaLibro.Invalido(e.message.orEmpty())
        }
    }

    companion object {
        const val CARPETA = "bible/libros"
    }
}
