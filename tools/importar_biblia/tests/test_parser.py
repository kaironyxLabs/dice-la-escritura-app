import pytest

from importar_biblia.extraer import Linea
from importar_biblia.modelo import DUPLICADO, ENCABEZADO, INFO, NOMBRE, SALTO, SIN_NUMERAR
from importar_biblia.parser import ErrorEstructura, analizar, clasificar_bloque, construir_bloques

from .ayudas import TABLA, lineas

FALSO = "Texto de prueba, no es RVR1960."


def analizar_basico(*textos: str):
    return analizar(lineas(*textos), TABLA)


def capitulo(resultado, libro: str, numero: int):
    return next(l for l in resultado.libros if l.id == libro).capitulos[numero - 1]


def tipos(resultado) -> set[str]:
    return {a.tipo for a in resultado.anomalias}


BASICO = [
    "LIBRO UNO", "", "Libro Uno 1", "", f"1 {FALSO} 2 Otro {FALSO}", "",
    "Libro Uno 2", "", f"1 {FALSO}", "",
    "LIBRO DOS", "", "Libro Dos 1", "", f"1 {FALSO}", "", f"2 {FALSO}",
]


def test_estructura_basica():
    r = analizar_basico(*BASICO)
    assert [l.id for l in r.libros] == ["UNO", "DOS"]
    assert [len(c.versiculos) for c in r.libros[0].capitulos] == [2, 1]
    assert capitulo(r, "UNO", 1).versiculos == [FALSO, f"Otro {FALSO}"]
    assert capitulo(r, "DOS", 1).versiculos == [FALSO, FALSO]
    assert r.encabezados_encontrados == 3
    assert not [a for a in r.anomalias if a.severidad != INFO]


def test_los_encabezados_de_libro_se_ignoran_y_se_registran():
    r = analizar_basico(*BASICO)
    assert any("LIBRO UNO" in texto for texto in r.ignorados)
    assert any("LIBRO DOS" in texto for texto in r.ignorados)
    assert capitulo(r, "UNO", 1).encabezado is None


def test_la_cantidad_de_encabezados_debe_coincidir_con_la_tabla():
    with pytest.raises(ErrorEstructura):
        analizar_basico("Libro Uno 1", "", f"1 {FALSO}")


def test_el_libro_se_asigna_por_orden_y_no_por_el_nombre_escrito():
    textos = [t.replace("Libro Uno 1", "LIBRU UNO 1") for t in BASICO]
    r = analizar_basico(*textos)
    assert capitulo(r, "UNO", 1).versiculos
    nombre = [a for a in r.anomalias if a.tipo == NOMBRE]
    assert nombre and nombre[0].severidad == INFO  # errata: informativa


def test_encabezado_con_numero_fuera_de_secuencia_es_un_error():
    textos = [t.replace("Libro Uno 2", "Libro Uno 5") for t in BASICO]
    r = analizar_basico(*textos)
    assert ENCABEZADO in tipos(r)


def test_titulos_paralelos_y_sobrescrito_se_guardan_aparte():
    r = analizar_basico(
        "LIBRO UNO", "", "Un título de sección", "", "(1 R. 3.3-15)", "", "Salmo de prueba, no es RVR1960.", "",
        "Libro Uno 1", "", f"1 {FALSO}", "", "Otro título", "", f"2 Segundo {FALSO}", "",
        "Libro Uno 2", "", f"1 {FALSO}", "", "Libro Dos 1", "", f"1 {FALSO}", f"2 {FALSO}",
    )
    c = capitulo(r, "UNO", 1)
    assert c.encabezado == "Salmo de prueba, no es RVR1960."
    assert [(s.antes_del_versiculo, s.titulo) for s in c.secciones] == [(1, "Un título de sección"), (2, "Otro título")]
    assert [(p.antes_del_versiculo, p.texto) for p in c.paralelos] == [(1, "(1 R. 3.3-15)")]
    assert c.versiculos == [FALSO, f"Segundo {FALSO}"]  # nada de lo anterior se mezcla con el versículo


def test_un_salto_de_numeracion_deja_un_hueco_y_se_registra():
    r = analizar_basico(
        "Libro Uno 1", "", f"1 {FALSO} 3 Tercero {FALSO}", "", "Libro Uno 2", "", f"1 {FALSO}", "",
        "Libro Dos 1", "", f"1 {FALSO}",
    )
    assert capitulo(r, "UNO", 1).versiculos == [FALSO, None, f"Tercero {FALSO}"]
    assert SALTO in tipos(r)


