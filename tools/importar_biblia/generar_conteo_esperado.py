"""Genera docs/versiculos_por_capitulo.json: cuántos versículos tiene cada capítulo, de una fuente independiente.

Fuente: King James Version (dominio público), repositorio thiagobodruk/bible, archivo json/en_kjv.json.
Solo se guardan los NÚMEROS de versículos por capítulo; no se copia ningún texto.

Ajuste a la numeración de la RVR1960: ver AJUSTES. Cualquier otra diferencia entre la KJV y la RVR1960
(o errores del PDF) aparece en el informe de importación y la resuelve el usuario.

Uso: python generar_conteo_esperado.py [ruta_de_salida]
"""

from __future__ import annotations

import json
import sys
import urllib.request
from pathlib import Path

from importar_biblia.libros import LIBROS_RVR1960

URL = "https://raw.githubusercontent.com/thiagobodruk/bible/master/json/en_kjv.json"
RAIZ = Path(__file__).resolve().parents[2]

# Diferencias conocidas entre la KJV y la numeración de la RVR1960.
AJUSTES = [
    {
        "libro": "3JN",
        "capitulo": 1,
        "esperado": 15,
        "motivo": "La RVR1960 numera 15 versículos en 3 Juan (la KJV, 14); el PDF también trae 15.",
    },
]


def main() -> int:
    salida = Path(sys.argv[1]) if len(sys.argv) > 1 else RAIZ / "docs" / "versiculos_por_capitulo.json"
    with urllib.request.urlopen(URL, timeout=120) as respuesta:
        libros_kjv = json.loads(respuesta.read().decode("utf-8-sig"))
    if len(libros_kjv) != len(LIBROS_RVR1960):
        raise SystemExit(f"La fuente trae {len(libros_kjv)} libros y se esperaban {len(LIBROS_RVR1960)}.")

    conteos: dict[str, list[int]] = {}
    for libro, kjv in zip(LIBROS_RVR1960, libros_kjv):
        capitulos = kjv["chapters"]
        if len(capitulos) != libro.capitulos:
            raise SystemExit(f"{libro.id}: la fuente trae {len(capitulos)} capítulos y se esperaban {libro.capitulos}.")
        conteos[libro.id] = [len(versiculos) for versiculos in capitulos]

    for ajuste in AJUSTES:
        conteos[ajuste["libro"]][ajuste["capitulo"] - 1] = ajuste["esperado"]

    datos = {
        "fuente": "King James Version (dominio público), thiagobodruk/bible, json/en_kjv.json (solo conteos)",
        "url": URL,
        "ajustes_rvr1960": AJUSTES,
        "total_versiculos": sum(sum(c) for c in conteos.values()),
        "libros": conteos,
    }
    salida.parent.mkdir(parents=True, exist_ok=True)
    # Cada libro en una sola línea para que el archivo sea legible y las diferencias, revisables.
    cabecera = {clave: valor for clave, valor in datos.items() if clave != "libros"}
    cuerpo = ",\n".join(f'  "{id_}": {json.dumps(c)}' for id_, c in conteos.items())
    texto = json.dumps(cabecera, ensure_ascii=False, indent=1)[:-2] + ',\n "libros": {\n' + cuerpo + "\n }\n}\n"
    json.loads(texto)  # comprobación de que el JSON resultante es válido
    salida.write_text(texto, encoding="utf-8", newline="\n")
    print(f"Escrito {salida} ({datos['total_versiculos']} versículos esperados)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
