# Contenido: cómo agregar y revisar preguntas

Las preguntas viven en `app/src/main/assets/content/`:

- `core-<categoria>.json` y `core-<categoria>-NN.json`: módulo base. Una categoría puede repartirse en varios archivos; los ids siguen la numeración de la categoría sin importar el archivo.
- `confesion1689-*.json`: módulo de la Confesión Bautista de 1689, detrás de una bandera.
- `confesion-capitulos.json`, `levels.json` y `memorize.json` no son bancos de preguntas: tabla de los 32 capítulos de la Confesión, niveles y pasajes a memorizar.

Cada archivo tiene `contentVersion` (entero; súbelo al cambiar el contenido) y la lista `questions`. La app lee con un lector estricto: un campo desconocido o mal escrito es un error, no se ignora.

## Esquema

| Campo | Obligatorio | Notas |
|---|---|---|
| `id` | sí | `modulo-categoria-0001`. Estable para siempre: el progreso se enlaza por id. Nunca reutilizar ni renumerar. |
| `module` | sí | `core` o `confesion1689`. |
| `category` | sí | `libros`, `historia`, `personajes`, `lugares`, `ensenanzas`, `cartas`, `donde_escrito` (core); `confesion` (1689). |
| `type` | sí | `multiple_choice`, `true_false` u `ordering`. |
| `difficulty` | sí | 1 a 5. |
| `question` | sí | Enunciado. |
| `options` + `answerIndex` | solo `multiple_choice` | 3 a 6 opciones distintas; `answerIndex` empieza en 0. |
| `answer` | solo `true_false` | Booleano. |
| `items` | solo `ordering` | 3 a 8 elementos distintos, **en el orden correcto** (la app los baraja). |
| `references` | sí | Citas como `"Romanos 3:23"`, `"Juan 3:16-18"`, `"Génesis 1:1-2:3"`, `"Salmos 23"`. |
| `explanation` | sí | Breve, parafraseada. |
| `tags` | no | Etiquetas libres. |
| `reviewed` | no (por defecto `false`) | Solo el usuario lo cambia a `true`. |
| `confessionRef` | solo 1689 | Ej. `"cap. 1, párr. 1"`. Prohibido en `core`. |

## Reglas
- Citar referencias y **parafrasear**; no transcribir versículos ni párrafos de traducciones modernas con derechos.
- Las preguntas base deben poder verificarse con un pasaje concreto y evitar posturas denominacionales.
- Todo contenido nuevo con `"reviewed": false`.
- Si una referencia o hecho es dudoso, descartar la pregunta.
- Un libro de un solo capítulo se cita como `"Judas 3"` (versículo 3) o `"Filemón 1:10-12"`.
- La app baraja las opciones al presentarlas: no uses opciones como "todas las anteriores" ni que dependan de su posición.
- Evita que la respuesta correcta sea siempre la más larga.
- En el módulo 1689, `confessionRef` indica dónde enseña eso la Confesión (`"cap. 11, párr. 1"`; si no estás seguro del párrafo, solo `"cap. 11"`; `"cap. 1-5"` para varias). Las preguntas son originales: no transcriben párrafos de ninguna traducción.

## Validación
`./gradlew testDebugUnitTest` ejecuta las pruebas de contenido sobre los archivos reales:

- `ContenidoAssetsTest` y `ValidadorContenidoTest`: ids únicos y bien formados, tipos y campos obligatorios, `answerIndex` válido, referencias que se interpretan contra la tabla de 66 libros con capítulos existentes, explicación no vacía y formato de `confessionRef`.
- `ContenidoCompletoTest`: mínimos (300 preguntas base, 96 de la Confesión con al menos 3 por capítulo), reparto por categoría y dificultad, preguntas de los tres tipos, suficientes preguntas de dificultad 1 para el nivel inicial, enunciados no repetidos, ninguna pregunta marcada como revisada por error y que **ningún versículo citado supere los versículos de su capítulo** (según `docs/versiculos_por_capitulo.json`).
- `PasajesTest`: los ~108 pasajes de `memorize.json`.

## Revisión
El contenido nuevo siempre se escribe con `"reviewed": false`; solo tú lo revisas.

1. Genera el informe: `python tools/contenido/informe_revision.py` (usa cualquier Python 3; solo necesita la biblioteca estándar). Crea `docs/REVISION_PREGUNTAS.md` con las preguntas por módulo y categoría, cada una con su casilla, opciones, respuesta correcta, referencias y explicación. Con `--solo-pendientes` omite las ya revisadas.
2. Revisa cada pregunta: que la respuesta sea la correcta, que las referencias la respalden, que el módulo base no adopte posturas denominacionales y que la explicación parafrasee. Marca la casilla (`- [x]`) solo de las que apruebas.
3. Aplica tus marcas: `python tools/contenido/aplicar_revision.py` (con `--simular` para ver el resultado sin escribir). Cambia `"reviewed"` a `true` solo en las preguntas marcadas, sin reformatear el resto del JSON, y nunca devuelve una pregunta a `false`.
4. Si una pregunta es dudosa, corrígela o bórrala en el JSON; no se marca.
5. Con "Mostrar solo preguntas revisadas" activado en Ajustes, la app usa únicamente las revisadas.

Pruebas de los scripts: `python -m pytest tools/contenido` (con el entorno virtual de `tools/importar_biblia`).
