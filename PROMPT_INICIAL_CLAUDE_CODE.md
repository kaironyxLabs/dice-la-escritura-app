# Proyecto: app Android "Lámpara" (nombre de trabajo), juego de aprendizaje bíblico offline

## 1. Rol y forma de empezar
Eres el ingeniero Android principal de este proyecto y lo construyes de principio a fin: código, contenido inicial, herramienta de importación del texto bíblico, pruebas y documentación. No escribas código todavía. Primero revisa el entorno y presenta un plan por hitos (sección 9) para que yo lo apruebe. Pregunta solo lo que bloquee de verdad. Lo demás decídelo y regístralo en docs/DECISIONES.md.

## 2. Objetivo y decisiones ya tomadas
App Android en español, 100 % sin conexión, para que un cristiano protestante aprenda la Biblia un poco cada día y supere niveles de conocimiento. Todo se basa en la Reina-Valera 1960 (RVR1960). Uso personal y de un círculo cercano: se instala por APK, no se publica en Play Store.

No cambies estas decisiones:
- Kotlin + Jetpack Compose con Material 3, una sola Activity, Navigation Compose.
- MVVM con ViewModel y StateFlow, Hilt, Room, DataStore (Preferences), WorkManager, kotlinx.serialization y coroutines.
- minSdk 26. targetSdk y librerías en su última versión estable, centralizadas en gradle/libs.versions.toml.
- Un solo módulo Gradle `app`, con paquetes por funcionalidad.
- Sin permiso INTERNET, sin anuncios, sin analíticas, sin cuentas. Los datos viven en el teléfono.
- Interfaz y contenido en español. Todo texto visible va en strings.xml.
- Id de paquete provisional: com.example.lampara. Dime dónde cambiarlo.

## 3. Reglas de contenido (críticas)
1. El texto de la RVR1960 tiene derechos de autor. No lo escribas de memoria, no lo pegues en documentos ni pruebas, y no lo subas a ningún repositorio remoto. El texto sale únicamente del archivo que aporta el usuario (sección 5.1). Si el repositorio se publica en algún remoto, debe ser privado y sin el texto bíblico ni el PDF.
2. Preguntas y explicaciones citan referencias (ej. "Romanos 3:23") y parafrasean. No transcriben versículos.
3. Las preguntas del módulo base deben poder verificarse con un pasaje concreto y evitar posturas denominacionales: solo lo que el texto bíblico afirma de forma directa.
4. La Confesión Bautista de Fe de 1689 va en un módulo aparte, protegido por una bandera de función (sección 6). Preguntas originales, sin transcribir párrafos de traducciones modernas con derechos.
5. Todo contenido nuevo se escribe con `reviewed: false`. Nunca lo marques `true`: lo revisa el usuario.
6. No inventes datos. Si dudas de una referencia o de un hecho bíblico, descarta la pregunta.

## 4. Funcionalidades
### 4.1 Niveles de conocimiento
- 10 niveles con nombre (propón una lista sobria con imágenes de crecimiento, p. ej. Semilla hasta Cedro). Los umbrales de XP viven en assets/content/levels.json, no en el código.
- Cada pregunta tiene dificultad de 1 a 5. El nivel del jugador desbloquea dificultades y categorías.
- XP por acierto según dificultad, con una bonificación pequeña por racha dentro de la partida. Sin vidas ni cronómetros agresivos.
- Examen de ascenso: al llenar la barra de XP del nivel, 10 preguntas del nivel con 80 % para subir. Se puede repetir sin límite.

### 4.2 Partidas
- Partida de 10 preguntas por categoría: Libros y orden de la Biblia, Historia bíblica, Personajes, Lugares y geografía, Enseñanzas y parábolas de Jesús, Cartas del Nuevo Testamento, "¿Dónde está escrito?".
- Tipos de pregunta: opción múltiple (incluye "¿en qué libro?"), verdadero/falso y ordenar (libros o eventos).
- Tras cada respuesta: retroalimentación breve con la referencia bíblica y una explicación corta.

### 4.3 Reto diario y racha
- Un reto de 10 preguntas al día: mezcla de nuevas y de repaso vencido. Se reinicia a medianoche local.
- Racha de días consecutivos con el reto completado, con un día de gracia por semana. Tono sobrio y sin culpa: sin mensajes que avergüencen ni mecánicas manipuladoras.
- Recordatorio opcional con hora configurable (WorkManager, canal de notificación propio, pedir POST_NOTIFICATIONS en contexto).

