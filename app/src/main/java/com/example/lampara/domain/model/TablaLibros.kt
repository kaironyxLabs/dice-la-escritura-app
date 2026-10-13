package com.example.lampara.domain.model

import com.example.lampara.domain.model.SeccionBiblica.CARTAS_DE_PABLO
import com.example.lampara.domain.model.SeccionBiblica.CARTAS_GENERALES
import com.example.lampara.domain.model.SeccionBiblica.EVANGELIOS
import com.example.lampara.domain.model.SeccionBiblica.HECHOS
import com.example.lampara.domain.model.SeccionBiblica.HISTORICOS
import com.example.lampara.domain.model.SeccionBiblica.PENTATEUCO
import com.example.lampara.domain.model.SeccionBiblica.POETICOS
import com.example.lampara.domain.model.SeccionBiblica.PROFECIA
import com.example.lampara.domain.model.SeccionBiblica.PROFETAS_MAYORES
import com.example.lampara.domain.model.SeccionBiblica.PROFETAS_MENORES
import com.example.lampara.domain.model.Testamento.ANTIGUO
import com.example.lampara.domain.model.Testamento.NUEVO
import java.text.Normalizer

/** Los 66 libros en el orden de la Biblia protestante, con los nombres de la RVR1960. */
object TablaLibros {

    val todos: List<Libro> = listOf(
        libro("GEN", "Génesis", "Gn.", ANTIGUO, PENTATEUCO, 50),
        libro("EXO", "Éxodo", "Éx.", ANTIGUO, PENTATEUCO, 40),
        libro("LEV", "Levítico", "Lv.", ANTIGUO, PENTATEUCO, 27),
        libro("NUM", "Números", "Nm.", ANTIGUO, PENTATEUCO, 36),
        libro("DEU", "Deuteronomio", "Dt.", ANTIGUO, PENTATEUCO, 34),
        libro("JOS", "Josué", "Jos.", ANTIGUO, HISTORICOS, 24),
        libro("JDG", "Jueces", "Jue.", ANTIGUO, HISTORICOS, 21),
        libro("RUT", "Rut", "Rt.", ANTIGUO, HISTORICOS, 4),
        libro("1SA", "1 Samuel", "1 S.", ANTIGUO, HISTORICOS, 31),
        libro("2SA", "2 Samuel", "2 S.", ANTIGUO, HISTORICOS, 24),
        libro("1KI", "1 Reyes", "1 R.", ANTIGUO, HISTORICOS, 22),
        libro("2KI", "2 Reyes", "2 R.", ANTIGUO, HISTORICOS, 25),
        libro("1CH", "1 Crónicas", "1 Cr.", ANTIGUO, HISTORICOS, 29),
        libro("2CH", "2 Crónicas", "2 Cr.", ANTIGUO, HISTORICOS, 36),
        libro("EZR", "Esdras", "Esd.", ANTIGUO, HISTORICOS, 10),
        libro("NEH", "Nehemías", "Neh.", ANTIGUO, HISTORICOS, 13),
        libro("EST", "Ester", "Est.", ANTIGUO, HISTORICOS, 10),
        libro("JOB", "Job", "Job", ANTIGUO, POETICOS, 42),
        libro("PSA", "Salmos", "Sal.", ANTIGUO, POETICOS, 150),
        libro("PRO", "Proverbios", "Pr.", ANTIGUO, POETICOS, 31),
        libro("ECC", "Eclesiastés", "Ec.", ANTIGUO, POETICOS, 12),
        libro("SNG", "Cantares", "Cnt.", ANTIGUO, POETICOS, 8),
        libro("ISA", "Isaías", "Is.", ANTIGUO, PROFETAS_MAYORES, 66),
        libro("JER", "Jeremías", "Jer.", ANTIGUO, PROFETAS_MAYORES, 52),
        libro("LAM", "Lamentaciones", "Lm.", ANTIGUO, PROFETAS_MAYORES, 5),
        libro("EZK", "Ezequiel", "Ez.", ANTIGUO, PROFETAS_MAYORES, 48),
        libro("DAN", "Daniel", "Dn.", ANTIGUO, PROFETAS_MAYORES, 12),
        libro("HOS", "Oseas", "Os.", ANTIGUO, PROFETAS_MENORES, 14),
        libro("JOL", "Joel", "Jl.", ANTIGUO, PROFETAS_MENORES, 3),
        libro("AMO", "Amós", "Am.", ANTIGUO, PROFETAS_MENORES, 9),
        libro("OBA", "Abdías", "Abd.", ANTIGUO, PROFETAS_MENORES, 1),
        libro("JON", "Jonás", "Jon.", ANTIGUO, PROFETAS_MENORES, 4),
        libro("MIC", "Miqueas", "Mi.", ANTIGUO, PROFETAS_MENORES, 7),
        libro("NAM", "Nahúm", "Nah.", ANTIGUO, PROFETAS_MENORES, 3),
        libro("HAB", "Habacuc", "Hab.", ANTIGUO, PROFETAS_MENORES, 3),
        libro("ZEP", "Sofonías", "Sof.", ANTIGUO, PROFETAS_MENORES, 3),
        libro("HAG", "Hageo", "Hag.", ANTIGUO, PROFETAS_MENORES, 2),
        libro("ZEC", "Zacarías", "Zac.", ANTIGUO, PROFETAS_MENORES, 14),
        libro("MAL", "Malaquías", "Mal.", ANTIGUO, PROFETAS_MENORES, 4),
        libro("MAT", "Mateo", "Mt.", NUEVO, EVANGELIOS, 28),
        libro("MRK", "Marcos", "Mr.", NUEVO, EVANGELIOS, 16),
        libro("LUK", "Lucas", "Lc.", NUEVO, EVANGELIOS, 24),
        libro("JHN", "Juan", "Jn.", NUEVO, EVANGELIOS, 21),
        libro("ACT", "Hechos", "Hch.", NUEVO, HECHOS, 28),
        libro("ROM", "Romanos", "Ro.", NUEVO, CARTAS_DE_PABLO, 16),
        libro("1CO", "1 Corintios", "1 Co.", NUEVO, CARTAS_DE_PABLO, 16),
        libro("2CO", "2 Corintios", "2 Co.", NUEVO, CARTAS_DE_PABLO, 13),
        libro("GAL", "Gálatas", "Gá.", NUEVO, CARTAS_DE_PABLO, 6),
        libro("EPH", "Efesios", "Ef.", NUEVO, CARTAS_DE_PABLO, 6),
        libro("PHP", "Filipenses", "Fil.", NUEVO, CARTAS_DE_PABLO, 4),
        libro("COL", "Colosenses", "Col.", NUEVO, CARTAS_DE_PABLO, 4),
        libro("1TH", "1 Tesalonicenses", "1 Ts.", NUEVO, CARTAS_DE_PABLO, 5),
        libro("2TH", "2 Tesalonicenses", "2 Ts.", NUEVO, CARTAS_DE_PABLO, 3),
        libro("1TI", "1 Timoteo", "1 Ti.", NUEVO, CARTAS_DE_PABLO, 6),
        libro("2TI", "2 Timoteo", "2 Ti.", NUEVO, CARTAS_DE_PABLO, 4),
        libro("TIT", "Tito", "Tit.", NUEVO, CARTAS_DE_PABLO, 3),
        libro("PHM", "Filemón", "Flm.", NUEVO, CARTAS_DE_PABLO, 1),
        libro("HEB", "Hebreos", "He.", NUEVO, CARTAS_GENERALES, 13),
        libro("JAS", "Santiago", "Stg.", NUEVO, CARTAS_GENERALES, 5),
        libro("1PE", "1 Pedro", "1 P.", NUEVO, CARTAS_GENERALES, 5),
        libro("2PE", "2 Pedro", "2 P.", NUEVO, CARTAS_GENERALES, 3),
        libro("1JN", "1 Juan", "1 Jn.", NUEVO, CARTAS_GENERALES, 5),
        libro("2JN", "2 Juan", "2 Jn.", NUEVO, CARTAS_GENERALES, 1),
        libro("3JN", "3 Juan", "3 Jn.", NUEVO, CARTAS_GENERALES, 1),
        libro("JUD", "Judas", "Jud.", NUEVO, CARTAS_GENERALES, 1),
        libro("REV", "Apocalipsis", "Ap.", NUEVO, PROFECIA, 22),
    ).mapIndexed { indice, base -> base.copy(orden = indice + 1) }

