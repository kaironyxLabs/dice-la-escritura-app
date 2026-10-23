package com.kaironyx.diceescritura.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaironyx.diceescritura.R
import com.kaironyx.diceescritura.core.theme.DiceEscrituraTheme
import com.kaironyx.diceescritura.core.theme.Espacio

/**
 * Pantalla de desarrollo: muestra los componentes base para revisarlos de un vistazo, en claro,
 * oscuro (según el tema del sistema) y con la fuente grande (ajuste del sistema). Solo se llega a
 * ella desde Ajustes en builds debug.
 */
@Composable
fun CatalogoComponentes(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Espacio.l, vertical = Espacio.m),
        verticalArrangement = Arrangement.spacedBy(Espacio.m),
    ) {
        Text(stringResource(R.string.catalogo_titulo), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.catalogo_intro), style = MaterialTheme.typography.bodyMedium)

        Seccion(R.string.catalogo_tipografia)
        Text(stringResource(R.string.catalogo_muestra), style = MaterialTheme.typography.displaySmall)
        Text(stringResource(R.string.catalogo_muestra), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.catalogo_muestra), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.catalogo_muestra), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.catalogo_muestra), style = MaterialTheme.typography.bodyLarge)
        Text(stringResource(R.string.catalogo_muestra), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.catalogo_muestra), style = MaterialTheme.typography.labelLarge)

        Seccion(R.string.catalogo_botones)
        BotonPrincipal(stringResource(R.string.catalogo_boton_principal), onClick = {})
        BotonSecundario(stringResource(R.string.catalogo_boton_secundario), onClick = {})
        BotonPrincipal(stringResource(R.string.catalogo_boton_deshabilitado), onClick = {}, habilitado = false)

        Seccion(R.string.catalogo_tarjetas)
        TarjetaContenido(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.catalogo_tarjeta_titulo), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.catalogo_tarjeta_cuerpo), style = MaterialTheme.typography.bodyMedium)
        }
        TarjetaContenido(modifier = Modifier.fillMaxWidth(), onClick = {}) {
            Text(stringResource(R.string.catalogo_tarjeta_tocable), style = MaterialTheme.typography.titleMedium)
        }

        Seccion(R.string.catalogo_citas)
        Row(horizontalArrangement = Arrangement.spacedBy(Espacio.s)) {
            ChipCita(stringResource(R.string.catalogo_cita_uno))
            ChipCita(stringResource(R.string.catalogo_cita_dos))
        }

        Seccion(R.string.catalogo_progreso)
        BarraProgresoAnimada(fraccion = 0.4f, descripcion = stringResource(R.string.catalogo_progreso_descripcion))
        Row(horizontalArrangement = Arrangement.spacedBy(Espacio.m), verticalAlignment = Alignment.CenterVertically) {
            InsigniaNivel(1)
            InsigniaNivel(7)
            InsigniaNivel(10)
        }

        Seccion(R.string.catalogo_jardin)
        JardinNivel(nivel = 4, celebrar = true)

        Seccion(R.string.catalogo_retroalimentacion)
        ContenedorRetroalimentacion(
            acierto = true,
            titulo = stringResource(R.string.catalogo_acierto),
            cuerpo = stringResource(R.string.catalogo_explicacion),
        )
        ContenedorRetroalimentacion(
            acierto = false,
            titulo = stringResource(R.string.catalogo_error),
            cuerpo = stringResource(R.string.catalogo_explicacion),
        )
    }
}

@Composable
private fun Seccion(titulo: Int) {
    Text(
        stringResource(titulo),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Preview(name = "Claro", showBackground = true)
@Composable
private fun CatalogoClaroPreview() = DiceEscrituraTheme(darkTheme = false) { CatalogoComponentes() }

@Preview(name = "Oscuro", showBackground = true, backgroundColor = 0xFF17140D)
@Composable
private fun CatalogoOscuroPreview() = DiceEscrituraTheme(darkTheme = true) { CatalogoComponentes() }

@Preview(name = "Fuente grande", showBackground = true, fontScale = 2f)
@Composable
private fun CatalogoFuenteGrandePreview() = DiceEscrituraTheme(darkTheme = false) { CatalogoComponentes() }
