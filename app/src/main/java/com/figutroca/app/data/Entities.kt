package com.figutroca.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A sticker album/collection — e.g. "Copa do Mundo 2026".
 * Only one collection is active at a time; older ones are archived.
 */
@Entity(tableName = "collections")
data class Collection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    /** Timestamp when the collection was archived, or null while active. */
    val archivedAt: Long? = null,
    /** Exactly one collection has isActive = true. */
    val isActive: Boolean = false
)

/**
 * A single sticker slot inside a collection.
 * [count] is how many copies the user physically owns:
 *   - 0        -> missing (falta)
 *   - 1        -> owned, no duplicates
 *   - 2 or more -> owned, with (count - 1) duplicates for trading (repetidas)
 */
@Entity(
    tableName = "stickers",
    foreignKeys = [
        ForeignKey(
            entity = Collection::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("collectionId"), Index(value = ["collectionId", "code"], unique = true)]
)
data class Sticker(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val collectionId: Long,
    /** Sticker identifier as printed in the album, e.g. "1", "23", "ARG 4", "FWC". */
    val code: String,
    /** Optional group/section label, e.g. team or country name. */
    @ColumnInfo(name = "grp") val group: String = "",
    val count: Int = 0,
    /** Numeric key used to sort codes naturally (1, 2, 10 instead of 1, 10, 2). */
    val sortKey: Long = 0
) {
    val owned: Boolean get() = count > 0
    val missing: Boolean get() = count == 0
    val duplicates: Int get() = if (count > 1) count - 1 else 0
}
