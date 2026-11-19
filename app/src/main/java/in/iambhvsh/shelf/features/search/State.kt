package `in`.iambhvsh.shelf.features.search

import `in`.iambhvsh.shelf.data.Bookmark

data class SearchState(
    val searchQuery: String = "",
    val searchResults: List<Bookmark> = emptyList(),
    val isLoading: Boolean = false,
    val error: String = ""
)
