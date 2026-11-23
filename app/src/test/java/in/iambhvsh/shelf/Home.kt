package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.features.home.HomeState
import `in`.iambhvsh.shelf.data.Bookmark
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeStateTest {

    @Test
    fun testDefaultHomeState_hasExpectedDefaults() {
        val state = HomeState()
        assertEquals("", state.inputUrl)
        assertFalse(state.isDialog)
        assertFalse(state.isBodySheet)
        assertFalse(state.showNoteEditor)
        assertFalse(state.showReminderPicker)
        assertTrue(state.activeTagFilters.isEmpty())
        assertTrue(state.bookmarkData.isEmpty())
    }

    @Test
    fun testTagFilterToggle_updatesStateCorrectly() {
        var state = HomeState()
        
        val filters1 = state.activeTagFilters.toMutableSet()
        filters1.add(1L)
        state = state.copy(activeTagFilters = filters1)
        assertTrue(state.activeTagFilters.contains(1L))
        assertEquals(1, state.activeTagFilters.size)

        val filters2 = state.activeTagFilters.toMutableSet()
        filters2.clear()
        filters2.add(2L)
        state = state.copy(activeTagFilters = filters2)
        assertFalse(state.activeTagFilters.contains(1L))
        assertTrue(state.activeTagFilters.contains(2L))

        val filters3 = state.activeTagFilters.toMutableSet()
        filters3.remove(2L)
        state = state.copy(activeTagFilters = filters3)
        assertTrue(state.activeTagFilters.isEmpty())
    }

    @Test
    fun testNoteEditorState_updatesCorrectly() {
        var state = HomeState(isBodySheet = true)
        val sampleBookmark = Bookmark(
            id = 42L,
            url = "https://example.com",
            title = "Example",
            description = "Description",
            imageUrl = null,
            createdAt = 1000L
        )

        state = state.copy(
            showNoteEditor = true,
            noteEditorText = "Important notes",
            tempBookmark = sampleBookmark,
            isBodySheet = false
        )
        assertTrue(state.showNoteEditor)
        assertEquals("Important notes", state.noteEditorText)
        assertEquals(42L, state.tempBookmark?.id)
        assertFalse(state.isBodySheet)

        state = state.copy(
            showNoteEditor = false,
            noteEditorText = null,
            tempBookmark = null
        )
        assertFalse(state.showNoteEditor)
        assertEquals(null, state.noteEditorText)
        assertEquals(null, state.tempBookmark)
    }
}
