package com.figutroca.app.data

import com.figutroca.app.util.ListParser
import kotlinx.coroutines.flow.Flow

/** What a pasted list represents, and how quantities map to owned copies. */
enum class ImportMode {
    /** Trade pile: quantity = spare copies, so total owned = quantity + 1. */
    DUPLICATES,
    /** Owned stickers: quantity = total copies owned. */
    OWNED,
    /** Missing stickers: creates the slots and sets them to 0. */
    MISSING
}

data class ImportResult(val teams: Int, val stickers: Int, val copies: Int)

/**
 * Single point of access to the database for the ViewModel.
 * Also owns the small pieces of domain logic: creating collections,
 * parsing/adding sticker slots, archiving and switching collections.
 */
class Repository(
    private val collectionDao: CollectionDao,
    private val stickerDao: StickerDao
) {
    /**
     * On first launch, creates the active "Copa do Mundo 2026" collection and
     * seeds it from the owner's real duplicate/missing lists (see [SeedData]).
     */
    suspend fun ensureActiveCollection() {
        if (collectionDao.getActive() != null) return
        collectionDao.clearActive()
        val id = collectionDao.insert(Collection(name = "Copa do Mundo 2026", isActive = true))
        seedCollection(id)
    }

    /**
     * Builds a complete album from [SeedData.REPETIDAS] + [SeedData.FALTAM].
     * Per team: missing numbers -> 0, spares -> spares+1, everything else -> 1
     * (owned single). Special sections (FWC, CC) only include listed numbers.
     */
    private suspend fun seedCollection(collectionId: Long) {
        val rep = ListParser.parse(SeedData.REPETIDAS)
        val miss = ListParser.parse(SeedData.FALTAM)

        val spares: Map<String, Map<Int, Int>> = rep.entries
            .groupBy { it.team }
            .mapValues { (_, es) -> es.associate { it.number to it.qty } }
        val missing: Map<String, Set<Int>> = miss.entries
            .groupBy { it.team }
            .mapValues { (_, es) -> es.map { it.number }.toSet() }

        val teams = (spares.keys + missing.keys)
        val stickers = teams.flatMap { team ->
            val name = Teams.name(team)
            val teamSpares = spares[team].orEmpty()
            val teamMissing = missing[team].orEmpty()
            val numbers: List<Int> = if (team in Teams.specials) {
                (teamSpares.keys + teamMissing).sorted()
            } else {
                (1..Teams.PER_TEAM).toList()
            }
            numbers.map { n ->
                val count = when {
                    teamSpares.containsKey(n) -> teamSpares.getValue(n) + 1
                    n in teamMissing -> 0
                    else -> 1
                }
                Sticker(
                    collectionId = collectionId,
                    code = "$team $n",
                    group = name,
                    count = count,
                    sortKey = n.toLong()
                )
            }
        }
        stickerDao.insertAll(stickers)
    }

    fun observeCollections(): Flow<List<Collection>> = collectionDao.observeAll()
    fun observeActiveCollection(): Flow<Collection?> = collectionDao.observeActive()
    fun observeStickers(collectionId: Long): Flow<List<Sticker>> =
        stickerDao.observeForCollection(collectionId)
    fun observeStats(collectionId: Long): Flow<CollectionStats?> =
        stickerDao.observeStats(collectionId)

    /** Creates a new collection and makes it the active one. */
    suspend fun createCollection(name: String, numberedTotal: Int): Long {
        collectionDao.clearActive()
        val id = collectionDao.insert(
            Collection(name = name.ifBlank { "Nova coleção" }, isActive = true)
        )
        if (numberedTotal > 0) {
            addNumberedRange(id, 1, numberedTotal, group = "")
        }
        return id
    }

    /** Adds sticker slots numbered [from]..[to] (inclusive) to a collection. */
    suspend fun addNumberedRange(collectionId: Long, from: Int, to: Int, group: String) {
        val lo = minOf(from, to)
        val hi = maxOf(from, to)
        val stickers = (lo..hi).map { n ->
            Sticker(
                collectionId = collectionId,
                code = n.toString(),
                group = group.trim(),
                sortKey = n.toLong()
            )
        }
        stickerDao.insertAll(stickers)
    }

    /**
     * Adds arbitrary sticker codes separated by comma / space / newline,
     * e.g. "ARG1, ARG2, FWC, 101". Duplicates already present are ignored.
     */
    suspend fun addCodes(collectionId: Long, raw: String, group: String) {
        val codes = raw.split(',', ' ', '\n', '\t', ';')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (codes.isEmpty()) return
        val stickers = codes.map { code ->
            Sticker(
                collectionId = collectionId,
                code = code,
                group = group.trim(),
                sortKey = code.filter { it.isDigit() }.toLongOrNull() ?: Long.MAX_VALUE
            )
        }
        stickerDao.insertAll(stickers)
    }

    /**
     * Imports a pasted collector list (see [ListParser]) into a collection.
     * Every team referenced is expanded to its full 1..[Teams.PER_TEAM] set so
     * the album stays complete and "Faltam" is accurate, then the listed
     * stickers get their counts set according to [mode].
     */
    suspend fun importList(collectionId: Long, raw: String, mode: ImportMode): ImportResult {
        val parsed = ListParser.parse(raw)
        if (parsed.entries.isEmpty()) return ImportResult(0, 0, 0)

        // 1) Ensure every referenced team has all of its numbered slots.
        val slots = parsed.teams.flatMap { team ->
            val name = Teams.name(team)
            (1..Teams.PER_TEAM).map { n ->
                Sticker(
                    collectionId = collectionId,
                    code = "$team $n",
                    group = name,
                    count = 0,
                    sortKey = n.toLong()
                )
            }
        }
        stickerDao.insertAll(slots) // IGNORE keeps existing counts untouched

        // 2) Apply counts for the listed stickers.
        for (e in parsed.entries) {
            val count = when (mode) {
                ImportMode.DUPLICATES -> e.qty + 1
                ImportMode.OWNED -> e.qty
                ImportMode.MISSING -> 0
            }
            val existing = stickerDao.findByCode(collectionId, e.code)
            if (existing != null) {
                stickerDao.setCount(existing.id, count)
            } else {
                stickerDao.insertAll(
                    listOf(
                        Sticker(
                            collectionId = collectionId,
                            code = e.code,
                            group = Teams.name(e.team),
                            count = count,
                            sortKey = e.number.toLong()
                        )
                    )
                )
            }
        }
        return ImportResult(parsed.teams.size, parsed.stickerCount, parsed.copies)
    }

    suspend fun setCount(sticker: Sticker, count: Int) =
        stickerDao.setCount(sticker.id, count.coerceAtLeast(0))

    suspend fun deleteSticker(sticker: Sticker) = stickerDao.delete(sticker.id)

    suspend fun renameCollection(collection: Collection, name: String) =
        collectionDao.update(collection.copy(name = name.ifBlank { collection.name }))

    /** Archives the current active collection and creates a fresh active one. */
    suspend fun archiveAndStartNew(newName: String, numberedTotal: Int): Long {
        collectionDao.getActive()?.let { active ->
            collectionDao.archive(active.id, System.currentTimeMillis())
        }
        return createCollection(newName, numberedTotal)
    }

    suspend fun archiveCollection(collection: Collection) =
        collectionDao.archive(collection.id, System.currentTimeMillis())

    /** Re-opens an archived collection as the active one. */
    suspend fun activateCollection(collection: Collection) {
        collectionDao.clearActive()
        collectionDao.markActive(collection.id)
    }

    suspend fun deleteCollection(collection: Collection) {
        collectionDao.delete(collection.id)
    }
}
