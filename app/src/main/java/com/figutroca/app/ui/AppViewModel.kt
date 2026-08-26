package com.figutroca.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.figutroca.app.data.AppDatabase
import com.figutroca.app.data.Collection
import com.figutroca.app.data.CollectionStats
import com.figutroca.app.data.Repository
import com.figutroca.app.data.Sticker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class StickerFilter(val label: String) {
    ALL("Todas"),
    OWNED("Tenho"),
    MISSING("Faltam"),
    DUPLICATES("Repetidas")
}

data class AlbumUiState(
    val activeCollection: Collection? = null,
    val stats: CollectionStats = CollectionStats(0, 0, 0),
    val stickers: List<Sticker> = emptyList(),
    val loading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: Repository = run {
        val db = AppDatabase.get(app)
        Repository(db.collectionDao(), db.stickerDao())
    }

    init {
        viewModelScope.launch { repo.ensureActiveCollection() }
    }

    val filter = MutableStateFlow(StickerFilter.ALL)
    val query = MutableStateFlow("")

    private val activeCollection = repo.observeActiveCollection()

    val collections: StateFlow<List<Collection>> =
        repo.observeCollections()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val activeStats = activeCollection.flatMapLatest { c ->
        if (c == null) flowOf(null) else repo.observeStats(c.id)
    }

    private val activeStickers = activeCollection.flatMapLatest { c ->
        if (c == null) flowOf(emptyList()) else repo.observeStickers(c.id)
    }

    /** Full album state (unfiltered), used by the header and Collections screen. */
    val albumState: StateFlow<AlbumUiState> =
        combine(activeCollection, activeStats, activeStickers) { c, stats, stickers ->
            AlbumUiState(
                activeCollection = c,
                stats = stats ?: CollectionStats(stickers.size, stickers.count { it.owned },
                    stickers.sumOf { it.duplicates }),
                stickers = stickers,
                loading = false
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AlbumUiState())

    /** Stickers after applying the active filter + search query. */
    val visibleStickers: StateFlow<List<Sticker>> =
        combine(activeStickers, filter, query) { stickers, f, q ->
            stickers.asSequence()
                .filter { s ->
                    when (f) {
                        StickerFilter.ALL -> true
                        StickerFilter.OWNED -> s.owned
                        StickerFilter.MISSING -> s.missing
                        StickerFilter.DUPLICATES -> s.duplicates > 0
                    }
                }
                .filter { s ->
                    q.isBlank() || s.code.contains(q, true) || s.group.contains(q, true)
                }
                .toList()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---- Actions ----------------------------------------------------------

    fun setFilter(f: StickerFilter) { filter.value = f }
    fun setQuery(q: String) { query.value = q }

    fun increment(s: Sticker) = viewModelScope.launch { repo.setCount(s, s.count + 1) }
    fun decrement(s: Sticker) = viewModelScope.launch { repo.setCount(s, s.count - 1) }
    fun setCount(s: Sticker, count: Int) = viewModelScope.launch { repo.setCount(s, count) }
    fun deleteSticker(s: Sticker) = viewModelScope.launch { repo.deleteSticker(s) }

    fun addRange(from: Int, to: Int, group: String) = viewModelScope.launch {
        val id = albumState.value.activeCollection?.id ?: return@launch
        repo.addNumberedRange(id, from, to, group)
    }

    fun addCodes(raw: String, group: String) = viewModelScope.launch {
        val id = albumState.value.activeCollection?.id ?: return@launch
        repo.addCodes(id, raw, group)
    }

    fun createCollection(name: String, numberedTotal: Int) = viewModelScope.launch {
        repo.createCollection(name, numberedTotal)
    }

    fun archiveAndStartNew(name: String, numberedTotal: Int) = viewModelScope.launch {
        repo.archiveAndStartNew(name, numberedTotal)
    }

    fun archiveCollection(c: Collection) = viewModelScope.launch { repo.archiveCollection(c) }
    fun activateCollection(c: Collection) = viewModelScope.launch { repo.activateCollection(c) }
    fun deleteCollection(c: Collection) = viewModelScope.launch { repo.deleteCollection(c) }
    fun renameCollection(c: Collection, name: String) =
        viewModelScope.launch { repo.renameCollection(c, name) }
}
