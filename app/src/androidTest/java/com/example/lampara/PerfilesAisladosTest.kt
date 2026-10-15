package com.example.lampara

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lampara.data.content.LectorNiveles
import com.example.lampara.data.db.AvanceEntity
import com.example.lampara.data.db.LamparaDatabase
import com.example.lampara.data.db.PerfilUsuarioEntity
import com.example.lampara.data.repository.PerfilActivo
import com.example.lampara.data.repository.ProgresoRepository
import com.example.lampara.domain.model.Perfil
import com.example.lampara.domain.usecase.GestorNiveles
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/** Dos perfiles en la misma base no se mezclan, y borrar uno se lleva solo su avance. */
@RunWith(AndroidJUnit4::class)
class PerfilesAisladosTest {

    private lateinit var db: LamparaDatabase
    private lateinit var activo: PerfilActivo
    private lateinit var repo: ProgresoRepository

    @Before
    fun abrir() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(contexto, LamparaDatabase::class.java).build()
        runBlocking {
            db.perfilUsuarioDao().guardar(PerfilUsuarioEntity(1, "Ana", "", 0))
            db.perfilUsuarioDao().guardar(PerfilUsuarioEntity(2, "Luis", "", 0))
        }
        val niveles = contexto.assets.open("content/levels.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val reloj = Clock.fixed(Instant.parse("2026-10-15T15:00:00Z"), ZoneOffset.UTC)
        activo = PerfilActivo()
        repo = ProgresoRepository(db, GestorNiveles(LectorNiveles.leer(niveles)), reloj, activo)
    }

    @After
    fun cerrar() = db.close()

    @Test
    fun cadaPerfilTieneSuPropioXpYSeguimiento() = runBlocking {
        activo.cambiarA(1)
        repo.registrarRespuesta("p1", acerto = true, xp = 10)
        activo.cambiarA(2)
        assertEquals(Perfil.INICIAL, repo.perfilActual())
        assertEquals(emptySet<String>(), repo.seguimiento().keys)
        repo.registrarRespuesta("p2", acerto = false, xp = 5)

        activo.cambiarA(1)
        assertEquals(10, repo.perfilActual().xp)
        assertEquals(setOf("p1"), repo.seguimiento().keys)
    }

    @Test
    fun eliminarUnPerfilBorraSoloSuAvance() = runBlocking {
        db.avanceDao().guardar(AvanceEntity(1, 2, 30))
        db.avanceDao().guardar(AvanceEntity(2, 4, 90))
        db.perfilUsuarioDao().eliminar(2)
        assertNull(db.avanceDao().obtener(2))
        assertEquals(30, db.avanceDao().obtener(1)!!.xp)
    }
}
