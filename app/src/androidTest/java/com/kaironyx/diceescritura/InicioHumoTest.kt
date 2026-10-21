package com.kaironyx.diceescritura

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaironyx.diceescritura.core.theme.DiceEscrituraTheme
import com.kaironyx.diceescritura.data.repository.PerfilLocal
import com.kaironyx.diceescritura.domain.model.Categoria
import com.kaironyx.diceescritura.domain.model.Nivel
import com.kaironyx.diceescritura.domain.usecase.EstadoNivel
import com.kaironyx.diceescritura.domain.usecase.Racha
import com.kaironyx.diceescritura.feature.home.EstadoReto
import com.kaironyx.diceescritura.feature.home.InicioContenido
import com.kaironyx.diceescritura.feature.home.InicioUiState
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
        persona = PerfilLocal("Ana", "c0", java.time.LocalDate.of(2026, 10, 1)),
        nivel = EstadoNivel(semilla, xpEnNivel = 60, xpDelNivel = 150, fraccion = 0.4f, examenDisponible = examenDisponible),
        racha = racha,
        reto = reto,
    )

    @Test
    fun muestraNivelBarraYBotonesPrincipales() {
        compose.setContent {
            DiceEscrituraTheme { InicioContenido(estado(), categorias = null, onElegirCategoria = {}, onExamen = {}, onReto = {}, onPerfil = {}) }
        }
        compose.onNodeWithText("Hola, Ana").assertIsDisplayed()
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
            DiceEscrituraTheme {
                InicioContenido(
                    estado(examenDisponible = true, reto = EstadoReto.EnCurso(respondidas = 4, total = 10)),
                    categorias = null,
                    onElegirCategoria = {},
                    onExamen = { examen++ },
                    onReto = { reto++ },
                    onPerfil = {},
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
            DiceEscrituraTheme {
                InicioContenido(
                    estado(
                        reto = EstadoReto.Completado(aciertos = 8, total = 10),
                        racha = Racha(actual = 5, mejor = 9, completadoHoy = true, graciaDisponibleEstaSemana = false),
                    ),
                    categorias = null,
                    onElegirCategoria = {},
                    onExamen = {},
                    onReto = {}, onPerfil = {},
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
            DiceEscrituraTheme { InicioContenido(null, categorias = null, onElegirCategoria = {}, onExamen = {}, onReto = {}, onPerfil = {}) }
        }
        compose.onNodeWithText("Cargando…").assertIsDisplayed()
    }
}
