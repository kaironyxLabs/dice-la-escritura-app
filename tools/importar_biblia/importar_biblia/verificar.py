"""Verificaciones obligatorias y detección de patrones sospechosos sobre el texto ya interpretado."""

from __future__ import annotations

import json
import re
from collections import Counter
from collections.abc import Sequence
from pathlib import Path

from .correcciones import Correcciones
from .libros import LibroTabla
from .modelo import (
    AVISO,
    CARACTER,
    CONTEO,
    DUPLICADO,
    ERROR,
    ESPACIO,
    HUECO,
    INFO,
    SALTO,
    SIN_NUMERAR,
    SOSPECHOSO,
    VACIO,
    Anomalia,
    Resultado,
)

LETRAS_PERMITIDAS = set("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzÁÉÍÓÚÜÑáéíóúüñ")
PERMITIDOS = LETRAS_PERMITIDAS | set("0123456789 .,;:¿?¡!()-–—")
LETRAS = "A-Za-zÁÉÍÓÚÜÑáéíóúüñ"
PATRON_DIGITO_PEGADO = re.compile(rf"(?<=[{LETRAS}])\d+|\d+(?=[{LETRAS}])")
PATRON_PALABRA = re.compile(rf"[{LETRAS}]+")
PATRON_GUION_CON_ESPACIO = re.compile(rf"[{LETRAS}]+- [{LETRAS}]+")
PATRON_PARALELO_EN_TEXTO = re.compile(rf"\(\s*(?:[123] )?[A-ZÁÉÍÓÚ][{LETRAS}]*\.? \d+[.:]\d+")
PATRON_REPETIDA = re.compile(rf"(?<![{LETRAS}])([{LETRAS}]+) \1(?![{LETRAS}])", re.IGNORECASE)
VOCALES_ACENTUADAS = "áéíóúÁÉÍÓÚ"
# Palabras cortas que pueden quedar pegadas a la anterior cuando el PDF pierde el espacio.
PALABRAS_FUNCIONALES = frozenset(
    "y e o u a de en que la el los las se su no me le lo con por al del mi tu te os nos es un una "
    "sus ya si pues como para sobre todo mas más".split(),
)


def cargar_esperado(ruta: Path) -> dict[str, list[int]]:
    datos = json.loads(ruta.read_text(encoding="utf-8"))
    return {libro: [int(n) for n in conteos] for libro, conteos in datos["libros"].items()}


def _textos_de_capitulo(capitulo) -> list[tuple[int | None, str]]:
    textos: list[tuple[int | None, str]] = []
    for i, versiculo in enumerate(capitulo.versiculos, start=1):
        if versiculo:
            textos.append((i, versiculo))
    if capitulo.encabezado:
        textos.append((None, capitulo.encabezado))
    textos += [(None, s.titulo) for s in capitulo.secciones]
    textos += [(None, p.texto) for p in capitulo.paralelos]
    return textos


def detectar_espacios_perdidos(resultado: Resultado) -> list[Anomalia]:
    """Palabras que parecen dos pegadas porque el PDF perdió el espacio tras una vocal acentuada.

    Es una heurística de ayuda: el importador solo informa, la corrección la decide el usuario.
    """
    frecuencia: Counter[str] = Counter()
    for libro in resultado.libros:
        for capitulo in libro.capitulos:
            for versiculo in capitulo.versiculos:
                if versiculo:
                    frecuencia.update(PATRON_PALABRA.findall(versiculo))

    sospechosas: dict[str, tuple[str, str]] = {}
    for palabra, veces in frecuencia.items():
        if len(palabra) < 5:
            continue
        for i in range(2, len(palabra)):
            if palabra[i - 1] not in VOCALES_ACENTUADAS:
                continue
            raiz, resto = palabra[:i], palabra[i:]
            if resto.lower() not in PALABRAS_FUNCIONALES or frecuencia.get(raiz, 0) < 3:
                continue
            if raiz[-1] in "íÍ" and resto.lower() == "a":
                continue  # "escondía", "abría": imperfecto en -ía, no "escondí a"
            if veces <= 5 or veces * 10 <= frecuencia[raiz]:
                sospechosas[palabra] = (raiz, resto)
                break

    anomalias: list[Anomalia] = []
    if not sospechosas:
        return anomalias
    patron = re.compile(rf"(?<![{LETRAS}])({'|'.join(re.escape(p) for p in sospechosas)})(?![{LETRAS}])")
    for libro in resultado.libros:
        for capitulo in libro.capitulos:
            for numero, versiculo in enumerate(capitulo.versiculos, start=1):
                if not versiculo:
                    continue
                for coincidencia in patron.finditer(versiculo):
                    palabra = coincidencia.group(1)
                    raiz, resto = sospechosas[palabra]
                    anomalias.append(Anomalia(
                        ESPACIO, AVISO, libro.id, capitulo.numero, numero,
                        f"\"{palabra}\": ¿falta un espacio? (\"{raiz} {resto}\")",
                        datos={"palabra": palabra, "por": f"{raiz} {resto}"},
                    ))
    return anomalias


