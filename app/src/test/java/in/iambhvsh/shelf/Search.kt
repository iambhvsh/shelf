package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.data.BookmarkEntity
import `in`.iambhvsh.shelf.data.BookmarkTagCrossRef
import `in`.iambhvsh.shelf.data.TagEntity
import `in`.iambhvsh.shelf.features.search.SearchState
import `in`.iambhvsh.shelf.features.search.SearchViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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

    @Test
    fun testSearchViewModel_searchesByQueryAndTagFiltersAndClearsOnBlank() = runTest {
        val repo = ControllableBookmarkRepository()
        val tagId = repo.store.tagDao.insertTag(TagEntity(name = "UI"))
        val bm1 = repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://compose.dev", title = "Compose UI", description = null, imageUrl = null, note = "animations")
        )
        repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://kotlin.dev", title = "Compose Backend", description = null, imageUrl = null)
        )
        repo.store.tagDao.addTagToBookmark(BookmarkTagCrossRef(bm1, tagId))

        val vm = SearchViewModel(repo)

        vm.onQueryChange("compose")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, vm.state.value.searchResults.size)

        vm.onTagsChange(setOf(tagId))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(bm1), vm.state.value.searchResults.map { it.id })

        vm.onQueryChange("   ")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.searchResults.isEmpty())
        assertFalse(vm.state.value.isLoading)

        vm.onQueryChange("no-match-anywhere")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.searchResults.isEmpty())
    }

    @Test
    fun testSearchViewModel_matchesUrlDescriptionNoteExcludesHiddenAndClearsErrors() = runTest {
        val store = InMemoryDaoStore()
        val repo = object : `in`.iambhvsh.shelf.data.BookmarkRepository by `in`.iambhvsh.shelf.data.BookmarkRepositoryImpl(
            store.bookmarkDao,
            store.collectionDao,
            store.tagDao
        ) {
            var failSearch = false
            override suspend fun searchBookmarks(text: String) =
                if (failSearch) kotlinx.coroutines.flow.flowOf(`in`.iambhvsh.shelf.util.Resource.Error("Search DB failed"))
                else `in`.iambhvsh.shelf.data.BookmarkRepositoryImpl(store.bookmarkDao, store.collectionDao, store.tagDao).searchBookmarks(text)
        }

        val idDesc = store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://one.dev", title = "One", description = "Deep Coroutines Guide", imageUrl = null)
        )
        val idNote = store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://two.dev", title = "Two", description = null, imageUrl = null, note = "Architecture secret")
        )
        val idHidden = store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://hidden.dev", title = "Hidden Coroutines", description = null, imageUrl = null, isHidden = true)
        )
        val vm = SearchViewModel(repo)

        vm.onQueryChange("COROUTINES")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(idDesc), vm.state.value.searchResults.map { it.id })
        assertFalse(vm.state.value.searchResults.any { it.id == idHidden })

        vm.onQueryChange("architecture")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(idNote), vm.state.value.searchResults.map { it.id })

        repo.failSearch = true
        vm.onQueryChange("one")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals("Search DB failed", vm.state.value.error)
        assertFalse(vm.state.value.isLoading)

        vm.onQueryChange("")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals("", vm.state.value.error)
        assertTrue(vm.state.value.searchResults.isEmpty())

        repo.failSearch = false
        val tagA = store.tagDao.insertTag(TagEntity(name = "Arch"))
        store.tagDao.addTagToBookmark(BookmarkTagCrossRef(idNote, tagA))
        vm.onTagsChange(setOf(tagA))
        vm.onQueryChange("two")
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(idNote), vm.state.value.searchResults.map { it.id })

        vm.onTagsChange(emptySet())
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(idNote), vm.state.value.searchResults.map { it.id })
    }
}
