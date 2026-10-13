package com.example.lampara.domain.usecase

import java.text.Normalizer
import java.time.LocalDate
import kotlin.random.Random

/** Grados del modo completar: 20 %, 40 % y 60 % de palabras ocultas, y 100 % escribiendo el pasaje. */
enum class GradoMemoria(val porcentaje: Int) {
    OCULTAR_20(20),
    OCULTAR_40(40),
    OCULTAR_60(60),
    ESCRIBIR(100),
    ;

    val esEscribir: Boolean get() = this == ESCRIBIR

    companion object {
        fun desdeIndice(indice: Int): GradoMemoria = entries[indice.coerceIn(0, entries.lastIndex)]
    }
}

/** Estado de repetición espaciada de un pasaje: grado actual, dominio acumulado y próximo repaso. */
data class EstadoMemoria(val grado: Int, val dominio: Int, val proximaFecha: LocalDate)

/**
 * Repetición espaciada propia de los versículos. Se sube de grado al acertar (20, 40, 60 y 100 %
 * escribiendo). Al acertar escribiendo el pasaje completo, el dominio sube y el repaso se aleja
 * (1, 3, 7, 14, 30 y 60 días). Un fallo baja un grado y el repaso vuelve al día siguiente.
 */
object Memorizacion {
    val INTERVALOS_DIAS = listOf(1, 3, 7, 14, 30, 60)
    private val ULTIMO_GRADO = GradoMemoria.entries.lastIndex

    fun iniciar(hoy: LocalDate) = EstadoMemoria(grado = 0, dominio = 0, proximaFecha = hoy)

    fun registrar(estado: EstadoMemoria, acierto: Boolean, hoy: LocalDate): EstadoMemoria = when {
        acierto && estado.grado < ULTIMO_GRADO ->
            // Se puede seguir practicando el grado siguiente hoy mismo.
            estado.copy(grado = estado.grado + 1, proximaFecha = hoy)
        acierto -> {
            val dominio = estado.dominio + 1
            val intervalo = INTERVALOS_DIAS[minOf(dominio, INTERVALOS_DIAS.size) - 1]
            estado.copy(dominio = dominio, proximaFecha = hoy.plusDays(intervalo.toLong()))
        }
        else -> estado.copy(
            grado = maxOf(estado.grado - 1, 0),
            dominio = if (estado.grado == ULTIMO_GRADO) maxOf(estado.dominio - 1, 0) else estado.dominio,
            proximaFecha = hoy.plusDays(1),
        )
    }

    fun estaVencido(estado: EstadoMemoria, hoy: LocalDate): Boolean = !estado.proximaFecha.isAfter(hoy)
}

/** Fragmento de un texto: palabra o separador (espacios y puntuación). */
data class Token(val texto: String, val esPalabra: Boolean)

/** Texto con algunas palabras ocultas. [ocultas] son índices dentro de [tokens]. */
data class TextoOculto(val tokens: List<Token>, val ocultas: Set<Int>) {
    /** Palabras ocultas, en orden de aparición. */
    val palabrasOcultas: List<String> get() = ocultas.sorted().map { tokens[it].texto }
}

object Ocultador {
    private val patronPalabra = Regex("""[\p{L}\p{M}\p{N}]+(?:['’\-][\p{L}\p{M}\p{N}]+)*""")

    fun tokenizar(texto: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var cursor = 0
        for (coincidencia in patronPalabra.findAll(texto)) {
            if (coincidencia.range.first > cursor) {
                tokens += Token(texto.substring(cursor, coincidencia.range.first), esPalabra = false)
            }
            tokens += Token(coincidencia.value, esPalabra = true)
            cursor = coincidencia.range.last + 1
        }
        if (cursor < texto.length) tokens += Token(texto.substring(cursor), esPalabra = false)
        return tokens
    }

    /**
     * Oculta [porcentaje] % de las palabras (al menos una si hay palabras). La elección es determinista
     * para una misma [semilla], de modo que girar la pantalla no cambia los huecos.
     */
    fun ocultar(texto: String, porcentaje: Int, semilla: Long): TextoOculto {
        require(porcentaje in 0..100) { "Porcentaje fuera de 0..100" }
        val tokens = tokenizar(texto)
        val indicesPalabra = tokens.indices.filter { tokens[it].esPalabra }
        if (indicesPalabra.isEmpty() || porcentaje == 0) return TextoOculto(tokens, emptySet())
        val cantidad = maxOf(1, Math.round(indicesPalabra.size * porcentaje / 100.0).toInt())
        val elegidas = indicesPalabra.shuffled(Random(semilla)).take(cantidad).toSet()
        return TextoOculto(tokens, elegidas)
    }
}

data class ResultadoMemoria(val aciertos: Int, val total: Int) {
    /** Se aprueba con el 90 % de las palabras; con pocas palabras, equivale a no fallar ninguna. */
    val aprobado: Boolean get() = total > 0 && aciertos * 100 >= UMBRAL_PORCENTAJE * total

    companion object {
        const val UMBRAL_PORCENTAJE = 90
    }
}

object EvaluadorMemoria {

    /** Minúsculas, sin tildes ni signos: "Dió," y "dio" cuentan como la misma palabra. */
    fun normalizar(palabra: String): String =
        Normalizer.normalize(palabra.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .filter { it.isLetterOrDigit() }

    fun palabras(texto: String): List<String> =
        Ocultador.tokenizar(texto).filter { it.esPalabra }.map { normalizar(it.texto) }.filter { it.isNotEmpty() }

    /**
     * Compara lo escrito con las palabras [esperadas] (en orden). Cuenta la subsecuencia común más larga,
     * así olvidar o repetir una palabra no desplaza y penaliza todas las siguientes.
     */
    fun evaluar(esperadas: List<String>, escrito: String): ResultadoMemoria {
        val esperado = esperadas.map(::normalizar).filter { it.isNotEmpty() }
        val dado = palabras(escrito)
        if (esperado.isEmpty()) return ResultadoMemoria(0, 0)
        // Subsecuencia común más larga, con una sola fila de memoria.
        var anterior = IntArray(dado.size + 1)
        for (palabra in esperado) {
            val actual = IntArray(dado.size + 1)
            for (j in dado.indices) {
                actual[j + 1] = if (palabra == dado[j]) anterior[j] + 1 else maxOf(anterior[j + 1], actual[j])
            }
            anterior = actual
        }
        return ResultadoMemoria(aciertos = anterior[dado.size], total = esperado.size)
    }
}
