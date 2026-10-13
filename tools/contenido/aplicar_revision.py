"""Pasa a `"reviewed": true` las preguntas cuyas casillas marcaste en docs/REVISION_PREGUNTAS.md.

Uso (desde la raíz del proyecto):
    python tools/contenido/aplicar_revision.py [--informe RUTA] [--simular]

Solo la ejecuta el usuario, después de revisar. Edita los JSON con un reemplazo de texto puntual para no reformatear
el resto del archivo. Una casilla desmarcada nunca cambia nada (no devuelve una pregunta a `false`).
"""

from __future__ import annotations

import argparse
import re
from pathlib import Path

RAIZ = Path(__file__).resolve().parents[2]
CONTENIDO = RAIZ / "app" / "src" / "main" / "assets" / "content"

CASILLA_MARCADA = re.compile(r"^- \[[xX]\] `([a-z0-9_]+-[a-z_]+-\d{4})`", re.MULTILINE)
ARCHIVOS = re.compile(r"^(core|confesion1689)-.*\.json$")


def ids_marcados(markdown: str) -> list[str]:
    return CASILLA_MARCADA.findall(markdown)


def marcar_revisada(texto: str, id_pregunta: str) -> tuple[str, bool]:
    """Cambia el primer `"reviewed": false` posterior al id. Devuelve (texto, si_cambio)."""
    inicio = texto.find(f'"id": "{id_pregunta}"')
    if inicio < 0:
        return texto, False
    siguiente = texto.find('"id": "', inicio + 1)
    fin = len(texto) if siguiente < 0 else siguiente
    bloque = texto[inicio:fin]
    nuevo = re.sub(r'"reviewed":\s*false', '"reviewed": true', bloque, count=1)
    if nuevo == bloque:
        return texto, False
    return texto[:inicio] + nuevo + texto[fin:], True


def aplicar(ids: list[str], carpeta: Path = CONTENIDO, simular: bool = False) -> dict[str, int]:
    pendientes = set(ids)
    cambios = 0
    for ruta in sorted(carpeta.iterdir()):
        if not ARCHIVOS.match(ruta.name):
            continue
        texto = ruta.read_text(encoding="utf-8")
        original = texto
        for id_pregunta in sorted(pendientes):
            texto, cambio = marcar_revisada(texto, id_pregunta)
            if cambio:
                cambios += 1
                pendientes.discard(id_pregunta)
        if texto != original and not simular:
            ruta.write_text(texto, encoding="utf-8", newline="\n")
    # Lo que quede en "pendientes" ya estaba revisado o no existe en los archivos.
    return {"marcadas": len(ids), "cambiadas": cambios, "sin_cambio": len(pendientes)}


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--informe", type=Path, default=RAIZ / "docs" / "REVISION_PREGUNTAS.md")
    parser.add_argument("--simular", action="store_true", help="muestra el resultado sin escribir nada")
    args = parser.parse_args(argv)
    ids = ids_marcados(args.informe.read_text(encoding="utf-8"))
    resultado = aplicar(ids, simular=args.simular)
    print(
        f"Casillas marcadas: {resultado['marcadas']}. Preguntas actualizadas: {resultado['cambiadas']}. "
        f"Sin cambio (ya revisadas o inexistentes): {resultado['sin_cambio']}."
        + (" (simulación: no se escribió nada)" if args.simular else ""),
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
