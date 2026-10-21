package com.kaironyx.diceescritura.data.bible

import com.kaironyx.diceescritura.domain.model.Referencia
import com.kaironyx.diceescritura.domain.model.ResultadoTexto
import com.kaironyx.diceescritura.domain.model.TextoBiblico
import javax.inject.Inject
import javax.inject.Singleton

/** Acceso al texto bíblico para el resto de la app. Nada más lo lee directamente. */
@Singleton
class BibleRepository @Inject constructor(
    private val fuente: BibleTextSource,
) {
    /** Estado de la instalación del texto. Comprueba un libro pequeño: no hace falta leer toda la Biblia. */
    suspend fun estado(): CargaTexto = when (val carga = fuente.libro(LIBRO_SONDA)) {
        is CargaLibro.Cargado -> CargaTexto.Cargado
        CargaLibro.Faltante -> CargaTexto.Faltante
        is CargaLibro.Invalido -> CargaTexto.Invalido(carga.motivo)
    }

    suspend fun disponible(): Boolean = estado() is CargaTexto.Cargado

    /** Texto de un pasaje de un capítulo. Nunca lanza por falta del archivo: lo informa en el resultado. */
    suspend fun textoDe(referencia: Referencia): ResultadoTexto {
        val id = referencia.libro.id
        return when (val carga = fuente.libro(id)) {
            is CargaLibro.Cargado -> TextoBiblico(mapOf(id to carga.capitulos)).versiculos(referencia)
            CargaLibro.Faltante, is CargaLibro.Invalido -> ResultadoTexto.ArchivoFaltante
        }
    }

    private companion object {
        const val LIBRO_SONDA = "RUT"
    }
}
