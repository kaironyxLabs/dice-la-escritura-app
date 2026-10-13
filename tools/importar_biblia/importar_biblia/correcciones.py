"""Correcciones escritas por el usuario (fuentes/correcciones.json).

El importador nunca corrige por su cuenta: todo cambio sale de este archivo, con el texto verificado en
una edición impresa u otra fuente. Formato:

{
  "correcciones": [
    {"libro": "GEN", "capitulo": 30, "versiculo": 7, "texto": "<texto verificado>"},
    {"libro": "EXO", "capitulo": 2, "versiculo": 15, "reemplazos": [{"buscar": "<tal cual>", "por": "<corregido>"}]},
    {"libro": "1CO", "capitulo": 1, "versiculo": 32, "renumerar_a": 31},
    {"libro": "EZK", "capitulo": 31, "versiculo": 19, "eliminar": true}
  ],
  "pendientes": [
    {"libro": "PSA", "capitulo": 39, "versiculo": 11, "motivo": "número dañado"}
  ],
  "aceptadas": [
    {"libro": "3JN", "capitulo": 1, "tipo": "conteo", "motivo": "RVR1960 numera 15 versículos"}
  ]
}

Cada corrección lleva exactamente una operación: "texto" (reemplaza o inserta el versículo),
"reemplazos" (cada "buscar" debe aparecer exactamente una vez), "renumerar_a" (mueve el texto a otro número
vacío) o "eliminar". Un "pendiente" deja el versículo sin texto utilizable hasta que se verifique.
Una "nota" corrige un título de sección o una referencia a pasajes paralelos (que se guardan aparte del
versículo): {"libro", "capitulo", "tipo": "paralelo" | "seccion", "buscar": "<tal cual>", "por": "<corregido>"};
"buscar" debe aparecer exactamente una vez entre los textos de ese tipo del capítulo.
Una "aceptada" declara que una anomalía (por su "tipo", y opcionalmente "versiculo") es correcta tal cual.
"""

from __future__ import annotations

import json
from collections.abc import Sequence
from dataclasses import dataclass, field
from pathlib import Path

from .libros import LibroTabla
from .modelo import Capitulo, Paralelo, Resultado

OPERACIONES = ("texto", "reemplazos", "renumerar_a", "eliminar")


class ErrorCorreccion(Exception):
    """El archivo de correcciones es inválido o no aplica al texto extraído."""


@dataclass
class Correcciones:
    correcciones: list[dict] = field(default_factory=list)
    pendientes: dict[tuple[str, int, int], str] = field(default_factory=dict)
    aceptadas: list[dict] = field(default_factory=list)
    notas: list[dict] = field(default_factory=list)

    def versiculos_corregidos(self) -> set[tuple[str, int, int]]:
        corregidos: set[tuple[str, int, int]] = set()
        for c in self.correcciones:
            corregidos.add((c["libro"], c["capitulo"], c["versiculo"]))
            if "renumerar_a" in c:
                corregidos.add((c["libro"], c["capitulo"], c["renumerar_a"]))
        return corregidos

    def esta_aceptada(self, tipo: str, libro: str, capitulo: int, versiculo: int | None) -> bool:
        for a in self.aceptadas:
            if a["tipo"] != tipo or a["libro"] != libro or a["capitulo"] != capitulo:
                continue
            if "versiculo" not in a or a["versiculo"] == versiculo:
                return True
        return False


