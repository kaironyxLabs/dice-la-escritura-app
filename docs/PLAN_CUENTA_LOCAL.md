# Plan: cuenta local (nombre, avatar y datos del avance)

Objetivo: que cada persona tenga en la app un perfil propio (nombre y avatar) donde se guarde todo su avance, **sin login, sin servidor y sin internet**. La "cuenta" es solo un perfil local en el teléfono.

## 1. Qué significa "cuenta local"

- No hay correo, contraseña ni registro. Nadie "inicia sesión".
- El perfil vive en el almacenamiento interno de la app, igual que el progreso actual.
- El perfil se puede exportar a un archivo y volver a importar en otro teléfono; ese archivo hace de "copia de la cuenta".
- Si desinstalas la app sin exportar, el perfil se pierde: es la consecuencia de no tener nube. Por eso el plan incluye mejores formas de respaldo (sección 7).

## 2. Lo que el perfil guarda

**Datos de identidad**
- Nombre para mostrar (por ejemplo, "Esteban"). Obligatorio, 1 a 30 caracteres.
- Avatar (sección 4).
- Fecha en que se creó el perfil (para mostrar "Estudiando desde…").

**Datos de avance (ya existen; pasan a colgar del perfil)**
- Nivel y XP.
- Seguimiento de cada pregunta (caja Leitner, fechas, aciertos y fallos).
- Historial de sesiones (partidas, exámenes, retos).
- Reto diario en curso y retos pasados.
- Pasajes que se memorizan, grado, dominio y fechas de repaso.

**Preferencias propias del perfil**
- Módulo de la Confesión activado o no.
- Recordatorio diario y su hora.
- "Mostrar solo preguntas revisadas".

Las preferencias que hoy están en DataStore pasan a ser por perfil, para que dos personas en el mismo teléfono no se pisen.

## 3. Decisión que necesito de ti: ¿uno o varios perfiles?

**Opción 1: un solo perfil por instalación (recomendada para empezar).**
- Es lo que usan hoy tú y tu círculo cercano: un teléfono, una persona.
- Cambio más pequeño y menos riesgo.
- Se puede ampliar a varios perfiles más tarde si el modelo de datos ya lo permite (ver abajo).

**Opción 2: varios perfiles en el mismo teléfono** (útil para familias o para ti y tu pareja).
- Una pantalla "¿Quién estudia?" al abrir la app, con los avatares.
- Cada perfil tiene su propio avance, racha, reto y ajustes.
- Más trabajo en pruebas: todo (reto diario, recordatorio, exportación) debe respetar el perfil activo.

Recomendación: construir el modelo de datos **ya preparado para varios perfiles** (cada tabla lleva el id del perfil), pero mostrar al principio solo uno. Así activar varios perfiles después no exige otra migración.

## 4. Avatar

Opciones, de menor a mayor esfuerzo:

1. **Avatares ilustrados incluidos en la app** (recomendado): un conjunto de 12 a 16 imágenes vectoriales (plantas, árboles, lámpara, paloma, libro, ancla…) coherentes con los niveles Semilla a Cedro. Sin permisos ni archivos externos, pesan casi nada y combinan con el rediseño.
2. **Inicial + color:** un círculo con la primera letra del nombre y un color a elegir. Como alternativa si no se elige ilustración.
3. **Foto de la galería** (opcional, más adelante): se elige con el selector de fotos del sistema, que no exige permisos; se copia recortada y reducida a la carpeta interna de la app.

Propuesta: 1 y 2 en la primera entrega; 3 solo si lo pides.

## 5. Diseño técnico

