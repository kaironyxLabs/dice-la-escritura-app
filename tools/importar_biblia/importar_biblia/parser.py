"""Convierte las líneas del PDF en libros, capítulos y versículos, sin alterar el texto.

Reglas (ver docs/TEXTO_BIBLICO.md):
- Un encabezado de capítulo es una línea "NOMBRE N". Los libros se asignan por orden de aparición
  contra la tabla de 66 libros, no por el nombre escrito (el PDF trae erratas).
- Los versículos van numerados en línea; se extraen por secuencia (1, 2, 3...) y los saltos de
  numeración se registran como anomalías.
- Títulos de sección, paralelos "(1 R. 3.3-15)" y el texto sin numerar antes del versículo 1 se
  guardan aparte, nunca mezclados con el versículo.
"""

from __future__ import annotations

import difflib
import re
from collections.abc import Sequence

from .extraer import SEPARADOR, Linea
from .libros import LIBROS_RVR1960, LibroTabla, normalizar_nombre
from .modelo import (
    AVISO,
    DUPLICADO,
    ENCABEZADO,
    ERROR,
    INFO,
    NOMBRE,
    SALTO,
    SIN_NUMERAR,
    SOSPECHOSO,
    Anomalia,
    Capitulo,
    Libro,
    Paralelo,
    Resultado,
    Seccion,
)

LETRAS = "A-Za-zÁÉÍÓÚÜÑáéíóúüñ"
PATRON_ENCABEZADO = re.compile(rf"^((?:[123]\s)?[{LETRAS}.]+(?:\s[{LETRAS}.]+)*)\s(\d{{1,3}})$")
PATRON_NUMERO = re.compile(r"(?<!\S)(\d{1,3})(?=\s|$)")
PATRON_INICIO_NUMERADO = re.compile(r"^\d{1,3}(\s|$)")
PATRON_PARALELO = re.compile(r"^\(.*\)$", re.DOTALL)
# Un número suelto tras puntuación y antes de más texto: casi seguro un marcador de versículo en medio del texto.
PATRON_MARCADOR_EN_LINEA = re.compile(r"[.,;:?!)»”\"’]\s+\d{1,3}\s+\S")
FINAL_DE_ORACION = re.compile(r"[.?!:;)»”\"’]\s*$")
PUNTUACION_PREVIA = ".,;:?!)»”\"’"
APERTURAS = "(¿¡«“\"‘"
MAX_SALTO_EN_LINEA = 3
LONGITUD_MAXIMA_TITULO = 70


class ErrorEstructura(Exception):
    """El PDF no tiene la estructura esperada (66 libros, 1.189 capítulos)."""


def normalizar_espacios(texto: str) -> str:
    return " ".join(texto.split())


def termina_oracion(texto: str) -> bool:
    return bool(FINAL_DE_ORACION.search(texto))


def es_mayusculas(texto: str) -> bool:
    letras = [c for c in texto if c.isalpha()]
    return bool(letras) and texto == texto.upper() and not any(c.isdigit() for c in texto)


def _candidato_a_titulo(texto: str) -> bool:
    if PATRON_PARALELO.match(texto):
        return True
    return (
        len(texto) <= LONGITUD_MAXIMA_TITULO
        and texto[:1].isalpha()
        and texto[:1].isupper()
        and not termina_oracion(texto)
    )


def construir_bloques(lineas: Sequence[Linea], ignorados: list[str]) -> list[list[str]]:
    """Agrupa líneas en bloques separados por líneas en blanco.

    Un salto de página no separa bloques, salvo que la primera línea de la página sea claramente un
    título o un paralelo y la línea anterior cierre una oración.
    """
    bloques: list[list[str]] = []
    actual: list[str] = []

    def cerrar() -> None:
        nonlocal actual
        if actual:
            bloques.append(actual)
            actual = []

    for linea in lineas:
        texto = linea.texto.strip()
        if not texto:
            cerrar()
            continue
        if SEPARADOR.match(texto):
            cerrar()
            ignorados.append("línea de guiones (separador del PDF)")
            continue
        if linea.inicio_de_pagina and actual and _candidato_a_titulo(texto) and termina_oracion(actual[-1]):
            cerrar()
        actual.append(texto)
    cerrar()
    return bloques


def clasificar_bloque(bloque: list[str]) -> str:
    texto = " ".join(bloque)
    if PATRON_INICIO_NUMERADO.match(bloque[0]):
        return "versiculos"
    if any(PATRON_INICIO_NUMERADO.match(linea) for linea in bloque[1:]):
        return "continuacion"  # poesía: sigue el versículo anterior y trae marcadores más abajo
    if PATRON_PARALELO.match(texto):
        return "paralelo"
    if PATRON_MARCADOR_EN_LINEA.search(texto):
        return "continuacion"  # continuación de un versículo (p. ej. tras un salto de página) con marcadores
    if es_mayusculas(texto):
        return "mayusculas"
    if termina_oracion(texto):
        return "texto"
    return "titulo"


