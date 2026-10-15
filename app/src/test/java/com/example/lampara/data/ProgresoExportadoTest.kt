package com.example.lampara.data

import com.example.lampara.data.export.AjustesExp
import com.example.lampara.data.export.CodecProgreso
import com.example.lampara.data.export.LecturaProgreso
import com.example.lampara.data.export.PerfilExp
import com.example.lampara.data.export.PreguntaExp
import com.example.lampara.data.export.ProgresoExportado
import com.example.lampara.data.export.RetoExp
import com.example.lampara.data.export.SesionExp
import com.example.lampara.data.export.UsuarioExp
import com.example.lampara.data.export.VersiculoExp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgresoExportadoTest {

    private fun ejemplo() = ProgresoExportado(
        exportadoEn = "2026-10-13T12:00:00Z",
        usuario = UsuarioExp("Ana María", "c3", 1_760_000_000_000),
        perfil = PerfilExp(nivel = 3, xp = 612),
        preguntas = listOf(
            PreguntaExp("core-libros-0001", caja = 2, proximaFechaDia = 20_000, aciertos = 4, fallos = 1, ultimaFechaDia = 19_998),
            PreguntaExp("confesion1689-confesion-0003", caja = 1, proximaFechaDia = 20_001, aciertos = 0, fallos = 2, ultimaFechaDia = 20_000),
        ),
        sesiones = listOf(
            SesionExp("partida", categoria = "libros", fechaDia = 19_998, creadaEnMs = 1_700_000_000_000, aciertos = 8, total = 10, xpGanado = 61),
            SesionExp("examen", categoria = null, fechaDia = 19_999, creadaEnMs = 1_700_000_100_000, aciertos = 9, total = 10, xpGanado = 0, aprobado = true),
            SesionExp("reto_diario", fechaDia = 20_000, creadaEnMs = 1_700_000_200_000, aciertos = 5, total = 10, xpGanado = 40),
        ),
        retos = listOf(
            RetoExp(20_000, listOf("core-libros-0001", "core-historia-0002"), respondidas = 2, aciertos = 1, xpGanado = 8, completado = true),
        ),
        versiculos = listOf(
            VersiculoExp("mem-001", memorizando = true, grado = 2, dominio = 1, proximaFechaDia = 20_003, intentos = 5, aciertos = 4),
        ),
        ajustes = AjustesExp(confesionActiva = true, soloRevisadas = false, recordatorioActivo = true, recordatorioMinutos = 1_200),
    )

    @Test
    fun `exportar e importar de ida y vuelta conserva todo`() {
        val original = ejemplo()
        val lectura = CodecProgreso.leer(CodecProgreso.aJson(original))
        assertTrue(lectura.toString(), lectura is LecturaProgreso.Valido)
        assertEquals(original, (lectura as LecturaProgreso.Valido).progreso)
    }

    @Test
    fun `un progreso vacio tambien hace el viaje de ida y vuelta`() {
        val vacio = ProgresoExportado(
            exportadoEn = "2026-10-13T12:00:00Z",
            perfil = PerfilExp(1, 0),
            ajustes = AjustesExp(false, false, false, 1_200),
        )
        assertEquals(vacio, (CodecProgreso.leer(CodecProgreso.aJson(vacio)) as LecturaProgreso.Valido).progreso)
    }

    @Test
    fun `el archivo lleva formato y version`() {
        val json = CodecProgreso.aJson(ejemplo())
        assertTrue("\"formato\":\"lampara-progreso\"" in json)
        assertTrue("\"version\":2" in json)
    }

    private fun invalido(texto: String): List<String> =
        (CodecProgreso.leer(texto) as LecturaProgreso.Invalido).errores

    @Test
    fun `rechaza archivos que no son de la app`() {
        assertTrue(invalido("{no es json").isNotEmpty())
        assertTrue(invalido("{}").isNotEmpty())
        assertTrue(invalido("[1,2,3]").isNotEmpty())
        // Un campo desconocido también es un error: no se acepta a ciegas.
        assertTrue(invalido(CodecProgreso.aJson(ejemplo()).replace("\"perfil\"", "\"otro\":1,\"perfil\"")).isNotEmpty())
    }

    @Test
    fun `lee un archivo de la version 1 sin usuario`() {
        val v1 = CodecProgreso.aJson(ejemplo().copy(usuario = null, version = 1))
            .replace("\"usuario\":null,", "")
        assertTrue("usuario" !in v1)
        val lectura = CodecProgreso.leer(v1)
        assertTrue(lectura.toString(), lectura is LecturaProgreso.Valido)
        assertEquals(null, (lectura as LecturaProgreso.Valido).progreso.usuario)
        assertEquals(1, lectura.progreso.version)
    }

    @Test
    fun `rechaza otra version o formato`() {
        val json = CodecProgreso.aJson(ejemplo())
        assertTrue(invalido(json.replace("\"version\":2", "\"version\":3")).any { "Versión" in it })
        assertTrue(invalido(json.replace("\"version\":2", "\"version\":0")).any { "Versión" in it })
        assertTrue(invalido(json.replace("lampara-progreso", "otra-cosa")).any { "Formato" in it })
    }

    @Test
    fun `rechaza valores fuera de rango`() {
        val base = ejemplo()
        fun errores(p: ProgresoExportado) = CodecProgreso.validar(p)
        assertTrue(errores(base.copy(perfil = PerfilExp(0, 10))).isNotEmpty())
        assertTrue(errores(base.copy(perfil = PerfilExp(1, -1))).isNotEmpty())
        assertTrue(errores(base.copy(preguntas = listOf(base.preguntas[0].copy(caja = 6)))).isNotEmpty())
        assertTrue(errores(base.copy(preguntas = listOf(base.preguntas[0], base.preguntas[0]))).any { "repetidas" in it })
        assertTrue(errores(base.copy(sesiones = listOf(base.sesiones[0].copy(aciertos = 11)))).isNotEmpty())
        assertTrue(errores(base.copy(sesiones = listOf(base.sesiones[0].copy(tipo = "inventada")))).isNotEmpty())
        assertTrue(errores(base.copy(sesiones = listOf(base.sesiones[0].copy(categoria = "inventada")))).isNotEmpty())
        assertTrue(errores(base.copy(retos = listOf(base.retos[0].copy(aciertos = 3)))).isNotEmpty())
        // Más respondidas que preguntas en el reto no tiene sentido.
        assertTrue(errores(base.copy(retos = listOf(base.retos[0].copy(respondidas = 3, aciertos = 1)))).isNotEmpty())
        assertTrue(errores(base.copy(versiculos = listOf(base.versiculos[0].copy(grado = 4)))).isNotEmpty())
        assertTrue(errores(base.copy(ajustes = base.ajustes.copy(recordatorioMinutos = 1_440))).isNotEmpty())
        assertTrue(errores(base.copy(usuario = UsuarioExp("   ", "c0", 0))).isNotEmpty())
        assertTrue(errores(base.copy(usuario = UsuarioExp("x".repeat(31), "c0", 0))).isNotEmpty())
        assertTrue(errores(base.copy(usuario = UsuarioExp("Ana", "rojo", 0))).isNotEmpty())
        assertTrue(errores(base.copy(version = 1)).any { "versión 1" in it })
        assertTrue(errores(base).isEmpty())
    }
}
