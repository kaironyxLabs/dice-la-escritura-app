import json

import pytest

from importar_biblia import correcciones as mod
from importar_biblia.modelo import Capitulo, Libro, Resultado

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
