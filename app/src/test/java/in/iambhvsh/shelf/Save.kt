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
        assertNull(state.bookmarkId)
        assertNull(state.selectedCollection)
        assertTrue(state.selectedTags.isEmpty())
        assertFalse(state.showCollectionPicker)
        assertFalse(state.showTagPicker)
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
}
