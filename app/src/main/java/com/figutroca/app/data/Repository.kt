package com.figutroca.app.data

import kotlinx.coroutines.flow.Flow

/**
 * Single point of access to the database for the ViewModel.
 * Also owns the small pieces of domain logic: creating collections,
 * parsing/adding sticker slots, archiving and switching collections.
 */
class Repository(
    private val collectionDao: CollectionDao,
    private val stickerDao: StickerDao
) {
    /** On first launch, creates a starter active collection so the app isn't empty. */
    suspend fun ensureActiveCollection() {
        if (collectionDao.getActive() == null) {
            createCollection("Copa do Mundo 2026", numberedTotal = 0)
        }
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
