# Decisiones

Registro de decisiones técnicas y de producto. Las de la sección 2 del prompt inicial son fijas y no se repiten aquí.

## D-001 Id de paquete provisional
`com.example.lampara`. Para cambiarlo: `namespace` y `applicationId` en `app/build.gradle.kts`, las carpetas bajo `app/src/*/java/com/example/lampara`, las declaraciones `package`/`import` y las reglas de `app/proguard-rules.pro`.

## D-002 Versiones (2026-10-13)
AGP 9.4.1, Gradle 9.8.1, Kotlin 2.4.21, KSP 2.3.12, Hilt 2.60.1, Room 2.8.5, Compose BOM 2026.09.00, Navigation 2.10.2, DataStore 1.2.1 (la 1.3.0 solo está en RC), WorkManager 2.12.0. Se eligió la última versión estable de cada librería; todas viven en `gradle/libs.versions.toml`. compileSdk y targetSdk 37.

## D-003 Navegación con rutas tipadas
Navigation Compose con `@Serializable` en vez de rutas de texto: evita errores de argumentos y reutiliza kotlinx.serialization, que ya es dependencia.

## D-004 Tema propio, sin colores dinámicos
Paleta de pergamino, tinta y teal con ámbar de acento, claro y oscuro, con pares de contraste AA. No se usa Material You para que la identidad sea la misma en todos los teléfonos.

## D-005 Fuentes empaquetadas
Inter (interfaz) y Source Serif 4 (pasajes), fuentes variables bajo licencia OFL 1.1, en `res/font`. Licencias en `docs/licencias/`.

## D-006 Sin copia de seguridad en la nube
`allowBackup=false` y reglas de extracción vacías: el progreso se respalda solo con exportar/importar JSON, coherente con "sin cuentas ni nube".

## D-007 Firma de release
`keystore.properties` (ignorado por git) define `storeFile`, `storePassword`, `keyAlias` y `keyPassword`. Si no existe, el release se compila sin firmar.

## D-008 Texto bíblico y repositorio
El PDF se queda en la raíz del repositorio (ignorado por git). El importador recibirá la ruta por argumento, con ese archivo como valor por defecto. Lo que necesita la app es la carpeta `assets/bible/libros/` (un JSON por libro), generada en local y empaquetado en el APK al compilar; nunca va a git.

## D-009 Importador: no corrige nada solo
El texto de la RVR1960 sale de un PDF no oficial con defectos. El importador asigna libros por orden, extrae versículos por secuencia, compara con un conteo independiente (KJV, solo números) y lo informa todo; las correcciones salen únicamente de `fuentes/correcciones.json`. Si queda algo sin resolver, escribe el JSON con esos versículos como pendientes y termina con código 3, de modo que la app nunca muestra un versículo roto.

## D-010 Marcadores de versículo dudosos
Un número dentro de un renglón solo se toma como marcador si es el esperado en la secuencia. Si no lo delata su entorno (tras puntuación, o antes de mayúscula, paréntesis o signo de apertura), se acepta únicamente cuando el mismo número no vuelve a aparecer después como marcador claro. Es la regla que separó bien los 31.104 versículos del PDF; los casos restantes los detecta la comparación de conteos.

## D-011 Espacios perdidos del PDF
El PDF pierde el espacio tras algunas vocales acentuadas (palabras pegadas). El importador los detecta con una heurística (palabra funcional pegada a una raíz que aparece suelta; se descartan los imperfectos en -ía) y deja sugerencias en `fuentes/correcciones_sugeridas.json`. Nunca se aplican sin que el usuario las copie a `correcciones.json`.

## D-012 Memorizar versículos
Sube de grado al acertar (20, 40, 60 % de palabras ocultas y 100 % escribiendo). Se aprueba con el 90 % de las palabras, ignorando mayúsculas, tildes y puntuación, con la subsecuencia común más larga para que una palabra olvidada no desplace las demás; cada palabra de más sobre las esperadas resta un acierto, para que copiar el pasaje entero no apruebe un grado con huecos. Al acertar escribiendo, el repaso se aleja (1, 3, 7, 14, 30 y 60 días), pero solo si el repaso ya tocaba: practicar antes de tiempo no suma dominio; un fallo baja un grado y vuelve a mañana. Solo se admiten pasajes de un capítulo. Los pasajes con un versículo pendiente se muestran como no disponibles.

## D-013 Esquema de Room v1 abierto
Hasta que la app se instale por primera vez en un teléfono, el esquema v1 puede seguir cambiando (se agregaron reto diario y progreso de versículos sin migraciones). Desde la primera instalación, cada cambio exige subir la versión y escribir su migración.

## D-014 Un solo perfil por teléfono
La app es de uso personal: un perfil local por teléfono (nombre y color) que agrupa avance, logros y estadísticas. No se implementa C4 (varios perfiles); las preferencias siguen en DataStore global. Si algún día hiciera falta, el modelo (`perfilId` en todas las tablas) ya lo permite.
