package com.kaironyx.diceescritura

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaironyx.diceescritura.data.db.DiceEscrituraDatabase
import com.kaironyx.diceescritura.data.db.PerfilUsuarioEntity
import com.kaironyx.diceescritura.data.repository.PerfilActivo
import com.kaironyx.diceescritura.data.repository.RetoDiarioRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Reto diario sobre una base de datos Room en memoria. Se ejecuta en un dispositivo o emulador con
 * `./gradlew connectedDebugAndroidTest`.
 */
@RunWith(AndroidJUnit4::class)
class RetoDiarioRepositoryTest {

    /** Reloj que se mueve a mano para simular el paso de la medianoche. */
    private class RelojManual(var ahora: Instant) : Clock() {
        override fun getZone(): ZoneId = ZoneOffset.UTC
        override fun withZone(zone: ZoneId): Clock = this
        override fun instant(): Instant = ahora
    }

    private val dia1 = LocalDate.of(2026, 10, 13)
    private val dia2 = dia1.plusDays(1)
    private val reloj = RelojManual(Instant.parse("2026-10-13T23:58:00Z"))

    private lateinit var db: DiceEscrituraDatabase
    private lateinit var repo: RetoDiarioRepository

    @Before
    fun abrir() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(contexto, DiceEscrituraDatabase::class.java).build()
        runBlocking { db.perfilUsuarioDao().guardar(PerfilUsuarioEntity(PerfilUsuarioEntity.ID_INICIAL, "Prueba", "", 0)) }
        repo = RetoDiarioRepository(db, reloj, PerfilActivo())
    }

    @After
    fun cerrar() = db.close()

    @Test
    fun unRetoQueCruzaLaMedianocheSeRegistraYCompletaEnElDiaQueEmpezo() = runBlocking {
        repo.crearRetoDeHoy(listOf("a", "b"))
        repo.registrarRespuesta(acerto = true, xp = 8, fecha = dia1)

        reloj.ahora = Instant.parse("2026-10-14T00:02:00Z") // ya es el día siguiente
        repo.registrarRespuesta(acerto = true, xp = 8, fecha = dia1)
        repo.completar(dia1)

        val reto = db.retoDiarioDao().obtener(PerfilUsuarioEntity.ID_INICIAL, dia1.toEpochDay())!!
        assertEquals(2, reto.respondidas)
        assertEquals(2, reto.aciertos)
        assertEquals(16, reto.xpGanado)
        assertTrue(reto.completado)
        assertNull(db.retoDiarioDao().obtener(PerfilUsuarioEntity.ID_INICIAL, dia2.toEpochDay()))
        assertNull(repo.retoDeHoy())
    }

    @Test
    fun sinFechaSeUsaElDiaDeHoy() = runBlocking {
        repo.crearRetoDeHoy(listOf("a"))
        repo.registrarRespuesta(acerto = false, xp = 0)
        repo.completar()
        val reto = repo.retoDeHoy()!!
        assertEquals(1, reto.respondidas)
        assertEquals(0, reto.aciertos)
        assertTrue(reto.completado)
    }

    private fun irAlDia(fecha: LocalDate) {
        reloj.ahora = fecha.atTime(12, 0).toInstant(ZoneOffset.UTC)
    }

    private suspend fun completarReto(fecha: LocalDate) {
        irAlDia(fecha)
        repo.crearRetoDeHoy(listOf("a"))
        repo.registrarRespuesta(acerto = true, xp = 5, fecha = fecha)
        repo.completar(fecha)
    }

    @Test
    fun rachaDeVariosDiasConDiaDeGraciaYRupturaPorSegundoSalto() = runBlocking {
        // Martes 13, miércoles 14 y jueves 15 de octubre de 2026.
        completarReto(LocalDate.of(2026, 10, 13))
        assertEquals(1, repo.observarRacha().first().actual)
        completarReto(LocalDate.of(2026, 10, 14))
        completarReto(LocalDate.of(2026, 10, 15))
        assertEquals(3, repo.observarRacha().first().actual)

        // Viernes 16 sin reto: sábado 17 a primera hora la racha sigue viva (día de gracia pendiente).
        irAlDia(LocalDate.of(2026, 10, 17))
        val sabado = repo.observarRacha().first()
        assertEquals(3, sabado.actual)
        assertFalse(sabado.completadoHoy)

        // Al completar el sábado, el viernes saltado se perdona y la racha crece a 4.
        completarReto(LocalDate.of(2026, 10, 17))
        val tras = repo.observarRacha().first()
        assertEquals(4, tras.actual)
        assertEquals(4, tras.mejor)
        assertFalse(tras.graciaDisponibleEstaSemana)

        // Domingo 18 sin reto y lunes 19 completado: el salto cae en la misma semana que el del viernes.
        // La gracia de esa semana ya estaba gastada: la racha se rompe y vuelve a 1; la mejor se conserva.
        completarReto(LocalDate.of(2026, 10, 19))
        val rota = repo.observarRacha().first()
        assertEquals(1, rota.actual)
        assertEquals(4, rota.mejor)
    }

    @Test
    fun unaRachaSinReto_pasadoDosDiasSePierde() = runBlocking {
        completarReto(LocalDate.of(2026, 10, 13))
        completarReto(LocalDate.of(2026, 10, 14))
        irAlDia(LocalDate.of(2026, 10, 17)) // 15 y 16 sin reto
        val r = repo.observarRacha().first()
        assertEquals(0, r.actual)
        assertEquals(2, r.mejor)
    }

    @Test
    fun crearDosVecesElRetoDeHoyConservaElPrimero() = runBlocking {
        val primero = repo.crearRetoDeHoy(listOf("a", "b"))
        val segundo = repo.crearRetoDeHoy(listOf("x", "y", "z"))
        assertEquals(primero.preguntaIds, segundo.preguntaIds)
        assertFalse(segundo.completado)
    }
}
