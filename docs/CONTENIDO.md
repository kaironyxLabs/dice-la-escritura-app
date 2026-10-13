# Contenido: cómo agregar y revisar preguntas

Las preguntas viven en `app/src/main/assets/content/`:

- `core-<categoria>.json`: módulo base (una categoría por archivo).
- `confesion1689-*.json`: módulo de la Confesión Bautista de 1689 (H5/H7), detrás de una bandera.

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

## Validación
`./gradlew testDebugUnitTest` ejecuta `ContenidoAssetsTest`, que valida los archivos reales: ids únicos y bien formados, tipos y campos obligatorios, `answerIndex` válido, referencias interpretables contra la tabla de 66 libros con capítulos existentes, y explicación no vacía. La validación de números de versículo por capítulo llegará con el texto importado (H4).

## Revisión
El informe de revisión en Markdown (H7) lista las preguntas por categoría con casillas para marcar. El usuario revisa y cambia `reviewed` a `true`; la opción "mostrar solo preguntas revisadas" en Ajustes permite jugar solo con las revisadas.
