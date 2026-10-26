package com.kaironyx.diceescritura.domain

import com.kaironyx.diceescritura.domain.usecase.EscenaHistoria
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull

class EscenaHistoriaTest {
    @Test fun `asigna la escena segun el capitulo`() {
        assertEquals(EscenaHistoria.CREACION, EscenaHistoria.para(listOf("Génesis 1:1")))
        assertEquals(EscenaHistoria.EDEN, EscenaHistoria.para(listOf("Génesis 3:6")))
        assertEquals(EscenaHistoria.PASCUA, EscenaHistoria.para(listOf("Éxodo 12:13")))
        assertEquals(EscenaHistoria.JOSE_EGIPTO, EscenaHistoria.para(listOf("Génesis 41:40")))
    }

    @Test fun `un rango que cruza capitulos toma la primera escena`() {
        assertEquals(EscenaHistoria.CREACION, EscenaHistoria.para(listOf("Génesis 1:1-2:3")))
    }

    @Test fun `sin escena para pasajes no ilustrados o invalidos`() {
        assertNull(EscenaHistoria.para(listOf("Romanos 3:23")))
        assertNull(EscenaHistoria.para(listOf("texto raro")))
        assertNull(EscenaHistoria.para(emptyList()))
    }
}
