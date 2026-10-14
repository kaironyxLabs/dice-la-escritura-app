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
