# Revisión previa de las 430 preguntas

Pasada completa hecha por Claude el 14/10/2026 antes de tu revisión. **No marca nada como revisado**: `reviewed` sigue en `false` en todas. Sirve para que llegues a las casillas de `docs/REVISION_PREGUNTAS.md` con los problemas ya señalados. Las decisiones son tuyas.

## Alcance y método
- Las 430 preguntas (333 base y 97 de la Confesión) se leyeron una por una: enunciado, opciones, respuesta, explicación y referencias.
- **Base:** cada hecho se contrastó con lo que la Biblia dice en el pasaje citado. Las 407 referencias base se resolvieron contra el texto local y no hubo ninguna sin resolver. No pude volcar versículos en la conversación, así que la comprobación de que el pasaje respalda la pregunta fue **automática** (solapamiento de palabras clave entre respuesta/explicación y el pasaje citado) más mi conocimiento del texto. El solapamiento no mostró ninguna cita claramente equivocada; sí sirvió para ver dos referencias que no cubren lo que la explicación afirma (ver abajo).
- **Confesión:** cada pregunta se contrastó con lo que enseña el capítulo y párrafo indicado en `confessionRef` (Confesión Bautista de 1689, 32 capítulos). No tuve a mano la traducción que usas, así que **no** pude comparar redacciones (ver "Derechos").
- Análisis automático de estructura: sesgos de longitud, posición de la respuesta, duplicados, fugas, distractores repetidos, tipografía, continuidad de ids y calibración por nivel.
- No cubre: `memorize.json` (pasajes a memorizar), ni el equilibrio de XP de `levels.json` más allá de contar preguntas disponibles por nivel.
- Mis juicios sobre doctrina se limitan a fidelidad a lo que cada capítulo dice, no a si la doctrina es correcta.

## Resultado en una línea
**No encontré ninguna respuesta incorrecta.** Sí encontré 2 preguntas ambiguas, 2 fugas de la respuesta en el enunciado, varios problemas sistémicos de calidad (el principal: en la Confesión la respuesta correcta es casi siempre la más larga) y un asunto de volumen en los niveles altos.

---

## 1. Problemas sistémicos (por importancia)

### 1.1 Confesión: la respuesta correcta se adivina por su longitud
- La correcta es la opción más larga en **56 de 67** preguntas de opción múltiple (84 %; por azar serían ~25 %). Media de 52 caracteres en la correcta frente a 25 en las erróneas.
- En el módulo base ocurre en 133 de 301 (44 %), menos grave pero también por encima del azar. Hay 66 preguntas (de las 368) en que la correcta mide al menos 1,5 veces la media de sus opciones.
- Las erróneas de la Confesión suelen ser absurdas o terminadas en "solo…" (21 % de las erróneas llevan "solo/únicamente/siempre/nunca…"); las correctas, frases doctrinales completas.
- **Qué hacer:** reescribir los distractores con la misma extensión y estilo que la correcta, con errores plausibles (el error típico que alguien cometería), o acortar las correctas. Candidatas con la correcta ≥ 1,6× la media: `confesion1689-confesion-` 0017, 0020, 0021, 0024, 0025, 0029, 0034, 0035, 0037, 0038, 0043, 0045, 0051, 0052, 0057, 0062, 0064, 0066, 0068, 0078, 0079, 0080, 0083, 0084, 0086, 0089, 0091, 0094, 0095, 0097 (y 0081, 0073); en base, entre otras: `core-ensenanzas-` 0010, 0011, 0021, 0036, 0037, 0046, 0047, 0048 y `core-historia-` 0018, 0025, 0027 (en Cartas, Personajes y Libros la diferencia viene de opciones muy cortas, como "2 Corintios" frente a "Tito", y pesa menos).

