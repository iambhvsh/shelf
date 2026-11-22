package `in`.iambhvsh.shelf.features.collection

import androidx.lifecycle.viewModelScope
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.data.Collection
import `in`.iambhvsh.shelf.data.SortOrder
import `in`.iambhvsh.shelf.util.Resource
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal fun CollectionViewModel.saveBookmarkInCollection() {
    val collectionId = _state.value.selectedCollection?.id ?: return
    val rawUrl = _state.value.addBookmarkUrl.trim()
    if (rawUrl.isEmpty()) {
        _state.update { it.copy(showAddBookmarkDialog = false, addBookmarkUrl = "") }
        return
    }

    val url = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
        "https://$rawUrl"
    } else {
        rawUrl
    }

    val normalizedInput = url.removeSuffix("/")
    val isDuplicate = _state.value.collectionBookmarks.any {
        it.url.removeSuffix("/") == normalizedInput
    }

    if (isDuplicate) {
        _state.update {
            it.copy(
                addBookmarkUrl = "",
                showAddBookmarkDialog = false,
                toastMessage = "Link already exists in this collection"
            )
        }
        return
    }

    viewModelScope.launch {
        try {
            _state.update { it.copy(isSavingBookmark = true) }
            val meta = parser.parse(url)

            val bookmark = Bookmark(
                url = meta?.url ?: url,
                title = meta?.title,
                description = meta?.description,
                imageUrl = meta?.imageUrl
            )
            
            val insertedId = repository.insertHiddenBookmark(bookmark)
            if (insertedId > 0 && collectionId != Collection.UNCATEGORISED_ID) {
                repository.addBookmarkToCollection(insertedId, collectionId)
            }

            _state.update {
                it.copy(
                    isSavingBookmark = false,
                    addBookmarkUrl = "",
                    toastMessage = "Added to collection"
                )
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isSavingBookmark = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
}

internal fun CollectionViewModel.selectCollection(collection: Collection) {
    if (_state.value.selectedCollection?.id == collection.id && _state.value.collectionBookmarks.isNotEmpty()) {
        _state.update { it.copy(selectedCollection = collection) }
        return
    }
    collectionJob?.cancel()
    _state.update { it.copy(selectedCollection = collection, collectionBookmarks = emptyList(), isDetailLoading = true) }
    collectionJob = viewModelScope.launch {
        repository.getBookmarksInCollection(collection.id).collect { resource ->
            when (resource) {
                is Resource.Loading -> _state.update { it.copy(isDetailLoading = true) }
                is Resource.Error -> _state.update { it.copy(isDetailLoading = false, error = resource.errorMessage ?: "Error") }
                is Resource.Success -> {
                    val items = resource.data ?: emptyList()
                    rawCollectionBookmarks = items
                    val sortOrder = _state.value.sortOrder
                    _state.update { it.copy(isDetailLoading = false, collectionBookmarks = sortBookmarks(items, sortOrder)) }
                }
            }
        }
    }
}

internal fun CollectionViewModel.selectCollectionById(collectionId: Long) {
    if (_state.value.selectedCollection?.id == collectionId && _state.value.collectionBookmarks.isNotEmpty()) {
        return
    }
    collectionJob?.cancel()
    val existing = if (collectionId == Collection.UNCATEGORISED_ID) {
        _state.value.collections.find { it.id == collectionId } ?: Collection(
            id = Collection.UNCATEGORISED_ID,
            name = "Uncategorised",
            isVirtual = true
        )
    } else {
        _state.value.collections.find { it.id == collectionId }
    }

    _state.update {
        it.copy(
            selectedCollection = existing ?: it.selectedCollection?.takeIf { col -> col.id == collectionId },
            collectionBookmarks = if (it.selectedCollection?.id == collectionId) it.collectionBookmarks else emptyList(),
            isDetailLoading = true
        )
    }

    collectionJob = viewModelScope.launch {
        repository.getBookmarksInCollection(collectionId).collect { resource ->
            when (resource) {
                is Resource.Loading -> _state.update { it.copy(isDetailLoading = true) }
                is Resource.Error -> _state.update { it.copy(isDetailLoading = false, error = resource.errorMessage ?: "Error") }
                is Resource.Success -> {
                    val items = resource.data ?: emptyList()
                    rawCollectionBookmarks = items
                    val sortOrder = _state.value.sortOrder
                    val col = _state.value.selectedCollection ?: if (collectionId == Collection.UNCATEGORISED_ID) {
                        Collection(id = Collection.UNCATEGORISED_ID, name = "Uncategorised", isVirtual = true)
                    } else {
                        _state.value.collections.find { it.id == collectionId }
                    }
                    _state.update {
                        it.copy(
                            selectedCollection = col ?: it.selectedCollection,
                            isDetailLoading = false,
                            collectionBookmarks = sortBookmarks(items, sortOrder)
                        )
                    }
                }
            }
        }
    }
}

internal fun CollectionViewModel.createCollection() {
    val name = _state.value.inputName.trim()
    if (name.isEmpty()) return
    viewModelScope.launch {
        repository.createCollection(name)
        _state.update { it.copy(showCreateDialog = false, inputName = "") }
    }
}

internal fun CollectionViewModel.deleteSelected() {
    val selected = _state.value.collections.filter { it.id in _state.value.selectedIds && !it.isVirtual }
    if (selected.isEmpty()) return
    viewModelScope.launch {
        selected.forEach { repository.deleteCollection(it) }
        _state.update { it.copy(selectedIds = emptySet(), isSelectionMode = false, toastMessage = "Deleted collection" + if (selected.size > 1) "s" else "") }
    }
}

internal fun CollectionViewModel.deleteCollectionById(collectionId: Long) {
    if (collectionId == Collection.UNCATEGORISED_ID) return
    viewModelScope.launch {
        val collection = _state.value.collections.find { it.id == collectionId } ?: return@launch
        repository.deleteCollection(collection)
        _state.update { it.copy(toastMessage = "Deleted collection") }
        backToCollections()
    }
}

internal fun CollectionViewModel.removeSelectedFromCollection(collectionId: Long) {
    val ids = _state.value.detailSelectedIds.toList()
    if (ids.isEmpty()) return
    viewModelScope.launch {
        ids.forEach { repository.removeBookmarkFromCollection(it, collectionId) }
        _state.update { it.copy(detailSelectedIds = emptySet(), isDetailSelectionMode = false, toastMessage = "Removed from collection") }
    }
}

internal fun CollectionViewModel.deleteBookmark(bookmark: Bookmark) {
    viewModelScope.launch {
        try {
            repository.hideBookmarks(listOf(bookmark.id))
            _state.update {
                it.copy(
                    toastMessage = "Deleted bookmark",
                    isDetailBodySheet = false,
                    tempBookmark = null,
                    tempBookmarkTags = emptyList()
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(error = e.message ?: "Delete failed") }
        }
    }
}

internal fun CollectionViewModel.loadCollections() {
    viewModelScope.launch {
        repository.getAllCollections().collect { resource ->
            when (resource) {
                is Resource.Loading -> _state.update { it.copy(isLoading = true) }
                is Resource.Error -> _state.update { it.copy(isLoading = false, error = resource.errorMessage ?: "Error") }
                is Resource.Success -> _state.update { it.copy(isLoading = false, collections = resource.data ?: emptyList()) }
            }
        }
    }
}

internal fun sortBookmarks(bookmarks: List<Bookmark>, sortOrder: SortOrder): List<Bookmark> {
    return when (sortOrder) {
        SortOrder.DATE_NEWEST -> bookmarks.sortedWith(compareByDescending<Bookmark> { it.isPinned }.thenByDescending { it.createdAt })
        SortOrder.DATE_OLDEST -> bookmarks.sortedWith(compareByDescending<Bookmark> { it.isPinned }.thenBy { it.createdAt })
        SortOrder.TITLE_ASC -> bookmarks.sortedWith(compareByDescending<Bookmark> { it.isPinned }.thenBy { it.title?.lowercase() })
        SortOrder.TITLE_DESC -> bookmarks.sortedWith(compareByDescending<Bookmark> { it.isPinned }.thenByDescending { it.title?.lowercase() })
    }
}

internal fun CollectionViewModel.loadTags() {
    tagsJob?.cancel()
    tagsJob = viewModelScope.launch {
        repository.getAllTags().collect { resource ->
            if (resource is Resource.Success) {
                _state.update { it.copy(tags = resource.data ?: emptyList()) }
            }
        }
    }
}