def cargar(ruta: Path | None, tabla: Sequence[LibroTabla]) -> Correcciones:
    if ruta is None or not ruta.exists():
        return Correcciones()
    try:
        datos = json.loads(ruta.read_text(encoding="utf-8"))
    except json.JSONDecodeError as error:
        raise ErrorCorreccion(f"{ruta}: JSON inválido ({error})") from error
    if not isinstance(datos, dict):
        raise ErrorCorreccion(f"{ruta}: se esperaba un objeto con 'correcciones', 'pendientes' y 'aceptadas'")
    desconocidas = set(datos) - {"correcciones", "pendientes", "aceptadas", "notas"}
    if desconocidas:
        raise ErrorCorreccion(f"{ruta}: claves desconocidas {sorted(desconocidas)}")

    capitulos = {libro.id: libro.capitulos for libro in tabla}

    def validar_ubicacion(entrada: dict, donde: str, versiculo_obligatorio: bool = True) -> None:
        libro = entrada.get("libro")
        capitulo = entrada.get("capitulo")
        if libro not in capitulos:
            raise ErrorCorreccion(f"{donde}: libro desconocido {libro!r}")
        if not isinstance(capitulo, int) or not 1 <= capitulo <= capitulos[libro]:
            raise ErrorCorreccion(f"{donde}: capítulo inválido {capitulo!r} en {libro}")
        if versiculo_obligatorio or "versiculo" in entrada:
            versiculo = entrada.get("versiculo")
            if not isinstance(versiculo, int) or versiculo < 1:
                raise ErrorCorreccion(f"{donde}: versículo inválido {versiculo!r}")

    resultado = Correcciones()
    for i, entrada in enumerate(datos.get("correcciones", []), start=1):
        donde = f"correcciones[{i}]"
        validar_ubicacion(entrada, donde)
        presentes = [op for op in OPERACIONES if op in entrada]
        if len(presentes) != 1:
            raise ErrorCorreccion(f"{donde}: debe llevar exactamente una operación de {OPERACIONES}")
        extras = set(entrada) - {"libro", "capitulo", "versiculo", "motivo", *OPERACIONES}
        if extras:
            raise ErrorCorreccion(f"{donde}: claves desconocidas {sorted(extras)}")
        operacion = presentes[0]
        if operacion == "texto" and not (isinstance(entrada["texto"], str) and entrada["texto"].strip()):
            raise ErrorCorreccion(f"{donde}: 'texto' debe ser un texto no vacío")
        if operacion == "reemplazos":
            reemplazos = entrada["reemplazos"]
            if not (isinstance(reemplazos, list) and reemplazos and all(
                isinstance(r, dict) and set(r) == {"buscar", "por"} and isinstance(r["buscar"], str)
                and r["buscar"] and isinstance(r["por"], str) for r in reemplazos
            )):
                raise ErrorCorreccion(f"{donde}: 'reemplazos' requiere objetos con 'buscar' y 'por'")
        if operacion == "renumerar_a" and not (isinstance(entrada["renumerar_a"], int) and entrada["renumerar_a"] >= 1):
            raise ErrorCorreccion(f"{donde}: 'renumerar_a' debe ser un número de versículo")
        if operacion == "eliminar" and entrada["eliminar"] is not True:
            raise ErrorCorreccion(f"{donde}: 'eliminar' solo admite true")
        resultado.correcciones.append(entrada)

    for i, entrada in enumerate(datos.get("pendientes", []), start=1):
        donde = f"pendientes[{i}]"
        validar_ubicacion(entrada, donde)
        resultado.pendientes[(entrada["libro"], entrada["capitulo"], entrada["versiculo"])] = entrada.get("motivo", "")

    for i, entrada in enumerate(datos.get("aceptadas", []), start=1):
        donde = f"aceptadas[{i}]"
        validar_ubicacion(entrada, donde, versiculo_obligatorio=False)
        if not isinstance(entrada.get("tipo"), str):
            raise ErrorCorreccion(f"{donde}: falta 'tipo'")
        resultado.aceptadas.append(entrada)
    for i, entrada in enumerate(datos.get("notas", []), start=1):
        donde = f"notas[{i}]"
        validar_ubicacion(entrada, donde, versiculo_obligatorio=False)
        extras = set(entrada) - {"libro", "capitulo", "tipo", "buscar", "por", "motivo"}
        if extras:
            raise ErrorCorreccion(f"{donde}: claves desconocidas {sorted(extras)}")
        if entrada.get("tipo") not in ("paralelo", "seccion"):
            raise ErrorCorreccion(f"{donde}: 'tipo' debe ser 'paralelo' o 'seccion'")
        if not (isinstance(entrada.get("buscar"), str) and entrada["buscar"] and isinstance(entrada.get("por"), str) and entrada["por"].strip()):
            raise ErrorCorreccion(f"{donde}: requiere 'buscar' y 'por' no vacíos")
        resultado.notas.append(entrada)
    return resultado


def _reclasificar_paralelos(capitulo: Capitulo) -> None:
    """Una referencia como "(Ex. 35.10-19)" mal cerrada en el PDF se tomó por título de sección; una vez
    corregida, pasa a ser un paralelo (conservando su posición y sin tocar su texto)."""
    movidas = [s for s in capitulo.secciones if s.titulo.startswith("(") and s.titulo.endswith(")")]
    for seccion in movidas:
        capitulo.secciones.remove(seccion)
        capitulo.paralelos.append(Paralelo(seccion.antes_del_versiculo, seccion.titulo))
    capitulo.paralelos.sort(key=lambda p: p.antes_del_versiculo)


def _asegurar_longitud(capitulo: Capitulo, versiculo: int) -> None:
    while len(capitulo.versiculos) < versiculo:
        capitulo.versiculos.append(None)


