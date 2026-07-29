package `in`.iambhvsh.shelf.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.iambhvsh.shelf.domain.repository.BookmarkRepository
import `in`.iambhvsh.shelf.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: BookmarkRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SearchState())
    val state = _state.asStateFlow()

    private var searchJob: Job? = null
    private var activeTagFilters: Set<Long> = emptySet()

    fun onTagsChange(tags: Set<Long>) {
        if (activeTagFilters != tags) {
            activeTagFilters = tags
            performSearch()
        }
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        performSearch()
    }

    private fun performSearch() {
        val query = _state.value.searchQuery
        searchJob?.cancel()
        if (query.isBlank()) {
            _state.update { it.copy(searchResults = emptyList(), isLoading = false) }
            return
        }

        searchJob = viewModelScope.launch {
            val flow = if (activeTagFilters.isEmpty()) {
                repository.searchBookmarks(query)
            } else {
                repository.searchBookmarksWithTags(query, activeTagFilters.toList())
            }
            flow.collect { resource ->
                when (resource) {
                    is Resource.Loading -> _state.update { it.copy(isLoading = true) }
                    is Resource.Error -> _state.update { it.copy(isLoading = false, error = resource.errorMessage ?: "Error") }
                    is Resource.Success -> _state.update { it.copy(isLoading = false, searchResults = resource.data ?: emptyList()) }
                }
            }
        }
    }
}