### 1.2 Volumen y calibración: los niveles altos se quedan sin contenido nuevo
| Nivel | Dificultad máx. | Preguntas disponibles | Nota |
|---|---|---|---|
| 1 Semilla | 1 | 52 | solo libros, historia y personajes; hay que llegar a 150 XP (~30 aciertos) |
| 2 Brote | 2 | 122 | se suma Lugares |
| 3 Retoño | 2 | 152 | se suma Enseñanzas |
| 4 Arbusto | 3 | 245 | se suma Cartas |
| 5 Vid | 3 | 276 | se suma ¿Dónde está escrito? |
| 6–7 | 4 | 323 | no se desbloquea nada |
| 8–10 | 5 | 333 | no se desbloquea nada |
- Los niveles 8 a 10 piden hasta 7.200 XP pero el pool ya no crece de 333 preguntas, y de dificultad 5 hay solo ~10 (libros 6; historia, personajes, lugares y cartas 1 cada una). A partir de ahí el jugador repite casi todo; los niveles 6 a 10 apenas se distinguen en lo que se pregunta.
- El nivel 1 se agota rápido: 52 preguntas nuevas bastan para unos cinco días de reto y partidas.
- Confesión (si se activa): solo 3 preguntas de dificultad 1 y 27 con dificultad ≤ 2; 56 de las 97 son de dificultad 3.
- **Qué hacer:** si quieres que los niveles 6–10 signifiquen algo, falta contenido (sobre todo dificultad 4–5 y más categorías o preguntas por categoría), y más preguntas de nivel 1. No es un error, pero es lo que más pesa para la rejugabilidad.

### 1.3 Cartas: distractores de relleno
- En **23 de las 36** preguntas de opción múltiple de Cartas, al menos 2 de los 3 distractores son cartas cortas de relleno (Tito, Judas, Filemón, 2 Juan, 3 Juan, Santiago). Esas cartas casi nunca son la respuesta (solo 5 de 36), así que se descartan sin saber el contenido.
- Como erróneas se repiten: Santiago 20 veces, Tito 19, Judas 17, Filemón 14, Hebreos 14, Gálatas 13.
- **Qué hacer:** distractores más plausibles (por ejemplo, otras cartas de Pablo con temas cercanos).

### 1.4 Verdadero/falso: la verdad gana 74 %
- De 47 preguntas V/F, 35 son verdaderas y 12 falsas (base: 13 V / 5 F; Confesión: 22 V / 7 F). Contestar siempre "Verdadero" acierta el 74 %.
- **Qué hacer:** añadir falsas con errores plausibles (no absurdas) hasta acercarse a 50/50.

### 1.5 Preguntas duplicadas o muy parecidas
Mismo hecho en varias preguntas (no es un error, pero el jugador lo ve varias veces):
- **Elías en el Carmelo:** `core-historia-0032`, `core-lugares-0027`, `core-donde_escrito-0024`.
- **Institución de la Cena:** `core-donde_escrito-0033`, `core-cartas-0037`, `core-ensenanzas-0040`.
- **Jonás:** `core-personajes-0019`, `core-historia-0018`, `core-historia-0037`.
- **Caída de Jericó:** `core-historia-0003`, `core-historia-0015`.
- **Gedeón y los 300:** `core-historia-0044`, `core-personajes-0042`.
- **David ungido:** `core-historia-0031`, `core-personajes-0010`.
- **Isaac, hijo de Abraham:** `core-personajes-0017`, `core-personajes-0018`.
- **Acab y Jezabel:** `core-historia-0051`, `core-personajes-0040`.
- **1 Corintios 13:** `core-cartas-0002`, `core-donde_escrito-0018`.
- **Lavamiento de pies:** `core-ensenanzas-0022`, `core-donde_escrito-0028`.
- **Huesos secos:** `core-donde_escrito-0025`, `core-personajes-0048`. **Isaías 6:** `core-donde_escrito-0026`, `core-personajes-0047`.
- **Filipenses desde la cárcel:** `core-cartas-0015`, `core-lugares-0023`. **Caná:** `core-historia-0022`, `core-lugares-0014`.
- **Respuesta "cuarenta":** `core-historia-` 0002, 0021, 0026, 0029, 0043 (el jugador aprende "ante la duda, 40").
- **Respuesta "Juan" en ¿En qué Evangelio…?:** `core-donde_escrito-` 0016, 0017, 0023, 0027, 0028.
- **Libros:** ocho preguntas de "¿cuántos capítulos tiene…?" / "¿qué libro tiene N capítulos?" (`core-libros-` 0020, 0044, 0045, 0046, 0048, 0049, 0050, 0052): poco valor formativo, conservar tres o cuatro.

