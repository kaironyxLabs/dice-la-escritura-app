package com.kaironyx.diceescritura.domain

import com.kaironyx.diceescritura.domain.usecase.EstadoMemoria
import com.kaironyx.diceescritura.domain.usecase.EvaluadorMemoria
import com.kaironyx.diceescritura.domain.usecase.GradoMemoria
import com.kaironyx.diceescritura.domain.usecase.Memorizacion
import com.kaironyx.diceescritura.domain.usecase.Ocultador
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MemorizacionTest {

    private val hoy = LocalDate.of(2026, 10, 13)
    private val texto = "Texto de prueba, no es RVR1960: uno dos tres cuatro cinco seis siete ocho."

    // --- Repetición espaciada ---

    @Test
    fun `empieza en el grado mas bajo y disponible hoy`() {
        val e = Memorizacion.iniciar(hoy)
        assertEquals(EstadoMemoria(0, 0, hoy), e)
        assertTrue(Memorizacion.estaVencido(e, hoy))
    }

    @Test
    fun `acertar sube de grado y permite seguir hoy`() {
        var e = Memorizacion.iniciar(hoy)
        for (esperado in 1..3) {
            e = Memorizacion.registrar(e, acierto = true, hoy = hoy)
            assertEquals(esperado, e.grado)
            assertEquals(hoy, e.proximaFecha)
            assertEquals(0, e.dominio)
        }
        assertEquals(GradoMemoria.ESCRIBIR, GradoMemoria.desdeIndice(e.grado))
    }

    @Test
    fun `acertar escribiendo aleja el repaso con intervalos crecientes`() {
        var e = EstadoMemoria(grado = 3, dominio = 0, proximaFecha = hoy)
        var dia = hoy
        val dias = mutableListOf<Long>()
        repeat(8) {
            e = Memorizacion.registrar(e, true, dia)
            dias += java.time.temporal.ChronoUnit.DAYS.between(dia, e.proximaFecha)
            assertEquals(3, e.grado)
            dia = e.proximaFecha // el siguiente repaso se hace cuando toca
        }
        assertEquals(listOf(1L, 3L, 7L, 14L, 30L, 60L, 60L, 60L), dias)
        assertEquals(8, e.dominio)
    }

    @Test
    fun `practicar antes de que toque no suma dominio ni cambia el repaso`() {
        val e = EstadoMemoria(grado = 3, dominio = 2, proximaFecha = hoy.plusDays(7))
        assertEquals(e, Memorizacion.registrar(e, true, hoy))
    }

    @Test
    fun `fallar antes de que toque si baja de grado`() {
        val e = Memorizacion.registrar(EstadoMemoria(3, 2, hoy.plusDays(7)), false, hoy)
        assertEquals(2, e.grado)
        assertEquals(1, e.dominio)
        assertEquals(hoy.plusDays(1), e.proximaFecha)
    }

    @Test
    fun `un fallo baja un grado y devuelve el repaso a manana`() {
        val e = Memorizacion.registrar(EstadoMemoria(2, 0, hoy), false, hoy)
        assertEquals(1, e.grado)
        assertEquals(hoy.plusDays(1), e.proximaFecha)
        assertEquals(0, Memorizacion.registrar(EstadoMemoria(0, 0, hoy), false, hoy).grado)
    }

    @Test
    fun `fallar escribiendo baja de grado y de dominio sin pasar de cero`() {
        val e = Memorizacion.registrar(EstadoMemoria(3, 4, hoy), false, hoy)
        assertEquals(2, e.grado)
        assertEquals(3, e.dominio)
        assertEquals(0, Memorizacion.registrar(EstadoMemoria(3, 0, hoy), false, hoy).dominio)
    }

    @Test
    fun `vencimiento`() {
        assertTrue(Memorizacion.estaVencido(EstadoMemoria(3, 1, hoy.minusDays(2)), hoy))
        assertFalse(Memorizacion.estaVencido(EstadoMemoria(3, 1, hoy.plusDays(3)), hoy))
    }

    // --- Ocultar palabras ---

    @Test
    fun `tokeniza palabras y separadores sin perder texto`() {
        val tokens = Ocultador.tokenizar(texto)
        assertEquals(texto, tokens.joinToString("") { it.texto })
        val palabras = tokens.filter { it.esPalabra }.map { it.texto }
        assertEquals(listOf("Texto", "de", "prueba", "no", "es", "RVR1960", "uno"), palabras.take(7))
    }

    @Test
    fun `oculta el porcentaje pedido de palabras`() {
        val total = Ocultador.tokenizar(texto).count { it.esPalabra } // 14
        for (porcentaje in listOf(20, 40, 60)) {
            val oculto = Ocultador.ocultar(texto, porcentaje, semilla = 1)
            assertEquals(Math.round(total * porcentaje / 100.0).toInt(), oculto.ocultas.size)
            assertTrue(oculto.ocultas.all { oculto.tokens[it].esPalabra })
        }
        assertEquals(total, Ocultador.ocultar(texto, 100, 1).ocultas.size)
        assertTrue(Ocultador.ocultar(texto, 0, 1).ocultas.isEmpty())
    }

    @Test
    fun `la eleccion es determinista para una misma semilla`() {
        assertEquals(Ocultador.ocultar(texto, 40, 7), Ocultador.ocultar(texto, 40, 7))
        assertTrue(Ocultador.ocultar(texto, 40, 7) != Ocultador.ocultar(texto, 40, 8))
    }

    @Test
    fun `siempre oculta al menos una palabra si hay palabras`() {
        assertEquals(1, Ocultador.ocultar("Una dos tres.", 20, 1).ocultas.size)
        assertTrue(Ocultador.ocultar("...", 60, 1).ocultas.isEmpty())
    }

    @Test
    fun `las palabras ocultas salen en orden de aparicion`() {
        val oculto = Ocultador.ocultar("a1 b2 c3 d4 e5", 60, 3)
        val enOrden = oculto.tokens.filter { it.esPalabra }.map { it.texto }.filter { it in oculto.palabrasOcultas }
        assertEquals(enOrden, oculto.palabrasOcultas)
    }

    // --- Evaluar lo escrito ---

    @Test
    fun `ignora mayusculas, tildes y puntuacion`() {
        val r = EvaluadorMemoria.evaluar(listOf("Dió", "gracias,", "Señor"), "dio gracias senor")
        assertEquals(3, r.aciertos)
        assertTrue(r.aprobado)
    }

    @Test
    fun `una palabra olvidada no desplaza las siguientes`() {
        val esperadas = EvaluadorMemoria.palabras(texto)
        val sinUna = "Texto prueba no es RVR1960 uno dos tres cuatro cinco seis siete ocho"
        val r = EvaluadorMemoria.evaluar(esperadas, sinUna)
        assertEquals(esperadas.size - 1, r.aciertos)
    }

    @Test
    fun `se aprueba con el 90 por ciento`() {
        val diez = (1..10).map { "p$it" }
        assertTrue(EvaluadorMemoria.evaluar(diez, "p1 p2 p3 p4 p5 p6 p7 p8 p9 mal").aprobado)
        assertFalse(EvaluadorMemoria.evaluar(diez, "p1 p2 p3 p4 p5 p6 p7 p8 mal mal").aprobado)
        // Con pocas palabras hay que acertarlas todas.
        assertFalse(EvaluadorMemoria.evaluar(listOf("a", "b", "c"), "a b x").aprobado)
    }

    @Test
    fun `copiar el pasaje completo no aprueba un grado con huecos`() {
        // Faltan 2 palabras (b y f) y se escriben las 8: 2 aciertos menos 6 sobrantes.
        val r = EvaluadorMemoria.evaluar(listOf("b", "f"), "a b c d e f g h")
        assertEquals(0, r.aciertos)
        assertFalse(r.aprobado)
    }

    @Test
    fun `una palabra de mas se tolera si hay margen y se descuenta si no`() {
        val veinte = (1..20).map { "p$it" }
        val conExtra = veinte.joinToString(" ") + " extra"
        val r = EvaluadorMemoria.evaluar(veinte, conExtra)
        assertEquals(19, r.aciertos) // 20 en orden menos 1 sobrante
        assertTrue(r.aprobado) // 95 %
        assertFalse(EvaluadorMemoria.evaluar(listOf("a", "b"), "a b c").aprobado)
    }

    @Test
    fun `escribir exactamente las palabras esperadas sigue dando todos los aciertos`() {
        val r = EvaluadorMemoria.evaluar(listOf("Porque", "de", "tal"), "porque, de tal")
        assertEquals(3, r.aciertos)
        assertTrue(r.aprobado)
    }

    @Test
    fun `lo escrito vacio o fuera de orden no aprueba`() {
        val esperadas = listOf("uno", "dos", "tres")
        assertEquals(0, EvaluadorMemoria.evaluar(esperadas, "").aciertos)
        assertFalse(EvaluadorMemoria.evaluar(esperadas, "tres dos uno").aprobado)
        assertEquals(0, EvaluadorMemoria.evaluar(emptyList(), "algo").total)
        assertFalse(EvaluadorMemoria.evaluar(emptyList(), "algo").aprobado)
    }

    @Test
    fun `marcar senala las palabras acertadas y las olvidadas`() {
        val marcas = EvaluadorMemoria.marcar(listOf("uno", "dos", "tres", "cuatro"), "uno tres cuatro")
        assertEquals(listOf(true, false, true, true), marcas)
    }

    @Test
    fun `marcar ignora tildes y mayusculas, y coincide con evaluar`() {
        val esperadas = listOf("Dió", "Señor", "amor")
        val escrito = "dio SEÑOR"
        val marcas = EvaluadorMemoria.marcar(esperadas, escrito)
        assertEquals(listOf(true, true, false), marcas)
        assertEquals(EvaluadorMemoria.evaluar(esperadas, escrito).aciertos, marcas.count { it })
    }
}
