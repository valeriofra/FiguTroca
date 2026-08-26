package com.figutroca.app.data

/**
 * Panini FIFA World Cup 2026 album structure.
 *
 * The album has 980 stickers: 48 national teams with 20 stickers each
 * (nº 1 = crest/logo, nº 13 = team photo, the rest players) plus a special
 * "FWC" opening section. Sticker codes follow the pattern "<TEAM> <number>",
 * e.g. "BRA 13" = Brazil #13.
 */
object Teams {

    /** Stickers per team/section in this album. */
    const val PER_TEAM = 20

    /** FIFA-style 3-letter code -> Portuguese name. Codes here match Panini's. */
    /** Special (non-team) sections whose size is not the standard 20. */
    val specials: Set<String> = setOf("FWC", "CC")

    private val names: Map<String, String> = mapOf(
        "FWC" to "FIFA World Cup",
        "CC" to "Especiais (CC)",
        // Anfitriões
        "MEX" to "México", "USA" to "Estados Unidos", "CAN" to "Canadá",
        // Seleções (e possíveis participantes) na numeração do usuário
        "RSA" to "África do Sul", "KOR" to "Coreia do Sul", "CZE" to "Chéquia",
        "BIH" to "Bósnia e Herzegovina", "QAT" to "Catar", "SUI" to "Suíça",
        "BRA" to "Brasil", "MAR" to "Marrocos", "HAI" to "Haiti",
        "SCO" to "Escócia", "PAR" to "Paraguai", "AUS" to "Austrália",
        "TUR" to "Turquia", "GER" to "Alemanha", "CUW" to "Curaçao",
        "CIV" to "Costa do Marfim", "ECU" to "Equador", "NED" to "Países Baixos",
        "JPN" to "Japão", "SWE" to "Suécia", "BEL" to "Bélgica",
        "EGY" to "Egito", "IRN" to "Irã", "ESP" to "Espanha",
        "CPV" to "Cabo Verde", "KSA" to "Arábia Saudita", "URU" to "Uruguai",
        "FRA" to "França", "SEN" to "Senegal", "IRQ" to "Iraque",
        "NOR" to "Noruega", "ARG" to "Argentina", "ALG" to "Argélia",
        "AUT" to "Áustria", "JOR" to "Jordânia", "POR" to "Portugal",
        "COD" to "RD Congo", "UZB" to "Uzbequistão", "COL" to "Colômbia",
        "ENG" to "Inglaterra", "CRO" to "Croácia", "GHA" to "Gana",
        "PAN" to "Panamá", "TUN" to "Tunísia",
        // Outras seleções comuns (caso apareçam)
        "ITA" to "Itália", "NGA" to "Nigéria", "CMR" to "Camarões",
        "SRB" to "Sérvia", "POL" to "Polônia", "DEN" to "Dinamarca",
        "WAL" to "País de Gales", "UKR" to "Ucrânia", "GRE" to "Grécia",
        "CHI" to "Chile", "PER" to "Peru", "VEN" to "Venezuela",
        "BOL" to "Bolívia", "NZL" to "Nova Zelândia", "PAN" to "Panamá",
        "CRC" to "Costa Rica", "HON" to "Honduras", "JAM" to "Jamaica",
        "GAB" to "Gabão", "MLI" to "Mali", "COG" to "Congo"
    )

    /** Returns the friendly name for a code, or the code itself if unknown. */
    fun name(code: String): String = names[code.uppercase()] ?: code.uppercase()

    fun isKnown(code: String): Boolean = names.containsKey(code.uppercase())
}
