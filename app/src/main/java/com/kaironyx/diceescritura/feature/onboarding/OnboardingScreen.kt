package com.kaironyx.diceescritura.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kaironyx.diceescritura.R
import com.kaironyx.diceescritura.feature.perfil.EditorPerfil

/**
 * Bienvenida. Presenta el módulo opcional de la Confesión de 1689 con tono neutral y deja elegir:
 * ninguna de las dos opciones está destacada por encima de la otra más que por el orden.
 */
@Composable
fun OnboardingScreen(
    alTerminar: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var avatar by rememberSaveable { mutableStateOf("c0") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.onboarding_titulo), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.onboarding_intro), style = MaterialTheme.typography.bodyLarge)
        Text(stringResource(R.string.onboarding_perfil_titulo), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.onboarding_perfil_descripcion), style = MaterialTheme.typography.bodyMedium)
        EditorPerfil(nombre, avatar, { nombre = it }, { avatar = it })
        Text(stringResource(R.string.onboarding_confesion_titulo), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.onboarding_confesion_descripcion), style = MaterialTheme.typography.bodyLarge)
        Text(
            stringResource(R.string.onboarding_confesion_cambiar),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = { viewModel.terminar(nombre, avatar, activarConfesion = true, alTerminar = alTerminar) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        ) { Text(stringResource(R.string.onboarding_activar)) }
        OutlinedButton(
            onClick = { viewModel.terminar(nombre, avatar, activarConfesion = false, alTerminar = alTerminar) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        ) { Text(stringResource(R.string.onboarding_ahora_no)) }
    }
}
