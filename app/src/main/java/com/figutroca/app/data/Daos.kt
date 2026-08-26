package com.figutroca.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Aggregate progress numbers for one collection. */
data class CollectionStats(
    val total: Int,
    val owned: Int,
    val duplicates: Int
) {
    val missing: Int get() = total - owned
    val completion: Float get() = if (total == 0) 0f else owned.toFloat() / total
}

@Dao
interface CollectionDao {

    @Insert
    suspend fun insert(collection: Collection): Long

    @Update
    suspend fun update(collection: Collection)

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM collections ORDER BY isActive DESC, createdAt DESC")
    fun observeAll(): Flow<List<Collection>>

    @Query("SELECT * FROM collections WHERE isActive = 1 LIMIT 1")
    fun observeActive(): Flow<Collection?>

    @Query("SELECT * FROM collections WHERE isActive = 1 LIMIT 1")
    suspend fun getActive(): Collection?

    @Query("UPDATE collections SET isActive = 0")
    suspend fun clearActive()

    @Query("UPDATE collections SET isActive = 1, archivedAt = null WHERE id = :id")
    suspend fun markActive(id: Long)

    @Query("UPDATE collections SET isActive = 0, archivedAt = :ts WHERE id = :id")
    suspend fun archive(id: Long, ts: Long)
}

@Dao
interface StickerDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(stickers: List<Sticker>)

    @Update
    suspend fun update(sticker: Sticker)

    @Query("DELETE FROM stickers WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM stickers WHERE collectionId = :collectionId ORDER BY sortKey, code")
    fun observeForCollection(collectionId: Long): Flow<List<Sticker>>

    @Query("UPDATE stickers SET count = :count WHERE id = :id")
    suspend fun setCount(id: Long, count: Int)

    @Query(
        """
        SELECT COUNT(*) AS total,
               SUM(CASE WHEN count > 0 THEN 1 ELSE 0 END) AS owned,
               SUM(CASE WHEN count > 1 THEN count - 1 ELSE 0 END) AS duplicates
        FROM stickers WHERE collectionId = :collectionId
        """
    )
    fun observeStats(collectionId: Long): Flow<CollectionStats?>

    @Query("SELECT COUNT(*) FROM stickers WHERE collectionId = :collectionId")
    suspend fun countFor(collectionId: Long): Int
}
