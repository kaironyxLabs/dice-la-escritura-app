package com.example.lampara.data.bible

import com.example.lampara.domain.model.Referencia
import com.example.lampara.domain.model.ResultadoTexto
import javax.inject.Inject
import javax.inject.Singleton

/** Acceso al texto bíblico para el resto de la app. Nada más lo lee directamente. */
@Singleton
class BibleRepository @Inject constructor(
    private val fuente: BibleTextSource,
) {
    /** Estado de la instalación del texto: cargado, faltante o inválido. */
    suspend fun estado(): CargaTexto = fuente.cargar()

    suspend fun disponible(): Boolean = fuente.cargar() is CargaTexto.Cargado

    /** Texto de un pasaje de un capítulo. Nunca lanza por falta del archivo: lo informa en el resultado. */
    suspend fun textoDe(referencia: Referencia): ResultadoTexto = when (val carga = fuente.cargar()) {
        is CargaTexto.Cargado -> carga.texto.versiculos(referencia)
        CargaTexto.Faltante, is CargaTexto.Invalido -> ResultadoTexto.ArchivoFaltante
    }
}
