# Plan de desarrollo: lo que falta para terminar Lámpara

Fecha: 14/10/2026. Este documento reúne lo pendiente, ordenado en etapas. Cada etapa dice qué se hace, cómo se sabe que está terminada y qué necesita de ti. Los planes detallados de la cuenta local y del rediseño visual están en `docs/PLAN_CUENTA_LOCAL.md` y `docs/PLAN_REFACTOR_UI.md`; el manual de lo que la app hace hoy está en `docs/MANUAL_USUARIO.md`.

## 0. Dónde estamos

**Hecho**
- Hitos H0 a H8: app completa en lo funcional (niveles, partidas, reto diario, racha, repetición espaciada, memorizar, progreso, ajustes, recordatorio, exportar e importar, módulo de la Confesión).
- Texto de la RVR1960 cerrado, igualado a BibleGateway; la app lo lee por libro.
- 473 preguntas (376 base y 97 de la Confesión), contrastadas con el texto local de la RVR1960 y con tu PDF de la Confesión, y marcadas como revisadas por ti.
- Tras cada respuesta la app muestra la respuesta correcta, su justificación bíblica y la invitación a comprobarla en la Biblia física (impresa).
- 160 pruebas JVM, 11 instrumentadas, importador con 87 pruebas; lint sin avisos; build de release con R8.
- Probada a mano en dos teléfonos (Xiaomi y TCL). Última versión instalada en el Xiaomi, con el avance en cero, para tu prueba manual.
- Repositorio privado en GitHub (`kaironyxLabs/lampara-app`) con CI.

**No está hecho**
- Tu prueba manual exhaustiva de la versión instalada (en curso).
- Cuenta local con nombre y avatar, y todo lo que cuelga de ella.
- Rediseño visual y de interacción.
- Firma y entrega de la versión de release.
- Varios temas de contenido y calidad (abajo).

## 1. Etapa A: cerrar lo que ya existe (prioridad inmediata)

1. **Resultado de tu prueba manual.** Anota cada hallazgo (qué hiciste, qué esperabas, qué pasó y en qué pantalla). Los agrupo y los corrijo por lotes. Criterio de cierre: una pasada completa sin errores de contenido ni de flujo.
2. **CI en verde.** El primer push falló por un detalle de permisos de `gradlew` en Windows (corregido). Hay que confirmar que `test`, `lint`, release y la verificación del manifiesto sin INTERNET pasan en GitHub.
3. **Pruebas en el TCL** tras tu OK (hoy solo el Xiaomi tiene la versión nueva). Incluye `connectedDebugAndroidTest`.
4. **Pasajes de Memorizar.** Los 108 pasajes de `memorize.json` siguen con `reviewed: false`. Decide si los apruebas en bloque o los revisas.
5. **Texto de Ajustes** "Mostrar solo preguntas revisadas": ya no tiene sentido decir que está desactivado "mientras el contenido siga pendiente de revisión". Se actualiza.

## 2. Etapa B: calidad y contenido

1. **Más contenido para los niveles altos.** Hoy los niveles 6 a 10 se distinguen poco: hay pocas preguntas de dificultad 4 y 5. Meta: al menos 25 preguntas de dificultad 4 y 5 por categoría. Regla: cada pregunta nace del pasaje leído en la RVR1960 (o del PDF de la Confesión), no de memoria.
2. **Preguntas duplicadas o muy parecidas.** Lista en `docs/REVISION_PREVIA.md`: unificar o diferenciar.
3. **Sesgo de longitud en el módulo base.** Es menor que antes, pero queda; se revisa por categorías.
4. **Confesión:** cruzar los versículos de apoyo de cada pregunta con las notas al pie de tu PDF.
5. **Un lector estricto del contenido en CI:** que un test falle si una pregunta cita una referencia que no existe en el texto local (hoy lo valida solo la estructura).
6. **Más pasajes para Memorizar** si quieres ampliar de 100 a una lista temática mayor.

## 3. Etapa C: cuenta local (nombre, avatar, datos del avance)

Detalle en `docs/PLAN_CUENTA_LOCAL.md`. Resumen:
- Perfil local sin login: nombre, avatar y fecha de inicio, guardados en el teléfono.
- Posibilidad de más de un perfil en el mismo teléfono (decisión tuya).
- Todo el avance cuelga del perfil y viaja en la exportación.
- **Debe hacerse antes de congelar la base de datos.** La decisión D-013 dice que el esquema de Room sigue abierto hasta la primera instalación "de verdad". Si lo congelamos antes de añadir el perfil, cada cambio necesitará una migración.

## 4. Etapa D: rediseño visual y de interacción

Detalle en `docs/PLAN_REFACTOR_UI.md`. Resumen:
- Sistema de diseño propio (colores, tipografía, formas, iconografía).
- Ilustraciones por nivel y categoría, animaciones y mayor interactividad.
- Pantalla de inicio como panel de avance; mapa de niveles.
- Mejoras de práctica de memorización (arrastrar palabras, tarjetas).
- Todo con pruebas de interfaz y revisión de accesibilidad.

## 5. Etapa E: entrega

1. **Firma de release.** El keystore ya existe; falta que respaldes `lampara.jks` y `keystore.properties` fuera del teléfono y del repo.
2. **`assembleRelease`** firmado, instalado y probado en un teléfono (con R8 activo, porque puede cambiar el comportamiento).
3. **Versión y nombre.** Definir número de versión (`versionName`) y decidir si el id de paquete provisional (`com.example.lampara`) se cambia ahora o nunca. **Cambiarlo después de que otras personas instalen la app obliga a que desinstalen y pierdan su avance.** Lo recomendable es decidirlo antes de repartir el APK.
4. **Instrucciones de instalación para tu círculo cercano** (APK + cómo permitir "fuentes desconocidas" + cómo exportar el avance).
5. **Plan de actualizaciones:** cómo se publican versiones nuevas sin perder datos (migraciones de Room probadas).

## 6. Riesgos y decisiones abiertas

| Tema | Riesgo | Qué hacer |
|---|---|---|
| Id de paquete provisional | Cambiarlo tarde borra el avance de todos | Decidir antes de repartir el APK |
| Esquema de Room | Cada cambio tras la primera instalación exige migración | Meter el perfil local ahora |
| Texto con derechos | Subirlo por error al repo o a un APK público | `fuentes/` y `assets/bible/libros/` ya están ignorados; el APK que repartas **lleva dentro el texto de la RVR1960**, así que repártelo solo a personas de confianza |
| Backup | Desinstalar borra todo | Exportar con regularidad; evaluar copia automática a una carpeta elegida |
| Rediseño grande | Romper lo que ya funciona | Hacerlo por pantallas, con las pruebas actuales en verde en cada paso |
| Contenido | Un error de contenido pasa desapercibido | Verificación contra fuente y la invitación a contrastar en la Biblia física (impresa) |

## 7. Orden sugerido

1. Etapa A (prueba manual y arreglos).
2. Etapa C (cuenta local), porque toca el esquema de datos.
3. Etapa B en paralelo, en los ratos de revisión.
4. Etapa D (rediseño), pantalla por pantalla.
5. Etapa E (entrega).

## 8. Lo que necesito de ti

- Resultado de tu prueba manual.
- Decisiones: ¿un solo perfil o varios? ¿id de paquete definitivo? ¿aprobamos los 108 pasajes de memorizar?
- Material gráfico si tienes alguno que quieras usar (logotipo, imágenes). Si no, las ilustraciones se dibujan como gráficos vectoriales propios.
- Autorización para usar el TCL cuando quieras que lo pruebe.
