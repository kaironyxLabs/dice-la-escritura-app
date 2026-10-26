# Prompts de Gemini para las ilustraciones del plan U8

Úsalos con el **bloque de estilo** de `docs/PROMPTS_MEDALLONES.md` (pégalo al inicio de cada prompt) cambiando el formato cuando se indique. Sin texto en la imagen, sin rostro de Cristo ni de Dios (de espaldas, de lejos, solo manos o luz), figuras de época y estilizadas.

Formatos: **cuadrado** = 1:1 (1024 x 1024); **banner** = 3:1 (3072 x 1024, o 16:9 si Gemini no ofrece 3:1: lo recorto); **lámina** = 3:4 vertical.
Guarda cada archivo con el nombre indicado (jpg o png) en una carpeta y avísame.

## Lote 1 — Banners de categoría (banner, nombre `banner_<categoría>`)
- `banner_libros`: biblioteca antigua con estantes de rollos y libros de cuero, luz dorada entrando por una ventana alta, atril con un rollo abierto.
- `banner_historia`: panorama de caravana de nómadas con camellos cruzando un desierto al amanecer, tiendas y una palmera, cielo dorado.
- `banner_personajes`: grupo de figuras de época vistas de espaldas caminando por un sendero entre olivos hacia una ciudad lejana al atardecer.
- `banner_lugares`: vista panorámica de colinas de Judea con un pueblo de casas de piedra, el mar de Galilea al fondo y barcas de pescadores.
- `banner_ensenanzas`: ladera verde con lirios y un campo de trigo, un sembrador lejano, pájaros en el cielo y una lámpara de aceite en primer plano.
- `banner_cartas`: mesa de escriba con pergaminos, pluma, tintero y lámpara, un mapa del mar Mediterráneo con barcos dibujado al fondo.
- `banner_donde_escrito`: scriptorium con un escriba de espaldas copiando un rollo a la luz de una vela, muros de piedra y un vitral cálido.
- `banner_confesion`: interior sobrio de una capilla de piedra del siglo XVII con un libro abierto sobre un facistol y luz de ventana lateral.

## Lote 1 — Niveles (cuadrado, nombre `nivel_01` a `nivel_10`)
Una planta o árbol que crece, siempre sobre tierra y luz cálida, con el mismo encuadre para que se vea la progresión:
`nivel_01` semilla en la tierra; `nivel_02` brote con dos hojas; `nivel_03` plántula; `nivel_04` tallo joven; `nivel_05` arbusto; `nivel_06` vid con hojas; `nivel_07` vid con racimos; `nivel_08` olivo joven; `nivel_09` olivo frondoso con aceitunas; `nivel_10` árbol de vida grande con luz dorada entre las ramas.

## Lote 1 — Sellos de logros (cuadrado, nombre `sello_01` a `sello_09`)
Un sello de lacre redondo (rojo oscuro, dorado o verde olivo) con un símbolo en relieve, sobre pergamino. Símbolos: `sello_01` lámpara de aceite; `sello_02` ancla; `sello_03` pluma; `sello_04` pez; `sello_05` espiga de trigo; `sello_06` paloma; `sello_07` corona de olivo; `sello_08` llave; `sello_09` rollo con cinta. (Te daré la correspondencia exacta con cada logro al integrarlos.)

## Lote 2 — Escenas de historias (cuadrado o 4:3, nombre `escena_<tema>`)
Cada una: una escena de época en el estilo de manuscrito, sin texto.
`escena_creacion` jardín al amanecer con luz sobre las aguas; `escena_eden` árbol en medio del jardín con animales y figuras de espaldas; `escena_babel` torre en construcción con obreros pequeños; `escena_abraham_estrellas` anciano de espaldas mirando un cielo lleno de estrellas; `escena_jacob_escalera` escalera de luz con ángeles subiendo y bajando sobre un hombre dormido; `escena_jose_egipto` joven con túnica de colores ante graneros de Egipto; `escena_zarza` zarza ardiendo en una montaña y un pastor descalzo de espaldas; `escena_pascua` casa con los marcos de la puerta marcados y una familia cenando; `escena_mar_rojo` mar partido con muros de agua y un pueblo que cruza; `escena_sinai` montaña con nube y fuego, pueblo al pie; `escena_mana` pueblo recogiendo pan del suelo del desierto al alba; `escena_jerico` muralla con sacerdotes y trompetas; `escena_gedeon` trescientos hombres con antorchas y cántaros de noche; `escena_david_goliat` joven pastor con honda ante un gigante de armadura, en un valle; `escena_templo` templo de piedra y cedro con obreros; `escena_elias_carmelo` altar con fuego bajando del cielo en el monte; `escena_daniel_leones` hombre orando entre leones tranquilos, foso de piedra; `escena_horno` tres hombres en un horno con una cuarta figura de luz; `escena_jonas` gran pez y un hombre en la orilla; `escena_ester` reina ante un trono en un palacio persa; `escena_nehemias_muros` muros de Jerusalén reconstruyéndose; `escena_belen` establo y pesebre con luz y pastores de espaldas; `escena_pentecostes` doce figuras con llamas suaves sobre ellas en un aposento; `escena_damasco` camino con una luz brillante y un hombre caído.

## Lote 3 — Escenas de parábolas (cuadrado, nombre `parabola_<tema>`)
`parabola_sembrador`; `parabola_hijo_prodigo` (un padre y su hijo abrazándose, ambos de espaldas); `parabola_samaritano` (camino a Jericó, un viajero vendando a otro); `parabola_oveja_perdida` (pastor con una oveja sobre los hombros en un monte); `parabola_virgenes` (diez lámparas, cinco encendidas y cinco apagadas); `parabola_talentos` (tres siervos con bolsas de monedas).

## Lote 4 — Bienvenida y resultado
Láminas 3:4 `bienvenida_1` (rollo de la Escritura abierto con luz), `bienvenida_2` (un camino de niveles entre plantas hasta un árbol), `bienvenida_3` (un grupo de figuras de espaldas leyendo juntos); cuadrado `fondo_resultado` (pergamino con una rama de olivo y una lámpara).

## Consejos
- Pide siempre «sin texto ni letras».
- Si el estilo se aleja de los medallones ya hechos, pega una de esas ilustraciones y di «mismo estilo que esta imagen».
- Revisa que no aparezca el rostro de Jesús ni de Dios; si sale, regenera con «solo la silueta, de espaldas».