### 4.4 Repetición espaciada
- Leitner de 5 cajas (1, 2, 4, 8 y 16 días). Un fallo devuelve la pregunta a la caja 1. Esta lógica alimenta el reto diario y el repaso.

### 4.5 Memorizar versículos
- Lista de unos 100 pasajes clave en assets/content/memorize.json (solo referencias y etiquetas temáticas). El texto sale de BibleRepository.
- El usuario marca los que está memorizando. Modo "completar": se ocultan palabras en grados crecientes (20 %, 40 %, 60 %, y 100 % escribiendo), con repetición espaciada propia.
- Los pasajes que incluyan versículos pendientes (sección 5.1) se muestran como no disponibles y no entran en el modo completar.
- Si falta el archivo del texto bíblico, estas pantallas explican qué falta y apuntan a docs/TEXTO_BIBLICO.md. El resto de la app sigue funcionando.

### 4.6 Progreso y ajustes
- Estadísticas: nivel, XP, racha actual y mejor racha, precisión por categoría, historial de 30 días.
- Exportar e importar el progreso como archivo JSON (Storage Access Framework) para cambiar de teléfono o hacer copia.
- Ajustes: recordatorio, módulo Confesión 1689, "mostrar solo preguntas revisadas" (apagado por defecto mientras el contenido esté pendiente de revisión), exportar/importar, acerca de.

## 5. Datos y contenido
- Preguntas en JSON bajo assets/content/ (core-*.json y confesion1689-*.json). Ids estables, porque el progreso se enlaza por id. Incluye `contentVersion` para actualizar contenido sin perder progreso.
- Esquema de una pregunta:

```json
{
  "id": "core-libros-0001",
  "module": "core",
  "category": "libros",
  "type": "multiple_choice",
  "difficulty": 1,
  "question": "...",
  "options": ["...", "...", "...", "..."],
  "answerIndex": 2,
  "references": ["Génesis 1:1"],
  "explanation": "...",
  "tags": ["pentateuco"],
  "reviewed": false
}
```
  Para `true_false` usa `answer` (booleano). Para `ordering` usa `items` en el orden correcto (la app los baraja). Las preguntas de 1689 añaden `confessionRef` (ej. "cap. 1, párr. 1").
- Tabla de los 66 libros con ids estables de tres letras, nombres de la RVR1960 (Cantares, Hechos...), abreviaturas, testamento, sección y número de capítulos. Sirve para validar referencias.
- Contenido inicial: al menos 300 preguntas base repartidas por categoría y dificultad, y al menos 96 de la Confesión (3 por cada uno de los 32 capítulos, con la tabla de capítulos y títulos).
- Script en tools/ que genere un informe de revisión de preguntas en Markdown, con las preguntas por categoría y casillas para marcar.

### 5.1 Texto bíblico: fuente, importación y lectura
El usuario copiará el PDF a fuentes/biblia_rv1960.pdf antes de empezar. Si no está, pídelo. La carpeta fuentes/ y el archivo assets/bible/rvr1960.json van en .gitignore.

La fuente es una copia no oficial hecha en Word en 2000, con texto extraíble y defectos conocidos. Crea tools/importar_biblia (Python 3 con PyMuPDF o pdfplumber, en un entorno virtual, con pruebas pytest que usen un PDF sintético falso, nunca texto de la RVR1960) que genera assets/bible/rvr1960.json sin modificar el texto salvo lo indicado:

