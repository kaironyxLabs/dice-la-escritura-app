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
 * Estado de una partida, examen o reto diario en curso: una pregunta a la vez, primero se responde
 * y después se avanza. Es lógica pura; la interfaz y el almacenamiento la usan sin que ella conozca Android.
 *
 * Para retomar un reto a medias, [respondidasPrevias], [aciertosPrevios] y [xpPrevio] suman lo ya hecho:
 * [preguntas] solo trae las que faltan, pero el total, el índice y el resumen cuentan el reto completo.
 */
class SesionJuego(
    private val preguntas: List<Pregunta>,
    private val esExamen: Boolean,
    private val gestor: GestorNiveles,
    private val random: Random = Random.Default,
    private val respondidasPrevias: Int = 0,
    aciertosPrevios: Int = 0,
    xpPrevio: Int = 0,
) {
    init {
        require(preguntas.isNotEmpty()) { "Una sesión necesita al menos una pregunta" }
        require(respondidasPrevias >= 0 && aciertosPrevios in 0..respondidasPrevias && xpPrevio >= 0) {
            "Progreso previo inválido"
        }
    }

    val total: Int = respondidasPrevias + preguntas.size

    private var posicion = 0
    private var aciertos = aciertosPrevios
    private var seguidos = 0
    private var xpGanado = xpPrevio
    private var respondida: ResultadoRespuesta? = null
    private var disposicion: List<String> = Disposicion.barajar(preguntas[0], random)

    /** Posición de la pregunta actual dentro de toda la sesión, empezando en 0. */
    val indice: Int get() = respondidasPrevias + posicion

    val pregunta: Pregunta get() = preguntas[posicion]

    /** Opciones u elementos barajados de la pregunta actual (vacío en verdadero/falso). */
    val disposicionActual: List<String> get() = disposicion

    /** Resultado de la pregunta actual si ya se contestó. */
    val resultadoActual: ResultadoRespuesta? get() = respondida

    val esUltima: Boolean get() = posicion == preguntas.size - 1

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
        posicion++
        respondida = null
        disposicion = Disposicion.barajar(pregunta, random)
        return true
    }
}
