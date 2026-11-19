package `in`.iambhvsh.shelf.home

import androidx.lifecycle.viewModelScope
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.data.SortOrder
import `in`.iambhvsh.shelf.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
            repository.hideBookmarks(ids)
            _state.update {
                it.copy(
                    selectedIds = emptySet(),
                    isSelectionMode = false,
                    toastMessage = "Deleted bookmark" + if (ids.size > 1) "s" else ""
                )
            }
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
    val url = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
        "https://$rawUrl"
    } else {
        rawUrl
    }

    val normalizedInput = url.removeSuffix("/")
    val isDuplicate = _state.value.bookmarkData.any {
        it.url.removeSuffix("/") == normalizedInput
    }
    if (isDuplicate) {
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
                    inputUrl = if (inserted) "" else it.inputUrl,
                    duplicateToastKey = if (inserted) it.duplicateToastKey else it.duplicateToastKey + 1
                )
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(error = e.message ?: "Unknown error")
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
                _state.update { it.copy(tags = resource.data ?: emptyList()) }
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
                    val sortOrder = _state.value.sortOrder
                    val sortedItems = sortBookmarks(items, sortOrder)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            bookmarkData = sortedItems
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
        if (missing.isEmpty() || isFetchingMetadata) return@launch
        isFetchingMetadata = true
        missing.map { bm ->
            async {
                try {
                    val meta = parser.parse(bm.url)
                    if (!meta?.imageUrl.isNullOrBlank()) {
                        repository.updateImageUrl(bm.id, meta.imageUrl)
                    }
                } catch (_: Exception) { }
            }
        }.forEach { it.await() }
        isFetchingMetadata = false
    }
}

internal suspend fun sortBookmarks(bookmarks: List<Bookmark>, sortOrder: SortOrder): List<Bookmark> = withContext(Dispatchers.Default) {
    when (sortOrder) {
        SortOrder.DATE_NEWEST -> bookmarks.sortedWith(compareByDescending<Bookmark> { it.isPinned }.thenByDescending { it.createdAt })
        SortOrder.DATE_OLDEST -> bookmarks.sortedWith(compareByDescending<Bookmark> { it.isPinned }.thenBy { it.createdAt })
        SortOrder.TITLE_ASC -> bookmarks.sortedWith(compareByDescending<Bookmark> { it.isPinned }.thenBy { it.title?.lowercase() })
        SortOrder.TITLE_DESC -> bookmarks.sortedWith(compareByDescending<Bookmark> { it.isPinned }.thenByDescending { it.title?.lowercase() })
    }
}
