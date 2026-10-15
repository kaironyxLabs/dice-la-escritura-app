# Plan: refactorización completa de la interfaz

Objetivo: pasar de una interfaz funcional y sobria a una **dinámica, visual e interactiva**, con ilustraciones, movimiento y retroalimentación, sin perder la sobriedad que corresponde a una app de estudio bíblico y sin tocar las reglas de la app (sin internet, sin analíticas, sin cuentas, todo texto en `strings.xml`).

## 1. Principios

1. **Funcionalidad primero.** Cada cambio visual debe hacer más fácil estudiar, no solo verse mejor.
2. **Sobrio, no infantil ni agresivo.** La racha informa, no presiona; no hay castigos, relojes que apuren ni sonidos molestos.
3. **Respeto por la Biblia.** Las ilustraciones son de plantas, objetos y símbolos neutrales (semilla, brote, vid, olivo, lámpara, libro). No se representa a Dios ni a Cristo, ni se dibujan escenas bíblicas con rostros.
4. **Funciona para todos.** Texto grande, contraste alto, lector de pantalla, modo oscuro, horizontal y pantallas pequeñas.
5. **Liviana.** Sin internet y sin imágenes pesadas: gráficos vectoriales y animaciones hechas en Compose. Presupuesto de peso: que la parte nueva no pase de unos pocos MB.
6. **Nada se rompe.** Se refactoriza pantalla por pantalla con las pruebas actuales en verde.

## 2. Estado actual de la interfaz

- Kotlin + Compose Material 3, una sola Activity, navegación con rutas tipadas.
- Tema propio sin colores dinámicos (decisión D-004), fuentes empaquetadas (D-005).
- Pantallas: bienvenida, inicio, categorías, partida (con retroalimentación y resultado), memorizar, práctica, progreso y ajustes.
- Casi todo son tarjetas de Material sin ilustraciones ni animaciones propias; el único elemento gráfico es la barra de progreso y el gráfico de barras de 30 días.

## 3. Sistema de diseño (etapa U0)

Antes de tocar pantallas se define un sistema reutilizable:

- **Paleta:** colores cálidos de "lámpara" (dorado suave, tinta oscura, crema de papel) con variantes claro y oscuro y verificación de contraste (AA como mínimo).
- **Tipografía:** una fuente de títulos con carácter (serifa legible para títulos y citas bíblicas) y otra neutra para el cuerpo; escala tipográfica definida y respetando el tamaño de fuente del sistema.
- **Formas y espaciado:** radios, elevaciones y una cuadrícula de 4/8 dp.
- **Iconografía:** conjunto propio de iconos vectoriales para categorías, niveles y acciones.
- **Componentes base** (en `core/ui`): tarjeta de contenido, botón principal y secundario, chip, barra de progreso animada, insignia de nivel, contenedor de retroalimentación, avatar, diálogo, hoja inferior.
- **Catálogo de componentes:** una pantalla de desarrollo (solo en builds debug) que los muestra todos en claro, oscuro y fuente grande, para revisarlos de un vistazo.

## 4. Ilustraciones y recursos gráficos

**De dónde salen.** Como no hay internet ni imágenes con licencia que usar sin revisar, el plan es crear todo como **gráficos vectoriales propios** (XML vectorial o dibujo en Compose). Así pesan poco, escalan sin perder calidad y se adaptan al modo oscuro. Si tienes material tuyo (logotipo, ilustraciones), se integra.

**Qué se ilustra**
- **Diez ilustraciones de nivel**, una por planta o árbol: Semilla, Brote, Retoño, Arbusto, Vid, Olivo, Higuera, Palmera, Encina y Cedro. Forman un "jardín" que crece con el usuario.
- **Iconos e ilustraciones de categoría** (libros, historia, personajes, lugares, enseñanzas, cartas, ¿dónde está escrito?, Confesión).
- **Avatares** del perfil local (ver `docs/PLAN_CUENTA_LOCAL.md`).
- **Estados vacíos y celebraciones:** "reto completado", "subiste de nivel", "sin actividad todavía".
- **Fondo y marca:** icono de la app, pantalla de bienvenida y pantalla de carga.

