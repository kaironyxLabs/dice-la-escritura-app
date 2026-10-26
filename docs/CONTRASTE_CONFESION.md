# Contraste del módulo 1689 con la fuente

Fuente aportada por el usuario: `lbcos.pdf` (Confesión Bautista de Fe de 1689, 32 capítulos). Está en `fuentes/confesion/` (ignorado por git, igual que el texto bíblico); este documento no transcribe nada de ella.

Se extrajo el texto, se separó en capítulos y párrafos y se comparó cada una de las 97 preguntas con el párrafo que cita `confessionRef`. La numeración coincide con la del PDF (32 capítulos; p. ej. el cap. 26 tiene 15 párrafos). La edición del PDF **no incluye** un párrafo 6 en el cap. 6.

## Resultado
- **Doctrina:** ninguna respuesta marcada como correcta contradice la fuente.
- **Referencias corregidas:** `-0021` (cap. 4, párr. 3), `-0050` y `-0051` (cap. 15, párr. 3), `-0052` (15.5), `-0060` (18.1), `-0073` (23.2), `-0085` (27.2); sin párrafo: `-0032` (8.1).
- **No verificables y reescritas:** `-0027` (citaba un párrafo que esta edición no tiene; ahora trata la corrupción que permanece en el regenerado, 6.5) y `-0067` (ahora trata la obra del Espíritu necesaria además del evangelio, 20.4).
- **Redacción:** se reformularon las preguntas, opciones y explicaciones que seguían la fuente casi palabra por palabra (tramos de 8 o más palabras seguidas).
- **Sesgo de longitud:** antes la correcta era la más larga en 56 de 67. Ahora lo es en unas 15 (sobre 67), y las posiciones de la correcta quedaron repartidas.
- **V/F:** 22 de 29 eran verdaderas; ahora 16 verdaderas y 13 falsas (`-0012/-0018/-0033/-0063/-0077/-0087` pasaron a falsas con enunciado verificable).
- **Explicaciones que no respaldaban la respuesta:** `-0020`, `-0037`, `-0052`; y `-0049` (V/F como inferencia) se ciñó a lo que dice el cap. 14.
- Títulos de `confesion-capitulos.json` y la pregunta de orden `-0013` ahora usan los títulos del PDF.

`contentVersion` subió en los dos archivos del módulo. `reviewed` sigue en `false` en todas.

## Cruce con las notas al pie (26-oct-2026)

Se comparó cada versículo de apoyo (`references`) de las preguntas con las notas al pie del párrafo que cita `confessionRef` en `lbcos.pdf` (cotejo automático por libro y capítulo, sin transcribir nada). De 165 versículos, 140 (85 %) aparecen en las notas de su párrafo. Siete preguntas no se pudieron cotejar porque la extracción no encontró notas para su párrafo (`-0003`, `-0013`, `-0034`, `-0035`, `-0090`, `-0091`, `-0092`).

Los 25 versículos que no figuran en las notas de su párrafo (no es un error de doctrina: son textos de apoyo que eligió la pregunta, no los de la edición) son:

| Pregunta | Párrafo | Versículo |
|---|---|---|
| `-0005` | 8.2 | Colosenses 2:9 |
| `-0011` | 25.1 | Mateo 19:4-6 |
| `-0015` | 1.9 | 2 Pedro 1:20-21 |
| `-0022` | 5.1 | Salmos 103:19 |
| `-0023` | 5.2 | Mateo 10:29-30 |
| `-0028` | 7.1 | Job 35:7-8 |
| `-0029` | 7.2 | Romanos 3:20-21 |
| `-0031` | 8.2 | Mateo 1:20 |
| `-0032` | 8.1 | Hechos 4:12 |
| `-0038` | 10.3 | Hechos 4:12 |
| `-0050` | 15.3 | Zacarías 12:10 |
| `-0055` | 16.5 | Efesios 2:8-9 |
| `-0057` | 17.2 | 2 Timoteo 2:19 |
| `-0058` | 17.3 | Salmos 89:31-32 |
| `-0059` | 18.1 | Job 8:13-14 |
| `-0061` | 18.3 | Isaías 50:10 |
| `-0067` | 20.4 | Juan 3:5-8 y Efesios 2:1-5 |
| `-0072` | 22.3 | 1 Timoteo 2:5 |
| `-0073` | 23.2 | Deuteronomio 10:20 y Mateo 5:34-37 |
| `-0075` | 23.1 | Jeremías 4:2 |
| `-0084` | 27.2 | 1 Pedro 4:10 |
| `-0088` | 28.2 | Mateo 28:19 |
| `-0095` | 31.2 | Job 19:26-27 |

(Los ids van precedidos de `confesion1689-confesion`.) No se cambió ninguna pregunta: estas 97 ya están revisadas por ti. Si prefieres que los versículos de apoyo coincidan siempre con los de la edición, se puede sustituir cada uno por el de la nota al pie, pero es decisión tuya.