class _ConstructorCapitulo:
    """Acumula los versículos de un capítulo siguiendo la secuencia de numeración."""

    def __init__(
        self, libro_id: str, numero: int, anomalias: list[Anomalia], lineas_del_cuerpo: list[str],
    ) -> None:
        self.libro_id = libro_id
        # Todas las líneas con texto de versículos del capítulo, para decidir marcadores dudosos mirando adelante.
        self._restantes = lineas_del_cuerpo
        self._pos_linea = 0
        self.capitulo = Capitulo(numero=numero)
        self.anomalias = anomalias
        self.esperado = 1
        self.versiculos: dict[int, list[str]] = {}
        self.actual: int | None = None
        self.pendientes_antes: list[tuple[str, str]] = []

    def _anomalia(self, tipo: str, severidad: str, versiculo: int | None, detalle: str) -> None:
        self.anomalias.append(
            Anomalia(tipo, severidad, self.libro_id, self.capitulo.numero, versiculo, detalle),
        )

    def _abrir(self, numero: int) -> None:
        self.versiculos[numero] = []
        self.actual = numero
        self.esperado = numero + 1
        for clase, texto in self.pendientes_antes:
            if clase == "titulo":
                self.capitulo.secciones.append(Seccion(numero, texto))
            else:
                self.capitulo.paralelos.append(Paralelo(numero, texto))
        self.pendientes_antes.clear()

    def _agregar_texto(self, texto: str) -> None:
        if texto and self.actual is not None:
            self.versiculos[self.actual].append(texto)

    def agregar_bloque_numerado(self, bloque: list[str]) -> None:
        for indice_linea, linea in enumerate(bloque):
            self._pos_linea += 1
            if self.actual is None and not PATRON_INICIO_NUMERADO.match(linea):
                # Texto sin numerar antes del primer versículo (sobrescrito): va al encabezado.
                texto = normalizar_espacios(linea)
                self.capitulo.encabezado = (
                    f"{self.capitulo.encabezado} {texto}" if self.capitulo.encabezado else texto
                )
                continue
            self._procesar_linea(linea, inicio_de_bloque=(indice_linea == 0))

    def _procesar_linea(self, linea: str, inicio_de_bloque: bool) -> None:
        """Recorre la línea en orden: cada marcador aceptado abre un versículo y actualiza lo esperado."""
        cursor = 0
        for coincidencia in PATRON_NUMERO.finditer(linea):
            valor = int(coincidencia.group(1))
            inicio = coincidencia.start()
            if inicio == 0:
                aceptado = self._decidir_inicio_de_linea(valor, inicio_de_bloque)
            else:
                aceptado = self._decidir_en_linea(valor, linea, inicio, coincidencia.end())
            if aceptado:
                self._agregar_texto(linea[cursor:inicio].strip())
                self._abrir(valor)
                cursor = coincidencia.end()
        self._agregar_texto(linea[cursor:].strip())

    def _decidir_inicio_de_linea(self, valor: int, inicio_de_bloque: bool) -> bool:
        """Un número al comienzo de una línea es casi siempre un marcador de versículo."""
        if valor == self.esperado:
            return True
        if valor > self.esperado:
            if inicio_de_bloque or valor - self.esperado <= MAX_SALTO_EN_LINEA:
                self._anomalia(
                    SALTO, INFO, valor,
                    f"salto de numeración: se esperaba {self.esperado} y aparece {valor}",
                )
                return True
            return False
        if inicio_de_bloque:
            self._anomalia(
                DUPLICADO, AVISO, valor,
                f"numeración repetida o retrocede: se esperaba {self.esperado} y aparece {valor}",
            )
        return False

    @staticmethod
    def _marcador_claro(linea: str, inicio: int, fin: int) -> bool:
        """Número que, por su entorno, es casi seguro un marcador: al inicio de línea, tras puntuación o "Selah",
        o seguido de algo que empieza como un versículo (mayúscula, paréntesis, signo de apertura, comillas)."""
        previo = linea[:inicio].rstrip()
        if not previo:
            return True
        siguiente = linea[fin:].lstrip()[:1]
        return (
            previo[-1] in PUNTUACION_PREVIA
            or previo.endswith("Selah")
            or (bool(siguiente) and (siguiente in APERTURAS or siguiente.isupper()))
        )

    def _hay_marcador_claro_despues(self, valor: int, linea: str, fin: int) -> bool:
        """¿Aparece más adelante en el capítulo el mismo número como marcador claro?"""
        siguientes = self._restantes[self._pos_linea:]  # _pos_linea ya cuenta la línea actual
        for coincidencia in PATRON_NUMERO.finditer(linea):
            if coincidencia.start() >= fin and int(coincidencia.group(1)) == valor:
                if self._marcador_claro(linea, coincidencia.start(), coincidencia.end()):
                    return True
        for texto in siguientes:
            for coincidencia in PATRON_NUMERO.finditer(texto):
                if int(coincidencia.group(1)) == valor and self._marcador_claro(
                    texto, coincidencia.start(), coincidencia.end(),
                ):
                    return True
        return False

    def _decidir_en_linea(self, valor: int, linea: str, inicio: int, fin: int) -> bool:
        """Un número en medio de una línea es marcador si es el esperado. Si su entorno no lo delata (sigue a
        una palabra y precede a una minúscula) solo se acepta cuando no hay más adelante un marcador claro
        con ese mismo número. Un salto de numeración solo se acepta si sigue a puntuación."""
        previo = linea[:inicio].rstrip()
        if not previo:
            return False
        if valor == self.esperado:
            if self._marcador_claro(linea, inicio, fin):
                return True
            if self._hay_marcador_claro_despues(valor, linea, fin):
                return False
            self._anomalia(
                SIN_NUMERAR, INFO, valor,
                "marcador de versículo poco claro (tras una palabra y antes de una minúscula)",
            )
            return True
        precede = previo[-1] in PUNTUACION_PREVIA or previo.endswith("Selah")
        if precede and self.esperado < valor <= self.esperado + MAX_SALTO_EN_LINEA:
            self._anomalia(
                SALTO, INFO, valor,
                f"salto de numeración: se esperaba {self.esperado} y aparece {valor}",
            )
            return True
        return False

    def agregar_bloque_no_numerado(self, clase: str, bloque: list[str]) -> None:
        texto = normalizar_espacios(" ".join(bloque))
        if clase in ("titulo", "paralelo"):
            self.pendientes_antes.append((clase, texto))
        elif clase == "texto":
            if self.actual is None:
                self._pos_linea += len(bloque)
                self.capitulo.encabezado = (
                    f"{self.capitulo.encabezado} {texto}" if self.capitulo.encabezado else texto
                )
            else:
                self._anomalia(
                    SIN_NUMERAR, INFO, self.actual,
                    "párrafo sin número dentro del capítulo, unido al versículo anterior",
                )
                self.agregar_bloque_numerado(bloque)
        else:
            self._anomalia(
                SOSPECHOSO, AVISO, None,
                "bloque en mayúsculas dentro del capítulo, no se incorporó al texto",
            )

    def terminar(self) -> Capitulo:
        if self.versiculos:
            maximo = max(self.versiculos)
            self.capitulo.versiculos = [
                normalizar_espacios(" ".join(self.versiculos[n])) if n in self.versiculos else None
                for n in range(1, maximo + 1)
            ]
        for clase, texto in self.pendientes_antes:  # sin versículo posterior: no debería ocurrir
            self._anomalia(SOSPECHOSO, AVISO, None, f"{clase} sin versículo posterior")
        return self.capitulo




