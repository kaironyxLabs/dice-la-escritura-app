package com.kaironyx.diceescritura.feature

import androidx.annotation.StringRes
import com.kaironyx.diceescritura.R
import com.kaironyx.diceescritura.domain.model.Categoria

@StringRes
fun Categoria.nombreRes(): Int = when (this) {
    Categoria.LIBROS -> R.string.cat_libros
    Categoria.HISTORIA -> R.string.cat_historia
    Categoria.PERSONAJES -> R.string.cat_personajes
    Categoria.LUGARES -> R.string.cat_lugares
    Categoria.ENSENANZAS -> R.string.cat_ensenanzas
    Categoria.CARTAS -> R.string.cat_cartas
    Categoria.DONDE_ESTA_ESCRITO -> R.string.cat_donde_escrito
    Categoria.CONFESION -> R.string.cat_confesion
}
