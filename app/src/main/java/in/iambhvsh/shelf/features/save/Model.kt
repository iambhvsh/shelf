package `in`.iambhvsh.shelf.features.save

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.data.BookmarkRepository
import `in`.iambhvsh.shelf.features.fetch.LinkMetadataParser
import `in`.iambhvsh.shelf.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuickSaveViewModel(
    private val repository: BookmarkRepository
) : ViewModel() {
    private val parser = LinkMetadataParser()
    private var metadataJob: Job? = null

    private val _state = MutableStateFlow(QuickSaveState())
    val state = _state.asStateFlow()

    init {
        fetchCollections()
        fetchTags()
    }

    fun onEvent(event: QuickSaveEvents) {
        when (event) {
            is QuickSaveEvents.Init -> {
                if (_state.value.url == event.url) return
                _state.update { it.copy(url = event.url, isSaving = true) }
                saveBookmarkInitially(event.url)
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
                metadataJob?.cancel()
                val bookmarkId = _state.value.bookmarkId ?: return
                if (_state.value.isAlreadySaved) return
                viewModelScope.launch {
                    val initialBookmark = Bookmark(id = bookmarkId, url = _state.value.url, title = null, description = null, imageUrl = null)
                    repository.deleteBookmark(initialBookmark)
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
            val exists = repository.existsByUrl(url)
            val initialBookmark = Bookmark(url = url, title = url, description = null, imageUrl = null)
            val id = repository.saveAndReturnId(initialBookmark)
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
        metadataJob = viewModelScope.launch {
            try {
                val meta = parser.parse(url)
                val title = meta?.title?.takeIf { it.isNotBlank() } ?: url
                val description = meta?.description?.takeIf { it.isNotBlank() }
                
                repository.updateBookmarkDetails(id, title, description)
                if (!meta?.imageUrl.isNullOrBlank()) {
                    repository.updateImageUrl(id, meta.imageUrl)
                }
                
                _state.update { it.copy(title = title, description = description, imageUrl = meta?.imageUrl, isSaving = false) }
            } catch (e: Exception) {
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
}
