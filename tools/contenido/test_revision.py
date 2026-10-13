"""Pruebas de los scripts de revisión. Usan preguntas inventadas, nunca texto de la RVR1960."""

import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))

import aplicar_revision  # noqa: E402
import informe_revision  # noqa: E402

PREGUNTAS = {
    "contentVersion": 1,
    "questions": [
        {
            "id": "core-libros-0001", "module": "core", "category": "libros", "type": "multiple_choice",
            "difficulty": 1, "question": "Pregunta de prueba uno", "options": ["A", "B", "C"], "answerIndex": 1,
            "references": ["Génesis 1:1"], "explanation": "Explicación de prueba uno", "tags": [], "reviewed": False,
        },
        {
            "id": "core-libros-0002", "module": "core", "category": "libros", "type": "true_false",
            "difficulty": 2, "question": "Pregunta de prueba dos", "answer": True,
            "references": ["Éxodo 1:1"], "explanation": "Explicación de prueba dos", "tags": [], "reviewed": True,
        },
        {
            "id": "confesion1689-confesion-0001", "module": "confesion1689", "category": "confesion", "type": "ordering",
            "difficulty": 3, "question": "Pregunta de prueba tres", "items": ["x", "y", "z"],
            "references": ["Romanos 1:1"], "explanation": "Explicación de prueba tres", "tags": [],
            "reviewed": False, "confessionRef": "cap. 1, párr. 1",
        },
    ],
}


def carpeta(tmp_path: Path) -> Path:
    destino = tmp_path / "content"
    destino.mkdir()
    # Un archivo con formato propio (no el de json.dump) para comprobar que no se reformatea.
    (destino / "core-libros.json").write_text(
        '{\n  "contentVersion": 1,\n  "questions": [\n'
        '    {"id": "core-libros-0001", "module": "core", "reviewed": false},\n'
        '    {"id": "core-libros-0002", "module": "core", "reviewed": true},\n'
        '    {"id": "core-libros-0003", "module": "core", "reviewed": false}\n'
        '  ]\n}\n',
        encoding="utf-8",
    )
    (destino / "levels.json").write_text('{"niveles": []}', encoding="utf-8")
    return destino


def test_el_informe_agrupa_por_modulo_y_categoria_con_casillas():
    informe = informe_revision.generar(PREGUNTAS["questions"])
    assert "## Módulo base" in informe and "## Módulo Confesión de 1689" in informe
    assert "### Libros y orden de la Biblia (2)" in informe
    assert "- [ ] `core-libros-0001`" in informe
    assert "- [x] `core-libros-0002`" in informe
    assert "Confesión: cap. 1, párr. 1" in informe
    assert "**B**  (correcta)" in informe
    assert "Revisadas: 1. Pendientes: 2." in informe


def test_solo_pendientes_omite_las_revisadas():
    informe = informe_revision.generar(PREGUNTAS["questions"], solo_pendientes=True)
    assert "core-libros-0002" not in informe
    assert "core-libros-0001" in informe


def test_ids_marcados_solo_toma_las_casillas_con_x():
    texto = "- [ ] `core-libros-0001` (a)\n- [x] `core-libros-0002` (b)\n- [X] `confesion1689-confesion-0001` (c)\n"
    assert aplicar_revision.ids_marcados(texto) == ["core-libros-0002", "confesion1689-confesion-0001"]


def test_marcar_revisada_cambia_solo_la_pregunta_pedida(tmp_path):
    texto = (carpeta(tmp_path) / "core-libros.json").read_text(encoding="utf-8")
    nuevo, cambio = aplicar_revision.marcar_revisada(texto, "core-libros-0001")
    assert cambio
    assert '"id": "core-libros-0001", "module": "core", "reviewed": true' in nuevo
    assert '"id": "core-libros-0003", "module": "core", "reviewed": false' in nuevo
    assert nuevo.count('"reviewed": true') == 2
    # Una que ya estaba revisada o no existe no cambia nada.
    assert aplicar_revision.marcar_revisada(texto, "core-libros-0002") == (texto, False)
    assert aplicar_revision.marcar_revisada(texto, "core-libros-9999") == (texto, False)


def test_aplicar_escribe_sin_reformatear_y_respeta_otros_archivos(tmp_path):
    destino = carpeta(tmp_path)
    resultado = aplicar_revision.aplicar(["core-libros-0003"], destino)
    assert resultado == {"marcadas": 1, "cambiadas": 1, "sin_cambio": 0}
    texto = (destino / "core-libros.json").read_text(encoding="utf-8")
    assert '{"id": "core-libros-0003", "module": "core", "reviewed": true}' in texto
    assert '{"id": "core-libros-0001", "module": "core", "reviewed": false}' in texto
    assert (destino / "levels.json").read_text(encoding="utf-8") == '{"niveles": []}'
    json.loads(texto)  # sigue siendo JSON válido


def test_simular_no_escribe(tmp_path):
    destino = carpeta(tmp_path)
    antes = (destino / "core-libros.json").read_text(encoding="utf-8")
    aplicar_revision.aplicar(["core-libros-0001"], destino, simular=True)
    assert (destino / "core-libros.json").read_text(encoding="utf-8") == antes


def test_las_casillas_desmarcadas_nunca_devuelven_una_pregunta_a_pendiente(tmp_path):
    destino = carpeta(tmp_path)
    aplicar_revision.aplicar([], destino)  # nada marcado
    assert '"id": "core-libros-0002", "module": "core", "reviewed": true' in (destino / "core-libros.json").read_text(encoding="utf-8")


def test_el_informe_real_incluye_todas_las_preguntas():
    preguntas = informe_revision.cargar_preguntas()
    assert len(preguntas) >= 400
    informe = informe_revision.generar(preguntas)
    assert all(f"`{p['id']}`" in informe for p in preguntas)
