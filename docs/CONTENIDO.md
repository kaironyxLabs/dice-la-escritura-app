# Contenido: cómo agregar y revisar preguntas

> Esqueleto de H0. Se completa en H1 y H7.

Las preguntas viven en `app/src/main/assets/content/` (`core-*.json` y `confesion1689-*.json`). El esquema y las reglas de contenido están en la sección 5 del prompt inicial y en `CLAUDE.md`.

Reglas esenciales:
- Ids estables: el progreso del usuario se enlaza por id. Nunca reutilizar ni renumerar.
- Citar referencias y parafrasear; no transcribir versículos.
- Todo contenido nuevo con `"reviewed": false`. Solo el usuario lo cambia a `true`.
- Si una referencia o hecho es dudoso, descartar la pregunta.
