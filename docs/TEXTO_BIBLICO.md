# Texto bíblico (RVR1960)

El texto de la Reina-Valera 1960 tiene derechos de autor. **No está en el repositorio** ni debe subirse a ningún remoto. Lo aporta el usuario en un PDF y una herramienta local lo convierte al archivo que lee la app.

| Archivo | Quién lo pone | ¿Va a git? |
|---|---|---|
| `biblia_rv1960.pdf` (raíz del proyecto, o `fuentes/`) | el usuario | no |
| `fuentes/correcciones.json` | el usuario | no |
| `app/src/main/assets/bible/rvr1960.json` | el importador | no |
| `docs/TEXTO_BIBLICO_informe.md` | el importador | no (contiene fragmentos del texto) |
| `fuentes/correcciones_sugeridas.json` | el importador | no |
| `docs/versiculos_por_capitulo.json` | `generar_conteo_esperado.py` | sí (solo números) |

El APK de uso personal lleva `rvr1960.json` dentro, porque el archivo se empaqueta al compilar desde tu carpeta local. Eso no obliga a subirlo a ningún repositorio.

## Procedimiento

Requiere Python 3.12 y un entorno virtual (se crea una sola vez):

```
cd tools/importar_biblia
python -m venv .venv
.venv\Scripts\python -m pip install -r requirements.txt
```

1. Genera el archivo:
   ```
   .venv\Scripts\python -m importar_biblia
   ```
   Por defecto lee `fuentes/biblia_rv1960.pdf` o, si no existe, `biblia_rv1960.pdf` de la raíz. Rutas alternativas: `--pdf`, `--salida`, `--correcciones`, `--esperado`, `--informe`.
2. Abre `docs/TEXTO_BIBLICO_informe.md`. Lista cada anomalía con su ubicación (libro, capítulo y versículo).
3. Resuelve cada anomalía en `fuentes/correcciones.json` (formato abajo), contrastando con tu edición impresa u otra fuente verificada.
4. Vuelve a ejecutar hasta que termine con "Todo resuelto" (código de salida 0).

Pruebas del importador (usan un PDF sintético con texto falso, nunca texto de la RVR1960):

```
cd tools/importar_biblia
.venv\Scripts\python -m pytest
```

## Qué hace y qué no hace

- Descarta la cabecera y el pie repetidos en cada página ("Santa Biblia – Versión Reina Valera 1960", "www.siguiendosuspisadas.com.ar"). Un salto de página no es un salto de párrafo.
- Un encabezado de capítulo es una línea "NOMBRE N". Los libros se asignan **por orden de aparición** contra la tabla de 66 libros y sus capítulos, no por el nombre escrito (el PDF trae erratas como "ECLESTIASTÉS").
- Los versículos van numerados en línea. Se extraen por secuencia (1, 2, 3...). Los saltos de numeración se registran. Un número dentro del texto ("130 años") no parte el versículo; los casos dudosos se deciden mirando si el mismo número aparece después como marcador claro.
- Se guardan **aparte** del versículo: títulos de sección, referencias a pasajes paralelos entre paréntesis (ej. `(1 R. 3.3-15)`) y el texto sin numerar antes del versículo 1 (sobrescritos de salmos, campo `encabezado`). Los encabezados en mayúsculas (nombre del libro, "LIBRO SEGUNDO" de los Salmos) se ignoran y quedan listados en el informe.
- Los párrafos sin número dentro de un capítulo (estrofas de poesía, continuaciones tras un salto de página) se unen al versículo anterior y se registran como informativos.
- Salvo unir las líneas con un espacio y colapsar espacios repetidos, **no modifica el texto**. Nunca corrige ni completa un versículo por su cuenta.

### Verificaciones obligatorias (fallan en voz alta)