1. Descarta las cabeceras y pies repetidos ("Santa Biblia – Versión Reina Valera 1960", "www.siguiendosuspisadas.com.ar"). Un salto de página no es un salto de párrafo.
2. Encabezado de capítulo = línea "NOMBRE N". El PDF mezcla mayúsculas y minúsculas y trae erratas (ej. "ECLESTIASTÉS"): asigna los libros por orden de aparición y valídalos contra la tabla de 66 libros y sus capítulos, no por el nombre escrito. Son 1.189 capítulos.
3. Los versículos van numerados en línea. Extrae por secuencia (1, 2, 3...) tolerando saltos de numeración y registrándolos. No descartes una línea por estar en mayúsculas si contiene un número (ej. "10 Y:").
4. Guarda aparte del versículo, nunca mezclados con él: títulos de sección (líneas sueltas sin punto final), referencias a pasajes paralelos entre paréntesis (unos 540 bloques tipo "(1 R. 3.3-15)") y el texto sin numerar antes del versículo 1 (sobrescritos de salmos).
5. Genera docs/TEXTO_BIBLICO_informe.md con: cada capítulo cuyo conteo difiera de docs/versiculos_por_capitulo.json, cada salto de numeración y cada carácter o patrón sospechoso (\ * _ = o dígitos pegados a letras). El conteo esperado debe salir de una fuente independiente y documentada, ajustada a la numeración de la RVR1960 (en 3 Juan este PDF trae 15 versículos, no 14). No corrijas nada de forma automática.
6. Hallazgos previos que debes confirmar con el informe, no asumir: Génesis 30:6-8 (falta el v. 7 y parte del v. 8, con un resto dañado), Salmo 39:11 (número dañado), 1 Corintios 1:31 (numerado como 32), Ezequiel 31:17-18 (fundidos), Éxodo 2:15 (dígito dentro de una palabra) y caracteres sueltos en Éxodo 39, 2 Samuel 4 y 8, Amós 5 y Filemón 1.
7. Las correcciones se aplican solo desde fuentes/correcciones.json, que escribe el usuario con el texto verificado en su edición impresa u otra fuente (libro, capítulo, versículo, texto). Registra cada corrección aplicada. Nunca completes ni "arregles" un versículo desde tu memoria.
8. Ninguna anomalía puede pasar en silencio: cada una queda corregida en correcciones.json o marcada como pendiente por el usuario. Verificaciones obligatorias que fallan en voz alta: 66 libros, 1.189 capítulos, numeración sin huecos salvo pendientes marcados, ningún versículo vacío fuera de los pendientes y ningún carácter fuera del conjunto permitido. Guarda el SHA-256 del PDF y del JSON resultante en docs/TEXTO_BIBLICO.md.
9. Formato de salida de assets/bible/rvr1960.json (documéntalo en docs/TEXTO_BIBLICO.md):

```json
{
  "traduccion": "RVR1960",
  "libros": [
    {
      "id": "GEN",
      "capitulos": [
        {
          "numero": 1,
          "encabezado": null,
          "versiculos": ["...", "..."],
          "secciones": [{ "antesDelVersiculo": 1, "titulo": "..." }],
          "paralelos": [{ "antesDelVersiculo": 1, "texto": "..." }],
          "pendientes": []
        }
      ]
    }
  ]
}
```

En la app, `BibleTextSource` es una interfaz y su implementación lee ese archivo. Si falta, los modos que dependen del texto avisan con claridad y el resto sigue funcionando. Para pruebas de la app usa un fixture claramente falso ("Texto de prueba, no es RVR1960"), solo en las fuentes de test.

## 6. Módulo Confesión 1689 (feature flag)
- `FeatureFlag` (enum) con CONFESION_1689 y un `FeatureFlags` con Flow<Boolean> respaldado por DataStore. Debe ser trivial agregar más banderas.
- Valor por defecto: desactivado. Pantalla de onboarding que explica el módulo con tono neutral y deja elegir, más un interruptor en Ajustes. (Yo la activaré en mi teléfono.)
- Con la bandera apagada no debe haber rastro del módulo: ni categoría, ni preguntas en el reto diario o el examen, ni estadísticas. El filtro se aplica en el repositorio de preguntas, no solo en la UI.
- Al apagarlo, el progreso del módulo se conserva, pero no cuenta ni se muestra.

## 7. Arquitectura y estructura
```
app/src/main/java/<paquete>/
  core/      (di, tema, utilidades, flags)
  data/      (db, content, datastore, repository)
  domain/    (model, usecase: puntaje, niveles, leitner, racha)
  feature/   (onboarding, home, quiz, daily, memorize, progress, settings, confesion)
app/src/main/assets/content/   (core-*.json, confesion1689-*.json, memorize.json, levels.json)
app/src/main/assets/bible/     (rvr1960.json: generado por la herramienta, ignorado por git)
fuentes/                       (biblia_rv1960.pdf y correcciones.json: del usuario, ignorados por git)
tools/importar_biblia/         (importador del texto bíblico con sus pruebas)
docs/  .github/workflows/  CLAUDE.md  README.md
```
- La lógica pura (XP, niveles, Leitner, racha, filtros) vive en `domain`, sin dependencias de Android.
- Usa java.time con un `Clock` inyectable para poder probar rachas y fechas.
- Room guarda el progreso por pregunta (caja, próxima fecha, aciertos y fallos), sesiones, retos diarios y progreso de versículos.
- Inicializa el repositorio git desde el primer hito, con un .gitignore que cubra fuentes/, assets/bible/rvr1960.json, local.properties, build/, keystores y entornos virtuales.

