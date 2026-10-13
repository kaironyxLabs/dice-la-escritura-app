# Instalación y compilación

## Requisitos
- JDK 17 o superior (`JAVA_HOME` apuntando a él; el `java` del PATH puede ser distinto).
- Android SDK con plataforma 37 y build-tools. Crear `local.properties` en la raíz (lo ignora git):
  `sdk.dir=C\:\\Users\\<usuario>\\AppData\\Local\\Android\\Sdk`
- Python 3.12 solo para las herramientas (`tools/`), en un entorno virtual:
  ```
  cd tools/importar_biblia
  python -m venv .venv
  .venv\Scripts\python -m pip install -r requirements.txt
  ```

## Antes de compilar: el texto bíblico
El texto de la RVR1960 no viene en el repositorio. Sin `app/src/main/assets/bible/rvr1960.json` la app compila y funciona, pero el modo memorizar avisa que falta el texto. Para generarlo desde tu PDF, sigue `docs/TEXTO_BIBLICO.md`. El archivo se empaqueta en tu APK al compilar y nunca se sube a git.

## Compilar
```
./gradlew test lint assembleDebug
```
APK en `app/build/outputs/apk/debug/app-debug.apk`. Para el APK de uso diario, firma de release (abajo).

Otros comandos:
- `./gradlew assembleRelease`: APK de release con R8 (sin firma si no hay `keystore.properties`).
- `./gradlew connectedDebugAndroidTest`: pruebas de humo de la interfaz (requieren un dispositivo o emulador).
- `cd tools/importar_biblia && .venv\Scripts\python -m pytest`: pruebas del importador. `python -m pytest tools/contenido`: pruebas de los scripts de revisión.

## Firma de release
1. Crear un keystore fuera del repositorio:
   `keytool -genkeypair -v -keystore lampara.jks -alias lampara -keyalg RSA -keysize 2048 -validity 10000`
2. Crear `keystore.properties` en la raíz (ignorado por git):
   ```
   storeFile=../lampara.jks
   storePassword=...
   keyAlias=lampara
   keyPassword=...
   ```
3. `./gradlew assembleRelease`. El APK queda en `app/build/outputs/apk/release/app-release.apk`.

El keystore y `keystore.properties` nunca se suben al repositorio. Guarda una copia del keystore: sin él no podrás actualizar la app instalada con otra versión firmada con la misma clave (tendrías que desinstalar y perder el progreso, salvo que lo hayas exportado).

## Instalar en el teléfono
Android 8 (API 26) o superior. Copia el APK al teléfono y ábrelo (permite instalar apps de orígenes desconocidos), o usa `adb install -r app-release.apk`.

La app no usa internet: el manifiesto no declara el permiso `INTERNET` (una prueba y el CI lo verifican). Solo pide, en contexto y si activas el recordatorio, el permiso de notificaciones.

## Cambiar de teléfono
En el teléfono viejo: Ajustes, Copia del progreso, Exportar progreso. Pasa el archivo al nuevo, instala la app y usa Importar progreso. El importar reemplaza el progreso del teléfono, incluidos la racha, los repasos, los versículos que memorizas y los ajustes de contenido y recordatorio.

## Cambiar el id de paquete
Ver `docs/DECISIONES.md` (D-001).
