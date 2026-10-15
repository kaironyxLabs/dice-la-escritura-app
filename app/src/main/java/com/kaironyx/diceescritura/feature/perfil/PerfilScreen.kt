package com.kaironyx.diceescritura.feature.perfil

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaironyx.diceescritura.R
import com.kaironyx.diceescritura.core.ui.EstadoCargando
import com.kaironyx.diceescritura.domain.usecase.NombrePerfil
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun PerfilScreen(
    alVolver: () -> Unit,
    viewModel: PerfilViewModel = hiltViewModel(),
) {
    val perfil by viewModel.perfil.collectAsStateWithLifecycle()
    var nombre by rememberSaveable { mutableStateOf<String?>(null) }
    var avatar by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(perfil) {
        perfil?.let {
            if (nombre == null) nombre = it.nombre
            if (avatar == null) avatar = it.avatar
        }
    }
    val actual = perfil
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.perfil_titulo), style = MaterialTheme.typography.headlineMedium)
        if (actual == null || nombre == null || avatar == null) {
            EstadoCargando()
            return@Column
        }
        EditorPerfil(nombre!!, avatar!!, { nombre = it }, { avatar = it })
        Text(
            stringResource(
                R.string.perfil_desde,
                actual.desde.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)),
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = { viewModel.guardar(nombre!!, avatar!!, alVolver) },
            enabled = NombrePerfil.normalizar(nombre!!) != null,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        ) { Text(stringResource(R.string.perfil_guardar)) }
        OutlinedButton(
            onClick = alVolver,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        ) { Text(stringResource(R.string.perfil_cancelar)) }
    }
}

/** Campo de nombre y selector de avatar, compartidos con la bienvenida. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun EditorPerfil(
    nombre: String,
    avatar: String,
    alCambiarNombre: (String) -> Unit,
    alCambiarAvatar: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        androidx.compose.material3.OutlinedTextField(
            value = nombre,
            onValueChange = { alCambiarNombre(it.take(NombrePerfil.MAX)) },
            label = { Text(stringResource(R.string.perfil_nombre)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(stringResource(R.string.perfil_avatar), style = MaterialTheme.typography.titleMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            for (i in 0 until NombrePerfil.CANTIDAD_AVATARES) {
                val id = "c$i"
                val elegido = id == avatar
                val descripcion = stringResource(R.string.perfil_avatar_opcion, i + 1)
                AvatarCircular(
                    nombre = nombre.ifBlank { "?" },
                    avatar = id,
                    tamano = 56.dp,
                    modifier = Modifier
                        .then(if (elegido) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                        .clickable { alCambiarAvatar(id) }
                        .semantics(mergeDescendants = true) {
                            contentDescription = descripcion
                            role = Role.RadioButton
                            selected = elegido
                        },
                )
            }
        }
    }
}
