package com.example.lampara

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lampara.data.db.LamparaDatabase
import com.example.lampara.data.repository.RetoDiarioRepository
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

    private lateinit var db: LamparaDatabase
    private lateinit var repo: RetoDiarioRepository

    @Before
    fun abrir() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(contexto, LamparaDatabase::class.java).build()
        repo = RetoDiarioRepository(db, reloj)
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

        val reto = db.retoDiarioDao().obtener(dia1.toEpochDay())!!
        assertEquals(2, reto.respondidas)
        assertEquals(2, reto.aciertos)
        assertEquals(16, reto.xpGanado)
        assertTrue(reto.completado)
        assertNull(db.retoDiarioDao().obtener(dia2.toEpochDay()))
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

    @Test
    fun crearDosVecesElRetoDeHoyConservaElPrimero() = runBlocking {
        val primero = repo.crearRetoDeHoy(listOf("a", "b"))
        val segundo = repo.crearRetoDeHoy(listOf("x", "y", "z"))
        assertEquals(primero.preguntaIds, segundo.preguntaIds)
        assertFalse(segundo.completado)
    }
}
