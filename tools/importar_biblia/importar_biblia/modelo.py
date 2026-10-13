"""Estructuras de datos del importador."""

from __future__ import annotations

from dataclasses import dataclass, field

# Severidades de una anomalía.
INFO = "info"        # informativa: no exige acción
AVISO = "aviso"      # sospechosa: debe corregirse, marcarse pendiente o aceptarse
ERROR = "error"      # rompe una verificación obligatoria: debe corregirse o marcarse pendiente

# Tipos de anomalía.
CONTEO = "conteo"
SALTO = "salto-numeracion"
DUPLICADO = "numeracion-duplicada"
HUECO = "hueco"
VACIO = "versiculo-vacio"
CARACTER = "caracter-no-permitido"
SOSPECHOSO = "patron-sospechoso"
ESPACIO = "espacio-perdido"
NOMBRE = "nombre-de-libro"
ENCABEZADO = "encabezado-fuera-de-secuencia"
SIN_NUMERAR = "texto-sin-numerar"


@dataclass
class Anomalia:
    tipo: str
    severidad: str
    libro: str
    capitulo: int
    versiculo: int | None
    detalle: str
    # Datos estructurados opcionales (p. ej. la palabra sospechosa y su posible corrección).
    datos: dict | None = None

    @property
    def clave(self) -> tuple:
        return (self.tipo, self.libro, self.capitulo, self.versiculo)

    def ubicacion(self) -> str:
        base = f"{self.libro} {self.capitulo}"
        return f"{base}:{self.versiculo}" if self.versiculo is not None else base


@dataclass
class Seccion:
    antes_del_versiculo: int
    titulo: str


@dataclass
class Paralelo:
    antes_del_versiculo: int
    texto: str


@dataclass
class Capitulo:
    numero: int
    encabezado: str | None = None
    # versiculos[i] es el versículo i + 1; None marca un hueco de numeración.
    versiculos: list[str | None] = field(default_factory=list)
    secciones: list[Seccion] = field(default_factory=list)
    paralelos: list[Paralelo] = field(default_factory=list)
    pendientes: list[int] = field(default_factory=list)
    nombre_escrito: str = ""


@dataclass
class Libro:
    id: str
    capitulos: list[Capitulo] = field(default_factory=list)


@dataclass
class Resultado:
    libros: list[Libro]
    anomalias: list[Anomalia]
    encabezados_encontrados: int
    # Registro de lo ignorado (cabeceras de libro, separadores) para el informe.
    ignorados: list[str] = field(default_factory=list)
    # Correcciones aplicadas, para el informe.
    correcciones_aplicadas: list[str] = field(default_factory=list)
