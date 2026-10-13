"""Correcciones SUGERIDAS para los espacios perdidos (fuentes/correcciones_sugeridas.json).

Es solo una ayuda: el importador no aplica nada de este archivo. El usuario revisa cada entrada y copia las
que aprueba a fuentes/correcciones.json. Una sugerencia solo se ofrece si la palabra sospechosa aparece
exactamente una vez en el versículo, de modo que el reemplazo sea inequívoco.
"""

from __future__ import annotations

import json
from collections.abc import Sequence
from pathlib import Path

from .modelo import ESPACIO, Anomalia, Resultado


def generar(resultado: Resultado, sin_resolver: Sequence[Anomalia]) -> dict:
    por_versiculo: dict[tuple[str, int, int], list[dict]] = {}
    omitidas: list[str] = []
    capitulos = {(libro.id, c.numero): c for libro in resultado.libros for c in libro.capitulos}
    for anomalia in sin_resolver:
        if anomalia.tipo != ESPACIO or not anomalia.datos or anomalia.versiculo is None:
            continue
        capitulo = capitulos[(anomalia.libro, anomalia.capitulo)]
        texto = capitulo.versiculos[anomalia.versiculo - 1] or ""
        palabra = anomalia.datos["palabra"]
        clave = (anomalia.libro, anomalia.capitulo, anomalia.versiculo)
        if texto.count(palabra) != 1:
            omitidas.append(f"{anomalia.ubicacion()} ({palabra!r} aparece {texto.count(palabra)} veces)")
            continue
        reemplazo = {"buscar": palabra, "por": anomalia.datos["por"]}
        if reemplazo not in por_versiculo.setdefault(clave, []):
            por_versiculo[clave].append(reemplazo)
    correcciones = [
        {"libro": libro, "capitulo": capitulo, "versiculo": versiculo, "reemplazos": reemplazos}
        for (libro, capitulo, versiculo), reemplazos in sorted(por_versiculo.items())
    ]
    return {
        "aviso": "SUGERENCIAS sin aplicar. Revisa cada una contra tu edición impresa y copia las aprobadas "
                 "a fuentes/correcciones.json (clave 'correcciones').",
        "omitidas": omitidas,
        "correcciones": correcciones,
    }


def escribir(datos: dict, ruta: Path) -> None:
    if not datos["correcciones"] and not datos["omitidas"]:
        if ruta.exists():
            ruta.unlink()  # nada que sugerir: no dejar un archivo viejo que confunda
        return
    ruta.parent.mkdir(parents=True, exist_ok=True)
    ruta.write_text(json.dumps(datos, ensure_ascii=False, indent=1) + "\n", encoding="utf-8", newline="\n")
