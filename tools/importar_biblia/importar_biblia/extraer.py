"""Extracción de líneas del PDF, sin interpretar el contenido bíblico."""

from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path

# Cabecera repetida al inicio de cada página: dos líneas con una en blanco en medio.
CABECERA_1 = re.compile(r"^Santa Biblia\W+Versi\w+ Reina Valera 1960\s*$", re.IGNORECASE)
CABECERA_2 = re.compile(r"^www\.siguiendosuspisadas\.com\.ar\s*$", re.IGNORECASE)
# Línea de guiones que separa bloques en el PDF; no es contenido.
SEPARADOR = re.compile(r"^-{5,}$")


@dataclass(frozen=True)
class Linea:
    texto: str
    # True si es la primera línea de contenido de una página (un salto de página no es un salto de párrafo).
    inicio_de_pagina: bool = False


def quitar_cabecera(lineas: list[str]) -> list[str]:
    """Quita la cabecera repetida de una página como un bloque (incluida su línea en blanco)."""
    if not lineas or not CABECERA_1.match(lineas[0].strip()):
        return lineas
    for i in range(1, min(len(lineas), 6)):
        if CABECERA_2.match(lineas[i].strip()):
            return lineas[i + 1:]
    return lineas


def lineas_de_pagina(texto: str) -> list[str]:
    """Líneas de una página tal como las entrega el PDF.

    El texto de una página termina con un salto de línea final que no es una línea en blanco real:
    si se dejara, cada salto de página parecería un salto de párrafo.
    """
    lineas = texto.split("\n")
    if lineas and lineas[-1] == "":
        lineas.pop()
    return lineas


def aplanar(paginas: list[list[str]]) -> list[Linea]:
    resultado: list[Linea] = []
    for pagina in paginas:
        contenido = quitar_cabecera(pagina)
        for i, texto in enumerate(contenido):
            resultado.append(Linea(texto.rstrip(), inicio_de_pagina=(i == 0)))
    return resultado


def extraer_lineas(ruta_pdf: Path) -> list[Linea]:
    import pymupdf  # importación diferida: los tests del parser no la necesitan

    with pymupdf.open(ruta_pdf) as documento:
        paginas = [lineas_de_pagina(pagina.get_text("text")) for pagina in documento]
    return aplanar(paginas)
