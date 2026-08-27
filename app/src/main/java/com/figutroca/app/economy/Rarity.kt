package com.figutroca.app.economy

/**
 * Rarity tiers for the collectible (economy) phase. Not wired to the current
 * offline organizer yet — it's the base for packs, drop rates and crafting.
 *
 * The design goal: every sticker is obtainable on the free plan (so a
 * collection can always be completed), rarer tiers just show up less often;
 * [SPECIAL_EXTRA] is mostly a paid source with only a few free copies.
 */
enum class Rarity(val label: String, val colorArgb: Long) {
    COMMON("Comum", 0xFF9AA3AC),
    UNCOMMON("Incomum", 0xFF4CAF50),
    RARE("Rara", 0xFF2196F3),
    EPIC("Épica", 0xFF9C27B0),
    LEGENDARY("Lendária", 0xFFFFC107),
    SPECIAL_EXTRA("Especial Extra", 0xFFE4002B);

    companion object {
        fun from(name: String?): Rarity =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: COMMON
    }
}
