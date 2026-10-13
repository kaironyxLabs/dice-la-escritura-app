package com.example.lampara

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lampara.core.theme.LamparaTheme
import com.example.lampara.domain.model.Categoria
import com.example.lampara.domain.model.Nivel
import com.example.lampara.domain.usecase.EstadoNivel
import com.example.lampara.domain.usecase.Racha
import com.example.lampara.feature.home.EstadoReto
import com.example.lampara.feature.home.InicioContenido
import com.example.lampara.feature.home.InicioUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de humo de la pantalla de inicio. Se ejecutan en un dispositivo o emulador con
 * `./gradlew connectedDebugAndroidTest`; no necesitan Hilt ni datos reales.
 */
@RunWith(AndroidJUnit4::class)
class InicioHumoTest {

    @get:Rule
    val compose = createComposeRule()

    private val semilla = Nivel(1, "Semilla", 0, 1, setOf(Categoria.LIBROS))

    private fun estado(
        reto: EstadoReto = EstadoReto.SinEmpezar,
        examenDisponible: Boolean = false,
        racha: Racha = Racha(0, 0, completadoHoy = false, graciaDisponibleEstaSemana = true),
    ) = InicioUiState(
        nivel = EstadoNivel(semilla, xpEnNivel = 60, xpDelNivel = 150, fraccion = 0.4f, examenDisponible = examenDisponible),
        racha = racha,
        reto = reto,
    )

    @Test
    fun muestraNivelBarraYBotonesPrincipales() {
        compose.setContent {
            LamparaTheme { InicioContenido(estado(), onJugarPorCategoria = {}, onExamen = {}, onReto = {}) }
        }
        compose.onNodeWithText("Semilla").assertIsDisplayed()
        compose.onNodeWithText("Nivel 1").assertIsDisplayed()
        compose.onNodeWithText("60 de 150 XP").assertIsDisplayed()
        compose.onNodeWithText("Empezar el reto de hoy").assertIsDisplayed()
        compose.onNodeWithText("Jugar por categoría").assertIsDisplayed()
        // Sin la barra llena no se ofrece el examen de ascenso.
        compose.onAllNodes(hasText("Examen de ascenso")).assertCountEquals(0)
    }

    @Test
    fun ofreceElExamenCuandoLaBarraEstaLlenaYLosBotonesResponden() {
        var examen = 0
        var reto = 0
        compose.setContent {
            LamparaTheme {
                InicioContenido(
                    estado(examenDisponible = true, reto = EstadoReto.EnCurso(respondidas = 4, total = 10)),
                    onJugarPorCategoria = {},
                    onExamen = { examen++ },
                    onReto = { reto++ },
                )
            }
        }
        compose.onNodeWithText("Examen de ascenso").performClick()
        compose.onNodeWithText("Continuar el reto").performClick()
        assertTrue(examen == 1 && reto == 1)
    }

    @Test
    fun muestraLaRachaConTonoSobrioYElRetoCompletado() {
        compose.setContent {
            LamparaTheme {
                InicioContenido(
                    estado(
                        reto = EstadoReto.Completado(aciertos = 8, total = 10),
                        racha = Racha(actual = 5, mejor = 9, completadoHoy = true, graciaDisponibleEstaSemana = false),
                    ),
                    onJugarPorCategoria = {},
                    onExamen = {},
                    onReto = {},
                )
            }
        }
        compose.onNodeWithText("Racha: 5 días").assertIsDisplayed()
        compose.onNodeWithText("Mejor racha: 9").assertIsDisplayed()
        compose.onNodeWithText("Completaste el reto de hoy: 8 de 10 correctas.").assertIsDisplayed()
    }

    @Test
    fun mientrasCargaMuestraUnMensajeSinFallar() {
        compose.setContent {
            LamparaTheme { InicioContenido(null, onJugarPorCategoria = {}, onExamen = {}, onReto = {}) }
        }
        compose.onNodeWithText("Cargando…").assertIsDisplayed()
    }
}
