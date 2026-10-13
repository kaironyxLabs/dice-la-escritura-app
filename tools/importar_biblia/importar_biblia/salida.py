"""Escritura de rvr1960.json, huellas SHA-256 y bloque de importación en docs/TEXTO_BIBLICO.md."""

from __future__ import annotations

import hashlib
import json
import re
from datetime import date
from pathlib import Path

from .modelo import Resultado

MARCA_INICIO = "<!-- importacion:inicio -->"
MARCA_FIN = "<!-- importacion:fin -->"


def sha256_de(ruta: Path) -> str:
    resumen = hashlib.sha256()
    with ruta.open("rb") as archivo:
        for bloque in iter(lambda: archivo.read(1 << 20), b""):
            resumen.update(bloque)
    return resumen.hexdigest()


def construir_json(resultado: Resultado, pendientes_extra: dict[tuple[str, int], set[int]]) -> dict:
    """Formato de assets/bible/rvr1960.json (documentado en docs/TEXTO_BIBLICO.md).

    El versículo n es versiculos[n - 1]; un hueco o pendiente va como cadena vacía y figura en "pendientes".
    """
    libros = []
    for libro in resultado.libros:
        capitulos = []
        for capitulo in libro.capitulos:
            pendientes = set(capitulo.pendientes) | pendientes_extra.get((libro.id, capitulo.numero), set())
            capitulos.append({
                "numero": capitulo.numero,
                "encabezado": capitulo.encabezado,
                "versiculos": [v if v is not None else "" for v in capitulo.versiculos],
                "secciones": [
                    {"antesDelVersiculo": s.antes_del_versiculo, "titulo": s.titulo} for s in capitulo.secciones
                ],
                "paralelos": [
                    {"antesDelVersiculo": p.antes_del_versiculo, "texto": p.texto} for p in capitulo.paralelos
                ],
                "pendientes": sorted(pendientes),
            })
        libros.append({"id": libro.id, "capitulos": capitulos})
    return {"traduccion": "RVR1960", "libros": libros}


def escribir_json(datos: dict, ruta: Path) -> None:
    ruta.parent.mkdir(parents=True, exist_ok=True)
    # Salida determinista (mismo PDF y mismas correcciones, mismo SHA-256).
    ruta.write_text(
        json.dumps(datos, ensure_ascii=False, separators=(",", ":")) + "\n",
        encoding="utf-8",
        newline="\n",
    )


def escribir_libros(datos: dict, carpeta: Path) -> None:
    """Un archivo por libro (<ID>.json) con {"id", "capitulos"}: la app lee solo el libro que necesita."""
    carpeta.mkdir(parents=True, exist_ok=True)
    for archivo in carpeta.glob("*.json"):
        archivo.unlink()  # sin restos de una importación anterior
    for libro in datos["libros"]:
        (carpeta / f"{libro['id']}.json").write_text(
            json.dumps(libro, ensure_ascii=False, separators=(",", ":")) + "\n",
            encoding="utf-8",
            newline="\n",
        )


def actualizar_documentacion(
    ruta_doc: Path,
    sha_pdf: str,
    sha_json: str,
    resumen: dict[str, int],
) -> bool:
    """Reescribe el bloque entre las marcas de importación. Devuelve False si el documento no tiene las marcas."""
    if not ruta_doc.exists():
        return False
    texto = ruta_doc.read_text(encoding="utf-8")
    if MARCA_INICIO not in texto or MARCA_FIN not in texto:
        return False
    bloque = "\n".join([
        MARCA_INICIO,
        f"- Última importación: {date.today().isoformat()}",
        f"- SHA-256 del PDF (`biblia_rv1960.pdf`): `{sha_pdf}`",
        f"- SHA-256 del JSON (`rvr1960.json`): `{sha_json}`",
        f"- Libros: {resumen['libros']}; capítulos: {resumen['capitulos']}; versículos: {resumen['versiculos']}",
        f"- Correcciones aplicadas: {resumen['correcciones']}; versículos pendientes: {resumen['pendientes']}",
        f"- Anomalías sin resolver: {resumen['sin_resolver']}",
        MARCA_FIN,
    ])
    nuevo = re.sub(
        re.escape(MARCA_INICIO) + r".*?" + re.escape(MARCA_FIN),
        lambda _: bloque,
        texto,
        flags=re.DOTALL,
    )
    ruta_doc.write_text(nuevo, encoding="utf-8", newline="\n")
    return True