### 1.6 Derechos: redacción muy cercana a la propia Confesión
La regla 4 de `CLAUDE.md` pide preguntas originales que no transcriban párrafos. En varias, la opción correcta (y a veces la pregunta) sigue casi palabra por palabra la frase de la Confesión, por ejemplo `confesion1689-confesion-` 0017, 0020, 0024, 0026, 0028, 0029, 0035, 0040, 0043, 0045, 0052, 0057, 0062, 0068, 0071, 0079, 0081, 0083, 0089, 0091, 0094, 0095 y 0097. El texto original de 1689 es de dominio público, pero las traducciones al español pueden tener derechos. **Qué hacer:** comparar con la traducción que usas y reformular con tus palabras las que coincidan casi literalmente.

### 1.7 Libros: las referencias apuntan al inicio del libro
Las 52 preguntas de Libros citan "Libro 1:1": no verifican posición, conteo ni clasificación. La verificación real está en la tabla de libros de la app (con pruebas). Es razonable, pero esas preguntas no cumplen el "verificable con un pasaje concreto". Para conteos de capítulos sí cita el último capítulo (p. ej. "Génesis 50:1"), lo que ayuda.

---

## 2. Preguntas con problema (por gravedad)

### Ambiguas (la respuesta marcada podría discutirse)
| Id | Problema | Propuesta |
|---|---|---|
| `core-historia-0050` | "¿Qué apóstol fue sacado de la cárcel por un ángel?" — además de Pedro (Hechos 12), en Hechos 5:19 un ángel saca a los apóstoles de la cárcel, entre ellos Juan, que es una de las opciones. | Precisar: "…preso por orden de Herodes y con cadenas…", o cambiar la opción "Juan". |
| `core-ensenanzas-0037` | "¿Qué debe hacer quien quiera seguirle?" — el distractor "Venderlo todo sin excepción" tiene apoyo bíblico (Lucas 14:33; Mateo 19:21). | Cambiarlo por un distractor claramente falso. |

### Fugas: la respuesta está en el enunciado
| Id | Problema | Propuesta |
|---|---|---|
| `core-ensenanzas-0030` | "En la parábola del trigo y la cizaña, ¿qué crecía junto al trigo?" → cizaña. | "¿Quién sembró la cizaña?" o "¿qué ordenó el dueño al llegar la siega?". |
| `core-cartas-0034` | "¿Qué carta dice que Pablo dejó a Tito en Creta?" → Tito. | "¿En qué carta se encarga establecer ancianos en cada ciudad de Creta?". |

### Menores: redacción, referencias o explicación
| Id | Nota |
|---|---|
| `core-historia-0021` | "antes de ser tentado": Mateo dice que fue tentado después de ayunar 40 días; Lucas, durante ellos. Mejor "¿Cuántos días ayunó Jesús en el desierto?". |
| `core-historia-0031` | David fue ungido cuando Saúl aún reinaba: "como futuro rey / sucesor de Saúl". |
| `core-historia-0048` | La capital del reino del norte fue primero Siquem y Tirsa; Samaria desde Omri. Matizar: "la capital fundada por Omri". |
| `core-historia-0013` | La explicación dice "cuarenta años" pero la referencia (Éx 16:14-15) no lo cubre: añadir Éx 16:35. |
| `core-historia-0059` | La explicación menciona a Acán; la referencia (Jos 8:28) no lo cubre: añadir Jos 7. |
| `core-personajes-0006` | "lo llamó Adán": la referencia (Gn 2:7) no lo dice; es Gn 5:2. |
| `core-personajes-0052` | El distractor "Joaquín y Ana" viene de tradición extrabíblica; sustituir por nombres bíblicos. |
| `core-personajes-0056` | Col 4:14 llama a Lucas "médico amado" pero no dice que sea evangelista (eso es tradición): añadir Lc 1:1-4 o reformular. |
| `core-personajes-0047` | "trono alto y sublime": Ezequiel 1 y Daniel 7 también ven un trono; mencionar "serafines" en el enunciado. |
| `core-lugares-0015` | Dice "huerto" y cita Mt 26:36 ("lugar"); el huerto es Jn 18:1: añadir esa cita. |
| `core-lugares-0023` | Colosenses, Efesios y Filemón también se escriben desde prisión; reforzar "carta que repite 'gozaos en el Señor'". |
| `core-ensenanzas-0022` | "antes de la última cena": Jn 13 lo sitúa durante la cena. |
| `core-ensenanzas-0015` | En Jn 10 Jesús se llama también "la puerta"; citar Jn 10:11 en el enunciado. |
| `core-ensenanzas-0038` | Redacción torpe: "¿Quiénes son los pámpanos en la imagen de la vid?". |
| `core-libros-0027` | "lleva el nombre de un apóstol que no es Pablo": Tito y Filemón tampoco son apóstoles; "carta atribuida a un apóstol distinto de Pablo". |
| `core-libros-0034` | 1–2 Timoteo y Tito también son a una persona; "la más breve de las cartas de Pablo". |
| `confesion1689-confesion-0049` | El V/F ("de la misma naturaleza en todo creyente") es una inferencia; el cap. 14.3 dice que, incluso en su menor grado, difiere de la fe de los creyentes temporales. |
| `confesion1689-confesion-0020`, `-0037`, `-0052` | La explicación no respalda la respuesta marcada (habla de otra parte del párrafo). |
| `confesion1689-confesion-0032`, `-0052`, `-0067` | `confessionRef` sin párrafo (`cap. 8`, `cap. 15`, `cap. 20`); `-0052` es 15.4. |

