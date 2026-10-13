"""Genera docs/REVISION_PREGUNTAS.md: las preguntas por módulo y categoría, con casillas para marcar.

Uso (desde la raíz del proyecto):
    python tools/contenido/informe_revision.py [--solo-pendientes] [--salida RUTA]

El informe no contiene texto bíblico: solo preguntas, opciones, referencias y explicaciones.
Al revisar, marca las casillas del Markdown y ejecuta `tools/contenido/aplicar_revision.py`:
solo así una pregunta pasa a `"reviewed": true`. Nada la marca por su cuenta.
"""

from __future__ import annotations

import argparse
import json
from pathlib import Path

RAIZ = Path(__file__).resolve().parents[2]
CONTENIDO = RAIZ / "app" / "src" / "main" / "assets" / "content"

NOMBRES_CATEGORIA = {
    "libros": "Libros y orden de la Biblia",
    "historia": "Historia bíblica",
    "personajes": "Personajes",
    "lugares": "Lugares y geografía",
    "ensenanzas": "Enseñanzas y parábolas de Jesús",
    "cartas": "Cartas del Nuevo Testamento",
    "donde_escrito": "¿Dónde está escrito?",
    "confesion": "Confesión Bautista de 1689",
}
ORDEN_CATEGORIAS = list(NOMBRES_CATEGORIA)
NOMBRES_MODULO = {"core": "Módulo base", "confesion1689": "Módulo Confesión de 1689"}
TIPOS = {"multiple_choice": "opción múltiple", "true_false": "verdadero o falso", "ordering": "ordenar"}


def es_archivo_de_preguntas(ruta: Path) -> bool:
    return ruta.suffix == ".json" and ruta.name.startswith(("core-", "confesion1689-"))


def cargar_preguntas(carpeta: Path = CONTENIDO) -> list[dict]:
    preguntas: list[dict] = []
    for ruta in sorted(carpeta.iterdir()):
        if es_archivo_de_preguntas(ruta):
            preguntas += json.loads(ruta.read_text(encoding="utf-8"))["questions"]
    return preguntas


def _respuesta(p: dict) -> list[str]:
    if p["type"] == "multiple_choice":
        return [
            f"  - {'**' if i == p['answerIndex'] else ''}{opcion}{'**  (correcta)' if i == p['answerIndex'] else ''}"
            for i, opcion in enumerate(p["options"])
        ]
    if p["type"] == "true_false":
        return [f"  - Respuesta: **{'verdadero' if p['answer'] else 'falso'}**"]
    return ["  - Orden correcto:"] + [f"    {n}. {item}" for n, item in enumerate(p["items"], start=1)]


def generar(preguntas: list[dict], solo_pendientes: bool = False) -> str:
    elegidas = [p for p in preguntas if not (solo_pendientes and p.get("reviewed", False))]
    total = len(preguntas)
    revisadas = sum(1 for p in preguntas if p.get("reviewed", False))
    lineas = [
        "# Revisión de preguntas",
        "",
        f"Preguntas: {total}. Revisadas: {revisadas}. Pendientes: {total - revisadas}.",
        "",
        "Marca con `x` las que hayas revisado (`- [x]`) y ejecuta `python tools/contenido/aplicar_revision.py`.",
        "Antes de marcar, comprueba cada una: que la respuesta sea la correcta, que las referencias respalden la",
        "pregunta, que no haya una postura denominacional en el módulo base y que la explicación parafrasee.",
        "Si dudas de una pregunta, déjala sin marcar y bórrala o corrígela en el JSON.",
        "",
    ]
    for modulo in NOMBRES_MODULO:
        del_modulo = [p for p in elegidas if p["module"] == modulo]
        if not del_modulo:
            continue
        lineas += [f"## {NOMBRES_MODULO[modulo]}", ""]
        for categoria in ORDEN_CATEGORIAS:
            grupo = [p for p in del_modulo if p["category"] == categoria]
            if not grupo:
                continue
            lineas += [f"### {NOMBRES_CATEGORIA[categoria]} ({len(grupo)})", ""]
            for p in sorted(grupo, key=lambda q: q["id"]):
                marca = "x" if p.get("reviewed", False) else " "
                detalle = f"dificultad {p['difficulty']}, {TIPOS[p['type']]}"
                lineas.append(f"- [{marca}] `{p['id']}` ({detalle}): {p['question']}")
                lineas += _respuesta(p)
                lineas.append(f"  - Referencias: {'; '.join(p['references'])}")
                if p.get("confessionRef"):
                    lineas.append(f"  - Confesión: {p['confessionRef']}")
                lineas.append(f"  - Explicación: {p['explanation']}")
                lineas.append("")
    return "\n".join(lineas).rstrip() + "\n"


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--solo-pendientes", action="store_true", help="omite las ya revisadas")
    parser.add_argument("--salida", type=Path, default=RAIZ / "docs" / "REVISION_PREGUNTAS.md")
    args = parser.parse_args(argv)
    preguntas = cargar_preguntas()
    args.salida.parent.mkdir(parents=True, exist_ok=True)
    args.salida.write_text(generar(preguntas, args.solo_pendientes), encoding="utf-8", newline="\n")
    print(f"{args.salida}: {len(preguntas)} preguntas")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
