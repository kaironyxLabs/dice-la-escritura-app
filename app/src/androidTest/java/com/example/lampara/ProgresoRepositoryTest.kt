package com.example.lampara

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lampara.data.content.LectorNiveles
import com.example.lampara.data.db.LamparaDatabase
import com.example.lampara.data.db.PerfilEntity
import com.example.lampara.data.repository.ProgresoRepository
import com.example.lampara.domain.usecase.GestorNiveles
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/** Progreso sobre Room en memoria. Se ejecuta en un dispositivo o emulador. */
@RunWith(AndroidJUnit4::class)
class ProgresoRepositoryTest {

    private lateinit var db: LamparaDatabase
    private lateinit var repo: ProgresoRepository

    @Before
    fun abrir() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(contexto, LamparaDatabase::class.java).build()
        val niveles = contexto.assets.open("content/levels.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val reloj = Clock.fixed(Instant.parse("2026-10-14T15:00:00Z"), ZoneOffset.UTC)
        repo = ProgresoRepository(db, GestorNiveles(LectorNiveles.leer(niveles)), reloj)
    }

    @After
    fun cerrar() = db.close()

    @Test
    fun devuelveElXpQueDeVerdadoEntroEnLaBarra() = runBlocking {
        // El nivel 1 se llena con 150 XP: con 140, de 25 XP ganados solo entran 10.
        db.perfilDao().guardar(PerfilEntity(nivel = 1, xp = 140))
        assertEquals(10, repo.registrarRespuesta("p1", acerto = true, xp = 25))
        assertEquals(150, repo.perfilActual().xp)

        // Con la barra llena no entra nada, pero la pregunta sí queda registrada para el repaso.
        assertEquals(0, repo.registrarRespuesta("p2", acerto = true, xp = 8))
        assertEquals(150, repo.perfilActual().xp)
        assertEquals(setOf("p1", "p2"), repo.seguimiento().keys)
    }

    @Test
    fun sinTopeEntraTodoElXpYUnaRespuestaSinXpNoCambiaElPerfil() = runBlocking {
        db.perfilDao().guardar(PerfilEntity(nivel = 1, xp = 20))
        assertEquals(8, repo.registrarRespuesta("p1", acerto = true, xp = 8))
        assertEquals(28, repo.perfilActual().xp)
        assertEquals(0, repo.registrarRespuesta("p1", acerto = false, xp = 0))
        assertEquals(28, repo.perfilActual().xp)
    }
}
