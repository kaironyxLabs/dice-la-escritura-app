# Lámpara

App Android en español para aprender la Biblia un poco cada día, sin conexión. Niveles de conocimiento, partidas por categoría, reto diario con repetición espaciada y memorización de versículos, todo basado en la Reina-Valera 1960.

Uso personal y de un círculo cercano: se instala por APK, no se publica en Play Store. No usa internet, anuncios, analíticas ni cuentas; el progreso vive en el teléfono y se respalda exportándolo a un archivo.

## Qué incluye
- 10 niveles (de Semilla a Cedro) con examen de ascenso (10 preguntas, 80 % para subir). Los umbrales viven en `assets/content/levels.json`.
- Partidas de 10 preguntas por categoría: libros y orden, historia, personajes, lugares, enseñanzas de Jesús, cartas y "¿dónde está escrito?". Opción múltiple, verdadero o falso y ordenar.
- Reto diario de 10 preguntas (nuevas y de repaso vencido), racha con un día de gracia por semana y recordatorio opcional.
- Repetición espaciada Leitner de 5 cajas (1, 2, 4, 8 y 16 días).
- Memorizar unos 100 pasajes clave: completar con 20, 40 y 60 % de palabras ocultas y escribiendo el pasaje completo.
- Módulo opcional de la Confesión Bautista de Fe de 1689, apagado por defecto y sin rastro en la app mientras lo esté.
- Progreso con estadísticas, exportar e importar el progreso en JSON y ajustes.
- Más de 330 preguntas base y 97 de la Confesión, todas **pendientes de tu revisión** (`reviewed: false`).

## Estado
Hitos H0 a H8 completos. El texto bíblico del PDF está revisado: el importador termina sin anomalías (66 libros, 1.189 capítulos, 31.104 versículos) con las correcciones que dictó el usuario desde su edición impresa. Pendiente de tu parte (ver `docs/`): revisar las preguntas (`docs/CONTENIDO.md`) y probar la app en un teléfono. La interfaz aún no se ha probado en un dispositivo real.

## Compilar
Requiere JDK 17 y el Android SDK (ver `docs/INSTALACION.md`).

```
./gradlew test lint assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/`.

## Texto bíblico
El texto de la RVR1960 tiene derechos de autor y no está en el repositorio. Se genera localmente desde un PDF que aporta el usuario con `tools/importar_biblia`; ver `docs/TEXTO_BIBLICO.md`.

## Documentación
- `docs/INSTALACION.md`: entorno, compilación, firma, instalación y cambio de teléfono.
- `docs/CONTENIDO.md`: cómo agregar y revisar preguntas.
- `docs/TEXTO_BIBLICO.md`: importación del texto bíblico y su procedimiento de correcciones.
- `docs/DECISIONES.md`: decisiones de diseño y sus motivos.
- `docs/REVISION_PREGUNTAS.md`: informe de revisión con casillas (generado con `tools/contenido/informe_revision.py`).
- `docs/licencias/`: licencias de las fuentes empaquetadas.
- `CLAUDE.md`: guía de trabajo del repositorio (comandos, convenciones y reglas de contenido).
