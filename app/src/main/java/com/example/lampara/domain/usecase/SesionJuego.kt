package com.example.lampara.domain.usecase

import com.example.lampara.domain.model.Pregunta
import kotlin.random.Random

data class ResultadoRespuesta(
    val correcta: Boolean,
    /** XP ganado con esta respuesta (siempre 0 en un examen de ascenso). */
    val xp: Int,
    val aciertosSeguidos: Int,
)

data class ResumenSesion(val aciertos: Int, val total: Int, val xpGanado: Int)

/**
 * Estado de una partida o examen en curso: una pregunta a la vez, primero se responde y después se avanza.
 * Es lógica pura; la interfaz y el almacenamiento la usan sin que ella conozca Android.
 */
class SesionJuego(
    private val preguntas: List<Pregunta>,
    private val esExamen: Boolean,
    private val gestor: GestorNiveles,
    private val random: Random = Random.Default,
) {
    init {
        require(preguntas.isNotEmpty()) { "Una sesión necesita al menos una pregunta" }
    }

    val total: Int = preguntas.size

    /** Posición de la pregunta actual, empezando en 0. */
    var indice: Int = 0
        private set

    private var aciertos = 0
    private var seguidos = 0
    private var xpGanado = 0
    private var respondida: ResultadoRespuesta? = null
    private var disposicion: List<String> = Disposicion.barajar(preguntas[0], random)

    val pregunta: Pregunta get() = preguntas[indice]

    /** Opciones u elementos barajados de la pregunta actual (vacío en verdadero/falso). */
    val disposicionActual: List<String> get() = disposicion

    /** Resultado de la pregunta actual si ya se contestó. */
    val resultadoActual: ResultadoRespuesta? get() = respondida

    val esUltima: Boolean get() = indice == total - 1

    val resumen: ResumenSesion get() = ResumenSesion(aciertos, total, xpGanado)

    fun responder(respuesta: Respuesta): ResultadoRespuesta {
        check(respondida == null) { "La pregunta ya fue respondida" }
        val correcta = Evaluador.esCorrecta(pregunta, respuesta)
        seguidos = if (correcta) seguidos + 1 else 0
        val xp = if (correcta && !esExamen) gestor.xpPorAcierto(pregunta.dificultad, seguidos) else 0
        if (correcta) aciertos++
        xpGanado += xp
        return ResultadoRespuesta(correcta, xp, seguidos).also { respondida = it }
    }

    /** Pasa a la siguiente pregunta. Devuelve false (sin avanzar) si ya era la última. */
    fun avanzar(): Boolean {
        check(respondida != null) { "Hay que responder antes de avanzar" }
        if (esUltima) return false
        indice++
        respondida = null
        disposicion = Disposicion.barajar(pregunta, random)
        return true
    }
}