## 8. Diseño y accesibilidad
- Material 3 con tema propio (no los colores por defecto), claro y oscuro. Tipografía serif legible para pasajes y sans para la interfaz, empaquetadas en la app con licencia abierta.
- Estilo sobrio. Sin ilustraciones infantiles, sin iconografía kitsch y sin emoji como decoración.
- Respeta el tamaño de fuente del sistema, TalkBack (contentDescription), contraste AA y objetivos táctiles de al menos 48 dp.
- Pantallas: onboarding, inicio (nivel, XP, racha, botón del reto diario), categorías, partida, resultado, examen de ascenso, memorizar (lista y práctica), progreso, ajustes.
- Ícono adaptativo sencillo y propio.

## 9. Calidad y forma de trabajar
Pruebas:
- JUnit para XP, niveles y ascenso, Leitner, racha con día de gracia, filtrado por bandera (con la bandera apagada jamás sale una pregunta de 1689) y exportar/importar de ida y vuelta.
- Prueba de validación de contenido: ids únicos, tipos y campos obligatorios, answerIndex válido, referencias que se interpretan contra la tabla de 66 libros con capítulos existentes, explicación no vacía.
- Un par de pruebas de humo de UI con Compose.
- pytest para el importador, con un PDF sintético falso.
- `./gradlew test lint assembleDebug` debe pasar antes de cada commit de hito.
- R8 activado en release. Documenta la firma. El keystore nunca va al repositorio.

Forma de trabajar:
1. Revisa el entorno (JDK 17 o superior, Android SDK, Gradle wrapper, Python 3, emulador o dispositivo). Dime qué falta y no instales nada global sin avisar.
2. Entra en modo plan y preséntame los hitos. Espera mi aprobación.
3. Trabaja hito a hito con commits pequeños en español (feat:, fix:, docs:, test:) y verifica cada hito compilando y probando.
4. Al cerrar cada hito, resume en cinco líneas qué quedó, qué probaste y qué sigue.
5. Mantén CLAUDE.md al día con los comandos de build, las convenciones y las reglas de contenido de la sección 3.

Hitos:
- H0: estructura del proyecto, git y .gitignore, CLAUDE.md, esqueleto de docs, workflow de CI, tema y navegación.
- H1: modelo de contenido, carga, tabla de libros, validación y 30 preguntas de muestra.
- H2: partidas, XP, niveles y examen de ascenso.
- H3: reto diario, racha, Leitner y recordatorio.
- H4: importador del texto bíblico (sección 5.1), informe de anomalías, BibleTextSource y modo memorizar versículos.
- H5: módulo 1689, feature flag y onboarding.
- H6: progreso, estadísticas, exportar/importar y ajustes.
- H7: contenido completo (mínimos de la sección 5) e informe de revisión de preguntas.
- H8: pulido, accesibilidad, ícono, R8, APK y documentación final.

## 10. Criterios de aceptación
- El APK debug se instala en Android 8 o superior y funciona sin internet.
- El manifiesto final no declara el permiso INTERNET (verifícalo).
- Con el módulo 1689 apagado no hay rastro de él en ninguna pantalla.
- Hay al menos 300 preguntas base y 96 de la Confesión, todas con `reviewed: false` y validadas por pruebas.
- El importador genera el JSON con las verificaciones de la sección 5.1 y un informe de anomalías; ninguna anomalía queda sin corregir o marcar como pendiente.
- Exportar e importar el progreso funciona de ida y vuelta.
- Existen README.md, docs/INSTALACION.md, docs/CONTENIDO.md (cómo agregar y revisar preguntas), docs/TEXTO_BIBLICO.md y docs/DECISIONES.md.

Empieza por el paso 1 de la sección 9 y por el plan. No escribas código hasta que yo lo apruebe.