## 5. Interacción y movimiento

Todo el movimiento debe poder reducirse o apagarse si el sistema tiene activada la opción de "quitar animaciones".

- **Barra de XP animada:** se llena con suavidad al ganar XP; al llenarse, pulso suave y aparición del botón de examen.
- **Retroalimentación de respuesta:** la tarjeta aparece con una transición breve; la opción elegida y la correcta se resaltan con color y forma (no solo con color).
- **Preguntas de ordenar:** arrastrar y soltar, además de tocar, con vibración suave al colocar.
- **Subida de nivel:** animación de la planta del nivel que "crece" a la siguiente, con un mensaje claro. Sin confeti estridente; algo sobrio.
- **Racha:** el indicador reacciona al completar el reto (una luz de lámpara que se enciende), y el día de gracia se muestra con un icono propio.
- **Transiciones entre pantallas:** deslizamiento y atenuación coherentes.
- **Vibración (háptica) discreta** en acierto y fallo, desactivable en Ajustes.
- **Sonido:** no por ahora (la app suele usarse en lugares tranquilos); se puede evaluar después.

## 6. Pantallas, una por una

1. **Bienvenida:** carrusel de 3 a 4 páginas ilustradas (qué es, cómo se avanza, el módulo opcional, tu perfil). Se puede saltar.
2. **Inicio (panel de avance):**
   - Cabecera con avatar, saludo y nivel.
   - Tarjeta del reto diario como protagonista, con 10 puntos que se van llenando.
   - Racha con la lámpara.
   - "Jardín" del nivel con la planta actual.
   - Accesos rápidos: categorías, memorizar y último pasaje practicado.
3. **Mapa de niveles:** camino vertical con los 10 niveles, la planta de cada uno, los candados y lo que desbloquean.
4. **Categorías:** tarjetas grandes con ilustración, progreso por categoría (porcentaje de preguntas dominadas) y estado de bloqueo explicado.
5. **Pregunta:** tarjeta central con el enunciado, opciones grandes, barra de progreso de la partida, y retroalimentación con respuesta, justificación y la invitación a comprobar en la Biblia física (impresa). Las citas se muestran como chips destacados.
6. **Resultado:** resumen visual (aciertos, XP, categorías que mejoraron) y una sugerencia de qué hacer después.
7. **Memorizar:** lista con tarjetas y filtros por etiqueta; indicador de "cuánto lo sé" por pasaje.
8. **Práctica de pasaje:** palabras ocultas como casillas que se rellenan; opción de escribir o de arrastrar palabras de un banco; vista del pasaje con resaltado al comprobar.
9. **Progreso:** gráficos más ricos (precisión por categoría, constancia, calendario de actividad estilo "mapa de calor"), logros.
10. **Logros:** conjunto pequeño y sobrio (primera racha de 7 días, primer nivel, 10 pasajes dominados…). Sin presión ni comparación.
11. **Perfil y Ajustes:** perfil con avatar y resumen; ajustes agrupados con descripciones claras.

## 7. Accesibilidad e idioma

- Etiquetas de accesibilidad en todos los elementos interactivos y en las ilustraciones informativas (las decorativas se marcan como tales).
- Objetivos táctiles de al menos 48 dp.
- Orden de foco lógico para teclado y TalkBack.
- Contraste verificado en claro y oscuro; no depender solo del color para marcar acierto o error.
- Pruebas con tamaño de fuente grande (200 %) y en horizontal.
- Todo texto visible sigue en `strings.xml` (español).

## 8. Técnica

- **Animaciones:** API de animación de Compose; no se añade ninguna librería nueva salvo que se justifique. Si se quiere una celebración más rica, se evalúa Lottie (offline, licencia Apache 2.0) como dependencia opcional, con aprobación tuya, porque suma peso.
- **Imágenes:** vectoriales (`ImageVector` / XML). Nada de descargas.
- **Estado:** cada pantalla conserva su `ViewModel` con `StateFlow`; el rediseño cambia la capa de composición, no la lógica.
- **Modularización de UI:** componentes comunes en `core/ui`; cada funcionalidad conserva su carpeta.
- **Previsualizaciones** (`@Preview`) en claro, oscuro y fuente grande para los componentes.
- **Pruebas de interfaz:** ampliar las 4 instrumentadas actuales con las pantallas nuevas; pruebas de capturas (golden) si se acepta añadir una herramienta para ello.
- **Rendimiento:** medir el arranque y la fluidez en el teléfono más modesto; sin recomposiciones innecesarias.

