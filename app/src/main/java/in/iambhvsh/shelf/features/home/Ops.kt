package `in`.iambhvsh.shelf.features.home

import androidx.lifecycle.viewModelScope
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.data.SortOrder
import `in`.iambhvsh.shelf.features.fetch.UrlUtils
import `in`.iambhvsh.shelf.util.Resource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

internal fun HomeViewModel.checkForUpdates() {
    viewModelScope.launch {
        val hasUpdate = updateManager.checkForUpdates(force = false)
        if (hasUpdate) {
            _state.update { it.copy(showUpdateSheet = true) }
        }
    }
}

internal fun HomeViewModel.deleteSelected() {
    val ids = _state.value.selectedIds.toList()
    if (ids.isEmpty()) return
    viewModelScope.launch {
        try {
            ids.forEach { reminderManager.cancelReminder(it) }
            repository.hideBookmarks(ids)
            _state.update {
                it.copy(
                    selectedIds = emptySet(),
                    isSelectionMode = false,
                    toastMessage = "Deleted bookmark" + if (ids.size > 1) "s" else ""
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.update {
                it.copy(error = e.message ?: "Delete failed")
            }
        }
    }
}

internal fun HomeViewModel.saveBookmark() {
    val rawUrl = _state.value.inputUrl.trim()
    if (rawUrl.isEmpty()) return
    val url = UrlUtils.ensureHttpScheme(rawUrl)

    val normalizedInput = UrlUtils.normalizeUrlKey(url)
    val isDuplicateInView = _state.value.bookmarkData.any {
        UrlUtils.normalizeUrlKey(it.url) == normalizedInput
    }
    if (isDuplicateInView) {
        _state.update {
            it.copy(
                inputUrl = "",
                duplicateToastKey = it.duplicateToastKey + 1,
                isDialog = false
            )
        }
        return
    }

    viewModelScope.launch {
        try {
            if (repository.existsByUrl(url)) {
                _state.update {
                    it.copy(
                        inputUrl = "",
                        duplicateToastKey = it.duplicateToastKey + 1,
                        isDialog = false
                    )
                }
                return@launch
            }
            _state.update { it.copy(isLoading = true) }
            val meta = parser.parse(url)

            val bookmark = Bookmark(
                url = meta?.url ?: url,
                title = meta?.title,
                description = meta?.description,
                imageUrl = meta?.imageUrl
            )
            val inserted = repository.insert(bookmark)
            _state.update {
                it.copy(
                    isLoading = false,
                    isDialog = false,
                    inputUrl = "",
                    duplicateToastKey = if (inserted) it.duplicateToastKey else it.duplicateToastKey + 1
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
}

internal fun HomeViewModel.loadCollections() {
    viewModelScope.launch {
        repository.getAllCollections().collect { resource ->
            if (resource is Resource.Success) {
                _state.update { it.copy(collections = resource.data ?: emptyList()) }
            }
        }
    }
}

internal fun HomeViewModel.loadTags() {
    viewModelScope.launch {
        repository.getAllTags().collect { resource ->
            if (resource is Resource.Success) {
                val loadedTags = resource.data ?: emptyList()
                val validIds = loadedTags.map { it.id }.toSet()
                val previous = _state.getAndUpdate { current ->
                    val filtered = current.activeTagFilters.intersect(validIds)
                    current.copy(tags = loadedTags, activeTagFilters = filtered)
                }
                if (previous.activeTagFilters.any { it !in validIds }) {
                    loadBookmarks()
                }
            }
        }
    }
}

internal fun HomeViewModel.addSelectedToCollection(collectionId: Long) {
    val ids = _state.value.selectedIds.toList()
    if (ids.isEmpty()) return
    viewModelScope.launch {
        repository.addBookmarksToCollection(ids, collectionId)
        val collectionName = _state.value.collections.find { it.id == collectionId }?.name ?: "Collection"
        _state.update {
            it.copy(
                showCollectionPicker = false,
                selectedIds = emptySet(),
                isSelectionMode = false,
                toastMessage = "Added to $collectionName"
            )
        }
    }
}

internal fun HomeViewModel.loadBookmarks() {
    bookmarkJob?.cancel()
    bookmarkJob = viewModelScope.launch {
        val filters = _state.value.activeTagFilters
        val flow = if (filters.isEmpty()) {
            repository.getBookmarks()
        } else {
            repository.getBookmarksByTags(filters.toList())
        }
        flow.collect { data ->
            when (data) {
                is Resource.Error<*> -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = data.errorMessage ?: "Unknown error"
                        )
                    }
                }
                is Resource.Loading<*> -> {
                    _state.update { it.copy(isLoading = true) }
                }
                is Resource.Success<*> -> {
                    val items = data.data ?: emptyList()
                    rawBookmarks = items
                    sortJob?.cancel()
                    var sortOrder = _state.value.sortOrder
                    var sortedItems = sortBookmarks(items, sortOrder, defaultDispatcher)
                    while (_state.value.sortOrder != sortOrder) {
                        sortOrder = _state.value.sortOrder
                        sortedItems = sortBookmarks(items, sortOrder, defaultDispatcher)
                    }
                    val itemIds = items.map { it.id }.toSet()
                    _state.update { current ->
                        val prunedSelected = current.selectedIds.intersect(itemIds)
                        val refreshedTemp = current.tempBookmark?.let { tmp ->
                            items.find { it.id == tmp.id }
                        }
                        current.copy(
                            isLoading = false,
                            bookmarkData = sortedItems,
                            selectedIds = prunedSelected,
                            isSelectionMode = prunedSelected.isNotEmpty(),
                            tempBookmark = refreshedTemp ?: current.tempBookmark
                        )
                    }
                    fetchMissingMetadataOnStart()
                }
            }
        }
    }
}

internal fun HomeViewModel.fetchMissingMetadataOnStart() {
    viewModelScope.launch {
        val missing = repository.getBookmarksWithoutImage()
        val candidates = synchronized(attemptedStartupMetadataIds) {
            missing.filter { attemptedStartupMetadataIds.add(it.id) }
        }
        if (candidates.isEmpty()) return@launch
        isFetchingMetadata = true
        try {
            candidates.map { bm ->
                async {
                    startupMetadataSemaphore.withPermit {
                        try {
                            val meta = parser.parse(bm.url)
                            if (!meta?.imageUrl.isNullOrBlank()) {
                                repository.updateImageUrl(bm.id, meta.imageUrl)
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                        }
                    }
                }
            }.awaitAll()
        } finally {
            isFetchingMetadata = false
        }
    }
}

internal suspend fun sortBookmarks(
    bookmarks: List<Bookmark>,
    sortOrder: SortOrder,
    dispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.Default
): List<Bookmark> = withContext(dispatcher) {
    when (sortOrder) {
        SortOrder.DATE_NEWEST -> bookmarks.sortedWith(compareByDescending<Bookmark> { it.isPinned }.thenByDescending { it.createdAt })
        SortOrder.DATE_OLDEST -> bookmarks.sortedWith(compareByDescending<Bookmark> { it.isPinned }.thenBy { it.createdAt })
        SortOrder.TITLE_ASC -> bookmarks.sortedWith(compareByDescending<Bookmark> { it.isPinned }.thenBy { it.title?.lowercase() })
        SortOrder.TITLE_DESC -> bookmarks.sortedWith(compareByDescending<Bookmark> { it.isPinned }.thenByDescending { it.title?.lowercase() })
    }
}
