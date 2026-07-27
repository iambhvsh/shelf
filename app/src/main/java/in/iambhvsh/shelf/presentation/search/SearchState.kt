package in.iambhvsh.shelf.presentation.search

import in.iambhvsh.shelf.domain.model.Bookmark

data class SearchState(
    val searchQuery: String = "",
    val searchResults: List<Bookmark> = emptyList(),
    val isLoading: Boolean = false,
    val error: String = ""
)