def _ultimo_bloque_numerado(clases: list[str]) -> int:
    for i in range(len(clases) - 1, -1, -1):
        if clases[i] in ("versiculos", "continuacion"):
            return i
    return -1


def _nombre_parecido(escrito: str, libro: LibroTabla) -> float:
    return difflib.SequenceMatcher(
        None, normalizar_nombre(escrito), normalizar_nombre(libro.nombre),
    ).ratio()


def _construir_capitulo(
    libro_id: str,
    numero: int,
    cuerpo: list[tuple[str, list[str]]],
    anomalias: list[Anomalia],
) -> Capitulo:
    lineas = [
        linea for clase, bloque in cuerpo if clase in ("versiculos", "continuacion", "texto") for linea in bloque
    ]
    constructor = _ConstructorCapitulo(libro_id, numero, anomalias, lineas)
    for clase, bloque in cuerpo:
        if clase in ("versiculos", "continuacion"):
            constructor.agregar_bloque_numerado(bloque)
        else:
            constructor.agregar_bloque_no_numerado(clase, bloque)
    return constructor.terminar()


def _aplicar_texto_previo(
    capitulo: Capitulo,
    libro: LibroTabla,
    previo: list[tuple[str, list[str]]],
    anomalias: list[Anomalia],
    ignorados: list[str],
) -> None:
    """Títulos, paralelos y texto sin numerar que el PDF coloca justo antes del encabezado del capítulo."""
    secciones: list[Seccion] = []
    paralelos: list[Paralelo] = []
    sobrescritos: list[str] = []
    for clase, bloque in previo:
        texto = normalizar_espacios(" ".join(bloque))
        if clase == "titulo":
            secciones.append(Seccion(1, texto))
        elif clase == "paralelo":
            paralelos.append(Paralelo(1, texto))
        elif clase == "texto":
            sobrescritos.append(texto)
        else:  # encabezado en mayúsculas: nombre del libro, "LIBRO SEGUNDO" de los Salmos, etc.
            ignorados.append(f"encabezado en mayúsculas ({libro.id} {capitulo.numero}): \"{texto}\"")
    capitulo.secciones = secciones + capitulo.secciones
    capitulo.paralelos = paralelos + capitulo.paralelos
    if sobrescritos:
        texto = " ".join(sobrescritos)
        capitulo.encabezado = f"{texto} {capitulo.encabezado}" if capitulo.encabezado else texto
        anomalias.append(Anomalia(
            SIN_NUMERAR, INFO, libro.id, capitulo.numero, None,
            f"texto sin numerar antes del versículo 1 ({len(texto)} caracteres)",
        ))


