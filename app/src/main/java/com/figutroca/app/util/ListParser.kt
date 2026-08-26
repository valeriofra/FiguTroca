package com.figutroca.app.util

/**
 * Parses the collector text format used to write duplicate/missing lists, e.g.:
 *
 *   FWC 1(2), 12, 11
 *   BRA 2, 3, 4(2), 14
 *   POR 8(2)
 *
 * A 2–4 letter token sets the current team prefix and stays active until the
 * next prefix (so a team's numbers may wrap across lines). A number token is a
 * sticker of the current team; an optional "(n)" gives a quantity (default 1).
 * Repeated numbers accumulate, so "12, 12" is the same as "12(2)".
 */
object ListParser {

    data class Entry(val team: String, val number: Int, val qty: Int) {
        val code: String get() = "$team $number"
    }

    data class Result(val entries: List<Entry>) {
        val teams: List<String> get() = entries.map { it.team }.distinct()
        val stickerCount: Int get() = entries.size
        val copies: Int get() = entries.sumOf { it.qty }
    }

    private val token = Regex("""[A-Za-z]{2,4}|\d+(?:\s*\(\s*\d+\s*\))?""")
    private val numQty = Regex("""(\d+)(?:\s*\(\s*(\d+)\s*\))?""")

    fun parse(raw: String): Result {
        // Accumulate quantities per (team, number) preserving first-seen order.
        val order = LinkedHashMap<String, Entry>()
        var team: String? = null

        for (m in token.findAll(raw)) {
            val t = m.value.trim()
            if (t.first().isLetter()) {
                team = t.uppercase()
            } else if (team != null) {
                val mq = numQty.matchEntire(t.replace(" ", "")) ?: continue
                val number = mq.groupValues[1].toIntOrNull() ?: continue
                val qty = mq.groupValues[2].toIntOrNull() ?: 1
                val key = "$team $number"
                val existing = order[key]
                order[key] = if (existing == null) Entry(team!!, number, qty)
                             else existing.copy(qty = existing.qty + qty)
            }
        }
        return Result(order.values.toList())
    }
}