def test_un_numero_dentro_del_texto_no_parte_el_versiculo():
    r = analizar_basico(
        "Libro Uno 1", "", f"1 Vivió 130 años y engendró hijos. 2 Otro {FALSO}", "", "Libro Uno 2", "", f"1 {FALSO}", "",
        "Libro Dos 1", "", f"1 {FALSO}",
    )
    assert capitulo(r, "UNO", 1).versiculos == ["Vivió 130 años y engendró hijos.", f"Otro {FALSO}"]


def test_marcador_debil_tras_una_palabra_y_antes_de_una_minuscula():
    r = analizar_basico(
        "Libro Uno 1", "", f"1 Fue llevado al desierto 2 por cuarenta días, y era tentado. 3 Entonces {FALSO}", "",
        "Libro Uno 2", "", f"1 {FALSO}", "", "Libro Dos 1", "", f"1 {FALSO}",
    )
    versiculos = capitulo(r, "UNO", 1).versiculos
    assert versiculos[0] == "Fue llevado al desierto"
    assert versiculos[1] == "por cuarenta días, y era tentado."
    assert any(a.tipo == SIN_NUMERAR and a.severidad == INFO for a in r.anomalias)


def test_un_numero_igual_al_esperado_que_tambien_aparece_como_marcador_claro_despues_es_texto():
    r = analizar_basico(
        "Libro Uno 1", "", f"1 Pasaron 2 días y nada más. 2 Entonces {FALSO}", "", "Libro Uno 2", "", f"1 {FALSO}", "",
        "Libro Dos 1", "", f"1 {FALSO}",
    )
    assert capitulo(r, "UNO", 1).versiculos == ["Pasaron 2 días y nada más.", f"Entonces {FALSO}"]


def test_marcador_antes_de_un_parentesis_sin_puntuacion_previa():
    r = analizar_basico(
        "Libro Uno 1", "", f"1 Termina sin punto 2 (empieza con paréntesis, {FALSO})", "", "Libro Uno 2", "",
        f"1 {FALSO}", "", "Libro Dos 1", "", f"1 {FALSO}",
    )
    assert len(capitulo(r, "UNO", 1).versiculos) == 2


def test_poesia_con_marcadores_en_lineas_internas():
    r = analizar_basico(
        "Libro Uno 1", "", f"1 Y dijo a sus mujeres:", "", "Primera línea de poema de prueba;",
        "Segunda línea de poema de prueba.", f"2 Tercera línea de poema de prueba,", "Cuarta línea de poema.", "",
        "Libro Uno 2", "", f"1 {FALSO}", "", "Libro Dos 1", "", f"1 {FALSO}",
    )
    versiculos = capitulo(r, "UNO", 1).versiculos
    assert len(versiculos) == 2
    assert versiculos[0].startswith("Y dijo a sus mujeres: Primera línea")
    assert versiculos[1].startswith("Tercera línea")


def test_un_parrafo_que_continua_el_versiculo_tras_un_salto_de_pagina():
    entrada = [
        Linea("Libro Uno 1"), Linea(""), Linea(f"1 {FALSO} 2 Empieza el segundo."), Linea(""),
        Linea("y termina aquí tras la página, 3 y sigue el tercero."),
        Linea(""), Linea("Libro Uno 2"), Linea(""), Linea(f"1 {FALSO}"), Linea(""),
        Linea("Libro Dos 1"), Linea(""), Linea(f"1 {FALSO}"),
    ]
    r = analizar(entrada, TABLA)
    versiculos = capitulo(r, "UNO", 1).versiculos
    assert len(versiculos) == 3
    assert versiculos[1].endswith("y termina aquí tras la página,")


def test_la_numeracion_repetida_se_registra():
    r = analizar_basico(
        "Libro Uno 1", "", f"1 {FALSO}", "", f"1 {FALSO} repetido", "", "Libro Uno 2", "", f"1 {FALSO}", "",
        "Libro Dos 1", "", f"1 {FALSO}",
    )
    assert DUPLICADO in tipos(r)


