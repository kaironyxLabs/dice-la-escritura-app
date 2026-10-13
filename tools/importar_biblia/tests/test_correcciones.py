import json

import pytest

from importar_biblia import correcciones as mod
from importar_biblia.modelo import Capitulo, Libro, Paralelo, Resultado, Seccion

from .ayudas import TABLA

FALSO = "Texto de prueba, no es RVR1960."


def resultado() -> Resultado:
    uno = Libro("UNO", [Capitulo(1, versiculos=[FALSO, None, f"Tercero {FALSO}"]), Capitulo(2, versiculos=[FALSO])])
    dos = Libro("DOS", [Capitulo(1, versiculos=[FALSO, f"Segundo {FALSO}", f"Extra {FALSO}"])])
    return Resultado([uno, dos], [], 3)


def cargar(tmp_path, datos) -> mod.Correcciones:
    ruta = tmp_path / "correcciones.json"
    ruta.write_text(json.dumps(datos), encoding="utf-8")
    return mod.cargar(ruta, TABLA)


def test_sin_archivo_no_hay_correcciones(tmp_path):
    c = mod.cargar(tmp_path / "no_existe.json", TABLA)
    assert c.correcciones == [] and c.pendientes == {} and c.aceptadas == []
    assert mod.cargar(None, TABLA).correcciones == []


def test_texto_inserta_en_un_hueco(tmp_path):
    r = resultado()
    c = cargar(tmp_path, {"correcciones": [{"libro": "UNO", "capitulo": 1, "versiculo": 2, "texto": "  Segundo   falso. "}]})
    mod.aplicar(r, c)
    assert r.libros[0].capitulos[0].versiculos[1] == "Segundo falso."
    assert r.correcciones_aplicadas == ["UNO 1:2: texto reemplazado o insertado"]


def test_texto_puede_agregar_un_versiculo_al_final(tmp_path):
    r = resultado()
    c = cargar(tmp_path, {"correcciones": [{"libro": "UNO", "capitulo": 2, "versiculo": 2, "texto": "Nuevo."}]})
    mod.aplicar(r, c)
    assert r.libros[0].capitulos[1].versiculos == [FALSO, "Nuevo."]


def test_reemplazos_exige_una_sola_aparicion(tmp_path):
    r = resultado()
    ok = cargar(tmp_path, {"correcciones": [
        {"libro": "UNO", "capitulo": 1, "versiculo": 3, "reemplazos": [{"buscar": "Tercero", "por": "Tercer"}]},
    ]})
    mod.aplicar(r, ok)
    assert r.libros[0].capitulos[0].versiculos[2].startswith("Tercer Texto")

    repetido = cargar(tmp_path, {"correcciones": [
        {"libro": "DOS", "capitulo": 1, "versiculo": 1, "reemplazos": [{"buscar": "e", "por": "i"}]},
    ]})
    with pytest.raises(mod.ErrorCorreccion, match="aparece"):
        mod.aplicar(resultado(), repetido)

    ausente = cargar(tmp_path, {"correcciones": [
        {"libro": "DOS", "capitulo": 1, "versiculo": 1, "reemplazos": [{"buscar": "inexistente", "por": "x"}]},
    ]})
    with pytest.raises(mod.ErrorCorreccion):
        mod.aplicar(resultado(), ausente)


def test_renumerar_mueve_el_texto_a_un_numero_vacio(tmp_path):
    r = resultado()
    c = cargar(tmp_path, {"correcciones": [{"libro": "UNO", "capitulo": 1, "versiculo": 3, "renumerar_a": 2}]})
    mod.aplicar(r, c)
    # El 3 queda vacío al final y se recorta; el texto ahora es el versículo 2.
    assert r.libros[0].capitulos[0].versiculos == [FALSO, f"Tercero {FALSO}"]


def test_renumerar_a_un_numero_ocupado_falla(tmp_path):
    c = cargar(tmp_path, {"correcciones": [{"libro": "DOS", "capitulo": 1, "versiculo": 3, "renumerar_a": 2}]})
    with pytest.raises(mod.ErrorCorreccion, match="ya tiene texto"):
        mod.aplicar(resultado(), c)


def test_eliminar_el_ultimo_versiculo_recorta_la_lista(tmp_path):
    r = resultado()
    c = cargar(tmp_path, {"correcciones": [{"libro": "DOS", "capitulo": 1, "versiculo": 3, "eliminar": True}]})
    mod.aplicar(r, c)
    assert len(r.libros[1].capitulos[0].versiculos) == 2


def test_pendientes_dejan_el_versiculo_sin_texto_y_marcado(tmp_path):
    r = resultado()
    c = cargar(tmp_path, {"pendientes": [{"libro": "UNO", "capitulo": 1, "versiculo": 2, "motivo": "número dañado"}]})
    mod.aplicar(r, c)
    capitulo = r.libros[0].capitulos[0]
    assert capitulo.pendientes == [2]
    assert capitulo.versiculos[1] is None


