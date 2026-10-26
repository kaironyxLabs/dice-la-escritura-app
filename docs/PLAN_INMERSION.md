# Plan U8: inmersión bíblica (más pergamino, más época, más imágenes)

Meta: que quien use la app se sienta dentro de las historias: pantallas con aire de manuscrito iluminado, una escena ilustrada ligada a lo que acaba de responder y recompensas con arte propio. Todo sigue siendo local y sin conexión.

Reglas que se mantienen (D-018 y CLAUDE.md): sin rostro de Cristo ni representación de Dios (se muestra de espaldas, de lejos, solo las manos o la luz); figuras humanas estilizadas y de época; las ilustraciones no llevan texto; ninguna imagen contiene versículos.

## Lo que ya se hizo solo con código (27-oct)
- Filete interior dorado con rombo y voluta en cada esquina de todas las hojas de pergamino (`hojaPergamino`).
- Separador ornamental `OrnamentoDivisor` (Inicio).
- Medallones de categoría (D-018) y hoja de olivo del día de gracia.

## Lo que falta y necesita arte (lo generas tú con Gemini, yo lo integro)
Prompts y nombres de archivo en `docs/PROMPTS_ILUSTRACIONES.md`. Total: unas 70 imágenes, en cuatro lotes por prioridad.

| Lote | Qué | Dónde se usa | Cantidad |
|---|---|---|---|
| 1 | Banners de categoría (panorámicos) | Cabecera de la partida de cada categoría | 8 |
| 1 | Niveles ilustrados (semilla a árbol de vida) | Inicio, «Tus niveles», celebración de nivel | 10 |
| 1 | Sellos de logros (lacre) | Progreso | 9 |
| 2 | Escenas de historias | Tras responder, bajo la referencia: «Escena de la historia» | 24 |
| 3 | Escenas de parábolas | Igual, para Enseñanzas | 6 |
| 4 | Bienvenida (3 láminas) y fondo del resultado | Onboarding y resumen de la partida | 4 |

## Cómo se integra (por etapas, yo)
1. **U8.1 Banners:** cabecera ilustrada con fundido a pergamino en la partida y el resultado.
2. **U8.2 Escenas:** cada pregunta de historia, personajes, lugares y enseñanzas se enlaza a una escena por etiqueta; tras responder aparece la ilustración con su pie (por ejemplo «La travesía del mar Rojo» y la referencia). Si una pregunta no tiene escena, no se muestra nada.
3. **U8.3 Niveles y sellos:** sustituyen a las plantas y chapas vectoriales.
4. **U8.4 Bienvenida:** láminas ilustradas en lugar de texto sobre fondo liso.
5. **U8.5 Pulido de época:** letra capital iluminada en los pasajes de Memorizar, transiciones de «pasar la página» y tarjetas de pregunta con forma de rollo, todo en código.

Cada imagen se reduce a WebP (unos 30 a 60 KB por pieza a 512 o 1024 px), de modo que las 70 suman cerca de 4 MB y el APK sigue pequeño.

## Decisiones tuyas
1. ¿Empezamos por el lote 1? (recomendado: es lo que más se nota y más rápido se integra).
2. ¿Quieres que las escenas lleven un pie de texto o solo la imagen?
