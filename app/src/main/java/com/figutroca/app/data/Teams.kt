package com.figutroca.app.data

/**
 * Panini FIFA World Cup 2026 album metadata: names (English + Portuguese),
 * ISO country code for the flag emoji, and a base brand colour per selection.
 * 48 teams of 20 stickers each (nº 1 = crest, nº 13 = team photo) plus the
 * special sections. Sticker codes are "<TEAM> <number>", e.g. "BRA 13".
 */
object Teams {

    const val PER_TEAM = 20

    /** Special (non-team) sections whose size isn't the standard 20. */
    val specials: Set<String> = setOf("FWC", "CC")

    data class Info(val pt: String, val en: String, val iso2: String, val color: Long)

    private val info: Map<String, Info> = mapOf(
        "FWC" to Info("FIFA World Cup", "FIFA World Cup", "", 0xFF0067B1),
        "CC" to Info("Coca-Cola", "Coca-Cola", "", 0xFFE61A27),
        "BRA" to Info("Brasil", "Brazil", "BR", 0xFF009C3B),
        "ARG" to Info("Argentina", "Argentina", "AR", 0xFF6CACE4),
        "FRA" to Info("França", "France", "FR", 0xFF002654),
        "ENG" to Info("Inglaterra", "England", "GB", 0xFFCE1124),
        "GER" to Info("Alemanha", "Germany", "DE", 0xFF1A1A1A),
        "ESP" to Info("Espanha", "Spain", "ES", 0xFFC60B1E),
        "POR" to Info("Portugal", "Portugal", "PT", 0xFFC8102E),
        "NED" to Info("Países Baixos", "Netherlands", "NL", 0xFFFF6A00),
        "BEL" to Info("Bélgica", "Belgium", "BE", 0xFFE30613),
        "CRO" to Info("Croácia", "Croatia", "HR", 0xFFD0021B),
        "URU" to Info("Uruguai", "Uruguay", "UY", 0xFF5CBFEB),
        "COL" to Info("Colômbia", "Colombia", "CO", 0xFFFCD116),
        "MEX" to Info("México", "Mexico", "MX", 0xFF006847),
        "USA" to Info("Estados Unidos", "United States", "US", 0xFF3C3B6E),
        "CAN" to Info("Canadá", "Canada", "CA", 0xFFD52B1E),
        "JPN" to Info("Japão", "Japan", "JP", 0xFF0033A0),
        "KOR" to Info("Coreia do Sul", "South Korea", "KR", 0xFFC8102E),
        "AUS" to Info("Austrália", "Australia", "AU", 0xFFFFCD00),
        "MAR" to Info("Marrocos", "Morocco", "MA", 0xFFC1272D),
        "SEN" to Info("Senegal", "Senegal", "SN", 0xFF00853F),
        "EGY" to Info("Egito", "Egypt", "EG", 0xFFCE1126),
        "GHA" to Info("Gana", "Ghana", "GH", 0xFF006B3F),
        "CIV" to Info("Costa do Marfim", "Ivory Coast", "CI", 0xFFFF8200),
        "CPV" to Info("Cabo Verde", "Cape Verde", "CV", 0xFF003893),
        "COD" to Info("RD Congo", "DR Congo", "CD", 0xFF007FFF),
        "TUN" to Info("Tunísia", "Tunisia", "TN", 0xFFE70013),
        "ALG" to Info("Argélia", "Algeria", "DZ", 0xFF007A3D),
        "IRN" to Info("Irã", "Iran", "IR", 0xFF239F40),
        "IRQ" to Info("Iraque", "Iraq", "IQ", 0xFFCE1126),
        "KSA" to Info("Arábia Saudita", "Saudi Arabia", "SA", 0xFF006C35),
        "QAT" to Info("Catar", "Qatar", "QA", 0xFF8A1538),
        "JOR" to Info("Jordânia", "Jordan", "JO", 0xFF007A3D),
        "UZB" to Info("Uzbequistão", "Uzbekistan", "UZ", 0xFF0099B5),
        "AUT" to Info("Áustria", "Austria", "AT", 0xFFED2939),
        "SUI" to Info("Suíça", "Switzerland", "CH", 0xFFD52B1E),
        "SCO" to Info("Escócia", "Scotland", "GB", 0xFF0065BF),
        "NOR" to Info("Noruega", "Norway", "NO", 0xFFBA0C2F),
        "SWE" to Info("Suécia", "Sweden", "SE", 0xFF006AA7),
        "TUR" to Info("Turquia", "Türkiye", "TR", 0xFFE30A17),
        "PAR" to Info("Paraguai", "Paraguay", "PY", 0xFFD52B1E),
        "ECU" to Info("Equador", "Ecuador", "EC", 0xFFFFD100),
        "RSA" to Info("África do Sul", "South Africa", "ZA", 0xFF007749),
        "BIH" to Info("Bósnia e Herzegovina", "Bosnia & Herz.", "BA", 0xFF002F6C),
        "HAI" to Info("Haiti", "Haiti", "HT", 0xFF00209F),
        "CUW" to Info("Curaçao", "Curaçao", "CW", 0xFF002B7F),
        "NZL" to Info("Nova Zelândia", "New Zealand", "NZ", 0xFF00247D),
        "PAN" to Info("Panamá", "Panama", "PA", 0xFFDA121A),
        "CZE" to Info("Chéquia", "Czechia", "CZ", 0xFFD7141A),
    )