## 9. Etapas (cada una termina con test, lint y assembleDebug en verde)

- **U0 Sistema de diseño (hecha, 15-oct).** Paleta bíblica con prueba de contraste, títulos en serifa, formas y espaciado (`core/theme`), componentes base en `core/ui` (tarjeta, botones, chip de cita, barra animada, insignia de nivel, retroalimentación) y catálogo de componentes accesible desde Ajustes solo en debug. Los iconos propios llegan con U2.
- **U1 Navegación y estructura (hecha, 15-oct).** Barra inferior con los colores del tema, transiciones entre pantallas (atenuación y desplazamiento breve, apagadas si el sistema quita las animaciones) y componentes comunes de estado de carga y estado vacío (`core/ui/Estados.kt`).
- **U2 Inicio y mapa de niveles (hecha, 15-oct).** Inicio con la planta del nivel, barra animada y puntos del reto diario; pantalla «Tus niveles» con los 10 niveles, su planta, lo que desbloquean y si ya se alcanzaron (se abre tocando la tarjeta del nivel). Las diez plantas son vectores propios dibujados en Compose (`core/ui/PlantasNivel.kt`). Falta el jardín como fondo y la animación de crecimiento, que van con U3 (subida de nivel).
- **Splash (hecho, 15-oct).** Pantalla de apertura propia (`core/splash/SplashBiblico.kt`) que reproduce el video que el usuario generó con Gemini, recortado a 5,6 s, sin audio y comprimido (`res/raw/splash_video.mp4`, unos 570 KB), con el reproductor del sistema (sin librerías nuevas). Sale en cada arranque en frío, se salta con un toque, se funde a oscuro al terminar y no se muestra si el sistema tiene las animaciones apagadas. El video cubre toda la pantalla: en pantallas más altas que 9:16 se amplía y se recortan un poco los lados (unos 10 % por lado en 20:9), sin franjas; entra con un fundido desde oscuro. (Una primera versión con franjas de relleno se descartó: se veían bandas oscuras al empezar.) Primero se probó una versión dibujada en código (pergamino vertical) y se descartó por no igualar la calidad del video. El título del video dice «escritura» con minúscula: se corrige regenerando el video con «Dice la Escritura» y reemplazando el archivo.
- **U3 Partida.** Tarjeta de pregunta, retroalimentación, ordenar con arrastre, resultado, animación de subida de nivel.
- **U4 Reto diario y racha.** Indicadores, lámpara y celebración.
- **U5 Memorizar y práctica.** Tarjetas, arrastrar palabras, resaltado de resultados.
- **U6 Progreso, logros y perfil.** Gráficos nuevos y logros sobrios.
- **U7 Accesibilidad y pulido.** Revisión con lector de pantalla, fuente grande, horizontal y modo oscuro; ajuste de rendimiento.

Tras cada etapa, tú pruebas en el teléfono y me dices qué ajustar.

## 10. Orden respecto a la cuenta local

Conviene hacer primero C1 y C2 del plan de la cuenta local (el modelo y el perfil), porque Inicio, Perfil y la bienvenida del rediseño usan el nombre y el avatar. Después U0 y U1 pueden avanzar en paralelo con las demás etapas de la cuenta.

## 11. Qué decisiones necesito de ti

1. Tono visual: ¿más cálido y clásico (papel, dorado, serifas) o más moderno y limpio?
2. ¿Aceptas el jardín de plantas como identidad de los niveles?
3. ¿Háptica y sonidos? (recomendado: háptica discreta, sin sonido).
4. ¿Logros sí o no? (recomendado: sí, pocos y sobrios).
5. ¿Material propio (logotipo, ilustraciones) que debamos usar?
