package `in`.iambhvsh.shelf.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.iambhvsh.shelf.data.UpdateManager
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.data.BookmarkRepository
import `in`.iambhvsh.shelf.features.fetch.LinkMetadataParser
import `in`.iambhvsh.shelf.features.reminder.ReminderManager
import `in`.iambhvsh.shelf.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    internal val repository: BookmarkRepository,
    internal val reminderManager: ReminderManager,
    internal val updateManager: UpdateManager
) : ViewModel() {
    internal val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    internal val parser = LinkMetadataParser()
    internal var rawBookmarks: List<Bookmark> = emptyList()
    internal var bookmarkJob: Job? = null
    internal var tempBookmarkTagsJob: Job? = null
    internal var isFetchingMetadata = false

    init {
        loadBookmarks()
        loadCollections()
        loadTags()
        fetchMissingMetadataOnStart()
        checkForUpdates()
    }

    fun homeEvents(events: HomeEvents) {
        when (events) {
            is HomeEvents.OnTextFieldValueChange -> {
                _state.update { it.copy(inputUrl = events.text) }
            }
            HomeEvents.SaveBookmark -> saveBookmark()
            HomeEvents.OnDialogDismissClick -> {
                _state.update { it.copy(isDialog = !it.isDialog) }
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
                _state.update { it.copy(isPhotoPreviewDialog = false) }
            }
            is HomeEvents.BookmarkPreviewClick -> {
                _state.update {
                    it.copy(tempBookmark = events.bookmark, isBodySheet = true)
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
                val s = _state.value
                if (s.showRenameDialog || s.showTagManager || s.showNoteEditor || s.showReminderPicker) {
                    _state.update { it.copy(isBodySheet = false) }
                } else {
                    tempBookmarkTagsJob?.cancel()
                    _state.update {
                        it.copy(isBodySheet = false, tempBookmark = null, tempBookmarkTags = emptyList())
                    }
                }
            }
            is HomeEvents.OpenBookmarkById -> {
                viewModelScope.launch {
                    val bookmark = repository.getBookmarkById(events.id)
                    if (bookmark != null && !_state.value.isBodySheet) {
                        homeEvents(HomeEvents.BookmarkPreviewClick(bookmark))
                        homeEvents(HomeEvents.CancelReminder(bookmark.id))
                    }
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
                    it.copy(selectedIds = newSelected, isSelectionMode = newSelected.isNotEmpty())
                }
            }
            HomeEvents.DeleteSelected -> deleteSelected()
            is HomeEvents.DeleteBookmark -> {
                viewModelScope.launch {
                    try {
                        repository.hideBookmarks(listOf(events.bookmark.id))
                        _state.update {
                            it.copy(
                                toastMessage = "Deleted bookmark",
                                isBodySheet = false,
                                tempBookmark = null,
                                tempBookmarkTags = emptyList()
                            )
                        }
                    } catch (e: Exception) {
                        _state.update { it.copy(error = e.message ?: "Delete failed") }
                    }
                }
            }
            HomeEvents.ClearSelection -> {
                _state.update { it.copy(selectedIds = emptySet(), isSelectionMode = false) }
            }
            HomeEvents.SelectAll -> {
                val allIds = _state.value.bookmarkData.map { it.id }.toSet()
                _state.update { it.copy(selectedIds = allIds, isSelectionMode = allIds.isNotEmpty()) }
            }
            HomeEvents.DeselectAll -> {
                _state.update { it.copy(selectedIds = emptySet(), isSelectionMode = false) }
            }
            HomeEvents.ShowCollectionPicker -> _state.update { it.copy(showCollectionPicker = true) }
            HomeEvents.HideCollectionPicker -> _state.update { it.copy(showCollectionPicker = false) }
            is HomeEvents.AddToCollection -> addSelectedToCollection(events.collectionId)
            is HomeEvents.SetSortOrder -> {
                viewModelScope.launch {
                    val sorted = sortBookmarks(rawBookmarks, events.sortOrder)
                    _state.update {
                        it.copy(
                            sortOrder = events.sortOrder,
                            showSortSheet = false,
                            bookmarkData = sorted
                        )
                    }
                }
            }
            HomeEvents.ShowSortSheet -> _state.update { it.copy(showSortSheet = true) }
            HomeEvents.HideSortSheet -> _state.update { it.copy(showSortSheet = false) }
            is HomeEvents.TogglePin -> {
                viewModelScope.launch {
                    repository.togglePinStatus(events.bookmark.id, !events.bookmark.isPinned)
                }
            }
            HomeEvents.ShowTagManager -> _state.update { it.copy(showTagManager = true, isBodySheet = false) }
            HomeEvents.HideTagManager -> {
                tempBookmarkTagsJob?.cancel()
                _state.update {
                    it.copy(showTagManager = false, tempBookmark = null, tempBookmarkTags = emptyList())
                }
            }
            is HomeEvents.CreateTag -> {
                viewModelScope.launch { repository.insertTag(events.name) }
            }
            is HomeEvents.ToggleTagForBookmark -> {
                val tempBm = _state.value.tempBookmark ?: return
                viewModelScope.launch {
                    if (events.isChecked) repository.addTagToBookmark(tempBm.id, events.tag.id)
                    else repository.removeTagFromBookmark(tempBm.id, events.tag.id)
                }
            }
            is HomeEvents.ToggleTagFilter -> {
                _state.update { state ->
                    val newFilters = state.activeTagFilters.toMutableSet()
                    if (newFilters.contains(events.tagId)) newFilters.remove(events.tagId)
                    else {
                        newFilters.clear()
                        newFilters.add(events.tagId)
                    }
                    state.copy(activeTagFilters = newFilters)
                }
                loadBookmarks()
            }
            is HomeEvents.DeleteTag -> {
                viewModelScope.launch { repository.deleteTag(events.tagId) }
            }
            is HomeEvents.ShowNoteEditor -> {
                _state.update {
                    it.copy(showNoteEditor = true, noteEditorText = events.initialNote, isBodySheet = false)
                }
            }
            HomeEvents.DismissUpdateSheet -> _state.update { it.copy(showUpdateSheet = false) }
            HomeEvents.InstallUpdate -> {
                _state.update { it.copy(showUpdateSheet = false) }
                updateManager.downloadAndInstallUpdate()
            }
            is HomeEvents.HideNoteEditor -> {
                _state.update { it.copy(showNoteEditor = false, noteEditorText = null, tempBookmark = null) }
            }
            is HomeEvents.UpdateNote -> {
                viewModelScope.launch {
                    repository.updateNote(events.id, events.note)
                    _state.update { it.copy(showNoteEditor = false, noteEditorText = null, tempBookmark = null) }
                }
            }
            is HomeEvents.ShowReminderPicker -> _state.update { it.copy(showReminderPicker = true, isBodySheet = false) }
            is HomeEvents.HideReminderPicker -> _state.update { it.copy(showReminderPicker = false, tempBookmark = null) }
            is HomeEvents.SetReminder -> {
                viewModelScope.launch {
                    repository.updateReminderTime(events.id, events.timeInMillis)
                    reminderManager.scheduleReminder(events.id, events.timeInMillis)
                    _state.update { it.copy(showReminderPicker = false, tempBookmark = null) }
                }
            }
            is HomeEvents.CancelReminder -> {
                viewModelScope.launch {
                    repository.updateReminderTime(events.id, null)
                    reminderManager.cancelReminder(events.id)
                    _state.update {
                        if (it.isBodySheet) it.copy(showReminderPicker = false)
                        else it.copy(showReminderPicker = false, tempBookmark = null)
                    }
                }
            }
            is HomeEvents.ShowEditDialog -> {
                _state.update { it.copy(showRenameDialog = true, tempBookmark = events.bookmark) }
            }
            is HomeEvents.HideEditDialog -> _state.update { it.copy(showRenameDialog = false) }
            is HomeEvents.UpdateBookmarkDetails -> {
                viewModelScope.launch {
                    repository.updateBookmarkDetails(events.id, events.title, events.description)
                }
                _state.update { it.copy(showRenameDialog = false) }
            }
            HomeEvents.ClearToast -> _state.update { it.copy(toastMessage = null) }
        }
    }
}
