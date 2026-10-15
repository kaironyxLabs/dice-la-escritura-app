import re
from pathlib import Path

from importar_biblia.libros import LIBROS_RVR1960, TOTAL_CAPITULOS, TOTAL_LIBROS, normalizar_nombre

KOTLIN = Path(__file__).resolve().parents[3] / "app/src/main/java/com/kaironyx/diceescritura/domain/model/TablaLibros.kt"


def test_hay_66_libros_y_1189_capitulos():
    assert TOTAL_LIBROS == 66
    assert TOTAL_CAPITULOS == 1189


def test_ids_unicos_de_tres_caracteres():
    ids = [libro.id for libro in LIBROS_RVR1960]
    assert len(set(ids)) == 66
    assert all(len(i) == 3 for i in ids)


def test_coincide_con_la_tabla_de_la_app():
    patron = re.compile(r'libro\("([A-Z0-9]{3})", "([^"]+)", "[^"]+", \w+, \w+, (\d+)\)')
    de_kotlin = [(m.group(1), m.group(2), int(m.group(3))) for m in patron.finditer(KOTLIN.read_text(encoding="utf-8"))]
    de_python = [(libro.id, libro.nombre, libro.capitulos) for libro in LIBROS_RVR1960]
    assert de_kotlin == de_python


def test_normalizar_nombre():
    assert normalizar_nombre("ECLESIASTÉS") == normalizar_nombre("Eclesiastés")
    assert normalizar_nombre("1 Cr.") == "1 cr"
