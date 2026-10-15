package com.example.lampara.domain.usecase

/** Reglas del nombre que la persona se pone en su perfil local. */
object NombrePerfil {
    const val MAX = 30

    /** Recorta los bordes y junta los espacios repetidos. Devuelve null si queda vacío. */
    fun normalizar(texto: String): String? =
        texto.trim().replace(Regex("\\s+"), " ").take(MAX).trimEnd().ifEmpty { null }

    /** Hay avatares numerados "c0".."c{CANTIDAD-1}"; cualquier otro valor cae en el primero. */
    const val CANTIDAD_AVATARES = 8

    fun avatarValido(id: String): String =
        id.removePrefix("c").toIntOrNull()?.takeIf { id.startsWith("c") && it in 0 until CANTIDAD_AVATARES }
            ?.let { "c$it" } ?: "c0"
}
