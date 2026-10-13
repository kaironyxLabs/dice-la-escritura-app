"""Línea de comandos: PDF -> fuentes/rvr1960.json + assets/bible/libros/<ID>.json + informe de anomalías.

Códigos de salida: 0 todo resuelto; 2 estructura o correcciones inválidas (no se escribe el JSON);
3 hay anomalías sin resolver (el JSON se escribe, con esos versículos como pendientes).
"""

from __future__ import annotations

import argparse
import sys
from collections.abc import Sequence
from pathlib import Path

from . import correcciones as modulo_correcciones
from . import informe, salida, sugerencias, verificar
from .extraer import extraer_lineas
from .libros import LIBROS_RVR1960, LibroTabla
from .parser import ErrorEstructura, analizar

RAIZ = Path(__file__).resolve().parents[3]


def _pdf_por_defecto() -> Path:
    for candidato in (RAIZ / "fuentes" / "biblia_rv1960.pdf", RAIZ / "biblia_rv1960.pdf"):
        if candidato.exists():
            return candidato
    return RAIZ / "fuentes" / "biblia_rv1960.pdf"


def construir_argumentos() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(prog="importar_biblia", description=__doc__)
    p.add_argument("--pdf", type=Path, default=None, help="PDF de origen (por defecto fuentes/ o la raíz del proyecto)")
    p.add_argument(
        "--salida", type=Path, default=RAIZ / "fuentes/rvr1960.json",
        help="texto completo en un solo archivo (copia local de referencia; no va dentro de la app)",
    )
    p.add_argument(
        "--salida-libros", type=Path, default=RAIZ / "app/src/main/assets/bible/libros",
        help="carpeta con un archivo por libro, que es lo que lee la app",
    )
    p.add_argument("--correcciones", type=Path, default=RAIZ / "fuentes/correcciones.json")
    p.add_argument("--esperado", type=Path, default=RAIZ / "docs/versiculos_por_capitulo.json")
    p.add_argument("--informe", type=Path, default=RAIZ / "docs/TEXTO_BIBLICO_informe.md")
    p.add_argument("--doc", type=Path, default=RAIZ / "docs/TEXTO_BIBLICO.md")
    p.add_argument(
        "--sugerencias", type=Path, default=RAIZ / "fuentes/correcciones_sugeridas.json",
        help="archivo donde dejar correcciones sugeridas (espacios perdidos) para que las revises",
    )
    return p


def main(argv: list[str] | None = None, tabla: Sequence[LibroTabla] = LIBROS_RVR1960) -> int:
    """`tabla` solo se cambia en las pruebas, que usan una Biblia diminuta."""
    args = construir_argumentos().parse_args(argv)
    ruta_pdf = args.pdf or _pdf_por_defecto()
    if not ruta_pdf.exists():
        print(f"ERROR: no se encuentra el PDF en {ruta_pdf}. Ver docs/TEXTO_BIBLICO.md.", file=sys.stderr)
        return 2

    print(f"Leyendo {ruta_pdf} ...")
    lineas = extraer_lineas(ruta_pdf)
    try:
        resultado = analizar(lineas, tabla)
    except ErrorEstructura as error:
        print(f"ERROR de estructura: {error}", file=sys.stderr)
        return 2

    try:
        correcciones = modulo_correcciones.cargar(args.correcciones, tabla)
        modulo_correcciones.aplicar(resultado, correcciones)
    except modulo_correcciones.ErrorCorreccion as error:
        print(f"ERROR en las correcciones: {error}", file=sys.stderr)
        return 2

    esperado = verificar.cargar_esperado(args.esperado) if args.esperado.exists() else None
    if esperado is None:
        print(f"AVISO: no existe {args.esperado}; no se comparan los conteos de versículos.", file=sys.stderr)

    # Anomalías del paso de interpretación (saltos, párrafos sin número...) más las del texto ya corregido.
    anomalias = list(resultado.anomalias) + verificar.verificar(resultado, tabla, esperado)
    resueltas = {i for i, a in enumerate(anomalias) if verificar.esta_resuelta(a, resultado, correcciones)}
    extra = verificar.pendientes_automaticos(anomalias, resueltas)

    datos = salida.construir_json(resultado, extra)
    salida.escribir_json(datos, args.salida)
    salida.escribir_libros(datos, args.salida_libros)
    sha_pdf = salida.sha256_de(ruta_pdf)
    sha_json = salida.sha256_de(args.salida)

    versiculos = sum(len(c.versiculos) for libro in resultado.libros for c in libro.capitulos)
    pendientes_total = sum(len(c["pendientes"]) for libro in datos["libros"] for c in libro["capitulos"])
    sin_resolver = len(anomalias) - len(resueltas)
    resumen = {
        "libros": len(resultado.libros),
        "capitulos": sum(len(libro.capitulos) for libro in resultado.libros),
        "versiculos": versiculos,
        "correcciones": len(resultado.correcciones_aplicadas),
        "pendientes": pendientes_total,
        "sin_resolver": sin_resolver,
    }
    pendientes_usuario = [f"{libro} {cap}:{v}" + (f" ({m})" if m else "")
                          for (libro, cap, v), m in sorted(correcciones.pendientes.items())]
    aceptadas = [
        f"{a['libro']} {a['capitulo']}" + (f":{a['versiculo']}" if "versiculo" in a else "")
        + f" [{a['tipo']}] {a.get('motivo', '')}".rstrip()
        for a in correcciones.aceptadas
    ]
    args.informe.parent.mkdir(parents=True, exist_ok=True)
    args.informe.write_text(
        informe.generar(resultado, anomalias, resueltas, pendientes_usuario, aceptadas, resumen, sha_pdf, sha_json),
        encoding="utf-8",
        newline="\n",
    )
    salida.actualizar_documentacion(args.doc, sha_pdf, sha_json, resumen)
    sugeridas = sugerencias.generar(resultado, [a for i, a in enumerate(anomalias) if i not in resueltas])
    sugerencias.escribir(sugeridas, args.sugerencias)

    total_capitulos = sum(libro.capitulos for libro in tabla)
    print(f"Libros: {resumen['libros']}/{len(tabla)}  Capítulos: {resumen['capitulos']}/{total_capitulos}  "
          f"Versículos: {versiculos}")
    print(f"JSON: {args.salida}")
    print(f"Libros: {args.salida_libros} ({len(datos['libros'])} archivos)")
    print(f"Informe: {args.informe}")
    if sugeridas["correcciones"]:
        print(f"Sugerencias ({len(sugeridas['correcciones'])} versículos): {args.sugerencias}")
    if sin_resolver:
        print(f"\n*** {sin_resolver} anomalía(s) sin resolver. Revisa el informe y fuentes/correcciones.json. ***",
              file=sys.stderr)
        return 3
    print("Todo resuelto.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
