package com.kaironyx.diceescritura.core.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

// Iconos propios (la biblioteca de iconos básicos no trae libro ni gráfico). Trazos de 24 x 24.

private fun icono(nombre: String, trazo: String): ImageVector =
    ImageVector.Builder(nombre, 24.dp, 24.dp, 24f, 24f)
        .addPath(addPathNodes(trazo), fill = SolidColor(Color.Black))
        .build()

/** Libro abierto. */
val IconoLibro: ImageVector by lazy {
    icono(
        "Libro",
        "M21,5c-1.11,-0.35 -2.33,-0.5 -3.5,-0.5c-1.95,0 -4.05,0.4 -5.5,1.5c-1.45,-1.1 -3.55,-1.5 -5.5,-1.5S2.45,4.9 1,6v14.65" +
            "c0,0.25 0.25,0.5 0.5,0.5c0.1,0 0.15,-0.05 0.25,-0.05C3.1,20.45 5.05,20 6.5,20c1.95,0 4.05,0.4 5.5,1.5" +
            "c1.35,-0.85 3.8,-1.5 5.5,-1.5c1.65,0 3.35,0.3 4.75,1.05c0.1,0.05 0.15,0.05 0.25,0.05c0.25,0 0.5,-0.25 0.5,-0.5V6" +
            "c-0.6,-0.45 -1.25,-0.75 -2,-1zM21,18.5c-1.1,-0.35 -2.3,-0.5 -3.5,-0.5c-1.7,0 -4.15,0.65 -5.5,1.5V8" +
            "c1.35,-0.85 3.8,-1.5 5.5,-1.5c1.2,0 2.4,0.15 3.5,0.5v11.5z",
    )
}

/** Gráfico de barras. */
val IconoGrafico: ImageVector by lazy {
    icono("Grafico", "M5,9.2h3L8,19L5,19zM10.6,5h2.8v14h-2.8zM16.2,13H19v6h-2.8z")
}
