# Lámpara: guía para el trabajo en este repositorio

App Android en español, 100 % sin conexión, para aprender la Biblia (RVR1960) por niveles. Especificación completa en `PROMPT_INICIAL_CLAUDE_CODE.md`. Decisiones tomadas en `docs/DECISIONES.md`.

## Comandos
Usar JDK 17 (el `java` del PATH puede ser otro):
```
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot   (cmd)
./gradlew test lint assembleDebug      # debe pasar antes de cada commit de hito
./gradlew assembleRelease              # R8 activo; firma desde keystore.properties
```
`local.properties` (ignorado por git) apunta al SDK. No hay ANDROID_HOME definido.

## Stack y convenciones
- Kotlin + Compose Material 3, una Activity, Navigation Compose con rutas tipadas (`core/navigation/Destinos.kt`).
- MVVM (ViewModel + StateFlow), Hilt, Room, DataStore, WorkManager, kotlinx.serialization.
- Un módulo `app`, paquetes por funcionalidad bajo `com.example.lampara` (provisional): `core`, `data`, `domain`, `feature`.
- `domain` es Kotlin puro, sin dependencias de Android. Fechas con `java.time` y un `Clock` inyectado.
- Todo texto visible va en `strings.xml`. Sin permiso INTERNET, sin analíticas, sin cuentas.
- Versiones solo en `gradle/libs.versions.toml`.
- Commits pequeños en español con prefijo (`feat:`, `fix:`, `docs:`, `test:`, `chore:`).
- Los commits, push, PR y merge NO llevan atribución a Claude (sin `Co-Authored-By` ni "Generated with").

## Reglas de contenido (críticas)
1. El texto de la RVR1960 tiene derechos. No se escribe de memoria, no va en docs ni pruebas, ni a ningún remoto. Solo sale del archivo que aporta el usuario. `biblia_rv1960.pdf`, `fuentes/` y `app/src/main/assets/bible/rvr1960.json` están en `.gitignore`.
2. Preguntas y explicaciones citan referencias y parafrasean; no transcriben versículos.
3. Preguntas base verificables con un pasaje concreto y sin posturas denominacionales.
4. La Confesión Bautista de 1689 va en módulo aparte tras la bandera `CONFESION_1689` (apagada por defecto); preguntas originales.
5. Todo contenido nuevo lleva `reviewed: false`. Nunca marcar `true`: lo revisa el usuario.
6. No inventar datos: si una referencia o hecho es dudoso, se descarta la pregunta.
7. El texto bíblico solo se corrige desde `fuentes/correcciones.json`, nunca de memoria.

## Dispositivos
No ejecutar nada en teléfono ni emulador sin que el usuario lo indique. Pruebas JVM y build no lo necesitan.
