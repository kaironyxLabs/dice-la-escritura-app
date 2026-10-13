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
El PDF se queda en la raíz del repositorio (ignorado por git). El importador recibirá la ruta por argumento, con ese archivo como valor por defecto. Lo que necesita la app es `assets/bible/rvr1960.json`, generado en local y empaquetado en el APK al compilar; nunca va a git.