def test_un_pendiente_al_final_conserva_su_posicion(tmp_path):
    r = resultado()
    c = cargar(tmp_path, {"pendientes": [{"libro": "UNO", "capitulo": 2, "versiculo": 3}]})
    mod.aplicar(r, c)
    assert len(r.libros[0].capitulos[1].versiculos) == 3


@pytest.mark.parametrize("entrada, mensaje", [
    ({"correcciones": [{"libro": "XXX", "capitulo": 1, "versiculo": 1, "texto": "a"}]}, "libro desconocido"),
    ({"correcciones": [{"libro": "UNO", "capitulo": 9, "versiculo": 1, "texto": "a"}]}, "capítulo inválido"),
    ({"correcciones": [{"libro": "UNO", "capitulo": 1, "versiculo": 0, "texto": "a"}]}, "versículo inválido"),
    ({"correcciones": [{"libro": "UNO", "capitulo": 1, "versiculo": 1}]}, "exactamente una operación"),
    ({"correcciones": [{"libro": "UNO", "capitulo": 1, "versiculo": 1, "texto": "a", "eliminar": True}]},
     "exactamente una operación"),
    ({"correcciones": [{"libro": "UNO", "capitulo": 1, "versiculo": 1, "texto": " "}]}, "texto no vacío"),
    ({"correcciones": [{"libro": "UNO", "capitulo": 1, "versiculo": 1, "texto": "a", "otro": 1}]}, "claves desconocidas"),
    ({"raro": []}, "claves desconocidas"),
    ({"aceptadas": [{"libro": "UNO", "capitulo": 1}]}, "falta 'tipo'"),
])
def test_validacion_del_archivo(tmp_path, entrada, mensaje):
    with pytest.raises(mod.ErrorCorreccion, match=mensaje):
        cargar(tmp_path, entrada)


def test_json_invalido(tmp_path):
    ruta = tmp_path / "c.json"
    ruta.write_text("{no es json", encoding="utf-8")
    with pytest.raises(mod.ErrorCorreccion, match="JSON inválido"):
        mod.cargar(ruta, TABLA)


def test_aceptadas(tmp_path):
    c = cargar(tmp_path, {"aceptadas": [
        {"libro": "UNO", "capitulo": 1, "tipo": "conteo", "motivo": "numeración de la RVR1960"},
        {"libro": "UNO", "capitulo": 2, "tipo": "patron-sospechoso", "versiculo": 1},
    ]})
    assert c.esta_aceptada("conteo", "UNO", 1, None)
    assert not c.esta_aceptada("conteo", "UNO", 2, None)
    assert c.esta_aceptada("patron-sospechoso", "UNO", 2, 1)
    assert not c.esta_aceptada("patron-sospechoso", "UNO", 2, 2)


def resultado_con_notas() -> Resultado:
    r = resultado()
    capitulo = r.libros[0].capitulos[0]
    capitulo.secciones = [Seccion(1, "Titulo de prueba")]
    capitulo.paralelos = [Paralelo(1, "(Ex. 1.1-2_"), Paralelo(2, "(Ex. 3.4)")]
    return r


def test_nota_corrige_un_paralelo(tmp_path):
    r = resultado_con_notas()
    c = cargar(tmp_path, {"notas": [{"libro": "UNO", "capitulo": 1, "tipo": "paralelo", "buscar": "1.1-2_", "por": "1.1-2)"}]})
    mod.aplicar(r, c)
    assert [p.texto for p in r.libros[0].capitulos[0].paralelos] == ["(Ex. 1.1-2)", "(Ex. 3.4)"]
    assert r.correcciones_aplicadas == ["UNO 1: paralelo corregido"]


def test_nota_corrige_una_seccion(tmp_path):
    r = resultado_con_notas()
    c = cargar(tmp_path, {"notas": [{"libro": "UNO", "capitulo": 1, "tipo": "seccion", "buscar": "prueba", "por": "ejemplo"}]})
    mod.aplicar(r, c)
    assert r.libros[0].capitulos[0].secciones[0].titulo == "Titulo de ejemplo"


def test_nota_exige_una_sola_aparicion(tmp_path):
    for buscar in ("(Ex.", "inexistente"):
        c = cargar(tmp_path, {"notas": [{"libro": "UNO", "capitulo": 1, "tipo": "paralelo", "buscar": buscar, "por": "x"}]})
        with pytest.raises(mod.ErrorCorreccion, match="aparece"):
            mod.aplicar(resultado_con_notas(), c)


def test_validacion_de_notas(tmp_path):
    with pytest.raises(mod.ErrorCorreccion, match="tipo"):
        cargar(tmp_path, {"notas": [{"libro": "UNO", "capitulo": 1, "tipo": "otro", "buscar": "a", "por": "b"}]})
    with pytest.raises(mod.ErrorCorreccion, match="buscar"):
        cargar(tmp_path, {"notas": [{"libro": "UNO", "capitulo": 1, "tipo": "paralelo", "buscar": "", "por": "b"}]})