1. 66 libros y 1.189 capítulos: si la estructura no coincide, el importador aborta con código 2 y no escribe nada.
2. Numeración sin huecos, salvo versículos marcados como pendientes.
3. Ningún versículo vacío fuera de los pendientes.
4. Ningún carácter fuera del conjunto permitido (letras españolas, dígitos, espacio y `. , ; : ¿ ? ¡ ! ( ) - – —`).
5. Conteo de versículos por capítulo igual al esperado (`docs/versiculos_por_capitulo.json`).

Si queda alguna anomalía sin resolver, el importador escribe igualmente el JSON, publica los versículos afectados como pendientes (la app no los muestra rotos), informa en rojo y termina con código **3**.

### Conteo esperado

`docs/versiculos_por_capitulo.json` sale de una fuente independiente y documentada: la King James Version (dominio público, repositorio `thiagobodruk/bible`), de la que solo se toman los **números** de versículos por capítulo, con el ajuste a la numeración de la RVR1960: en 3 Juan la RVR1960 numera 15 versículos (la KJV, 14). Se regenera con `python generar_conteo_esperado.py`. Cualquier otra diferencia entre la KJV y la RVR1960 aparecerá en el informe y la decides tú.

## `fuentes/correcciones.json`

Lo escribes tú, con el texto verificado en tu edición impresa u otra fuente.

```json
{
  "correcciones": [
    {"libro": "GEN", "capitulo": 30, "versiculo": 7, "texto": "<texto verificado>"},
    {"libro": "EXO", "capitulo": 2, "versiculo": 15,
     "reemplazos": [{"buscar": "<fragmento tal cual está>", "por": "<fragmento corregido>"}]},
    {"libro": "1CO", "capitulo": 1, "versiculo": 32, "renumerar_a": 31},
    {"libro": "PSA", "capitulo": 47, "versiculo": 10, "eliminar": true}
  ],
  "pendientes": [
    {"libro": "PSA", "capitulo": 39, "versiculo": 11, "motivo": "número dañado"}
  ],
  "aceptadas": [
    {"libro": "3JN", "capitulo": 1, "tipo": "conteo", "motivo": "la RVR1960 numera 15 versículos"}
  ]
}
```

- `libro` usa el id de tres caracteres (`GEN`, `EXO`, `1CO`... ver `TablaLibros.kt`).
- Cada corrección lleva exactamente **una** operación:
  - `texto`: reemplaza el versículo o lo inserta si falta.
  - `reemplazos`: cada `buscar` debe aparecer exactamente una vez en el versículo; si no, el importador se detiene.
  - `renumerar_a`: mueve el texto a otro número vacío (útil cuando el PDF numera mal).
  - `eliminar`: quita el versículo.
- `palabras`: reemplaza una palabra completa en toda la Biblia (para los espacios perdidos): `{"buscar": "estáen", "por": "está en"}`. Solo cambia la palabra entera, distingue mayúsculas (la forma con mayúscula inicial se aprueba aparte) y se aplica después de las correcciones por versículo. El informe registra cuántas veces se aplicó.
- `normalizaciones`: reglas generales sobre todos los versículos. Disponible: `"guion_con_espacio"` (une "Bet- el" en "Bet-el", nombres compuestos que el PDF partió).
- `notas`: corrige un título de sección o una referencia a pasajes paralelos (que se guardan aparte): `{"libro": "EXO", "capitulo": 39, "tipo": "seccion", "buscar": "(Ex. 35.10-19_", "por": "(Ex. 35.10-19)"}`. `buscar` debe aparecer exactamente una vez. Una referencia entre paréntesis que el PDF dejó mal cerrada se toma por título; tras corregirla pasa a pasajes paralelos.
- `pendientes`: el versículo queda sin texto utilizable hasta que lo verifiques. La app lo muestra como no disponible.
- `aceptadas`: la anomalía (por su `tipo` y, opcionalmente, `versiculo`) es correcta tal cual; solo para diferencias legítimas.
- Las correcciones se registran en el informe (sin copiar el texto).