def verificar(
    resultado: Resultado,
    tabla: Sequence[LibroTabla],
    esperado: dict[str, list[int]] | None,
) -> list[Anomalia]:
    """Anomalías calculadas sobre el texto final (después de aplicar correcciones y pendientes)."""
    anomalias: list[Anomalia] = []
    for libro in resultado.libros:
        for capitulo in libro.capitulos:
            ubicacion = (libro.id, capitulo.numero)
            if esperado is not None and libro.id in esperado:
                conteos = esperado[libro.id]
                if capitulo.numero <= len(conteos) and len(capitulo.versiculos) != conteos[capitulo.numero - 1]:
                    anomalias.append(Anomalia(
                        CONTEO, ERROR, *ubicacion, None,
                        f"tiene {len(capitulo.versiculos)} versículos y se esperaban {conteos[capitulo.numero - 1]}",
                    ))
            for numero, versiculo in enumerate(capitulo.versiculos, start=1):
                if versiculo is None:
                    if numero not in capitulo.pendientes:
                        anomalias.append(Anomalia(
                            HUECO, ERROR, *ubicacion, numero, "falta el versículo en la numeración",
                        ))
                elif not versiculo.strip():
                    if numero not in capitulo.pendientes:
                        anomalias.append(Anomalia(VACIO, ERROR, *ubicacion, numero, "versículo vacío"))
            for numero, texto in _textos_de_capitulo(capitulo):
                raros = sorted({c for c in texto if c not in PERMITIDOS})
                if raros:
                    anomalias.append(Anomalia(
                        CARACTER, ERROR, *ubicacion, numero,
                        "caracteres fuera del conjunto permitido: " + " ".join(
                            f"{c!r} (U+{ord(c):04X})" for c in raros
                        ),
                    ))
                if numero is not None:  # solo texto de versículos (los títulos y paralelos pueden llevar guiones)
                    for coincidencia in PATRON_GUION_CON_ESPACIO.finditer(texto):
                        anomalias.append(Anomalia(
                            SOSPECHOSO, AVISO, *ubicacion, numero,
                            f"guion seguido de espacio: \"{coincidencia.group(0)}\" (¿nombre compuesto cortado?)",
                        ))
                    for coincidencia in PATRON_PARALELO_EN_TEXTO.finditer(texto):
                        anomalias.append(Anomalia(
                            SOSPECHOSO, AVISO, *ubicacion, numero,
                            "referencia a pasaje paralelo dentro del texto del versículo (debería ir aparte)",
                        ))
                    for coincidencia in PATRON_REPETIDA.finditer(texto):
                        inicio = max(0, coincidencia.start() - 12)
                        anomalias.append(Anomalia(
                            SOSPECHOSO, AVISO, *ubicacion, numero,
                            f"palabra repetida seguida: \"…{texto[inicio:coincidencia.end() + 12]}…\"",
                        ))
                for pegado in PATRON_DIGITO_PEGADO.finditer(texto):
                    inicio = max(0, pegado.start() - 8)
                    anomalias.append(Anomalia(
                        SOSPECHOSO, AVISO, *ubicacion, numero,
                        f"dígitos pegados a letras: \"…{texto[inicio:pegado.end() + 8]}…\"",
                    ))
    anomalias += detectar_espacios_perdidos(resultado)
    return anomalias


def esta_resuelta(anomalia: Anomalia, resultado: Resultado, correcciones: Correcciones) -> bool:
    """Una anomalía deja de requerir acción si es informativa, está aceptada, el versículo está pendiente
    o el usuario ya corrigió ese versículo."""
    if anomalia.severidad == INFO:
        return True
    if correcciones.esta_aceptada(anomalia.tipo, anomalia.libro, anomalia.capitulo, anomalia.versiculo):
        return True
    if anomalia.versiculo is None:
        return False
    clave = (anomalia.libro, anomalia.capitulo, anomalia.versiculo)
    if clave in correcciones.pendientes:
        return True
    # Solo los avisos del paso de interpretación se dan por revisados con una corrección del versículo;
    # los demás se vuelven a calcular sobre el texto corregido y no pueden quedar obsoletos.
    if anomalia.tipo in (SALTO, DUPLICADO, SIN_NUMERAR):
        return clave in correcciones.versiculos_corregidos()
    return False


def pendientes_automaticos(anomalias: Sequence[Anomalia], resueltas: set[int]) -> dict[tuple[str, int], set[int]]:
    """Versículos con una anomalía de error sin resolver: se publican como pendientes para que la app
    no los muestre rotos, y el importador termina con error hasta que se resuelvan."""
    pendientes: dict[tuple[str, int], set[int]] = {}
    for i, anomalia in enumerate(anomalias):
        if i in resueltas or anomalia.severidad != ERROR or anomalia.versiculo is None:
            continue
        pendientes.setdefault((anomalia.libro, anomalia.capitulo), set()).add(anomalia.versiculo)
    return pendientes
