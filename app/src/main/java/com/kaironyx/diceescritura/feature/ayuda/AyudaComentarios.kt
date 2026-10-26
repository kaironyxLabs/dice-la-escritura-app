package com.kaironyx.diceescritura.feature.ayuda

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kaironyx.diceescritura.BuildConfig
import com.kaironyx.diceescritura.R
import com.kaironyx.diceescritura.core.ui.BotonDorado
import com.kaironyx.diceescritura.core.ui.TarjetaPergamino

/** Destino de los reportes y comentarios. Es el único lugar donde está escrito. */
const val CORREO_CONTACTO = "contacto@kaironixlabs.com"

/** Datos técnicos que la persona puede adjuntar; nunca incluyen nombre, avance ni texto bíblico. */
internal fun informeTecnico(): String =
    "App: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n" +
        "Teléfono: ${Build.MANUFACTURER} ${Build.MODEL}\n" +
        "Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

/** Intent de correo (sin permiso de red: lo envía la app de correo que la persona elija). */
internal fun intentCorreo(asunto: String, cuerpo: String): Intent =
    Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(CORREO_CONTACTO))
        putExtra(Intent.EXTRA_SUBJECT, asunto)
        putExtra(Intent.EXTRA_TEXT, cuerpo)
    }

/** Tarjeta para reportar un error o enviar un comentario. */
@Composable
fun TarjetaAyuda(modifier: Modifier = Modifier, semilla: Int = 4) {
    val contexto = LocalContext.current
    var esError by rememberSaveable { mutableStateOf(true) }
    var mensaje by rememberSaveable { mutableStateOf("") }
    var conDatos by rememberSaveable { mutableStateOf(true) }
    var sinCorreo by rememberSaveable { mutableStateOf(false) }
    val asunto = stringResource(if (esError) R.string.ayuda_asunto_error else R.string.ayuda_asunto_comentario)

    TarjetaPergamino(modifier = modifier.fillMaxWidth(), semilla = semilla) {
        Text(stringResource(R.string.ayuda_titulo), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.ayuda_descripcion), style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = esError, onClick = { esError = true }, label = { Text(stringResource(R.string.ayuda_tipo_error)) })
            FilterChip(selected = !esError, onClick = { esError = false }, label = { Text(stringResource(R.string.ayuda_tipo_comentario)) })
        }
        OutlinedTextField(
            value = mensaje,
            onValueChange = { mensaje = it.take(2000) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
            label = { Text(stringResource(if (esError) R.string.ayuda_pista_error else R.string.ayuda_pista_comentario)) },
        )
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                .toggleable(value = conDatos, role = Role.Checkbox, onValueChange = { conDatos = it }),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = conDatos, onCheckedChange = null)
            Column(Modifier.padding(start = 8.dp)) {
                Text(stringResource(R.string.ayuda_datos_tecnicos), style = MaterialTheme.typography.bodyMedium)
                if (conDatos) {
                    Text(informeTecnico(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        BotonDorado(
            texto = stringResource(R.string.ayuda_enviar),
            onClick = {
                val cuerpo = mensaje.trim() + if (conDatos) "\n\n--\n" + informeTecnico() else ""
                sinCorreo = !abrirCorreo(contexto, asunto, cuerpo)
            },
        )
        if (sinCorreo) {
            Text(
                stringResource(R.string.ayuda_sin_correo, CORREO_CONTACTO),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

private fun abrirCorreo(contexto: Context, asunto: String, cuerpo: String): Boolean = try {
    contexto.startActivity(intentCorreo(asunto, cuerpo).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    true
} catch (_: ActivityNotFoundException) {
    false
}
