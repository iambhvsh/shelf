package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.data.Collection
import `in`.iambhvsh.shelf.data.Tag
import `in`.iambhvsh.shelf.features.save.QuickSaveState
import org.junit.Assert.*
import org.junit.Test

class SaveTest {

    @Test
    fun testQuickSaveState_defaultsToSaving() {
        val state = QuickSaveState(url = "https://example.com")
        assertEquals("https://example.com", state.url)
        assertTrue(state.isSaving)
        assertFalse(state.isAlreadySaved)
        assertNull(state.bookmarkId)
        assertNull(state.selectedCollection)
        assertTrue(state.selectedTags.isEmpty())
        assertFalse(state.showCollectionPicker)
        assertFalse(state.showTagPicker)
    }

    @Test
    fun testQuickSaveState_tracksAlreadySavedStatus() {
        var state = QuickSaveState(url = "https://example.com")
        state = state.copy(
            isAlreadySaved = true,
            isSaving = false,
            bookmarkId = 42L,
            message = "Already saved"
        )
        assertTrue(state.isAlreadySaved)
        assertFalse(state.isSaving)
        assertEquals(42L, state.bookmarkId)
        assertEquals("Already saved", state.message)
    }

    @Test
    fun testQuickSaveState_updatesCollectionAndTags() {
        val col = Collection(id = 12L, name = "Reading")
        val tag1 = Tag(id = 1L, name = "Tech")
        val tag2 = Tag(id = 2L, name = "Design")

        var state = QuickSaveState(
            url = "https://example.com",
            selectedCollection = col,
            selectedTags = listOf(tag1, tag2),
            isSaving = false,
            bookmarkId = 999L
        )

        assertEquals(12L, state.selectedCollection?.id)
        assertEquals(2, state.selectedTags.size)
        assertFalse(state.isSaving)
        assertEquals(999L, state.bookmarkId)

        state = state.copy(showCollectionPicker = true)
        assertTrue(state.showCollectionPicker)
    }

    @Test
    fun testQuickSaveState_populatesExistingBookmarkMetadata() {
        val col = Collection(id = 5L, name = "Android")
        var state = QuickSaveState(url = "https://example.com")
        state = state.copy(
            bookmarkId = 100L,
            title = "Existing Title",
            description = "Existing Description",
            imageUrl = "https://example.com/thumb.jpg",
            selectedCollection = col,
            isAlreadySaved = true,
            isSaving = false
        )
        assertEquals("Existing Title", state.title)
        assertEquals("Existing Description", state.description)
        assertEquals("https://example.com/thumb.jpg", state.imageUrl)
        assertEquals(5L, state.selectedCollection?.id)
        assertTrue(state.isAlreadySaved)
        assertFalse(state.isSaving)
    }
}