def _recortar_cola(capitulo: Capitulo) -> None:
    """Quita los huecos al final de la lista, salvo los que el usuario marcó como pendientes."""
    minimo = max(capitulo.pendientes, default=0)
    while len(capitulo.versiculos) > minimo and capitulo.versiculos[-1] is None:
        capitulo.versiculos.pop()


def _capitulo(resultado: Resultado, libro: str, numero: int) -> Capitulo:
    for objeto in resultado.libros:
        if objeto.id == libro:
            return objeto.capitulos[numero - 1]
    raise ErrorCorreccion(f"libro desconocido {libro}")


def aplicar(resultado: Resultado, correcciones: Correcciones) -> None:
    """Aplica correcciones y marcas de pendiente sobre el resultado (sin escribir el texto en ningún registro)."""
    for entrada in correcciones.correcciones:
        libro, numero, versiculo = entrada["libro"], entrada["capitulo"], entrada["versiculo"]
        capitulo = _capitulo(resultado, libro, numero)
        donde = f"{libro} {numero}:{versiculo}"
        if "texto" in entrada:
            _asegurar_longitud(capitulo, versiculo)
            capitulo.versiculos[versiculo - 1] = " ".join(entrada["texto"].split())
            resultado.correcciones_aplicadas.append(f"{donde}: texto reemplazado o insertado")
        elif "reemplazos" in entrada:
            actual = capitulo.versiculos[versiculo - 1] if versiculo <= len(capitulo.versiculos) else None
            if actual is None:
                raise ErrorCorreccion(f"{donde}: no hay texto sobre el que aplicar 'reemplazos'")
            for reemplazo in entrada["reemplazos"]:
                veces = actual.count(reemplazo["buscar"])
                if veces != 1:
                    raise ErrorCorreccion(
                        f"{donde}: el texto a buscar aparece {veces} veces (debe aparecer exactamente una)",
                    )
                actual = actual.replace(reemplazo["buscar"], reemplazo["por"], 1)
            capitulo.versiculos[versiculo - 1] = " ".join(actual.split())
            resultado.correcciones_aplicadas.append(f"{donde}: {len(entrada['reemplazos'])} reemplazo(s)")
        elif "renumerar_a" in entrada:
            destino = entrada["renumerar_a"]
            origen_texto = capitulo.versiculos[versiculo - 1] if versiculo <= len(capitulo.versiculos) else None
            if origen_texto is None:
                raise ErrorCorreccion(f"{donde}: no hay texto que renumerar")
            _asegurar_longitud(capitulo, destino)
            if capitulo.versiculos[destino - 1] is not None:
                raise ErrorCorreccion(f"{donde}: el versículo destino {destino} ya tiene texto")
            capitulo.versiculos[destino - 1] = origen_texto
            capitulo.versiculos[versiculo - 1] = None
            resultado.correcciones_aplicadas.append(f"{donde}: renumerado a {destino}")
        else:
            if versiculo > len(capitulo.versiculos):
                raise ErrorCorreccion(f"{donde}: no existe el versículo que se quiere eliminar")
            capitulo.versiculos[versiculo - 1] = None
            resultado.correcciones_aplicadas.append(f"{donde}: eliminado")
        _recortar_cola(capitulo)

    for entrada in correcciones.notas:
        libro, numero = entrada["libro"], entrada["capitulo"]
        capitulo = _capitulo(resultado, libro, numero)
        elementos = capitulo.paralelos if entrada["tipo"] == "paralelo" else capitulo.secciones
        atributo = "texto" if entrada["tipo"] == "paralelo" else "titulo"
        coincidencias = [e for e in elementos if entrada["buscar"] in getattr(e, atributo)]
        veces = sum(getattr(e, atributo).count(entrada["buscar"]) for e in elementos)
        if len(coincidencias) != 1 or veces != 1:
            raise ErrorCorreccion(
                f"{libro} {numero}: el texto a buscar en {entrada['tipo']} aparece {veces} veces (debe aparecer exactamente una)",
            )
        elemento = coincidencias[0]
        setattr(elemento, atributo, " ".join(getattr(elemento, atributo).replace(entrada["buscar"], entrada["por"], 1).split()))
        resultado.correcciones_aplicadas.append(f"{libro} {numero}: {entrada['tipo']} corregido")
        _reclasificar_paralelos(capitulo)

    for (libro, numero, versiculo) in correcciones.pendientes:
        capitulo = _capitulo(resultado, libro, numero)
        _asegurar_longitud(capitulo, versiculo)
        if versiculo not in capitulo.pendientes:
            capitulo.pendientes.append(versiculo)
        capitulo.pendientes.sort()