---

## 3. Lo que comprobé y está bien
- **Hechos bíblicos:** nombres, cifras (40 días y noches, 430 años, 70 años de cautiverio, 52 días del muro, 12 tribus/apóstoles, 10 plagas, 13 cartas de Pablo…), personajes, lugares y cronología. Ninguna respuesta incorrecta.
- **Libros y orden:** posiciones, conteos de libros y de capítulos, clasificación en testamentos y secciones, y las 15 preguntas de ordenar (14 base y 1 de la Confesión): el orden marcado es el correcto en todas.
- **Estructura:** ids continuos sin huecos ni duplicados en las 8 categorías; cuatro opciones en todas las de opción múltiple; referencias bien formadas; la posición de la respuesta correcta no importa porque la app baraja las opciones.
- **Confesión:** las 97 se corresponden con el capítulo y párrafo indicados; la numeración de capítulos coincide con los 32 de la Confesión de 1689. Ninguna es doctrinalmente infiel a lo que el párrafo dice, salvo el matiz de redacción señalado en `-0049`.
- **Postura:** el módulo base evita posturas denominacionales. Algunos puntos de la Confesión (inmersión, día del Señor, elección, ordenanzas) son propios de la postura bautista, pero están en el módulo aparte y formulados como "según la Confesión".
- **Tipografía:** sin dobles espacios, puntuación rara, comillas rectas ni palabras repetidas.

## 4. Orden de trabajo sugerido
1. Decidir qué hacer con el sesgo de longitud en la Confesión (1.1) y la cercanía de redacción (1.6): conviene resolverlo antes de revisarla pregunta por pregunta.
2. Corregir las 2 ambiguas y las 2 fugas, y las menores que quieras.
3. Reforzar distractores de Cartas (1.3) y añadir V/F falsas (1.4).
4. Valorar el contenido nuevo de niveles altos (1.2).
5. Entonces revisar con `docs/REVISION_PREGUNTAS.md`.

Claude puede aplicar las correcciones que apruebes (editando los JSON, sin tocar `reviewed` y subiendo `contentVersion`), pero no marca nada como revisado.

---

## 5. Correcciones ya aplicadas (módulo base)
Aplicadas en los JSON (`contentVersion` +1 en los seis archivos `-02`, `reviewed` intacto en `false`): las 2 ambiguas, las 2 fugas y 16 de las menores del módulo base (`core-historia-0013/0021/0031/0048/0059`, `core-personajes-0006/0047/0052/0056`, `core-lugares-0015/0023`, `core-ensenanzas-0015/0022/0038`, `core-libros-0027/0034`). Se comprobaron contra el texto local del pasaje.

**Pendiente: todo lo de la Confesión** (sesgo de longitud, cercanía de redacción, `-0020/-0037/-0049/-0052`). El repositorio no tiene el texto de la Confesión de 1689 y las preguntas se escribieron de memoria, así que no se pueden verificar ni corregir sin una fuente que aporte el usuario. Tampoco se han tocado los distractores de Cartas ni las V/F falsas ni el contenido de niveles altos.
