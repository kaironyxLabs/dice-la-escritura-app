from importar_biblia import correcciones as mod
from importar_biblia import verificar
from importar_biblia.modelo import (
    CARACTER,
    CONTEO,
    ERROR,
    ESPACIO,
    HUECO,
    INFO,
    SOSPECHOSO,
    VACIO,
    Anomalia,
    Capitulo,
    Libro,
    Resultado,
)

from .ayudas import TABLA

FALSO = "Texto de prueba, no es RVR 1960."


def resultado(*versiculos: str | None, pendientes=()) -> Resultado:
    capitulo = Capitulo(1, versiculos=list(versiculos), pendientes=list(pendientes))
    libros = [Libro("UNO", [capitulo, Capitulo(2, versiculos=[FALSO])]), Libro("DOS", [Capitulo(1, versiculos=[FALSO])])]
    return Resultado(libros, [], 3)


def tipos(anomalias) -> list[str]:
    return [a.tipo for a in anomalias]


def test_texto_limpio_no_da_anomalias():
    assert verificar.verificar(resultado(FALSO, FALSO), TABLA, None) == []


def test_conteo_distinto_del_esperado():
    esperado = {"UNO": [3, 1], "DOS": [1]}
    anomalias = verificar.verificar(resultado(FALSO, FALSO), TABLA, esperado)
    assert tipos(anomalias) == [CONTEO]
    assert anomalias[0].severidad == ERROR and "esperaban 3" in anomalias[0].detalle


def test_hueco_y_versiculo_vacio_salvo_pendientes():
    assert tipos(verificar.verificar(resultado(FALSO, None, FALSO), TABLA, None)) == [HUECO]
    assert tipos(verificar.verificar(resultado(FALSO, "  ", FALSO), TABLA, None)) == [VACIO]
    assert verificar.verificar(resultado(FALSO, None, FALSO, pendientes=[2]), TABLA, None) == []


def test_caracteres_fuera_del_conjunto_permitido():
    for caracter in ("\\", "*", "_", "="):
        anomalias = verificar.verificar(resultado(f"Texto {caracter} de prueba."), TABLA, None)
        assert CARACTER in tipos(anomalias), caracter
    # Todo lo habitual es válido.
    texto = "¿Qué? ¡Sí! Ñandú, áéíóú ü; (x): a-b – c — d 12."
    assert CARACTER not in tipos(verificar.verificar(resultado(texto), TABLA, None))


def test_digitos_pegados_a_letras():
    anomalias = verificar.verificar(resultado("Moisés; p8ero Moisés huyó."), TABLA, None)
    assert SOSPECHOSO in tipos(anomalias)
    assert SOSPECHOSO not in tipos(verificar.verificar(resultado("Vivió 130 años, y a los 12 días."), TABLA, None))


def test_detecta_espacios_perdidos_tras_vocal_acentuada():
    # "está" aparece muchas veces; "estáen" una sola: probablemente falta un espacio.
    normales = [f"Ya está en casa {i}." for i in range(1, 8)]
    sospechoso = "Todo estáen orden."
    anomalias = verificar.verificar(resultado(*normales, sospechoso), TABLA, None)
    espacios = [a for a in anomalias if a.tipo == ESPACIO]
    assert len(espacios) == 1
    assert espacios[0].versiculo == 8 and "estáen" in espacios[0].detalle and "está en" in espacios[0].detalle


def test_no_marca_palabras_normales_con_tilde():
    normales = [f"También después más allá {i}, después." for i in range(1, 9)]
    assert ESPACIO not in tipos(verificar.verificar(resultado(*normales), TABLA, None))


def test_resolucion_de_anomalias():
    r = resultado(FALSO)
    sin_nada = mod.Correcciones()
    error = Anomalia(HUECO, ERROR, "UNO", 1, 2, "")
    info = Anomalia("salto-numeracion", INFO, "UNO", 1, 2, "")
    assert verificar.esta_resuelta(info, r, sin_nada)
    assert not verificar.esta_resuelta(error, r, sin_nada)

    con_pendiente = mod.Correcciones(pendientes={("UNO", 1, 2): "dañado"})
    assert verificar.esta_resuelta(error, r, con_pendiente)

    con_aceptada = mod.Correcciones(aceptadas=[{"libro": "UNO", "capitulo": 1, "tipo": HUECO}])
    assert verificar.esta_resuelta(error, r, con_aceptada)

    conteo = Anomalia(CONTEO, ERROR, "UNO", 1, None, "")
    assert not verificar.esta_resuelta(conteo, r, con_pendiente)
    aceptada_conteo = mod.Correcciones(aceptadas=[{"libro": "UNO", "capitulo": 1, "tipo": CONTEO}])
    assert verificar.esta_resuelta(conteo, r, aceptada_conteo)


def test_una_correccion_no_silencia_anomalias_recalculadas():
    # Un carácter raro en un versículo corregido se vuelve a detectar si la corrección no lo quitó.
    r = resultado("Texto = malo.")
    c = mod.Correcciones(correcciones=[{"libro": "UNO", "capitulo": 1, "versiculo": 1, "texto": "Texto = malo."}])
    anomalia = verificar.verificar(r, TABLA, None)[0]
    assert not verificar.esta_resuelta(anomalia, r, c)


def test_pendientes_automaticos_solo_para_errores_con_versiculo():
    anomalias = [
        Anomalia(HUECO, ERROR, "UNO", 1, 2, ""),
        Anomalia(CONTEO, ERROR, "UNO", 1, None, ""),
        Anomalia(ESPACIO, "aviso", "UNO", 1, 1, ""),
        Anomalia(CARACTER, ERROR, "UNO", 2, 1, ""),
    ]
    assert verificar.pendientes_automaticos(anomalias, resueltas={3}) == {("UNO", 1): {2}}


def test_no_confunde_el_imperfecto_en_ia_con_un_espacio_perdido():
    # "escondí" aparece suelto varias veces, pero "escondía" es una palabra correcta.
    normales = [f"Yo escondí algo {i}." for i in range(1, 6)] + ["Lo escondía bien.", "Se abría solo."]
    assert ESPACIO not in tipos(verificar.verificar(resultado(*normales), TABLA, None))


def test_detecta_guion_con_espacio_paralelo_dentro_del_texto_y_palabra_repetida():
    anomalias = verificar.verificar(resultado("Fue a Bet- el y volvió.", "Un talento de de oro.", "Salomón (1 R. 10.26-29) comercia."), TABLA, None)
    detalles = [a.detalle for a in anomalias if a.tipo == SOSPECHOSO]
    assert any("guion seguido de espacio" in d for d in detalles)
    assert any("palabra repetida" in d for d in detalles)
    assert any("paralelo dentro del texto" in d for d in detalles)


def test_los_detectores_nuevos_no_marcan_texto_normal():
    normal = "Fue a Bet-el con Is-boset, y dijo: (a) uno y otro; Santo, santo, santo."
    assert verificar.verificar(resultado(normal), TABLA, None) == []
