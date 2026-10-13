"""Ayudas de prueba. Todo el texto es FALSO: nunca se usa texto de la RVR1960."""

from __future__ import annotations

from pathlib import Path

import pymupdf

from importar_biblia.extraer import Linea
from importar_biblia.libros import LibroTabla

CABECERA = ["Santa Biblia – Versión Reina Valera 1960 ", " ", "www.siguiendosuspisadas.com.ar "]

# Tabla pequeña: dos libros con tres capítulos en total.
TABLA = (LibroTabla("UNO", "Libro Uno", 2), LibroTabla("DOS", "Libro Dos", 1))


def lineas(*textos: str) -> list[Linea]:
    """Líneas sueltas; una cadena vacía es una línea en blanco (separador de párrafo)."""
    return [Linea(t) for t in textos]


def crear_pdf(ruta: Path, paginas: list[list[str]], con_cabecera: bool = True) -> Path:
    """PDF sintético con la misma disposición que el real: cabecera repetida y líneas en blanco como ' '."""
    documento = pymupdf.open()
    for contenido in paginas:
        pagina = documento.new_page()
        y = 60
        for texto in (CABECERA if con_cabecera else []) + contenido:
            pagina.insert_text((50, y), texto if texto else " ", fontsize=9)
            y += 11
    documento.save(ruta)
    documento.close()
    return ruta


def libro_basico() -> list[str]:
    """Contenido mínimo y válido para TABLA: Libro Uno 1 (2 versículos), Libro Uno 2 (1) y Libro Dos 1 (2)."""
    return [
        "LIBRO UNO ", " ", "Titulo de prueba ", " ", "Libro Uno 1 ", " ",
        "1 Texto de prueba, no es RVR1960. 2 Otro texto de prueba, no es RVR1960. ", " ",
        "Libro Uno 2 ", " ",
        "1 Texto de prueba tres, no es RVR1960. ", " ",
        "LIBRO DOS ", " ", "Libro Dos 1 ", " ",
        "1 Texto de prueba cuatro, no es RVR1960. ", " ",
        "2 Texto de prueba cinco, no es RVR1960. ",
    ]
