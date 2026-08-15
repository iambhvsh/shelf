package `in`.iambhvsh.shelf.presentation.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.iambhvsh.shelf.domain.model.Bookmark
import `in`.iambhvsh.shelf.domain.model.Collection
import `in`.iambhvsh.shelf.domain.repository.BookmarkRepository
import `in`.iambhvsh.shelf.domain.model.SortOrder
import `in`.iambhvsh.shelf.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CollectionViewModel(
    private val repository: BookmarkRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CollectionState())
    val state = _state.asStateFlow()
    private var collectionJob: Job? = null
    private var tagsJob: Job? = null
    private var tempBookmarkTagsJob: Job? = null
    private var rawCollectionBookmarks: List<Bookmark> = emptyList()

    init {
        loadCollections()
        loadTags()
    }

    fun onEvent(event: CollectionEvents) {
        when (event) {
            is CollectionEvents.InputNameChanged -> {
                _state.update { it.copy(inputName = event.name) }
            }

            CollectionEvents.ShowCreateDialog -> {
                _state.update { it.copy(showCreateDialog = true, inputName = "") }
            }

            CollectionEvents.HideCreateDialog -> {
                _state.update { it.copy(showCreateDialog = false, inputName = "") }
            }

            CollectionEvents.CreateCollection -> {
                createCollection()
            }

            is CollectionEvents.SelectCollection -> {
                selectCollection(event.collection)
            }

            CollectionEvents.ClearSelectedCollection -> {
                collectionJob?.cancel()
                _state.update { it.copy(selectedCollection = null, collectionBookmarks = emptyList(), isDetailSelectionMode = false, detailSelectedIds = emptySet()) }
            }

            is CollectionEvents.ToggleSelection -> {
                val current = _state.value
                val newSelected = if (event.id in current.selectedIds) {
                    current.selectedIds - event.id
                } else {
                    current.selectedIds + event.id
                }
                _state.update {
                    it.copy(
                        selectedIds = newSelected,
                        isSelectionMode = newSelected.isNotEmpty()
                    )
                }
            }

            CollectionEvents.ClearSelection -> {
                _state.update { it.copy(selectedIds = emptySet(), isSelectionMode = false) }
            }

            CollectionEvents.SelectAll -> {
                val allIds = _state.value.collections.map { it.id }.toSet()
                _state.update {
                    it.copy(
                        selectedIds = allIds,
                        isSelectionMode = allIds.isNotEmpty()
                    )
                }
            }

            CollectionEvents.DeselectAll -> {
                _state.update { it.copy(selectedIds = emptySet(), isSelectionMode = false) }
            }

            CollectionEvents.DeleteSelected -> {
                deleteSelected()
            }

            is CollectionEvents.DeleteCollectionById -> {
                deleteCollectionById(event.collectionId)
            }

            is CollectionEvents.ShowDetailBodySheet -> {
                _state.update { it.copy(tempBookmark = event.bookmark, isDetailBodySheet = true) }
                tempBookmarkTagsJob?.cancel()
                tempBookmarkTagsJob = viewModelScope.launch {
                    repository.getTagsForBookmark(event.bookmark.id).collect { resource ->
                        if (resource is Resource.Success) {
                            _state.update { it.copy(tempBookmarkTags = resource.data ?: emptyList()) }
                        }
                    }
                }
            }

            CollectionEvents.DismissDetailBodySheet -> {
                val s = _state.value
                if (s.showRenameBookmarkDialog || s.showTagManager || s.showNoteEditor || s.showReminderPicker) {
                    _state.update { it.copy(isDetailBodySheet = false) }
                } else {
                    tempBookmarkTagsJob?.cancel()
                    _state.update { it.copy(tempBookmark = null, isDetailBodySheet = false, tempBookmarkTags = emptyList()) }
                }
            }

            is CollectionEvents.ToggleDetailSelection -> {
                val current = _state.value
                val newSelected = if (event.id in current.detailSelectedIds) {
                    current.detailSelectedIds - event.id
                } else {
                    current.detailSelectedIds + event.id
                }
                _state.update {
                    it.copy(
                        detailSelectedIds = newSelected,
                        isDetailSelectionMode = newSelected.isNotEmpty()
                    )
                }
            }

            CollectionEvents.ClearDetailSelection -> {
                _state.update { it.copy(detailSelectedIds = emptySet(), isDetailSelectionMode = false) }
            }

            CollectionEvents.SelectAllDetail -> {
                val allIds = _state.value.collectionBookmarks.map { it.id }.toSet()
                _state.update {
                    it.copy(
                        detailSelectedIds = allIds,
                        isDetailSelectionMode = allIds.isNotEmpty()
                    )
                }
            }

            CollectionEvents.DeselectAllDetail -> {
                _state.update { it.copy(detailSelectedIds = emptySet(), isDetailSelectionMode = false) }
            }

            is CollectionEvents.RemoveSelectedFromCollection -> {
                removeSelectedFromCollection(event.collectionId)
            }

            is CollectionEvents.SetSortOrder -> {
                _state.update {
                    it.copy(
                        sortOrder = event.sortOrder,
                        showSortSheet = false,
                        collectionBookmarks = sortBookmarks(rawCollectionBookmarks, event.sortOrder)
                    )
                }
            }

            CollectionEvents.ShowSortSheet -> {
                _state.update { it.copy(showSortSheet = true) }
            }

            CollectionEvents.HideSortSheet -> {
                _state.update { it.copy(showSortSheet = false) }
            }

            is CollectionEvents.ShowRenameCollectionDialog -> {
                _state.update { it.copy(showRenameCollectionDialog = true, renameCollectionDialogText = event.initialName) }
            }

            CollectionEvents.HideRenameCollectionDialog -> {
                _state.update { it.copy(showRenameCollectionDialog = false, renameCollectionDialogText = null) }
            }

            is CollectionEvents.UpdateCollectionName -> {
                val newName = event.name.trim()
                if (newName.isBlank()) return // Validation: do not save empty
                
                viewModelScope.launch {
                    val currentCollection = _state.value.collections.find { it.id == event.id }
                    if (currentCollection != null && currentCollection.name != newName) {
                        repository.updateCollectionName(event.id, newName)
                    }
                    _state.update { it.copy(showRenameCollectionDialog = false, renameCollectionDialogText = null, toastMessage = "Collection renamed") }
                }
            }

            is CollectionEvents.TogglePin -> {
                viewModelScope.launch {
                    repository.togglePinStatus(event.bookmark.id, !event.bookmark.isPinned)
                }
            }

            is CollectionEvents.ShowRenameBookmarkDialog -> {
                _state.update { it.copy(showRenameBookmarkDialog = true, renameBookmarkDialogText = event.initialTitle, renameBookmarkDialogId = event.id, isDetailBodySheet = false) }
            }

            CollectionEvents.HideRenameBookmarkDialog -> {
                _state.update { it.copy(showRenameBookmarkDialog = false, renameBookmarkDialogText = null, renameBookmarkDialogId = null, tempBookmark = null) }
            }

            is CollectionEvents.UpdateBookmarkTitle -> {
                val newTitle = event.title.trim()
                if (newTitle.isBlank()) return // Validation: do not save empty
                
                viewModelScope.launch {
                    repository.updateBookmarkTitle(event.id, newTitle)
                    _state.update { it.copy(showRenameBookmarkDialog = false, renameBookmarkDialogText = null, renameBookmarkDialogId = null, tempBookmark = null, toastMessage = "Bookmark renamed") }
                }
            }

            CollectionEvents.ShowTagManager -> {
                _state.update { it.copy(showTagManager = true, isDetailBodySheet = false) }
            }

            CollectionEvents.HideTagManager -> {
                _state.update { 
                    it.copy(
                        showTagManager = false,
                        tempBookmark = null,
                        tempBookmarkTags = emptyList()
                    ) 
                }
            }

            is CollectionEvents.CreateTag -> {
                viewModelScope.launch {
                    repository.insertTag(event.name)
                }
            }

            is CollectionEvents.ToggleTagForBookmark -> {
                val tempBm = _state.value.tempBookmark ?: return
                viewModelScope.launch {
                    if (event.isChecked) {
                        repository.addTagToBookmark(tempBm.id, event.tag.id)
                    } else {
                        repository.removeTagFromBookmark(tempBm.id, event.tag.id)
                    }
                }
            }

            is CollectionEvents.DeleteTag -> {
                viewModelScope.launch {
                    repository.deleteTag(event.tagId)
                }
            }

            is CollectionEvents.ShowNoteEditor -> {
                _state.update { it.copy(showNoteEditor = true, noteEditorText = event.initialNote, isDetailBodySheet = false) }
            }

            CollectionEvents.HideNoteEditor -> {
                _state.update { it.copy(showNoteEditor = false, noteEditorText = null, tempBookmark = null) }
            }

            is CollectionEvents.UpdateNote -> {
                viewModelScope.launch {
                    repository.updateNote(event.id, event.note)
                    _state.update { it.copy(showNoteEditor = false, noteEditorText = null, tempBookmark = null) }
                }
            }

            CollectionEvents.ShowReminderPicker -> {
                _state.update { it.copy(showReminderPicker = true, isDetailBodySheet = false) }
            }

            CollectionEvents.HideReminderPicker -> {
                _state.update { it.copy(showReminderPicker = false, tempBookmark = null) }
            }

            is CollectionEvents.SetReminder -> {
                viewModelScope.launch {
                    repository.updateReminderTime(event.id, event.timeInMillis)
                    _state.update { it.copy(showReminderPicker = false, tempBookmark = null) }
                }
            }

            is CollectionEvents.CancelReminder -> {
                viewModelScope.launch {
                    repository.updateReminderTime(event.id, null)
                    _state.update { it.copy(showReminderPicker = false, tempBookmark = null) }
                }
            }
            
            CollectionEvents.ClearToast -> {
                _state.update { it.copy(toastMessage = null) }
            }
        }
    }

    fun backToCollections() {
        collectionJob?.cancel()
        _state.update { it.copy(selectedCollection = null, collectionBookmarks = emptyList()) }
    }

    private fun selectCollection(collection: Collection) {
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

    private fun createCollection() {
        val name = _state.value.inputName.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            repository.createCollection(name)
            _state.update { it.copy(showCreateDialog = false, inputName = "") }
        }
    }

    private fun deleteSelected() {
        val selected = _state.value.collections.filter { it.id in _state.value.selectedIds }
        if (selected.isEmpty()) return
        viewModelScope.launch {
            selected.forEach { repository.deleteCollection(it) }
            _state.update { it.copy(selectedIds = emptySet(), isSelectionMode = false, toastMessage = "Deleted collection" + if (selected.size > 1) "s" else "") }
        }
    }

    private fun deleteCollectionById(collectionId: Long) {
        viewModelScope.launch {
            val collection = _state.value.collections.find { it.id == collectionId } ?: return@launch
            repository.deleteCollection(collection)
            _state.update { it.copy(toastMessage = "Deleted collection") }
            backToCollections()
        }
    }

    private fun removeSelectedFromCollection(collectionId: Long) {
        val ids = _state.value.detailSelectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            ids.forEach { repository.removeBookmarkFromCollection(it, collectionId) }
            _state.update { it.copy(detailSelectedIds = emptySet(), isDetailSelectionMode = false, toastMessage = "Removed from collection") }
        }
    }

    private fun loadCollections() {
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

    private fun sortBookmarks(bookmarks: List<Bookmark>, sortOrder: SortOrder): List<Bookmark> {
        return when (sortOrder) {
            SortOrder.DATE_NEWEST -> bookmarks.sortedWith(compareByDescending<Bookmark> { it.isPinned }.thenByDescending { it.createdAt })
            SortOrder.DATE_OLDEST -> bookmarks.sortedWith(compareByDescending<Bookmark> { it.isPinned }.thenBy { it.createdAt })
            SortOrder.TITLE_ASC -> bookmarks.sortedWith(compareByDescending<Bookmark> { it.isPinned }.thenBy { it.title?.lowercase() })
            SortOrder.TITLE_DESC -> bookmarks.sortedWith(compareByDescending<Bookmark> { it.isPinned }.thenByDescending { it.title?.lowercase() })
        }
    }

    private fun loadTags() {
        tagsJob?.cancel()
        tagsJob = viewModelScope.launch {
            repository.getAllTags().collect { resource ->
                if (resource is Resource.Success) {
                    _state.update { it.copy(tags = resource.data ?: emptyList()) }
                }
            }
        }
    }
}
