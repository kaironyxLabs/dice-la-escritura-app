package com.kaironyx.diceescritura

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** La app es 100 % sin conexión: el manifiesto no debe pedir permisos de red. */
class ManifiestoTest {

    private val manifiesto = File("src/main/AndroidManifest.xml").readText(Charsets.UTF_8)

    @Test
    fun `no declara el permiso INTERNET`() {
        assertFalse(manifiesto.contains("android.permission.INTERNET"))
    }

    @Test
    fun `solo pide el permiso de notificaciones para el recordatorio opcional`() {
        val permisos = Regex("""<uses-permission[^>]*android:name="([^"]+)"""").findAll(manifiesto).map { it.groupValues[1] }.toList()
        assertTrue(permisos.toString(), permisos == listOf("android.permission.POST_NOTIFICATIONS"))
    }

    @Test
    fun `no respalda datos en la nube`() {
        assertTrue(manifiesto.contains("""android:allowBackup="false""""))
    }
}
