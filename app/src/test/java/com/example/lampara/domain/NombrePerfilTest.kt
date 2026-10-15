package com.example.lampara.domain

import com.example.lampara.domain.usecase.NombrePerfil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NombrePerfilTest {
    @Test
    fun recortaYJuntaEspacios() {
        assertEquals("Ana María", NombrePerfil.normalizar("  Ana    María \n"))
    }

    @Test
    fun vacioONadaMasQueEspaciosEsNulo() {
        assertNull(NombrePerfil.normalizar(""))
        assertNull(NombrePerfil.normalizar("   \t "))
    }

    @Test
    fun seCortaEnElMaximoSinDejarEspacioFinal() {
        val largo = "a".repeat(29) + " bbbb"
        val resultado = NombrePerfil.normalizar(largo)!!
        assertEquals(29, resultado.length)
        assertEquals("a".repeat(29), resultado)
        assertEquals(NombrePerfil.MAX, NombrePerfil.normalizar("x".repeat(80))!!.length)
    }

    @Test
    fun avatarDesconocidoCaeEnElPrimero() {
        assertEquals("c3", NombrePerfil.avatarValido("c3"))
        assertEquals("c0", NombrePerfil.avatarValido(""))
        assertEquals("c0", NombrePerfil.avatarValido("c99"))
        assertEquals("c0", NombrePerfil.avatarValido("3"))
        assertEquals("c0", NombrePerfil.avatarValido("x2"))
    }
}
