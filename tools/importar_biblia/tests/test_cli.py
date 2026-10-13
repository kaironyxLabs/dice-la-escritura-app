"""Flujo completo con un PDF sintético: todo el texto es falso, nunca RVR1960."""

import hashlib
import json

import pytest

from importar_biblia.cli import main

from .ayudas import TABLA, crear_pdf

FALSO = "Texto de prueba, no es RVR 1960."

CONTENIDO = [
    "LIBRO UNO ", " ", "Un titulo de prueba ", " ", "Libro Uno 1 ", " ",
    f"1 {FALSO} 2 Segundo {FALSO} ", " ",
    "Libro Uno 2 ", " ", f"1 Tercero {FALSO} ", " ",
    "LIBRO DOS ", " ", "Libro Dos 1 ", " ",
    f"1 Cuarto {FALSO} ", " ", f"2 Quinto {FALSO} ",
]

ESPERADO = {"fuente": "prueba", "libros": {"UNO": [2, 1], "DOS": [2]}}

DOC = "# Texto\n\n<!-- importacion:inicio -->\nantiguo\n<!-- importacion:fin -->\n\nResto.\n"


@pytest.fixture
def entorno(tmp_path):
    esperado = tmp_path / "esperado.json"
    esperado.write_text(json.dumps(ESPERADO), encoding="utf-8")
    doc = tmp_path / "TEXTO_BIBLICO.md"
    doc.write_text(DOC, encoding="utf-8")
    return {
        "tmp": tmp_path,
        "esperado": esperado,
        "doc": doc,
        "salida": tmp_path / "salida" / "rvr1960.json",
        "informe": tmp_path / "informe.md",
        "correcciones": tmp_path / "correcciones.json",
    }


def ejecutar(entorno, pdf, extra=()):
    argumentos = [
        "--pdf", str(pdf), "--salida", str(entorno["salida"]), "--esperado", str(entorno["esperado"]),
        "--informe", str(entorno["informe"]), "--doc", str(entorno["doc"]),
        "--correcciones", str(entorno["correcciones"]),
        "--sugerencias", str(entorno["tmp"] / "sugeridas.json"), *extra,
    ]
    return main(argumentos, tabla=TABLA)


def test_importacion_limpia(entorno):
    pdf = crear_pdf(entorno["tmp"] / "prueba.pdf", [CONTENIDO])
    assert ejecutar(entorno, pdf) == 0

    datos = json.loads(entorno["salida"].read_text(encoding="utf-8"))
    assert datos["traduccion"] == "RVR1960"
    assert [libro["id"] for libro in datos["libros"]] == ["UNO", "DOS"]
    capitulo = datos["libros"][0]["capitulos"][0]
    assert capitulo["numero"] == 1
    assert capitulo["versiculos"] == [FALSO, f"Segundo {FALSO}"]
    assert capitulo["secciones"] == [{"antesDelVersiculo": 1, "titulo": "Un titulo de prueba"}]
    assert capitulo["paralelos"] == [] and capitulo["pendientes"] == [] and capitulo["encabezado"] is None

    doc = entorno["doc"].read_text(encoding="utf-8")
    sha_pdf = hashlib.sha256(pdf.read_bytes()).hexdigest()
    sha_json = hashlib.sha256(entorno["salida"].read_bytes()).hexdigest()
    assert sha_pdf in doc and sha_json in doc and "Resto." in doc and "antiguo" not in doc
    assert "Anomalías sin resolver: 0" in doc


def test_la_salida_es_determinista(entorno):
    pdf = crear_pdf(entorno["tmp"] / "prueba.pdf", [CONTENIDO])
    ejecutar(entorno, pdf)
    primera = entorno["salida"].read_bytes()
    ejecutar(entorno, pdf)
    assert entorno["salida"].read_bytes() == primera


def test_pdf_inexistente(entorno):
    assert ejecutar(entorno, entorno["tmp"] / "no_existe.pdf") == 2


def test_estructura_incorrecta_no_escribe_el_json(entorno):
    pdf = crear_pdf(entorno["tmp"] / "corto.pdf", [CONTENIDO[:12]])
    assert ejecutar(entorno, pdf) == 2
    assert not entorno["salida"].exists()