def test_una_referencia_mal_cerrada_clasificada_como_seccion_pasa_a_paralelo(tmp_path):
    r = resultado()
    capitulo = r.libros[0].capitulos[0]
    capitulo.secciones = [Seccion(2, "Titulo de prueba"), Seccion(2, "(Ex. 1.1-2_")]
    c = cargar(tmp_path, {"notas": [{"libro": "UNO", "capitulo": 1, "tipo": "seccion", "buscar": "1.1-2_", "por": "1.1-2)"}]})
    mod.aplicar(r, c)
    assert [s.titulo for s in capitulo.secciones] == ["Titulo de prueba"]
    assert [(p.antes_del_versiculo, p.texto) for p in capitulo.paralelos] == [(2, "(Ex. 1.1-2)")]


def resultado_con_palabras() -> Resultado:
    uno = Libro("UNO", [Capitulo(1, versiculos=["Ya estáen casa y estáen paz.", "Nada estáenorme aquí.", None])])
    dos = Libro("DOS", [Capitulo(1, versiculos=["Estáen el Señor. Estáen."])])
    return Resultado([uno, dos], [], 2)


def test_palabras_reemplaza_la_palabra_completa_en_toda_la_biblia(tmp_path):
    r = resultado_con_palabras()
    c = cargar(tmp_path, {"palabras": [{"buscar": "estáen", "por": "está en"}]})
    mod.aplicar(r, c)
    assert r.libros[0].capitulos[0].versiculos == ["Ya está en casa y está en paz.", "Nada estáenorme aquí.", None]
    # Distingue mayúsculas: "Estáen" es otra palabra para este reemplazo.
    assert r.libros[1].capitulos[0].versiculos == ["Estáen el Señor. Estáen."]
    assert r.correcciones_aplicadas == ["palabra 'estáen' -> 'está en': 2 aparición(es)"]


def test_palabras_con_mayuscula_inicial_se_aprueban_aparte(tmp_path):
    r = resultado_con_palabras()
    c = cargar(tmp_path, {"palabras": [{"buscar": "Estáen", "por": "Está en"}]})
    mod.aplicar(r, c)
    assert r.libros[1].capitulos[0].versiculos == ["Está en el Señor. Está en."]


def test_palabras_sin_apariciones_no_falla(tmp_path):
    c = cargar(tmp_path, {"palabras": [{"buscar": "inexistente", "por": "in existente"}]})
    r = resultado_con_palabras()
    mod.aplicar(r, c)
    assert r.correcciones_aplicadas == ["palabra 'inexistente' -> 'in existente': 0 aparición(es)"]


def test_palabras_valida_el_formato(tmp_path):
    with pytest.raises(mod.ErrorCorreccion, match="palabra o frase"):
        cargar(tmp_path, {"palabras": [{"buscar": "dos, palabras", "por": "x"}]})
    with pytest.raises(mod.ErrorCorreccion, match="palabra o frase"):
        cargar(tmp_path, {"palabras": [{"buscar": "doble  espacio", "por": "x"}]})
    with pytest.raises(mod.ErrorCorreccion, match="no vacíos"):
        cargar(tmp_path, {"palabras": [{"buscar": "a", "por": " "}]})
    with pytest.raises(mod.ErrorCorreccion, match="repetida"):
        cargar(tmp_path, {"palabras": [{"buscar": "ab", "por": "a b"}, {"buscar": "ab", "por": "a b"}]})
    with pytest.raises(mod.ErrorCorreccion, match="claves desconocidas"):
        cargar(tmp_path, {"palabras": [{"buscar": "ab", "por": "a b", "otra": 1}]})


def test_palabras_acepta_frases_como_la_tilde_de_el(tmp_path):
    uno = Libro("UNO", [Capitulo(1, versiculos=["El les dijo: El mismo día. El lesionado. Y El les habló.", None])])
    r = Resultado([uno], [], 1)
    c = cargar(tmp_path, {"palabras": [{"buscar": "El les", "por": "Él les"}]})
    mod.aplicar(r, c)
    # Solo la frase completa y con la palabra entera: "El lesionado" y "El mismo" no cambian.
    assert r.libros[0].capitulos[0].versiculos[0] == "Él les dijo: El mismo día. El lesionado. Y Él les habló."


def test_normalizacion_une_el_guion_con_espacio(tmp_path):
    uno = Libro("UNO", [Capitulo(1, versiculos=["Fue a Bet- el y a Is- boset; bien - dicho; ¡Ven! - sí."])])
    r = Resultado([uno], [], 1)
    c = cargar(tmp_path, {"normalizaciones": ["guion_con_espacio"]})
    mod.aplicar(r, c)
    # Solo une letra-guion-espacio-letra; un guion suelto con espacios a ambos lados no se toca.
    assert r.libros[0].capitulos[0].versiculos[0] == "Fue a Bet-el y a Is-boset; bien - dicho; ¡Ven! - sí."
    assert r.correcciones_aplicadas == ["normalización 'guion_con_espacio': 2 aparición(es)"]


def test_normalizacion_desconocida_falla(tmp_path):
    with pytest.raises(mod.ErrorCorreccion, match="desconocida"):
        cargar(tmp_path, {"normalizaciones": ["inventada"]})
