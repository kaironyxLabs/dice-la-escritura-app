package com.example.lampara.domain.model

data class SeccionTexto(val antesDelVersiculo: Int, val titulo: String)

data class ParaleloTexto(val antesDelVersiculo: Int, val texto: String)

/**
 * Capítulo del texto bíblico importado. El versículo n es `versiculos[n - 1]`; un versículo pendiente
 * (marcado por el importador porque su texto no está verificado) no debe mostrarse ni usarse.
 */
data class CapituloTexto(
    val numero: Int,
    /** Texto sin numerar antes del versículo 1 (sobrescritos de salmos). */
    val encabezado: String?,
    val versiculos: List<String>,
    val secciones: List<SeccionTexto>,
    val paralelos: List<ParaleloTexto>,
    val pendientes: Set<Int>,
)

data class VersiculoTexto(val numero: Int, val texto: String)

enum class MotivoNoDisponible {
    /** El importador marcó el versículo como pendiente de verificación. */
    PENDIENTE,
    /** El versículo no existe en el texto cargado. */
    INEXISTENTE,
}

sealed interface ResultadoTexto {
    data class Disponible(val versiculos: List<VersiculoTexto>) : ResultadoTexto {
        /** Texto continuo del pasaje, sin números de versículo. */
        val texto: String get() = versiculos.joinToString(" ") { it.texto }
    }

    data class NoDisponible(val motivo: MotivoNoDisponible, val versiculo: Int) : ResultadoTexto

    /** No está instalado el archivo del texto bíblico (assets/bible/rvr1960.json). */
    data object ArchivoFaltante : ResultadoTexto
}

/** Texto bíblico completo, indexado por id de libro. */
class TextoBiblico(private val libros: Map<String, List<CapituloTexto>>) {

    fun capitulo(libroId: String, numero: Int): CapituloTexto? = libros[libroId]?.getOrNull(numero - 1)

    val totalCapitulos: Int get() = libros.values.sumOf { it.size }

    /**
     * Versículos de una referencia de un solo capítulo con versículos explícitos ("Juan 3:16-18").
     * Si cualquiera está pendiente, vacío o no existe, el pasaje completo no está disponible.
     */
    fun versiculos(referencia: Referencia): ResultadoTexto {
        val inicio = referencia.versiculoInicio
        val fin = referencia.versiculoFin
        require(referencia.capituloInicio == referencia.capituloFin && inicio != null && fin != null) {
            "Solo se admiten pasajes de un capítulo con versículos explícitos"
        }
        val capitulo = capitulo(referencia.libro.id, referencia.capituloInicio)
            ?: return ResultadoTexto.NoDisponible(MotivoNoDisponible.INEXISTENTE, inicio)
        val resultado = mutableListOf<VersiculoTexto>()
        for (numero in inicio..fin) {
            val texto = capitulo.versiculos.getOrNull(numero - 1)
                ?: return ResultadoTexto.NoDisponible(MotivoNoDisponible.INEXISTENTE, numero)
            if (numero in capitulo.pendientes || texto.isBlank()) {
                return ResultadoTexto.NoDisponible(MotivoNoDisponible.PENDIENTE, numero)
            }
            resultado += VersiculoTexto(numero, texto)
        }
        return ResultadoTexto.Disponible(resultado)
    }
}
