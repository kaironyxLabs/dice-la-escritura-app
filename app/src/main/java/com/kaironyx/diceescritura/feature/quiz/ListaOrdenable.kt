package com.kaironyx.diceescritura.feature.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.kaironyx.diceescritura.R
import com.kaironyx.diceescritura.core.theme.Espacio

/**
 * Lista que se reordena arrastrando el asa de cada fila. Con TalkBack, cada fila ofrece las acciones
 * «subir» y «bajar». Cada [elementos] se mueve con [alMover] (índice de origen, índice de destino).
 */
@Composable
fun ListaOrdenable(
    elementos: List<String>,
    alMover: (desde: Int, hacia: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var arrastrando by remember { mutableStateOf<String?>(null) }
    var desplazamiento by remember { mutableFloatStateOf(0f) }
    var altoFila by remember { mutableIntStateOf(0) }
    val separacion = with(LocalDensity.current) { Espacio.s.roundToPx() }
    val subir = stringResource(R.string.ordenar_subir)
    val bajar = stringResource(R.string.ordenar_bajar)

    Column(modifier, verticalArrangement = Arrangement.spacedBy(Espacio.s)) {
        elementos.forEachIndexed { indice, elemento -> key(elemento) {
            val activa = elemento == arrastrando
            // La fila sigue al elemento al cambiar de sitio, así el arrastre en curso no se corta.
            val indiceActual by rememberUpdatedState(indice)
            val moverActual by rememberUpdatedState(alMover)
            val ultimo by rememberUpdatedState(elementos.lastIndex)
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(if (activa) 2.dp else 1.dp, if (activa) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .onSizeChanged { altoFila = it.height }
                    .zIndex(if (activa) 1f else 0f)
                    .graphicsLayer { translationY = if (activa) desplazamiento else 0f }
                    .then(if (activa) Modifier.shadow(6.dp, MaterialTheme.shapes.medium) else Modifier)
                    .semantics {
                        customActions = listOfNotNull(
                            if (indice > 0) CustomAccessibilityAction(subir) { alMover(indice, indice - 1); true } else null,
                            if (indice < elementos.lastIndex) CustomAccessibilityAction(bajar) { alMover(indice, indice + 1); true } else null,
                        )
                    },
            ) {
                Row(
                    modifier = Modifier.padding(start = Espacio.m).height(IntrinsicSize.Min).heightIn(min = 56.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.posicion_elemento, indice + 1, elemento),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f).padding(vertical = Espacio.s),
                    )
                    // Asa de arrastre: zona táctil de 56 dp.
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(56.dp)
                            .fillMaxHeight()
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { arrastrando = elemento; desplazamiento = 0f },
                                    onDragEnd = { arrastrando = null; desplazamiento = 0f },
                                    onDragCancel = { arrastrando = null; desplazamiento = 0f },
                                ) { cambio, arrastre ->
                                    cambio.consume()
                                    desplazamiento += arrastre.y
                                    val paso = (altoFila + separacion).coerceAtLeast(1)
                                    val saltos = (desplazamiento / paso).let { if (it > 0) (it + 0.5f).toInt() else (it - 0.5f).toInt() }
                                    val destino = (indiceActual + saltos).coerceIn(0, ultimo)
                                    if (destino != indiceActual) {
                                        desplazamiento -= (destino - indiceActual) * paso
                                        moverActual(indiceActual, destino)
                                    }
                                }
                            },
                    ) {
                        Text(
                            "≡",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } }
    }
}
