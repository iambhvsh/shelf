package `in`.iambhvsh.shelf.features.save

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.data.BookmarkRepository
import `in`.iambhvsh.shelf.data.Tag
import `in`.iambhvsh.shelf.features.fetch.LinkMetadataParser
import `in`.iambhvsh.shelf.features.fetch.UrlUtils
import `in`.iambhvsh.shelf.util.Resource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuickSaveViewModel(
    private val repository: BookmarkRepository,
    private val parser: LinkMetadataParser = LinkMetadataParser(),
    private val externalScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : ViewModel() {
    private var metadataJob: Job? = null
    private var pendingCancel = false
    private var trashedSnapshot: Bookmark? = null
    private var trashedCollectionId: Long? = null
    private var trashedTags: List<Tag> = emptyList()

    private val _state = MutableStateFlow(QuickSaveState())
    val state = _state.asStateFlow()

    init {
        fetchCollections()
        fetchTags()
    }

    fun onEvent(event: QuickSaveEvents) {
        when (event) {
            is QuickSaveEvents.Init -> {
                val normalizedUrl = UrlUtils.extractUrl(event.url)
                if (normalizedUrl.isBlank()) {
                    _state.update { it.copy(isSaving = false, message = "No valid URL found") }
                    return
                }
                if (_state.value.url == normalizedUrl && _state.value.bookmarkId != null) return
                pendingCancel = false
                trashedSnapshot = null
                trashedCollectionId = null
                trashedTags = emptyList()
                _state.update { it.copy(url = normalizedUrl, isSaving = true, wasRestoredFromTrash = false) }
                saveBookmarkInitially(normalizedUrl)
            }
            
            is QuickSaveEvents.SelectCollection -> {
                val bookmarkId = _state.value.bookmarkId ?: return
                viewModelScope.launch {
                    val collection = _state.value.collections.find { it.id == event.collectionId }
                    val currentColId = repository.getCollectionIdForBookmark(bookmarkId)
                    if (currentColId != null) {
                        repository.removeBookmarkFromCollection(bookmarkId, currentColId)
                    }
                    if (collection != null) {
                        repository.addBookmarkToCollection(bookmarkId, collection.id)
                    }
                    _state.update { it.copy(selectedCollection = collection, showCollectionPicker = false) }
                }
            }
            
            is QuickSaveEvents.ToggleTag -> {
                val bookmarkId = _state.value.bookmarkId ?: return
                viewModelScope.launch {
                    val tag = _state.value.allTags.find { it.id == event.tagId } ?: return@launch
                    val currentTags = _state.value.selectedTags.toMutableList()
                    if (currentTags.any { it.id == event.tagId }) {
                        repository.removeTagFromBookmark(bookmarkId, event.tagId)
                        currentTags.removeAll { it.id == event.tagId }
                    } else {
                        repository.addTagToBookmark(bookmarkId, event.tagId)
                        currentTags.add(tag)
                    }
                    _state.update { it.copy(selectedTags = currentTags) }
                }
            }

            is QuickSaveEvents.CreateTag -> {
                val bookmarkId = _state.value.bookmarkId ?: return
                viewModelScope.launch {
                    val newTagId = repository.insertTag(event.name)
                    if (newTagId > 0) {
                        repository.addTagToBookmark(bookmarkId, newTagId)
                    }
                }
            }
            
            is QuickSaveEvents.DeleteTag -> {
                viewModelScope.launch {
                    repository.deleteTag(event.tagId)
                    val currentTags = _state.value.selectedTags.toMutableList()
                    currentTags.removeAll { it.id == event.tagId }
                    _state.update { it.copy(selectedTags = currentTags) }
                }
            }

            QuickSaveEvents.CancelSave -> {
                pendingCancel = true
                metadataJob?.cancel()
                val current = _state.value
                val bookmarkId = current.bookmarkId ?: return
                if (current.isAlreadySaved) return
                val snapshot = trashedSnapshot
                val colId = trashedCollectionId
                val tagsToRestore = trashedTags
                externalScope.launch {
                    if (current.wasRestoredFromTrash && snapshot != null) {
                        restoreTrashedSnapshot(bookmarkId, snapshot, colId, tagsToRestore)
                    } else if (current.wasRestoredFromTrash) {
                        repository.hideBookmarks(listOf(bookmarkId))
                    } else {
                        val initialBookmark = Bookmark(
                            id = bookmarkId,
                            url = current.url,
                            title = null,
                            description = null,
                            imageUrl = null
                        )
                        repository.deleteBookmark(initialBookmark)
                    }
                }
            }
            
            QuickSaveEvents.MessageShown -> _state.update { it.copy(message = null) }

            QuickSaveEvents.ShowCollectionPicker -> _state.update { it.copy(showCollectionPicker = true) }
            QuickSaveEvents.HideCollectionPicker -> _state.update { it.copy(showCollectionPicker = false) }
            QuickSaveEvents.ShowTagPicker -> _state.update { it.copy(showTagPicker = true) }
            QuickSaveEvents.HideTagPicker -> _state.update { it.copy(showTagPicker = false) }
        }
    }

    private fun saveBookmarkInitially(url: String) {
        viewModelScope.launch {
            val status = repository.findBookmarkStatusByUrl(url)
            val existingByUrl = status?.bookmark
            val exists = status != null && !status.isHidden
            val wasInTrash = status != null && status.isHidden

            if (wasInTrash && existingByUrl != null) {
                trashedSnapshot = existingByUrl
                trashedCollectionId = repository.getCollectionIdForBookmark(existingByUrl.id)
                val tagsRes = repository.getTagsForBookmark(existingByUrl.id)
                    .first { it !is Resource.Loading }
                trashedTags = (tagsRes as? Resource.Success)?.data.orEmpty()
            }

            if (pendingCancel) return@launch

            val initialBookmark = Bookmark(url = url, title = url, description = null, imageUrl = null)
            val id = repository.saveAndReturnId(initialBookmark)

            if (pendingCancel) {
                if (!exists) {
                    if (wasInTrash && trashedSnapshot != null) {
                        restoreTrashedSnapshot(id, trashedSnapshot!!, trashedCollectionId, trashedTags)
                    } else if (wasInTrash) {
                        repository.hideBookmarks(listOf(id))
                    } else {
                        repository.deleteBookmark(initialBookmark.copy(id = id))
                    }
                }
                return@launch
            }

            val existingBookmark = if (exists) repository.getBookmarkById(id) else null
            val existingColId = if (exists) repository.getCollectionIdForBookmark(id) else null

            _state.update { current ->
                val matchingCol = if (existingColId != null) current.collections.find { it.id == existingColId } else current.selectedCollection
                current.copy(
                    bookmarkId = id,
                    title = existingBookmark?.title ?: current.title,
                    description = existingBookmark?.description ?: current.description,
                    imageUrl = existingBookmark?.imageUrl ?: current.imageUrl,
                    selectedCollection = matchingCol,
                    isAlreadySaved = exists,
                    wasRestoredFromTrash = wasInTrash,
                    message = if (exists) "Already saved" else "Saved successfully"
                )
            }

            fetchBookmarkTags(id)
            if (!exists) {
                fetchMetadataInBackground(id, url)
            } else {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }
    
    private fun fetchBookmarkTags(bookmarkId: Long) {
        viewModelScope.launch {
            repository.getTagsForBookmark(bookmarkId).collect { result ->
                if (result is Resource.Success) {
                    _state.update { it.copy(selectedTags = result.data ?: emptyList()) }
                }
            }
        }
    }

    private fun fetchMetadataInBackground(id: Long, url: String) {
        metadataJob?.cancel()
        metadataJob = externalScope.launch {
            try {
                val meta = parser.parse(url)
                val title = meta?.title?.takeIf { it.isNotBlank() } ?: url
                val description = meta?.description?.takeIf { it.isNotBlank() }

                repository.updateBookmarkDetails(id, title, description)
                if (!meta?.imageUrl.isNullOrBlank()) {
                    repository.updateImageUrl(id, meta.imageUrl)
                }

                _state.update { it.copy(title = title, description = description, imageUrl = meta?.imageUrl, isSaving = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun fetchCollections() {
        viewModelScope.launch {
            repository.getAllCollections().collect { result ->
                if (result is Resource.Success) {
                    val cols = result.data ?: emptyList()
                    val bmId = _state.value.bookmarkId
                    val colId = if (bmId != null) repository.getCollectionIdForBookmark(bmId) else null
                    val matching = if (colId != null) cols.find { it.id == colId } else null
                    _state.update { current ->
                        current.copy(
                            collections = cols,
                            selectedCollection = matching ?: current.selectedCollection
                        )
                    }
                }
            }
        }
    }

    private fun fetchTags() {
        viewModelScope.launch {
            repository.getAllTags().collect { result ->
                if (result is Resource.Success) {
                    _state.update { it.copy(allTags = result.data ?: emptyList()) }
                }
            }
        }
    }

    private suspend fun restoreTrashedSnapshot(
        bookmarkId: Long,
        snapshot: Bookmark,
        originalCollectionId: Long?,
        originalTags: List<Tag>
    ) {
        val currentCollectionId = repository.getCollectionIdForBookmark(bookmarkId)
        if (currentCollectionId != null && currentCollectionId != originalCollectionId) {
            repository.removeBookmarkFromCollection(bookmarkId, currentCollectionId)
        }
        val currentTagsRes = repository.getTagsForBookmark(bookmarkId)
            .first { it !is Resource.Loading }
        val currentTags = (currentTagsRes as? Resource.Success)?.data.orEmpty()
        val originalTagIds = originalTags.map { it.id }.toSet()
        currentTags.filterNot { it.id in originalTagIds }.forEach { extraTag ->
            repository.removeTagFromBookmark(bookmarkId, extraTag.id)
        }
        repository.hideBookmarks(listOf(bookmarkId))
        repository.updateBookmarkDetails(bookmarkId, snapshot.title, snapshot.description)
        repository.updateImageUrl(bookmarkId, snapshot.imageUrl)
        repository.togglePinStatus(bookmarkId, snapshot.isPinned)
        repository.updateNote(bookmarkId, snapshot.note)
        repository.updateReminderTime(bookmarkId, snapshot.reminderTime)
        if (originalCollectionId != null && currentCollectionId != originalCollectionId) {
            repository.addBookmarkToCollection(bookmarkId, originalCollectionId)
        }
        originalTags.forEach { tag ->
            repository.addTagToBookmark(bookmarkId, tag.id)
        }
    }
}