def analizar(
    lineas: Sequence[Linea],
    tabla: Sequence[LibroTabla] = LIBROS_RVR1960,
) -> Resultado:
    """Interpreta las líneas. Lanza ErrorEstructura si no hay un encabezado por cada capítulo de la tabla."""
    esperados = [(libro, n) for libro in tabla for n in range(1, libro.capitulos + 1)]
    encabezados = [
        (i, coincidencia)
        for i, linea in enumerate(lineas)
        if (coincidencia := PATRON_ENCABEZADO.match(linea.texto.strip()))
    ]
    if len(encabezados) != len(esperados):
        raise ErrorEstructura(
            f"Se esperaban {len(esperados)} encabezados de capítulo y se encontraron {len(encabezados)}.",
        )

    anomalias: list[Anomalia] = []
    ignorados: list[str] = []
    posiciones = [i for i, _ in encabezados]

    # Zona j: lo que hay entre el encabezado j-1 y el j (la 0 es lo anterior al primero, la última llega al final).
    # Cada zona trae el cuerpo del capítulo j-1 seguido del texto previo del capítulo j.
    zonas: list[Sequence[Linea]] = [lineas[: posiciones[0]]]
    for k, inicio in enumerate(posiciones):
        fin = posiciones[k + 1] if k + 1 < len(posiciones) else len(lineas)
        zonas.append(lineas[inicio + 1: fin])

    libros = {libro.id: Libro(libro.id) for libro in tabla}
    capitulos: list[Capitulo] = []
    previos: list[list[tuple[str, list[str]]]] = []

    for j, zona in enumerate(zonas):
        bloques = construir_bloques(zona, ignorados)
        clases = [clasificar_bloque(b) for b in bloques]
        ultimo = _ultimo_bloque_numerado(clases) if j > 0 else -1
        if j > 0:
            libro, numero = esperados[j - 1]
            cuerpo = list(zip(clases[: ultimo + 1], bloques[: ultimo + 1]))
            capitulo = _construir_capitulo(libro.id, numero, cuerpo, anomalias)
            capitulos.append(capitulo)
        cola = list(zip(clases[ultimo + 1:], bloques[ultimo + 1:]))
        if j < len(esperados):
            previos.append(cola)
        else:
            for clase, bloque in cola:
                ignorados.append(f"texto tras el último capítulo ({clase})")
                libro, numero = esperados[-1]
                anomalias.append(Anomalia(
                    SOSPECHOSO, AVISO, libro.id, numero, None,
                    f"texto de tipo {clase} después del último versículo",
                ))

    for k, ((libro, numero), capitulo) in enumerate(zip(esperados, capitulos)):
        _, coincidencia = encabezados[k]
        nombre_escrito, numero_escrito = coincidencia.group(1), int(coincidencia.group(2))
        capitulo.nombre_escrito = nombre_escrito
        if numero_escrito != numero:
            anomalias.append(Anomalia(
                ENCABEZADO, ERROR, libro.id, numero, None,
                f"el encabezado dice {numero_escrito} y por orden corresponde {numero}",
            ))
        if numero == 1:
            if _nombre_parecido(nombre_escrito, libro) < 0.6:
                anomalias.append(Anomalia(
                    NOMBRE, AVISO, libro.id, numero, None,
                    f"el nombre escrito \"{nombre_escrito}\" no se parece a \"{libro.nombre}\"",
                ))
            elif normalizar_nombre(nombre_escrito) != normalizar_nombre(libro.nombre):
                anomalias.append(Anomalia(
                    NOMBRE, INFO, libro.id, numero, None,
                    f"nombre escrito \"{nombre_escrito}\" (errata); se asigna por orden a \"{libro.nombre}\"",
                ))
        _aplicar_texto_previo(capitulo, libro, previos[k], anomalias, ignorados)
        libros[libro.id].capitulos.append(capitulo)

    return Resultado(
        libros=[libros[libro.id] for libro in tabla],
        anomalias=anomalias,
        encabezados_encontrados=len(encabezados),
        ignorados=ignorados,
    )