def test_anomalias_sin_resolver_escriben_json_con_pendientes_y_salen_con_3(entorno):
    contenido = [linea.replace("2 Segundo", "3 Segundo") for linea in CONTENIDO]  # falta el versículo 2
    pdf = crear_pdf(entorno["tmp"] / "hueco.pdf", [contenido])
    assert ejecutar(entorno, pdf) == 3

    datos = json.loads(entorno["salida"].read_text(encoding="utf-8"))
    capitulo = datos["libros"][0]["capitulos"][0]
    assert capitulo["versiculos"][1] == ""
    assert capitulo["pendientes"] == [2]  # la app no mostrará un versículo roto
    informe = entorno["informe"].read_text(encoding="utf-8")
    assert "UNO 1:2" in informe and "hueco" in informe


def test_una_correccion_resuelve_el_hueco(entorno):
    contenido = [linea.replace("2 Segundo", "3 Segundo") for linea in CONTENIDO]
    pdf = crear_pdf(entorno["tmp"] / "hueco.pdf", [contenido])
    entorno["esperado"].write_text(json.dumps({"libros": {"UNO": [3, 1], "DOS": [2]}}), encoding="utf-8")
    entorno["correcciones"].write_text(json.dumps({"correcciones": [
        {"libro": "UNO", "capitulo": 1, "versiculo": 2, "texto": "Segundo verificado."},
    ]}), encoding="utf-8")
    assert ejecutar(entorno, pdf) == 0
    capitulo = json.loads(entorno["salida"].read_text(encoding="utf-8"))["libros"][0]["capitulos"][0]
    assert capitulo["versiculos"][1] == "Segundo verificado."
    assert capitulo["pendientes"] == []


def test_marcar_pendiente_resuelve_la_anomalia(entorno):
    contenido = [linea.replace("2 Segundo", "3 Segundo") for linea in CONTENIDO]
    pdf = crear_pdf(entorno["tmp"] / "hueco.pdf", [contenido])
    entorno["esperado"].write_text(json.dumps({"libros": {"UNO": [3, 1], "DOS": [2]}}), encoding="utf-8")
    entorno["correcciones"].write_text(json.dumps({"pendientes": [
        {"libro": "UNO", "capitulo": 1, "versiculo": 2, "motivo": "número dañado"},
    ]}), encoding="utf-8")
    assert ejecutar(entorno, pdf) == 0
    capitulo = json.loads(entorno["salida"].read_text(encoding="utf-8"))["libros"][0]["capitulos"][0]
    assert capitulo["pendientes"] == [2] and capitulo["versiculos"][1] == ""


def test_conteo_distinto_exige_correccion_o_aceptarlo(entorno):
    pdf = crear_pdf(entorno["tmp"] / "prueba.pdf", [CONTENIDO])
    entorno["esperado"].write_text(json.dumps({"libros": {"UNO": [2, 2], "DOS": [2]}}), encoding="utf-8")
    assert ejecutar(entorno, pdf) == 3
    entorno["correcciones"].write_text(json.dumps({"aceptadas": [
        {"libro": "UNO", "capitulo": 2, "tipo": "conteo", "motivo": "numeración distinta"},
    ]}), encoding="utf-8")
    assert ejecutar(entorno, pdf) == 0


def test_caracter_fuera_del_conjunto_permitido_falla_en_voz_alta(entorno):
    contenido = [linea.replace("Tercero", "Ter=cero") for linea in CONTENIDO]
    pdf = crear_pdf(entorno["tmp"] / "raro.pdf", [contenido])
    assert ejecutar(entorno, pdf) == 3
    capitulo = json.loads(entorno["salida"].read_text(encoding="utf-8"))["libros"][0]["capitulos"][1]
    assert capitulo["pendientes"] == [1]
    assert "caracter-no-permitido" in entorno["informe"].read_text(encoding="utf-8")


def test_correcciones_invalidas_no_escriben_el_json(entorno):
    pdf = crear_pdf(entorno["tmp"] / "prueba.pdf", [CONTENIDO])
    entorno["correcciones"].write_text("{no es json", encoding="utf-8")
    assert ejecutar(entorno, pdf) == 2
    assert not entorno["salida"].exists()


def test_las_pruebas_no_tocan_los_archivos_reales_del_usuario(entorno):
    """Regresión: una prueba llegó a borrar fuentes/correcciones_sugeridas.json por no redirigir su salida."""
    from importar_biblia.cli import RAIZ

    real = RAIZ / "fuentes" / "correcciones_sugeridas.json"
    antes = real.read_bytes() if real.exists() else None
    ejecutar(entorno, crear_pdf(entorno["tmp"] / "prueba.pdf", [CONTENIDO]))
    assert (real.read_bytes() if real.exists() else None) == antes
