package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.data.Collection
import `in`.iambhvsh.shelf.data.SortOrder
import `in`.iambhvsh.shelf.features.collection.CollectionState
import org.junit.Assert.*
import org.junit.Test

class CollectionStateTest {

    @Test
    fun testDefaultState_hasExpectedDefaults() {
        val state = CollectionState()
        assertTrue(state.collections.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isSelectionMode)
        assertTrue(state.selectedIds.isEmpty())
        assertFalse(state.isDetailSelectionMode)
        assertTrue(state.detailSelectedIds.isEmpty())
        assertEquals(SortOrder.DATE_NEWEST, state.sortOrder)
        assertNull(state.selectedCollection)
    }

    @Test
    fun testCollectionSelectionToggle_updatesSelectionMode() {
        var state = CollectionState()
        
        val selected = state.selectedIds.toMutableSet()
        selected.add(10L)
        state = state.copy(selectedIds = selected, isSelectionMode = selected.isNotEmpty())
        assertTrue(state.isSelectionMode)
        assertTrue(state.selectedIds.contains(10L))

        val unselected = state.selectedIds.toMutableSet()
        unselected.remove(10L)
        state = state.copy(selectedIds = unselected, isSelectionMode = unselected.isNotEmpty())
        assertFalse(state.isSelectionMode)
        assertTrue(state.selectedIds.isEmpty())
    }

    @Test
    fun testDetailSelectionToggle_maintainsDetailIds() {
        var state = CollectionState()

        val detailIds = mutableSetOf(101L, 102L)
        state = state.copy(detailSelectedIds = detailIds, isDetailSelectionMode = true)
        assertTrue(state.isDetailSelectionMode)
        assertEquals(2, state.detailSelectedIds.size)

        state = state.copy(detailSelectedIds = emptySet(), isDetailSelectionMode = false)
        assertFalse(state.isDetailSelectionMode)
        assertTrue(state.detailSelectedIds.isEmpty())
    }

    @Test
    fun testRenameCollectionDialog_updatesStateCorrectly() {
        var state = CollectionState()
        
        state = state.copy(
            showRenameCollectionDialog = true,
            renameCollectionDialogText = "Reading List"
        )
        assertTrue(state.showRenameCollectionDialog)
        assertEquals("Reading List", state.renameCollectionDialogText)

        state = state.copy(
            showRenameCollectionDialog = false,
            renameCollectionDialogText = null
        )
        assertFalse(state.showRenameCollectionDialog)
        assertNull(state.renameCollectionDialogText)
    }

    @Test
    fun testSortOrder_transitionsCleanly() {
        var state = CollectionState(sortOrder = SortOrder.DATE_NEWEST)
        state = state.copy(sortOrder = SortOrder.TITLE_ASC)
        assertEquals(SortOrder.TITLE_ASC, state.sortOrder)
        state = state.copy(sortOrder = SortOrder.DATE_OLDEST)
        assertEquals(SortOrder.DATE_OLDEST, state.sortOrder)
    }

    @Test
    fun testVirtualCollectionHandling_inCollectionState() {
        val virtualCol = Collection(
            id = Collection.UNCATEGORISED_ID,
            name = "Uncategorised",
            bookmarkCount = 3,
            previewUrls = emptyList(),
            isVirtual = true
        )
        assertTrue(virtualCol.isVirtual)

        val normalCol = Collection(
            id = 5L,
            name = "Articles",
            bookmarkCount = 10,
            previewUrls = emptyList(),
            isVirtual = false
        )
        assertFalse(normalCol.isVirtual)

        var state = CollectionState(collections = listOf(virtualCol, normalCol))
        assertEquals(2, state.collections.size)
        assertTrue(state.collections.any { it.isVirtual })
        assertTrue(state.collections.any { !it.isVirtual })
    }

    @Test
    fun testPhotoPreviewState_transitionsCleanly() {
        var state = CollectionState()
        assertFalse(state.isPhotoPreviewDialog)
        assertNull(state.previewPhotoUrl)

        state = state.copy(isPhotoPreviewDialog = true, previewPhotoUrl = "https://example.com/image.jpg")
        assertTrue(state.isPhotoPreviewDialog)
        assertEquals("https://example.com/image.jpg", state.previewPhotoUrl)

        state = state.copy(isPhotoPreviewDialog = false, previewPhotoUrl = null)
        assertFalse(state.isPhotoPreviewDialog)
        assertNull(state.previewPhotoUrl)
    }
}
