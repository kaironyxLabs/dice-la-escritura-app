package com.kaironyx.diceescritura

import org.junit.Assert.assertEquals
import org.junit.Test

class HumoTest {
    @Test
    fun `el paquete base es el provisional`() {
        assertEquals("com.kaironyx.diceescritura", DiceEscrituraApp::class.java.`package`?.name)
    }
}
