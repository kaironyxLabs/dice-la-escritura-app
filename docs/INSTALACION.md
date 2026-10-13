# Instalación y compilación

## Requisitos
- JDK 17 o superior (`JAVA_HOME` apuntando a él; el `java` del PATH puede ser distinto).
- Android SDK con plataforma 37 y build-tools. Crear `local.properties` en la raíz (lo ignora git):
  `sdk.dir=C\:\\Users\\<usuario>\\AppData\\Local\\Android\\Sdk`
- Python 3.12 solo para el importador del texto bíblico (desde H4), en un entorno virtual dentro de `tools/importar_biblia`.

## Compilar
```
./gradlew test lint assembleDebug
```
APK en `app/build/outputs/apk/debug/app-debug.apk`.

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
3. `./gradlew assembleRelease`. El APK queda en `app/build/outputs/apk/release/`.

El keystore y `keystore.properties` nunca se suben al repositorio. Hay que guardar una copia del keystore: sin él no se pueden publicar actualizaciones instalables sobre la misma app.

## Instalar en el teléfono
Android 8 (API 26) o superior. Copiar el APK al teléfono y abrirlo (permitir instalar apps de orígenes desconocidos), o con `adb install -r app-debug.apk`.
