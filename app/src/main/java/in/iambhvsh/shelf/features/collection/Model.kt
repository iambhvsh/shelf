package `in`.iambhvsh.shelf.features.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.data.Collection
import `in`.iambhvsh.shelf.data.BookmarkRepository
import `in`.iambhvsh.shelf.features.fetch.LinkMetadataParser
import `in`.iambhvsh.shelf.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CollectionViewModel(
    internal val repository: BookmarkRepository
) : ViewModel() {
    internal val _state = MutableStateFlow(CollectionState())
    val state = _state.asStateFlow()
    internal var collectionJob: Job? = null
    internal var tagsJob: Job? = null
    internal var tempBookmarkTagsJob: Job? = null
    internal var rawCollectionBookmarks: List<Bookmark> = emptyList()
    internal val parser = LinkMetadataParser()

    init {
        loadCollections()
        loadTags()
    }

    fun onEvent(event: CollectionEvents) {
        when (event) {
            is CollectionEvents.InputNameChanged -> _state.update { it.copy(inputName = event.name) }
            CollectionEvents.ShowCreateDialog -> _state.update { it.copy(showCreateDialog = true, inputName = "") }
            CollectionEvents.HideCreateDialog -> _state.update { it.copy(showCreateDialog = false, inputName = "") }
            CollectionEvents.CreateCollection -> createCollection()
            is CollectionEvents.SelectCollection -> selectCollection(event.collection)
            is CollectionEvents.SelectCollectionById -> selectCollectionById(event.collectionId)
            CollectionEvents.ClearSelectedCollection -> {
                collectionJob?.cancel()
                _state.update { it.copy(selectedCollection = null, collectionBookmarks = emptyList(), isDetailSelectionMode = false, detailSelectedIds = emptySet()) }
            }
            is CollectionEvents.ToggleSelection -> {
                if (event.id == Collection.UNCATEGORISED_ID) return
                val current = _state.value
                val newSelected = if (event.id in current.selectedIds) current.selectedIds - event.id else current.selectedIds + event.id
                _state.update { it.copy(selectedIds = newSelected, isSelectionMode = newSelected.isNotEmpty()) }
            }
            CollectionEvents.ClearSelection -> _state.update { it.copy(selectedIds = emptySet(), isSelectionMode = false) }
            CollectionEvents.SelectAll -> {
                val allIds = _state.value.collections.filter { !it.isVirtual }.map { it.id }.toSet()
                _state.update { it.copy(selectedIds = allIds, isSelectionMode = allIds.isNotEmpty()) }
            }
            CollectionEvents.DeselectAll -> _state.update { it.copy(selectedIds = emptySet(), isSelectionMode = false) }
            CollectionEvents.DeleteSelected -> deleteSelected()
            is CollectionEvents.DeleteCollectionById -> deleteCollectionById(event.collectionId)
            is CollectionEvents.DeleteBookmark -> deleteBookmark(event.bookmark)
            is CollectionEvents.ShowDetailBodySheet -> {
                _state.update { it.copy(tempBookmark = event.bookmark, isDetailBodySheet = true) }
                tempBookmarkTagsJob?.cancel()
                tempBookmarkTagsJob = viewModelScope.launch {
                    repository.getTagsForBookmark(event.bookmark.id).collect { resource ->
                        if (resource is Resource.Success) _state.update { it.copy(tempBookmarkTags = resource.data ?: emptyList()) }
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
                val newSelected = if (event.id in current.detailSelectedIds) current.detailSelectedIds - event.id else current.detailSelectedIds + event.id
                _state.update { it.copy(detailSelectedIds = newSelected, isDetailSelectionMode = newSelected.isNotEmpty()) }
            }
            CollectionEvents.ClearDetailSelection -> _state.update { it.copy(detailSelectedIds = emptySet(), isDetailSelectionMode = false) }
            CollectionEvents.SelectAllDetail -> {
                val allIds = _state.value.collectionBookmarks.map { it.id }.toSet()
                _state.update { it.copy(detailSelectedIds = allIds, isDetailSelectionMode = allIds.isNotEmpty()) }
            }
            CollectionEvents.DeselectAllDetail -> _state.update { it.copy(detailSelectedIds = emptySet(), isDetailSelectionMode = false) }
            is CollectionEvents.RemoveSelectedFromCollection -> removeSelectedFromCollection(event.collectionId)
            is CollectionEvents.SetSortOrder -> {
                _state.update { it.copy(sortOrder = event.sortOrder, showSortSheet = false, collectionBookmarks = sortBookmarks(rawCollectionBookmarks, event.sortOrder)) }
            }
            CollectionEvents.ShowSortSheet -> _state.update { it.copy(showSortSheet = true) }
            CollectionEvents.HideSortSheet -> _state.update { it.copy(showSortSheet = false) }
            is CollectionEvents.ShowRenameCollectionDialog -> {
                _state.update { it.copy(showRenameCollectionDialog = true, renameCollectionDialogText = event.initialName) }
            }
            CollectionEvents.HideRenameCollectionDialog -> {
                _state.update { it.copy(showRenameCollectionDialog = false, renameCollectionDialogText = null) }
            }
            is CollectionEvents.UpdateCollectionName -> {
                val newName = event.name.trim()
                if (newName.isBlank()) return
                viewModelScope.launch {
                    val currentCollection = _state.value.collections.find { it.id == event.id }
                    if (currentCollection != null && currentCollection.name != newName) {
                        repository.updateCollectionName(event.id, newName)
                    }
                    _state.update { it.copy(showRenameCollectionDialog = false, renameCollectionDialogText = null, toastMessage = "Collection renamed") }
                }
            }
            is CollectionEvents.TogglePin -> {
                viewModelScope.launch { repository.togglePinStatus(event.bookmark.id, !event.bookmark.isPinned) }
            }
            is CollectionEvents.ShowEditDialog -> {
                _state.update { it.copy(showRenameBookmarkDialog = true, tempBookmark = event.bookmark, isDetailBodySheet = false) }
            }
            CollectionEvents.HideEditDialog -> _state.update { it.copy(showRenameBookmarkDialog = false, tempBookmark = null) }
            is CollectionEvents.UpdateBookmarkDetails -> {
                viewModelScope.launch {
                    repository.updateBookmarkDetails(event.id, event.title?.trim(), event.description?.trim())
                    _state.update { it.copy(showRenameBookmarkDialog = false, tempBookmark = null, toastMessage = "Bookmark updated") }
                }
            }
            CollectionEvents.ShowTagManager -> _state.update { it.copy(showTagManager = true, isDetailBodySheet = false) }
            CollectionEvents.HideTagManager -> {
                _state.update { it.copy(showTagManager = false, tempBookmark = null, tempBookmarkTags = emptyList()) }
            }
            is CollectionEvents.CreateTag -> viewModelScope.launch { repository.insertTag(event.name) }
            is CollectionEvents.ToggleTagForBookmark -> {
                val tempBm = _state.value.tempBookmark ?: return
                viewModelScope.launch {
                    if (event.isChecked) repository.addTagToBookmark(tempBm.id, event.tag.id)
                    else repository.removeTagFromBookmark(tempBm.id, event.tag.id)
                }
            }
            is CollectionEvents.DeleteTag -> viewModelScope.launch { repository.deleteTag(event.tagId) }
            is CollectionEvents.ShowNoteEditor -> {
                _state.update { it.copy(showNoteEditor = true, noteEditorText = event.initialNote, isDetailBodySheet = false) }
            }
            CollectionEvents.HideNoteEditor -> _state.update { it.copy(showNoteEditor = false, noteEditorText = null, tempBookmark = null) }
            is CollectionEvents.UpdateNote -> {
                viewModelScope.launch {
                    repository.updateNote(event.id, event.note)
                    _state.update { it.copy(showNoteEditor = false, noteEditorText = null, tempBookmark = null) }
                }
            }
            CollectionEvents.ShowReminderPicker -> _state.update { it.copy(showReminderPicker = true, isDetailBodySheet = false) }
            CollectionEvents.HideReminderPicker -> _state.update { it.copy(showReminderPicker = false, tempBookmark = null) }
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
            CollectionEvents.ClearToast -> _state.update { it.copy(toastMessage = null) }
            CollectionEvents.ShowAddBookmarkDialog -> _state.update { it.copy(showAddBookmarkDialog = true, addBookmarkUrl = "") }
            CollectionEvents.HideAddBookmarkDialog -> _state.update { it.copy(showAddBookmarkDialog = false, addBookmarkUrl = "") }
            is CollectionEvents.OnAddBookmarkUrlChange -> _state.update { it.copy(addBookmarkUrl = event.url) }
            CollectionEvents.SaveBookmarkInCollection -> saveBookmarkInCollection()
        }
    }

    fun backToCollections() {
        collectionJob?.cancel()
        _state.update { it.copy(selectedCollection = null, collectionBookmarks = emptyList()) }
    }
}
