package com.example.lampara

import org.junit.Assert.assertEquals
import org.junit.Test

class HumoTest {
    @Test
    fun `el paquete base es el provisional`() {
        assertEquals("com.example.lampara", LamparaApp::class.java.`package`?.name)
    }
}
