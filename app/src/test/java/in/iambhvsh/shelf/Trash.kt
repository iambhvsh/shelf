package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.features.collection.CollectionState
import `in`.iambhvsh.shelf.features.setting.SettingState
import `in`.iambhvsh.shelf.nav.AppRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrashStateTest {

    @Test
    fun testDefaultTrashState_hasStandardDefaults() {
        val state = SettingState()
        assertTrue(state.trashedBookmarks.isEmpty())
        assertEquals(0, state.trashedCount)
        assertTrue(state.selectedTrashIds.isEmpty())
        assertFalse(state.isTrashLoading)
        assertFalse(state.showEmptyTrashDialog)
        assertFalse(state.showDeletePermanentlyDialog)
    }

    @Test
    fun testTrashSelectionToggle_updatesSelectedSet() {
        var state = SettingState()
        val bookmark1 = Bookmark(id = 1L, url = "https://example.com/1", title = "One", description = null, imageUrl = null)
        val bookmark2 = Bookmark(id = 2L, url = "https://example.com/2", title = "Two", description = null, imageUrl = null)
        state = state.copy(trashedBookmarks = listOf(bookmark1, bookmark2), trashedCount = 2)

        state = state.copy(selectedTrashIds = setOf(1L))
        assertTrue(state.selectedTrashIds.contains(1L))
        assertEquals(1, state.selectedTrashIds.size)

        state = state.copy(selectedTrashIds = setOf(1L, 2L))
        assertEquals(2, state.selectedTrashIds.size)

        state = state.copy(selectedTrashIds = emptySet())
        assertTrue(state.selectedTrashIds.isEmpty())
    }

    @Test
    fun testTrashDialogs_stateTransitions() {
        var state = SettingState()

        state = state.copy(showDeletePermanentlyDialog = true)
        assertTrue(state.showDeletePermanentlyDialog)

        state = state.copy(showDeletePermanentlyDialog = false)
        assertFalse(state.showDeletePermanentlyDialog)

        state = state.copy(showEmptyTrashDialog = true)
        assertTrue(state.showEmptyTrashDialog)

        state = state.copy(showEmptyTrashDialog = false)
        assertFalse(state.showEmptyTrashDialog)
    }

    @Test
    fun testCollectionState_trashedCountInCollection() {
        var state = CollectionState()
        assertEquals(0, state.trashedCountInCollection)

        state = state.copy(trashedCountInCollection = 5)
        assertEquals(5, state.trashedCountInCollection)
    }

    @Test
    fun testRoute_settingsTrashHasCorrectPath() {
        assertEquals("settings_trash", AppRoute.SettingsTrash.route)
    }
}
