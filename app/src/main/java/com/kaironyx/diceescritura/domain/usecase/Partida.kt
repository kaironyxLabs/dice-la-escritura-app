package com.kaironyx.diceescritura.domain.usecase

import com.kaironyx.diceescritura.domain.model.Categoria
import com.kaironyx.diceescritura.domain.model.Contenido
import com.kaironyx.diceescritura.domain.model.Modulo
import com.kaironyx.diceescritura.domain.model.Pregunta
import java.time.LocalDate
import kotlin.random.Random

/** Lo que el jugador contestó. Las opciones se identifican por su texto porque se presentan barajadas. */
sealed interface Respuesta {
    data class Opcion(val texto: String) : Respuesta
    data class VerdaderoFalso(val valor: Boolean) : Respuesta
    data class Orden(val elementos: List<String>) : Respuesta
}

object Evaluador {
    fun esCorrecta(pregunta: Pregunta, respuesta: Respuesta): Boolean = when (val c = pregunta.contenido) {
        is Contenido.OpcionMultiple ->
            respuesta is Respuesta.Opcion && respuesta.texto == c.opciones[c.indiceCorrecto]
        is Contenido.VerdaderoFalso ->
            respuesta is Respuesta.VerdaderoFalso && respuesta.valor == c.respuesta
        is Contenido.Ordenar ->
            respuesta is Respuesta.Orden && respuesta.elementos == c.elementos
    }
}

/** Qué se muestra al jugador en una pregunta: opciones o elementos en orden aleatorio. */
object Disposicion {
    /**
     * Opciones (opción múltiple) o elementos (ordenar) barajados; vacío en verdadero/falso.
     * En ordenar nunca devuelve el orden correcto.
     */
    fun barajar(pregunta: Pregunta, random: Random): List<String> = when (val c = pregunta.contenido) {
        is Contenido.OpcionMultiple -> c.opciones.shuffled(random)
        is Contenido.VerdaderoFalso -> emptyList()
        is Contenido.Ordenar -> {
            var mezcla = c.elementos.shuffled(random)
            var intentos = 0
            while (mezcla == c.elementos && intentos < 10) {
                mezcla = c.elementos.shuffled(random)
                intentos++
            }
            if (mezcla == c.elementos) mezcla = c.elementos.drop(1) + c.elementos.first()
            mezcla
        }
    }
}

/** Progreso de una pregunta que el selector usa para priorizar. */
data class SeguimientoPregunta(val caja: Int, val proximaFecha: LocalDate)

/** Restricciones de una partida o examen. */
data class FiltroJuego(
    val modulosHabilitados: Set<Modulo>,
    val soloRevisadas: Boolean,
    val dificultadMaxima: Int,
    val categoriasPermitidas: Set<Categoria>,
    /** Si no es null, la partida es de esa única categoría. */
    val categoria: Categoria? = null,
)

class SelectorPreguntas(private val random: Random = Random.Default) {

    /** Hasta [cantidad] preguntas: primero las nuevas, luego el repaso vencido y por último el resto. */
    fun partida(
        banco: List<Pregunta>,
        filtro: FiltroJuego,
        seguimiento: Map<String, SeguimientoPregunta>,
        hoy: LocalDate,
        cantidad: Int,
    ): List<Pregunta> {
        val candidatas = elegibles(banco, filtro)
        return candidatas.shuffled(random)
            .sortedBy { prioridad(it, seguimiento, hoy) }
            .take(cantidad)
            .shuffled(random)
    }

    /**
     * Reto diario: mezcla de repaso vencido (hasta la mitad, el más atrasado primero) y preguntas nuevas.
     * Si faltan nuevas se completa con más repaso y, por último, con preguntas aún no vencidas.
     */
    fun reto(
        banco: List<Pregunta>,
        filtro: FiltroJuego,
        seguimiento: Map<String, SeguimientoPregunta>,
        hoy: LocalDate,
        cantidad: Int,
    ): List<Pregunta> {
        val candidatas = elegibles(banco, filtro)
        val (vistas, nuevas) = candidatas.partition { it.id in seguimiento }
        val (vencidas, vigentes) = vistas.partition { Leitner.estaVencida(seguimiento.getValue(it.id).proximaFecha, hoy) }
        val vencidasPorAtraso = vencidas.sortedBy { seguimiento.getValue(it.id).proximaFecha }
        val repaso = vencidasPorAtraso.take(cantidad / 2)

        val elegidas = repaso.toMutableList()
        elegidas += nuevas.shuffled(random).take(cantidad - elegidas.size)
        if (elegidas.size < cantidad) {
            elegidas += vencidasPorAtraso.drop(repaso.size).take(cantidad - elegidas.size)
        }
        if (elegidas.size < cantidad) {
            elegidas += vigentes.sortedBy { seguimiento.getValue(it.id).proximaFecha }.take(cantidad - elegidas.size)
        }
        return elegidas.shuffled(random)
    }

    /**
     * Examen de ascenso: preguntas de las categorías desbloqueadas, favoreciendo las de la dificultad
     * más alta del nivel. Si no hay [cantidad] disponibles devuelve las que haya.
     */
    fun examen(banco: List<Pregunta>, filtro: FiltroJuego, cantidad: Int): List<Pregunta> {
        val candidatas = elegibles(banco, filtro).shuffled(random)
        val piso = maxOf(1, filtro.dificultadMaxima - 1)
        val (altas, resto) = candidatas.partition { it.dificultad >= piso }
        return (altas + resto).take(cantidad).shuffled(random)
    }

    private fun elegibles(banco: List<Pregunta>, filtro: FiltroJuego): List<Pregunta> = banco.filter {
        it.modulo in filtro.modulosHabilitados &&
            it.categoria in filtro.categoriasPermitidas &&
            (filtro.categoria == null || it.categoria == filtro.categoria) &&
            it.dificultad <= filtro.dificultadMaxima &&
            (!filtro.soloRevisadas || it.revisada)
    }

    private fun prioridad(p: Pregunta, seguimiento: Map<String, SeguimientoPregunta>, hoy: LocalDate): Int {
        val s = seguimiento[p.id] ?: return 0
        return if (Leitner.estaVencida(s.proximaFecha, hoy)) 1 else 2
    }
}