**Datos**
- Nueva tabla `perfil_usuario` (id, nombre, avatar, creado en, activo).
- Las tablas de progreso (`perfil`, `progreso_pregunta`, `sesion`, `reto_diario`, `progreso_versiculo`) pasan a llevar `perfilId`.
- Hoy la tabla `perfil` (nivel y XP) tiene una sola fila fija; se renombra o se unifica para no confundirla con el perfil de usuario.
- Preferencias por perfil: tabla `ajustes_perfil` (en lugar de claves globales de DataStore), o DataStore con una clave por perfil. Se prefiere la tabla para exportarla junto con el resto.
- Room pasa de la versión 1 a la 2 **con migración escrita y probada**: los datos actuales pasan al perfil 1 sin pérdida. (Nota: la instalación de prueba actual empieza de cero, así que la migración se prueba con una base de datos de versión 1 creada en una prueba instrumentada.)

**Código**
- `PerfilRepository` y un caso de uso `PerfilActivo` que expone el perfil activo como `StateFlow`.
- `ProgresoRepository`, `RetoDiarioRepository`, `MemorizacionRepository`, `AjustesRepository` y los trabajos de recordatorio reciben el perfil activo en lugar de asumir un perfil único.
- El dominio sigue siendo Kotlin puro; el perfil activo se inyecta desde la capa de datos.

**Exportación**
- El formato JSON sube de versión 1 a 2 e incluye `usuario` (nombre, avatar, creado).
- La importación acepta la versión 1 (sin usuario) y la versión 2. Si el archivo es de la 1, se crea un perfil con nombre por defecto y se le pide al usuario que lo ponga.
- La importación sigue siendo "reemplazar" y con confirmación.

**Privacidad**
- Sin permisos nuevos. Sin internet.
- Si algún día se añade foto, se usa el selector del sistema (sin permiso de almacenamiento).

## 6. Pantallas

1. **Bienvenida ampliada:** además de la elección del módulo de la Confesión, pide nombre y avatar. Debe poder saltarse con un nombre por defecto si alguien no quiere ponerlo.
2. **Saludo en Inicio:** "Buenos días, Esteban", el avatar y el nivel.
3. **Pantalla de perfil** (desde Ajustes o tocando el avatar): cambiar nombre y avatar, ver "Estudiando desde…", resumen de logros y botones de exportar e importar.
4. **Si hay varios perfiles:** selector al abrir, y opción para añadir, renombrar o eliminar un perfil (con doble confirmación; eliminar borra su avance).

## 7. Respaldo mejorado (opcional, dentro de esta etapa)

Como no hay nube, el riesgo real es perder el teléfono o desinstalar. Dos mejoras posibles:
- **Recordatorio de respaldo:** si pasan más de 30 días sin exportar, la app lo sugiere una vez al abrir.
- **Copia automática a una carpeta que tú elijas** (por ejemplo, en Documentos o en una carpeta sincronizada por otra app): una vez a la semana se escribe el archivo de progreso. La app solo escribe en esa carpeta, con permiso otorgado por ti mediante el selector de carpetas del sistema. No requiere internet.

## 8. Pruebas

- Migración 1 → 2 con datos reales simulados (nivel, XP, un reto, un pasaje).
- Pruebas JVM del dominio sin cambios y pruebas de repositorio con dos perfiles distintos.
- Exportar con la versión nueva e importar en un perfil vacío.
- Importar un archivo antiguo (versión 1).
- Instrumentadas: crear perfil en la bienvenida, cambiar de perfil, eliminarlo.

## 9. Etapas

1. **C1 Modelo y migración.** Tablas, perfil único, migración y pruebas. La app se ve igual.
2. **C2 Bienvenida y perfil.** Nombre y avatar en la bienvenida y pantalla de perfil.
3. **C3 Exportación v2.** Con compatibilidad hacia atrás.
4. **C4 Varios perfiles** (si decides la opción 2).
5. **C5 Respaldo mejorado** (opcional).

Cada etapa termina con `test`, `lint` y `assembleDebug` en verde y, si hay cambios de esquema, con la migración probada.

## 10. Qué no incluye

- Login, correo, contraseñas o recuperación de cuenta.
- Sincronización entre teléfonos.
- Clasificaciones entre usuarios o cualquier función social (requeriría servidor).
- Datos personales más allá del nombre y el avatar.
