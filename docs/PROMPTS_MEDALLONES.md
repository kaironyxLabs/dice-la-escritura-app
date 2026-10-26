# Prompts para los medallones de categoría (Gemini)

Ocho ilustraciones cuadradas, una por categoría. La app las recorta en círculo y les pone el aro dorado (`Medallon` en `core/ui/Pergamino.kt`), así que cada una debe funcionar dentro de un círculo.

## Cómo generarlas
- Formato **1:1 (1024 x 1024)**. Si Gemini no deja elegir, pídelo en el texto.
- Genera las ocho con el mismo **bloque de estilo** (abajo) para que parezcan una colección.
- El motivo va **centrado y ocupa unos dos tercios** del cuadrado: el recorte circular come las esquinas.
- Fondo propio de la ilustración (no transparente); se recomienda un fondo suave con luz cálida.
- Guarda cada archivo con el nombre indicado (`medallon_libros.png`, etc.) y entrégalos; yo los recorto y los integro.
- Regla del proyecto: sin rostro de Cristo ni representación de Dios. Los personajes humanos van en estilo de época, no como retratos.

## Bloque de estilo (pégalo al comienzo de cada prompt)
> Ilustración cuadrada 1:1 para un medallón circular de una app bíblica. Estilo de manuscrito iluminado antiguo, pintura gouache sobre pergamino, trazo fino de tinta sepia, colores cálidos y apagados (ocre, siena, verde olivo, azul profundo desvaído, toques de oro), textura sutil de papel envejecido, luz suave y dorada. Composición centrada y simple, un solo motivo principal que ocupe dos tercios del cuadro, con los bordes del cuadro vacíos y degradados al color del pergamino. Sin texto, sin letras, sin marcos, sin marcas de agua. Sin rostros detallados: figuras humanas, si hay, estilizadas, de época, de espaldas o de perfil.

## Prompts por categoría

1. **Libros y orden de la Biblia** (`medallon_libros.png`)
   > [bloque de estilo] Motivo: un rollo de pergamino abierto sobre un atril de madera, junto a un libro antiguo de tapas de cuero cerrado con un broche dorado y una pluma de ave en un tintero. Sin texto legible: las líneas del rollo son solo trazos de tinta.

2. **Historia bíblica** (`medallon_historia.png`)
   > [bloque de estilo] Motivo: el arca de Noé sobre aguas tranquilas, con un arco iris tenue en el cielo y una paloma con una rama de olivo volando hacia el arca. Cielo dorado del amanecer.

3. **Personajes** (`medallon_personajes.png`)
   > [bloque de estilo] Motivo: tres siluetas de figuras de época vistas de espaldas sobre una colina al atardecer: un pastor con cayado, una mujer con un cántaro y un hombre con bastón de peregrino. Ropajes antiguos de lino y lana, sin rostros visibles.

4. **Lugares y geografía** (`medallon_lugares.png`)
   > [bloque de estilo] Motivo: un mapa antiguo de la tierra de la Biblia pintado sobre pergamino, con montañas dibujadas a tinta, el mar Mediterráneo y un río con forma de cinta, y una pequeña brújula de rosa de los vientos dorada en una esquina. Sin nombres escritos.

5. **Enseñanzas y parábolas de Jesús** (`medallon_ensenanzas.png`)
   > [bloque de estilo] Motivo: un sembrador de época visto de espaldas esparciendo semillas en un campo al amanecer, con espigas, una oveja pequeña a un lado y una lámpara de aceite encendida en primer plano. No representar a Jesús.

6. **Cartas del Nuevo Testamento** (`medallon_cartas.png`)
   > [bloque de estilo] Motivo: una carta antigua de pergamino enrollada y atada con una cinta, sellada con lacre rojo oscuro, junto a una pluma y una pequeña lámpara de aceite. Sin texto legible.

7. **¿Dónde está escrito?** (`medallon_donde_escrito.png`)
   > [bloque de estilo] Motivo: una lupa antigua de latón dorado sobre un libro abierto cuyas páginas tienen líneas de escritura a mano; un rayo de luz dorada cae sobre el lugar que enfoca la lupa. Sin texto legible.

8. **Confesión Bautista de 1689** (`medallon_confesion.png`, módulo opcional)
   > [bloque de estilo] Motivo: un libro antiguo de tapas oscuras con una cruz sencilla grabada en la portada, sobre una mesa de madera junto a un tintero y una vela encendida. Tono sobrio, más oscuro y recogido que el resto, siglo XVII.

## Si el resultado no sirve
- Texto raro o letras inventadas: añade «sin ningún texto ni letras, solo trazos».
- Demasiado oscuro o moderno: repite «estilo de manuscrito antiguo, colores cálidos apagados».
- Composición pegada a los bordes: añade «el motivo ocupa solo la parte central, con amplio margen».