    fun name(code: String): String = info[code.uppercase()]?.pt ?: code.uppercase()
    fun enName(code: String): String = info[code.uppercase()]?.en ?: code.uppercase()
    fun color(code: String): Long = info[code.uppercase()]?.color ?: 0xFF5B6572
    fun isKnown(code: String): Boolean = info.containsKey(code.uppercase())

    /** Emoji flag for the team's country, or a fallback. */
    fun flag(code: String): String {
        val c = code.uppercase()
        // UK constituent nations have their own subdivision (tag-sequence) flags.
        when (c) {
            "SCO" -> return tagFlag("gbsct")
            "ENG" -> return tagFlag("gbeng")
            "WAL" -> return tagFlag("gbwls")
        }
        val iso = info[c]?.iso2 ?: ""
        if (iso.length != 2) return when (c) {
            "FWC" -> "🏆"
            "CC" -> "🥤"
            else -> "🎽"
        }
        return iso.uppercase().map { ch -> 0x1F1E6 + (ch.code - 'A'.code) }
            .joinToString("") { cp -> String(Character.toChars(cp)) }
    }

    /** Builds a subdivision flag emoji (e.g. "gbsct" -> Scotland). */
    private fun tagFlag(sub: String): String {
        val sb = StringBuilder()
        sb.appendCodePoint(0x1F3F4)
        for (ch in sub) sb.appendCodePoint(0xE0000 + ch.code)
        sb.appendCodePoint(0xE007F)
        return sb.toString()
    }

    /**
     * Official album order (specials first, then teams). Used to sort the team
     * cards and the generated lists the same way. Codes not listed sort last.
     */
    val albumOrder: List<String> = listOf(
        "FWC", "CC",
        "MEX", "USA", "CAN",
        "RSA", "KOR", "CZE", "QAT", "BIH", "SUI", "BRA", "MAR", "HAI", "SCO",
        "PAR", "AUS", "TUR", "GER", "CUW", "CIV", "ECU", "NED", "JPN", "SWE",
        "TUN", "BEL", "EGY", "IRN", "NZL", "URU", "KSA", "CPV", "ESP", "FRA",
        "SEN", "IRQ", "NOR", "POR", "COD", "UZB", "COL", "ENG", "CRO", "GHA",
        "PAN", "ARG", "ALG", "AUT", "JOR"
    )

    fun orderIndex(code: String): Int {
        val i = albumOrder.indexOf(code.uppercase())
        return if (i >= 0) i else Int.MAX_VALUE
    }

    /**
     * Fixed sticker labels for a special section, or null to use only the
     * numbers seen in the imported lists. FWC runs 00, 1..18.
     */
    fun specialLabels(code: String): List<Pair<String, Int>>? = when (code.uppercase()) {
        "FWC" -> listOf("00" to 0) + (1..18).map { it.toString() to it }
        else -> null
    }
}
