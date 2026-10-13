from importar_biblia.extraer import (
    Linea,
    aplanar,
    extraer_lineas,
    lineas_de_pagina,
    quitar_cabecera,
)

from .ayudas import CABECERA, crear_pdf


def test_quita_la_cabecera_como_un_bloque_con_su_linea_en_blanco():
    pagina = [*CABECERA, "1 Texto de prueba, no es RVR1960. "]
    assert quitar_cabecera(pagina) == ["1 Texto de prueba, no es RVR1960. "]


def test_no_toca_paginas_sin_cabecera():
    pagina = ["1 Texto de prueba. ", " "]
    assert quitar_cabecera(pagina) == pagina


def test_el_salto_final_de_pagina_no_es_una_linea_en_blanco():
    assert lineas_de_pagina("a \nb \n") == ["a ", "b "]
    # Una línea en blanco real (un espacio) al final se conserva.
    assert lineas_de_pagina("a \n \n") == ["a ", " "]


def test_un_salto_de_pagina_no_crea_un_salto_de_parrafo():
    paginas = [["1 Texto de prueba que sigue "], [*CABECERA, "en la página siguiente. "]]
    lineas = aplanar(paginas)
    assert [linea.texto for linea in lineas] == ["1 Texto de prueba que sigue", "en la página siguiente."]
    assert lineas[1].inicio_de_pagina is True
    assert lineas[0].inicio_de_pagina is True  # primera línea de su propia página


def test_extrae_de_un_pdf_sintetico(tmp_path):
    ruta = crear_pdf(tmp_path / "prueba.pdf", [["Libro Uno 1 ", " ", "1 Texto de prueba, no es RVR1960. "]])
    lineas = extraer_lineas(ruta)
    textos = [linea.texto for linea in lineas]
    assert textos == ["Libro Uno 1", "", "1 Texto de prueba, no es RVR1960."]
    assert all(isinstance(linea, Linea) for linea in lineas)