    val porId: Map<String, Libro> = todos.associateBy { it.id }

    /** Nombres alternativos aceptados al leer referencias, además del nombre y la abreviatura. */
    private val alias: Map<String, String> = mapOf(
        "salmo" to "PSA",
        "cantar de los cantares" to "SNG",
        "cantares de salomon" to "SNG",
        "apocalipsis de juan" to "REV",
        "hechos de los apostoles" to "ACT",
    )

    private val porNombreNormalizado: Map<String, Libro> = buildMap {
        todos.forEach { libro ->
            put(normalizar(libro.nombre), libro)
            put(normalizar(libro.abreviatura), libro)
        }
        alias.forEach { (nombre, id) -> put(nombre, porId.getValue(id)) }
    }

    /** Busca un libro por nombre o abreviatura, sin distinguir mayúsculas, tildes ni puntos. */
    fun buscar(nombre: String): Libro? = porNombreNormalizado[normalizar(nombre)]

    /** Minúsculas, sin tildes ni puntos, espacios colapsados y "1juan" igual que "1 juan". */
    internal fun normalizar(texto: String): String {
        val sinTildes = Normalizer.normalize(texto.trim().lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return sinTildes
            .replace(".", " ")
            .replace(Regex("^([123])\\s*(?=\\p{L})"), "$1 ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun libro(
        id: String,
        nombre: String,
        abreviatura: String,
        testamento: Testamento,
        seccion: SeccionBiblica,
        capitulos: Int,
    ) = Libro(id, 0, nombre, abreviatura, testamento, seccion, capitulos)
}
