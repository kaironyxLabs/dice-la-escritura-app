# Dice la Escritura

App Android en español para aprender la Biblia un poco cada día, sin conexión. Niveles de conocimiento, partidas por categoría, reto diario con repetición espaciada y memorización de versículos, todo basado en la Reina-Valera 1960.

Uso personal y de un círculo cercano: se instala por APK, no se publica en Play Store. No usa internet, anuncios, analíticas ni cuentas en línea (solo un perfil local con nombre y color); el progreso vive en el teléfono y se respalda exportándolo a un archivo.

## Qué incluye
- 10 niveles (de Semilla a Cedro) con examen de ascenso (10 preguntas, 80 % para subir). Los umbrales viven en `assets/content/levels.json`.
- Partidas de 10 preguntas por categoría: libros y orden, historia, personajes, lugares, enseñanzas de Jesús, cartas y "¿dónde está escrito?". Opción múltiple, verdadero o falso y ordenar (se reordena arrastrando el asa de cada elemento; con TalkBack, acciones subir y bajar).
- Reto diario de 10 preguntas (nuevas y de repaso vencido), que se retoma si sales a mitad (también tras cerrar la app) y que, si cruza la medianoche, cuenta en el día en que empezó. Racha con un día de gracia por semana y recordatorio opcional (pide el permiso de notificaciones en Android 13 o superior y ofrece abrir los ajustes del sistema si lo rechazas).
- El XP tiene tope por nivel: con la barra llena, lo ganado espera al examen de ascenso. El resultado de cada partida muestra el XP que de verdad entró y avisa de lo que no se sumó.
- Repetición espaciada Leitner de 5 cajas (1, 2, 4, 8 y 16 días).
- Memorizar unos 100 pasajes clave: completar con 20, 40 y 60 % de palabras ocultas y escribiendo el pasaje completo. Aprueba con el 90 % de las palabras; cada palabra de más resta un acierto, y practicar un pasaje antes de que toque su repaso no acelera los intervalos (1, 3, 7, 14, 30 y 60 días).
- Módulo opcional de la Confesión Bautista de Fe de 1689, apagado por defecto y sin rastro en la app mientras lo esté.
- Perfil local único por teléfono: nombre y color de avatar en la bienvenida, saludo en Inicio y pantalla de perfil. El avance cuelga de ese perfil (Room v2, migración 1→2).
- Progreso con estadísticas, exportar e importar el progreso en JSON (versión 2 con el usuario; sigue leyendo la versión 1) y ajustes.
- Después de cada respuesta la app muestra siempre la respuesta correcta, su justificación bíblica y las citas con el versículo de la RVR1960 leído del texto local (las preguntas guardan solo la referencia); en historia, personajes, lugares y enseñanzas el enunciado lleva además una pista con libro y capítulo (en la Confesión añade además el capítulo y párrafo).
- Memorizar: 159 pasajes, todos aprobados por el usuario (26-oct; el texto sale del archivo de la RVR1960 y las pruebas comprueban que cada versículo existe).
- 518 preguntas base y 97 de la Confesión, todas aprobadas por el usuario el 26-oct-2026 (`reviewed: true`). Guardan solo la referencia y cada una se contrastó con el texto local de la RVR1960 o con el PDF de la Confesión (ver `docs/CONTRASTE_CONFESION.md` y `docs/REVISION_PREVIA.md`). Todo contenido nuevo vuelve a entrar con `reviewed: false` hasta que el usuario lo apruebe.

## Estado
Hitos H0 a H8 completos. El texto bíblico del PDF está revisado: el importador termina sin anomalías (66 libros, 1.189 capítulos, 31.104 versículos) con las correcciones que dictó el usuario desde su edición impresa. El texto se compara con BibleGateway RVR1960 (fuente de referencia indicada por el usuario) y la app lo lee de a un libro por vez. La app se probó a mano y con pruebas instrumentadas en dos teléfonos (TCL con Android 12 y Xiaomi con Android 15): reto diario con reanudación, racha, partidas, examen de ascenso, memorizar con práctica, módulo 1689, exportar e importar el progreso, permiso y disparo puntual del recordatorio, y rotación de pantalla funcionan. Pendiente de tu parte (ver `docs/`): revisar las preguntas (`docs/REVISION_PREGUNTAS.md`, guía en `docs/CONTENIDO.md`) y respaldar el keystore. Cuenta local C1 a C3 hecha (decisión: un solo perfil por teléfono, sin C4). Probado en el TCL: migración Room 1→2 sobre una instalación v1 real (datos intactos) y 13/13 androidTest. Nombre definitivo «Dice la Escritura» e id de paquete `com.kaironyx.diceescritura` (D-015).

