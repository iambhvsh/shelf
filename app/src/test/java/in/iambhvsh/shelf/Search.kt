package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.features.search.SearchState
import org.junit.Assert.*
import org.junit.Test

class SearchTest {

    @Test
    fun testSearchState_handlesQueryAndResults() {
        var state = SearchState()
        assertEquals("", state.searchQuery)
        assertTrue(state.searchResults.isEmpty())
        assertFalse(state.isLoading)

        state = state.copy(searchQuery = "compose", isLoading = true)
        assertEquals("compose", state.searchQuery)
        assertTrue(state.isLoading)

        val results = listOf(
            Bookmark(id = 1L, url = "https://compose.dev", title = "Compose", description = null, imageUrl = null)
        )
        state = state.copy(searchResults = results, isLoading = false)
        assertFalse(state.isLoading)
        assertEquals(1, state.searchResults.size)
        assertEquals("https://compose.dev", state.searchResults[0].url)

        state = state.copy(searchQuery = "", searchResults = emptyList())
        assertEquals("", state.searchQuery)
        assertTrue(state.searchResults.isEmpty())
    }
}
