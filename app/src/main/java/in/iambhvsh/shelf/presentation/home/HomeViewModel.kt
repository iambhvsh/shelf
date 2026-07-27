package `in`.iambhvsh.shelf.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.iambhvsh.shelf.domain.model.Bookmark
import `in`.iambhvsh.shelf.domain.model.Collection
import `in`.iambhvsh.shelf.domain.repository.BookmarkRepository
import `in`.iambhvsh.shelf.link_fetcher.LinkMetadataParser
import `in`.iambhvsh.shelf.domain.model.SortOrder
import `in`.iambhvsh.shelf.utils.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

class HomeViewModel(private val repository: BookmarkRepository) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    private val parser = LinkMetadataParser()
    private var rawBookmarks: List<Bookmark> = emptyList()
    private var bookmarkJob: Job? = null
    private var tempBookmarkTagsJob: Job? = null
    private var isFetchingMetadata = false

    init {
        loadBookmarks()
        loadCollections()
        loadTags()
        fetchMissingMetadataOnStart()
    }

    fun homeEvents(events: HomeEvents) {
        when (events) {
            is HomeEvents.OnTextFieldValueChange -> {
                _state.update { it.copy(inputUrl = events.text) }
            }

            HomeEvents.SaveBookmark -> {
                saveBookmark()
            }

            HomeEvents.OnDialogDismissClick -> {
                _state.update {
                    it.copy(
                        isDialog = !it.isDialog,
                    )
                }
            }

            HomeEvents.FabClick -> {
                _state.update { it.copy(isDialog = true) }
            }

            is HomeEvents.PreviewImageClick -> {
                _state.update {
                    it.copy(
                        isPhotoPreviewDialog = true,
                        dialogPhotoUrl = events.url
                    )
                }
            }

            HomeEvents.PreviewImageDismissClick -> {
                _state.update {
                    it.copy(isPhotoPreviewDialog = false)
                }
            }

            is HomeEvents.BookmarkPreviewClick -> {
                _state.update {
                    it.copy(
                        tempBookmark = events.bookmark,
                        isBodySheet = true
                    )
                }
                tempBookmarkTagsJob?.cancel()
                tempBookmarkTagsJob = viewModelScope.launch {
                    repository.getTagsForBookmark(events.bookmark.id).collect { resource ->
                        if (resource is Resource.Success) {
                            _state.update { it.copy(tempBookmarkTags = resource.data ?: emptyList()) }
                        }
                    }
                }
            }

            HomeEvents.BookmarkPreviewDismissClick -> {
                tempBookmarkTagsJob?.cancel()
                _state.update {
                    it.copy(
                        isBodySheet = false,
                        tempBookmark = null,
                        tempBookmarkTags = emptyList()
                    )
                }
            }

            is HomeEvents.ToggleSelection -> {
                val current = _state.value
                val newSelected = if (events.id in current.selectedIds) {
                    current.selectedIds - events.id
                } else {
                    current.selectedIds + events.id
                }
                _state.update {
                    it.copy(
                        selectedIds = newSelected,
                        isSelectionMode = newSelected.isNotEmpty()
                    )
                }
            }

            HomeEvents.DeleteSelected -> {
                deleteSelected()
            }

            HomeEvents.ClearSelection -> {
                _state.update {
                    it.copy(
                        selectedIds = emptySet(),
                        isSelectionMode = false
                    )
                }
            }

            HomeEvents.SelectAll -> {
                val allIds = _state.value.bookmarkData.map { it.id }.toSet()
                _state.update {
                    it.copy(
                        selectedIds = allIds,
                        isSelectionMode = allIds.isNotEmpty()
                    )
                }
            }

            HomeEvents.DeselectAll -> {
                _state.update {
                    it.copy(
                        selectedIds = emptySet(),
                        isSelectionMode = false
                    )
                }
            }

            HomeEvents.ShowCollectionPicker -> {
                _state.update { it.copy(showCollectionPicker = true) }
            }

            HomeEvents.HideCollectionPicker -> {
                _state.update { it.copy(showCollectionPicker = false) }
            }

            is HomeEvents.AddToCollection -> {
                addSelectedToCollection(events.collectionId)
            }

            is HomeEvents.SetSortOrder -> {
                _state.update {
                    it.copy(
                        sortOrder = events.sortOrder,
                        showSortSheet = false,
                        bookmarkData = sortBookmarks(rawBookmarks, events.sortOrder)
                    )
                }
            }

            HomeEvents.ShowSortSheet -> {
                _state.update { it.copy(showSortSheet = true) }
            }

            HomeEvents.HideSortSheet -> {
                _state.update { it.copy(showSortSheet = false) }
            }
            
            is HomeEvents.TogglePin -> {
                viewModelScope.launch {
                    repository.togglePinStatus(events.bookmark.id, !events.bookmark.isPinned)
                }
            }
            
            HomeEvents.ShowTagManager -> {
                _state.update { it.copy(showTagManager = true) }
            }
            
            HomeEvents.HideTagManager -> {
                _state.update { it.copy(showTagManager = false) }
            }
            
            is HomeEvents.CreateTag -> {
                viewModelScope.launch {
                    repository.insertTag(events.name)
                }
            }
            
            is HomeEvents.ToggleTagForBookmark -> {
                val tempBm = _state.value.tempBookmark ?: return
                viewModelScope.launch {
                    if (events.isChecked) {
                        repository.addTagToBookmark(tempBm.id, events.tag.id)
                    } else {
                        repository.removeTagFromBookmark(tempBm.id, events.tag.id)
                    }
                }
            }
            
            is HomeEvents.ToggleTagFilter -> {
                _state.update { state ->
                    val newFilters = state.activeTagFilters.toMutableSet()
                    if (newFilters.contains(events.tagId)) {
                        newFilters.remove(events.tagId)
                    } else {
                        newFilters.add(events.tagId)
                    }
                    state.copy(activeTagFilters = newFilters)
                }
                loadBookmarks()
            }
        }
    }

    private fun deleteSelected() {
        val ids = _state.value.selectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            try {
                repository.hideBookmarks(ids)
                _state.update {
                    it.copy(
                        selectedIds = emptySet(),
                        isSelectionMode = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message ?: "Delete failed")
                }
            }
        }
    }

    fun saveBookmark() {

        val rawUrl = _state.value.inputUrl.trim()

        if (rawUrl.isEmpty()) return
        val url = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
            "https://$rawUrl"
        } else {
            rawUrl
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


    private fun loadCollections() {
        viewModelScope.launch {
            repository.getAllCollections().collect { resource ->
                if (resource is Resource.Success) {
                    _state.update { it.copy(collections = resource.data ?: emptyList()) }
                }
            }
        }
    }

    private fun loadTags() {
        viewModelScope.launch {
            repository.getAllTags().collect { resource ->
                if (resource is Resource.Success) {
                    _state.update { it.copy(tags = resource.data ?: emptyList()) }
                }
            }
        }
    }

    private fun addSelectedToCollection(collectionId: Long) {
        val ids = _state.value.selectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.addBookmarksToCollection(ids, collectionId)
            _state.update {
                it.copy(
                    showCollectionPicker = false,
                    selectedIds = emptySet(),
                    isSelectionMode = false
                )
            }
        }
    }

    private fun loadBookmarks() {
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
                        _state.update {
                            it.copy(
                                isLoading = false,
                                bookmarkData = sortBookmarks(items, sortOrder)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun fetchMissingMetadataOnStart() {
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

    private fun sortBookmarks(bookmarks: List<Bookmark>, sortOrder: SortOrder): List<Bookmark> {
        return when (sortOrder) {
            SortOrder.DATE_NEWEST -> bookmarks.sortedWith(compareByDescending<Bookmark> { it.isPinned }.thenByDescending { it.createdAt })
            SortOrder.DATE_OLDEST -> bookmarks.sortedWith(compareByDescending<Bookmark> { it.isPinned }.thenBy { it.createdAt })
            SortOrder.TITLE_ASC -> bookmarks.sortedWith(compareByDescending<Bookmark> { it.isPinned }.thenBy { it.title?.lowercase() })
            SortOrder.TITLE_DESC -> bookmarks.sortedWith(compareByDescending<Bookmark> { it.isPinned }.thenByDescending { it.title?.lowercase() })
        }
    }
}