`fuentes/correcciones_sugeridas.json` es solo una ayuda: propone cómo separar palabras pegadas. No se aplica nada de él; revisa cada entrada y copia a `correcciones.json` las que apruebes.

## Defectos conocidos del PDF

Copia no oficial hecha en Word en 2000, con texto extraíble. Confirmados por el informe en la importación del 13 de octubre de 2026:

- Génesis 30:6-8 (falta el v. 7 y el v. 8; resto dañado), Salmo 39:10-11 (carácter suelto y número dañado), 1 Corintios 1:31 (numerado como 32), Ezequiel 31:17-18 (fundidos), Éxodo 2:15 (dígito dentro de una palabra) y caracteres sueltos en Éxodo 39, 2 Samuel 4 y 8, Amós 5 y Filemón 1.
- **Salmo 47**: tiene 10 versículos en la edición del usuario (la KJV, usada para el conteo esperado, trae 9); el ajuste está en `generar_conteo_esperado.py`.
- **Acentos de "Él"**: el PDF escribe el pronombre "Él" sin tilde en mayúscula; la edición impresa del usuario sí la lleva. Es una diferencia de edición, no un daño. Se corrigieron solo las combinaciones donde "El" va seguido de un verbo o pronombre (es siempre pronombre) y el usuario las aprobó; "El mismo" se revisó caso por caso porque puede ser artículo ("El mismo día").
- **Títulos y referencias pegados**: en 2 Crónicas (y un título con punto en 1 Crónicas 6:49) el PDF deja el título y su referencia paralela en dos líneas seguidas sin línea en blanco; el importador las separa como título y paralelo del versículo siguiente. El verificador marca como aviso cualquier referencia paralela, guion con espacio o palabra repetida que quede dentro del texto de un versículo.
- **Espacios perdidos**: el PDF pierde el espacio tras algunas vocales acentuadas, de modo que aparecen palabras pegadas ("estáen", "Asíque"). Es visible en la propia página del PDF, no es un fallo de la extracción. El informe los lista; hay más de 300 versículos afectados.

## Formato de `rvr1960.json`

```json
{
  "traduccion": "RVR1960",
  "libros": [
    {
      "id": "GEN",
      "capitulos": [
        {
          "numero": 1,
          "encabezado": null,
          "versiculos": ["...", "..."],
          "secciones": [{ "antesDelVersiculo": 1, "titulo": "..." }],
          "paralelos": [{ "antesDelVersiculo": 1, "texto": "..." }],
          "pendientes": []
        }
      ]
    }
  ]
}
```

- El versículo *n* es `versiculos[n - 1]`. Un hueco o pendiente va como cadena vacía y su número figura en `pendientes`.
- `encabezado` es el texto sin numerar antes del versículo 1 (sobrescritos de salmos), o `null`.
- `secciones` y `paralelos` indican ante qué versículo van.

## En la app

`BibleTextSource` es una interfaz; su implementación lee `assets/bible/rvr1960.json`. Si el archivo falta, las pantallas que dependen del texto (memorizar versículos) lo explican y apuntan a este documento; el resto de la app sigue funcionando. Las pruebas de la app usan un fixture claramente falso ("Texto de prueba, no es RVR1960") solo en las fuentes de test.

## Última importación

<!-- importacion:inicio -->
- Última importación: 2026-10-13
- SHA-256 del PDF (`biblia_rv1960.pdf`): `a750b2f89e73d9043892cd2ca9c1ed068bacf09049c96c334612b83bbd939f50`
- SHA-256 del JSON (`rvr1960.json`): `510c32a4520f5cde293158c658a0cc0f184c6823490aee625d9db3ef3902aa3d`
- Libros: 66; capítulos: 1189; versículos: 31104
- Correcciones aplicadas: 507; versículos pendientes: 0
- Anomalías sin resolver: 0
<!-- importacion:fin -->
