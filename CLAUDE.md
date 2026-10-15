# Dice la Escritura: guía para el trabajo en este repositorio

App Android en español, 100 % sin conexión, para aprender la Biblia (RVR1960) por niveles. Especificación completa en `PROMPT_INICIAL_CLAUDE_CODE.md`. Decisiones tomadas en `docs/DECISIONES.md`.

## Comandos
Usar JDK 17 (el `java` del PATH puede ser otro):
```
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot   (cmd)
./gradlew test lint assembleDebug      # debe pasar antes de cada commit de hito
./gradlew assembleRelease              # R8 activo; firma desde keystore.properties
```
`local.properties` (ignorado por git) apunta al SDK. No hay ANDROID_HOME definido.

## Importador del texto bíblico (tools/importar_biblia)
Python 3.12 en un entorno virtual propio del proyecto (`tools/importar_biblia/.venv`, ignorado por git):
```
cd tools/importar_biblia
.venv\Scripts\python -m importar_biblia       # PDF -> fuentes/rvr1960.json + app/src/main/assets/bible/libros/<ID>.json + informe
.venv\Scripts\python -m pytest                # pruebas con un PDF sintético y texto falso
```
Salida 0 = todo resuelto; 2 = estructura o correcciones inválidas; 3 = anomalías sin resolver (el usuario las resuelve
en `fuentes/correcciones.json`). El informe `docs/TEXTO_BIBLICO_informe.md` contiene fragmentos del texto y no se
sube. Detalle completo en `docs/TEXTO_BIBLICO.md`.

## Contenido y revisión (tools/contenido)
- `python tools/contenido/informe_revision.py`: genera `docs/REVISION_PREGUNTAS.md` con casillas para que el usuario revise.
- `python tools/contenido/aplicar_revision.py`: pasa a `reviewed: true` solo lo que el usuario marcó. Claude nunca ejecuta esto ni marca `true` por su cuenta.
- Las pruebas de contenido (`ContenidoCompletoTest` y otras) validan mínimos, referencias y versículos citados; corren con `./gradlew test`.

## Stack y convenciones
- Kotlin + Compose Material 3, una Activity, Navigation Compose con rutas tipadas (`core/navigation/Destinos.kt`).
- MVVM (ViewModel + StateFlow), Hilt, Room, DataStore, WorkManager, kotlinx.serialization.
- Un módulo `app`, paquetes por funcionalidad bajo `com.kaironyx.diceescritura`: `core`, `data`, `domain`, `feature`.
- `domain` es Kotlin puro, sin dependencias de Android. Fechas con `java.time` y un `Clock` inyectado.
- Todo texto visible va en `strings.xml`. Sin permiso INTERNET, sin analíticas, sin cuentas.
- Versiones solo en `gradle/libs.versions.toml`.
- Commits pequeños en español con prefijo (`feat:`, `fix:`, `docs:`, `test:`, `chore:`).
- **Regla permanente:** antes de cada merge (o de preparar un PR para merge) se actualiza el `README.md` global para que refleje el estado real del proyecto (funcionalidades, hitos, comandos, pendientes y enlaces a docs), en un commit `docs:` de la misma rama. Lo refuerza el hook `tools/hooks/pre-merge-commit`, que rechaza un merge que no toque el README (se activa con `git config core.hooksPath tools/hooks` y `git config merge.ff false`; se salta a conciencia con `--no-verify`).
- Los commits, push, PR y merge NO llevan atribución a Claude (sin `Co-Authored-By` ni "Generated with").

## Reglas de contenido (críticas)
1. El texto de la RVR1960 tiene derechos. No se escribe de memoria, no va en docs ni pruebas, ni a ningún remoto. Solo sale del archivo que aporta el usuario. `biblia_rv1960.pdf`, `fuentes/` y `app/src/main/assets/bible/libros/` están en `.gitignore`.
2. Preguntas y explicaciones citan referencias y parafrasean; no transcriben versículos.
3. Preguntas base verificables con un pasaje concreto y sin posturas denominacionales.
4. La Confesión Bautista de 1689 va en módulo aparte tras la bandera `CONFESION_1689` (apagada por defecto); preguntas originales.
5. Todo contenido nuevo lleva `reviewed: false`. Nunca marcar `true`: lo revisa el usuario.
6. No inventar datos: si una referencia o hecho es dudoso, se descarta la pregunta.
7. El texto bíblico solo se corrige desde `fuentes/correcciones.json`, nunca de memoria.
8. Las fuentes del usuario son la base de todo el contenido: el PDF de la RVR1960 (`fuentes/rvr1960.json`) y el PDF de la Confesión de 1689 (`fuentes/confesion/lbcos.pdf`), ambos ignorados por git. Toda pregunta nueva o modificada se deriva del texto de esas fuentes y, después de escribirla, se lee el pasaje citado para comprobar que lo sostiene. Que la cita exista no basta. No se afirma «verificado» si solo se comprobó eso.

## Release
`./gradlew assembleRelease` aplica R8 (reglas en `app/proguard-rules.pro`). El manifiesto no debe declarar INTERNET (`ManifiestoTest` y el CI lo verifican). El keystore y `keystore.properties` nunca van al repositorio.

## Dispositivos
No ejecutar nada en teléfono ni emulador sin que el usuario lo indique. Pruebas JVM y build no lo necesitan.