## Pruebas
- `./gradlew test`: 160 pruebas JVM (dominio, contenido, texto bíblico y exportación del progreso).
- `./gradlew connectedDebugAndroidTest`: 13 pruebas instrumentadas (4 de interfaz y 9 sobre Room con reloj simulado: reto que cruza la medianoche, racha de varios días, XP con tope, aislamiento de perfiles). En algunos teléfonos hay que arrancarlas de otra forma; ver `docs/INSTALACION.md`.
- `tools/importar_biblia` (87 pruebas) y `tools/contenido` (8): ver los comandos en `docs/INSTALACION.md`.

## Compilar
Requiere JDK 17 y el Android SDK (ver `docs/INSTALACION.md`).

```
./gradlew test lint assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/`.

## Texto bíblico
El texto de la RVR1960 tiene derechos de autor y no está en el repositorio. Se genera localmente desde un PDF que aporta el usuario con `tools/importar_biblia` (66 archivos, uno por libro, en `assets/bible/libros/`); ver `docs/TEXTO_BIBLICO.md`.

## Documentación
- `docs/INSTALACION.md`: entorno, compilación, firma, instalación y cambio de teléfono.
- `docs/CONTENIDO.md`: cómo agregar y revisar preguntas.
- `docs/TEXTO_BIBLICO.md`: importación del texto bíblico y su procedimiento de correcciones.
- `docs/DECISIONES.md`: decisiones de diseño y sus motivos.
- `docs/MANUAL_USUARIO.md`: manual de usuario detallado.
- `docs/CONTRASTE_CONFESION.md` suma un cruce (26-oct) de los versículos de apoyo con las notas al pie del PDF: el 85 % coincide y se listan los demás.
- `docs/INSTALACION.md` incluye la sección «Actualizar la app» (versionCode, misma firma, migraciones de Room).
- `docs/PLAN_DESARROLLO.md`: lo que falta para terminar, por etapas. Con `docs/PLAN_CUENTA_LOCAL.md` (perfil local con nombre y avatar) y `docs/PLAN_REFACTOR_UI.md` (rediseño de la interfaz).
- Release probado el 26-oct-2026: `assembleRelease` (R8 activo, firmado) instalado en el TCL (Android 12) y 300 eventos de monkey sin cierres ni errores de clases (`versionName` 0.1.0).
- Horizontal (26-oct): con menos de 480 dp de alto la app oculta el título y pasa la navegación a un riel lateral, para que el contenido use casi todo el alto (comprobado en el TCL).
- Rediseño: U0 a U6 hechas y U7 (accesibilidad y pulido) en marcha: revisada la fuente grande (escala 2,0) en el TCL; la barra inferior limita su escala a 1,3 y no parte las etiquetas, y la precisión por categoría apila nombre y valor con fuente muy grande. La app sigue siendo siempre clara (D-017), sin modo oscuro. Etapas: U0 a U2 hechas, U6 (calendario de actividad y nueve logros en Progreso), U5 (práctica con banco de palabras y resaltado de aciertos y olvidos), U4 (lámpara de la racha, que se enciende al completar el reto diario) hecha, U3 (partida) casi lista (ordenar con arrastre; subida de nivel con jardín de plantas y destellos; resultado en tarjetas de pergamino), estilo de pergamino de la maqueta del usuario (textura de papel tomada del video del splash, bordes rasgados con poco quemado, título dorado en Cormorant Garamond, bordes rasgados, medallones) en la cabecera, el Inicio con cuadrícula de categorías, la barra inferior, Progreso, Memorizar y Ajustes con tarjetas de pergamino (D-017), splash en video del pergamino (6 s, sin audio, tercera versión del 21-oct; se ve a todo el ancho, sin recortar personajes, con los bordes de arriba y abajo difuminados; la app se compone cuando el video ya está en marcha, lo que adelanta unos 2 s su inicio en el TCL) e icono nuevo con el emblema del pergamino (sistema de diseño, catálogo de componentes en debug, transiciones, estados de carga, plantas de los 10 niveles y mapa de niveles, ver `docs/PLAN_REFACTOR_UI.md`).
- `docs/REVISION_PREGUNTAS.md`: informe de revisión con casillas (generado con `tools/contenido/informe_revision.py`).
- `docs/REVISION_PREVIA.md`: pasada previa de Claude sobre las preguntas (con los cambios posteriores) (problemas sistémicos, correcciones ya aplicadas al módulo base y lo pendiente de la Confesión); no marca nada como revisado.
- `docs/licencias/`: licencias de las fuentes empaquetadas.
- `CLAUDE.md`: guía de trabajo del repositorio (comandos, convenciones y reglas de contenido).
