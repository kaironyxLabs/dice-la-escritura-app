"""Informe de anomalías en Markdown (docs/TEXTO_BIBLICO_informe.md).

El informe se genera en local y NO se sube a ningún remoto: puede contener fragmentos cortos del texto
(la palabra sospechosa y su entorno) que sirven para localizar el problema en la edición impresa.
"""

from __future__ import annotations

from collections import Counter
from collections.abc import Sequence
from datetime import datetime

from .modelo import CONTEO, ERROR, ESPACIO, INFO, SALTO, SIN_NUMERAR, Anomalia, Resultado


def _tabla(filas: Sequence[tuple[str, ...]], cabecera: tuple[str, ...]) -> list[str]:
    lineas = ["| " + " | ".join(cabecera) + " |", "|" + "|".join("---" for _ in cabecera) + "|"]
    lineas += ["| " + " | ".join(fila) + " |" for fila in filas]
    return lineas


def generar(
    resultado: Resultado,
    anomalias: Sequence[Anomalia],
    resueltas: set[int],
    pendientes_usuario: Sequence[str],
    aceptadas: Sequence[str],
    resumen: dict[str, int],
    sha_pdf: str,
    sha_json: str,
) -> str:
    sin_resolver = [(i, a) for i, a in enumerate(anomalias) if i not in resueltas]
    por_tipo: Counter[str] = Counter(a.tipo for _, a in sin_resolver)

    salida: list[str] = [
        "# Informe de importación del texto bíblico",
        "",
        "Generado por `tools/importar_biblia`. Contiene fragmentos del texto: no subir a ningún remoto.",
        "",
        f"Generado el {datetime.now().strftime('%Y-%m-%d %H:%M')}.",
        "",
        "## Resumen",
        "",
        f"- Libros: {resumen['libros']} (esperados 66)",
        f"- Capítulos: {resumen['capitulos']} (esperados 1.189)",
        f"- Versículos: {resumen['versiculos']}",
        f"- Correcciones aplicadas: {resumen['correcciones']}",
        f"- Versículos pendientes (del usuario): {len(pendientes_usuario)}",
        f"- Anomalías sin resolver: **{len(sin_resolver)}**",
        f"- SHA-256 del PDF: `{sha_pdf}`",
        f"- SHA-256 del JSON: `{sha_json}`",
        "",
    ]
    if sin_resolver:
        salida += ["Sin resolver por tipo:", ""]
        salida += [f"- `{tipo}`: {n}" for tipo, n in sorted(por_tipo.items())]
        salida += [""]
        salida += [
            "Cada anomalía se resuelve en `fuentes/correcciones.json`: con una corrección (texto verificado en tu",
            "edición impresa), marcando el versículo como pendiente o aceptándola tal cual. Nada se corrige solo.",
            "",
        ]

    def seccion(titulo: str, tipos: tuple[str, ...], incluir_resueltas: bool = False) -> None:
        elegidas = [
            (i, a) for i, a in enumerate(anomalias)
            if a.tipo in tipos and (incluir_resueltas or i not in resueltas)
        ]
        salida.append(f"## {titulo} ({len(elegidas)})")
        salida.append("")
        if not elegidas:
            salida.extend(["Ninguna.", ""])
            return
        filas = [(a.ubicacion(), a.severidad, a.detalle.replace("|", "\\|")) for _, a in elegidas]
        salida.extend(_tabla(filas, ("Ubicación", "Severidad", "Detalle")))
        salida.append("")

    seccion("Capítulos cuyo conteo difiere del esperado", (CONTEO,))
    seccion("Huecos y versículos vacíos", ("hueco", "versiculo-vacio"))
    seccion("Caracteres fuera del conjunto permitido", ("caracter-no-permitido",))
    seccion("Dígitos pegados a letras y otros patrones sospechosos", ("patron-sospechoso",))
    seccion("Posibles espacios perdidos", (ESPACIO,))
    seccion("Numeración repetida, párrafos sin número y encabezados", (
        "numeracion-duplicada", "encabezado-fuera-de-secuencia", "nombre-de-libro",
    ))

    saltos = [a for a in anomalias if a.tipo == SALTO]
    salida += [f"## Saltos de numeración registrados ({len(saltos)})", ""]
    if saltos:
        salida.extend(_tabla([(a.ubicacion(), a.detalle) for a in saltos], ("Ubicación", "Detalle")))
    else:
        salida.append("Ninguno.")
    salida.append("")

    sin_numerar = [a for a in anomalias if a.tipo == SIN_NUMERAR]
    salida += [f"## Texto sin numerar ({len(sin_numerar)})", ""]
    salida += [
        "Texto antes del versículo 1 (sobrescritos de salmos) y párrafos sin número dentro de un capítulo.",
        "Revisa especialmente los de libros que no sean Salmos o Habacuc.",
        "",
    ]
    if sin_numerar:
        salida.extend(_tabla(
            [(a.ubicacion(), a.severidad, a.detalle) for a in sin_numerar],
            ("Ubicación", "Severidad", "Detalle"),
        ))
    salida.append("")

    salida += ["## Correcciones aplicadas", ""]
    salida += [f"- {c}" for c in resultado.correcciones_aplicadas] or ["Ninguna."]
    salida += ["", "## Versículos marcados como pendientes por el usuario", ""]
    salida += [f"- {p}" for p in pendientes_usuario] or ["Ninguno."]
    salida += ["", "## Anomalías aceptadas tal cual", ""]
    salida += [f"- {a}" for a in aceptadas] or ["Ninguna."]
    salida += ["", "## Elementos ignorados del PDF", ""]
    contados = Counter(resultado.ignorados)
    salida += [f"- {texto} (x{n})" for texto, n in sorted(contados.items())] or ["Ninguno."]
    salida.append("")
    return "\n".join(salida)
