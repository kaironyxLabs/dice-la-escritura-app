package com.example.lampara.data.repository

import com.example.lampara.data.db.LamparaDatabase
import com.example.lampara.data.db.PerfilUsuarioEntity
import com.example.lampara.domain.usecase.NombrePerfil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** Identidad de la persona que estudia: solo nombre, avatar y desde cuándo. Sin cuenta ni servidor. */
data class PerfilLocal(val nombre: String, val avatar: String, val desde: LocalDate)

@Singleton
class PerfilRepository @Inject constructor(
    db: LamparaDatabase,
    private val activo: PerfilActivo,
    private val clock: Clock,
) {
    private val dao = db.perfilUsuarioDao()

    fun observarActivo(): Flow<PerfilLocal?> = activo.observar { dao.observar(it) }.map { it?.aDominio() }

    /** Guarda nombre y avatar del perfil activo. Un nombre vacío deja el que había. */
    suspend fun guardar(nombre: String, avatar: String) {
        val actual = dao.obtener(activo.actual) ?: return
        dao.guardar(
            actual.copy(
                nombre = NombrePerfil.normalizar(nombre) ?: actual.nombre,
                avatar = NombrePerfil.avatarValido(avatar),
            ),
        )
    }

    private fun PerfilUsuarioEntity.aDominio() =
        PerfilLocal(nombre, NombrePerfil.avatarValido(avatar), Instant.ofEpochMilli(creadoEnMs).atZone(clock.zone).toLocalDate())
}
