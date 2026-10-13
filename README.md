# Lámpara

App Android en español para aprender la Biblia un poco cada día, sin conexión. Niveles de conocimiento, partidas por categoría, reto diario con repetición espaciada y memorización de versículos, todo basado en la Reina-Valera 1960.

Uso personal y de un círculo cercano: se instala por APK, no se publica en Play Store. No usa internet, anuncios, analíticas ni cuentas.

## Estado
En construcción por hitos (H0 a H8). Ver `PROMPT_INICIAL_CLAUDE_CODE.md` y `docs/DECISIONES.md`.

## Compilar
Requiere JDK 17 y el Android SDK (ver `docs/INSTALACION.md`).

```
./gradlew test lint assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/`.

## Texto bíblico
El texto de la RVR1960 tiene derechos de autor y no está en el repositorio. Se genera localmente desde un PDF que aporta el usuario; ver `docs/TEXTO_BIBLICO.md`.

## Documentación
- `docs/INSTALACION.md`: entorno, compilación, firma e instalación.
- `docs/CONTENIDO.md`: cómo agregar y revisar preguntas.
- `docs/TEXTO_BIBLICO.md`: importación del texto bíblico.
- `docs/DECISIONES.md`: decisiones de diseño y sus motivos.
- `docs/licencias/`: licencias de las fuentes empaquetadas.
