"""Prueba del hook pre-merge-commit en un repositorio temporal."""

import shutil
import subprocess
from pathlib import Path

HOOK = Path(__file__).parent / "pre-merge-commit"


def git(cwd, *args, check=True):
    return subprocess.run(
        ["git", "-c", "user.name=t", "-c", "user.email=t@t", *args],
        cwd=cwd, capture_output=True, text=True, check=check,
    )


def preparar(tmp_path):
    repo = tmp_path / "repo"
    (repo / "tools" / "hooks").mkdir(parents=True)
    git(repo, "init", "-q", "-b", "main")
    shutil.copy(HOOK, repo / "tools" / "hooks" / "pre-merge-commit")
    git(repo, "config", "core.hooksPath", "tools/hooks")
    git(repo, "config", "merge.ff", "false")
    (repo / "README.md").write_text("inicio\n")
    (repo / "a.txt").write_text("a\n")
    git(repo, "add", "-A")
    git(repo, "commit", "-q", "-m", "base")
    return repo


def rama(repo, nombre, archivos):
    git(repo, "checkout", "-q", "-b", nombre)
    for ruta, texto in archivos.items():
        (repo / ruta).write_text(texto)
    git(repo, "add", "-A")
    git(repo, "commit", "-q", "-m", nombre)
    git(repo, "checkout", "-q", "main")


def test_rechaza_un_merge_que_no_toca_el_readme(tmp_path):
    repo = preparar(tmp_path)
    rama(repo, "sin-readme", {"b.txt": "b\n"})
    r = git(repo, "merge", "sin-readme", check=False)
    assert r.returncode != 0
    assert "README" in r.stderr


def test_acepta_un_merge_que_actualiza_el_readme(tmp_path):
    repo = preparar(tmp_path)
    rama(repo, "con-readme", {"b.txt": "b\n", "README.md": "inicio\nnuevo\n"})
    assert git(repo, "merge", "con-readme", check=False).returncode == 0


def test_no_bloquea_si_la_rama_no_cambia_nada(tmp_path):
    repo = preparar(tmp_path)
    git(repo, "branch", "igual")
    assert git(repo, "merge", "igual", check=False).returncode == 0


def test_se_puede_saltar_a_conciencia(tmp_path):
    repo = preparar(tmp_path)
    rama(repo, "sin-readme", {"b.txt": "b\n"})
    assert git(repo, "merge", "--no-verify", "sin-readme", check=False).returncode == 0
