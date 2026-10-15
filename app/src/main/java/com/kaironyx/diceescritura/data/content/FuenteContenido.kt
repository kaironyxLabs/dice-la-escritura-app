package com.kaironyx.diceescritura.data.content

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/** Origen de los archivos de contenido (assets en la app, texto en memoria en las pruebas). */
interface FuenteContenido {
    /** Devuelve el JSON de cada archivo de contenido, indexado por nombre de archivo. */
    fun leerArchivos(): Map<String, String>
}

@Singleton
class FuenteContenidoAssets @Inject constructor(
    @ApplicationContext private val context: Context,
) : FuenteContenido {
    override fun leerArchivos(): Map<String, String> {
        val assets = context.assets
        return assets.list(CARPETA).orEmpty()
            .filter { esArchivoDePreguntas(it) }
            .sorted()
            .associateWith { nombre ->
                assets.open("$CARPETA/$nombre").bufferedReader(Charsets.UTF_8).use { it.readText() }
            }
    }

    companion object {
        const val CARPETA = "content"

        fun esArchivoDePreguntas(nombre: String): Boolean =
            nombre.endsWith(".json") && (nombre.startsWith("core-") || nombre.startsWith("confesion1689-"))
    }
}

/** Lector estricto: un campo desconocido es un error de contenido, no se ignora. */
object LectorContenido {
    private val json = Json { ignoreUnknownKeys = false }

    fun leer(texto: String): ArchivoContenidoDto = json.decodeFromString(texto)
}