def test_clasificacion_de_bloques():
    assert clasificar_bloque(["1 Texto de prueba."]) == "versiculos"
    assert clasificar_bloque(["(1 R. 3.3-15)"]) == "paralelo"
    assert clasificar_bloque(["LIBRO PRIMERO DE MOISÉS"]) == "mayusculas"
    assert clasificar_bloque(["Un título sin punto final"]) == "titulo"
    assert clasificar_bloque(["Una frase que termina en punto."]) == "texto"
    assert clasificar_bloque(["Verso suelto,", "2 otro verso"]) == "continuacion"


def test_un_salto_de_pagina_solo_separa_bloques_si_la_linea_parece_titulo():
    entrada = [
        Linea("1 Una oración completa."),
        Linea("Un título al inicio de página", inicio_de_pagina=True),
        Linea("2 Sigue el texto."),
    ]
    bloques = construir_bloques(entrada, [])
    assert bloques[0] == ["1 Una oración completa."]
    assert bloques[1][0] == "Un título al inicio de página"

    continuacion = [Linea("1 Una oración que sigue"), Linea("en la otra página.", inicio_de_pagina=True)]
    assert len(construir_bloques(continuacion, [])) == 1


def test_titulo_y_paralelo_en_lineas_seguidas_no_se_pegan_al_versiculo_anterior():
    r = analizar_basico(
        "Libro Uno 1", "", f"1 {FALSO}", "", "Titulo de prueba", "(Ex. 1.1-2)", "", f"2 Segundo {FALSO}", "",
        "Libro Uno 2", "", f"1 {FALSO}", "", "Libro Dos 1", "", f"1 {FALSO}",
    )
    c = capitulo(r, "UNO", 1)
    assert c.versiculos == [FALSO, f"Segundo {FALSO}"]  # nada pegado al versículo 1
    assert [(s.antes_del_versiculo, s.titulo) for s in c.secciones] == [(2, "Titulo de prueba")]
    assert [(p.antes_del_versiculo, p.texto) for p in c.paralelos] == [(2, "(Ex. 1.1-2)")]


def test_titulo_y_paralelo_antes_de_un_capitulo_van_al_inicio_de_ese_capitulo():
    r = analizar_basico(
        "Libro Uno 1", "", f"1 {FALSO}", "", "Titulo del capitulo dos", "(Ex. 9.9)", "", "Libro Uno 2", "", f"1 {FALSO}", "",
        "Libro Dos 1", "", f"1 {FALSO}",
    )
    c2 = capitulo(r, "UNO", 2)
    assert [(s.antes_del_versiculo, s.titulo) for s in c2.secciones] == [(1, "Titulo del capitulo dos")]
    assert [(p.antes_del_versiculo, p.texto) for p in c2.paralelos] == [(1, "(Ex. 9.9)")]
    assert capitulo(r, "UNO", 1).versiculos == [FALSO]


def test_titulo_corto_con_punto_entre_versiculos_es_un_titulo():
    r = analizar_basico(
        "Libro Uno 1", "", f"1 {FALSO}", "", "Descendientes de Aaron.", "", f"2 Segundo {FALSO}", "",
        "Libro Uno 2", "", f"1 {FALSO}", "", "Libro Dos 1", "", f"1 {FALSO}",
    )
    c = capitulo(r, "UNO", 1)
    assert c.versiculos == [FALSO, f"Segundo {FALSO}"]
    assert [(s.antes_del_versiculo, s.titulo) for s in c.secciones] == [(2, "Descendientes de Aaron.")]


def test_una_cita_corta_tras_dos_puntos_sigue_siendo_parte_del_versiculo():
    r = analizar_basico(
        "Libro Uno 1", "", f"1 Dijo el profeta, y:", "", "Tome otro su oficio.", "", f"2 Segundo {FALSO}", "",
        "Libro Uno 2", "", f"1 {FALSO}", "", "Libro Dos 1", "", f"1 {FALSO}",
    )
    c = capitulo(r, "UNO", 1)
    assert c.versiculos[0] == "Dijo el profeta, y: Tome otro su oficio."
    assert c.secciones == []


def test_una_frase_corta_que_empieza_con_y_sigue_siendo_parte_del_versiculo():
    r = analizar_basico(
        "Libro Uno 1", "", f"1 {FALSO}", "", "Y él les respondió.", "", f"2 Segundo {FALSO}", "",
        "Libro Uno 2", "", f"1 {FALSO}", "", "Libro Dos 1", "", f"1 {FALSO}",
    )
    assert capitulo(r, "UNO", 1).versiculos[0] == f"{FALSO} Y él les respondió."
