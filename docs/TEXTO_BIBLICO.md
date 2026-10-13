# Texto bíblico (RVR1960)

> Esqueleto de H0. Se completa en H4 con el formato del JSON, los SHA-256 y el procedimiento.

El texto de la RVR1960 tiene derechos de autor. No está en el repositorio ni debe subirse a ningún remoto.

- Fuente: `biblia_rv1960.pdf` en la raíz del proyecto (aportado por el usuario, ignorado por git).
- Salida: `app/src/main/assets/bible/rvr1960.json`, generado en local por `tools/importar_biblia` (ignorado por git).
- Correcciones: solo desde `fuentes/correcciones.json`, con texto verificado por el usuario.

Si el archivo de salida falta, las pantallas que dependen del texto lo avisan y el resto de la app funciona.
