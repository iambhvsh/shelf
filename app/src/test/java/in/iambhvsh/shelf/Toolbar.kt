package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.components.root.canSortCurrentScreen
import `in`.iambhvsh.shelf.components.root.filterCollectionSearchBookmarks
import `in`.iambhvsh.shelf.components.root.isAllCollectionsSelected
import `in`.iambhvsh.shelf.components.root.resolveHeaderTitleEndPadding
import `in`.iambhvsh.shelf.screens.root.canNavigateFromEntry
import `in`.iambhvsh.shelf.screens.root.canPopDeepScreen
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.data.Collection
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolbarTest {

    @Test
    fun testIsAllCollectionsSelected_ignoresVirtualUncategorisedCollection() {
        val uncategorised = Collection(id = Collection.UNCATEGORISED_ID, name = "Uncategorised", bookmarkCount = 2, isVirtual = true)
        val col1 = Collection(id = 1L, name = "Work")
        val col2 = Collection(id = 2L, name = "Personal")

        assertFalse(isAllCollectionsSelected(listOf(uncategorised), emptySet()))
        assertFalse(isAllCollectionsSelected(listOf(uncategorised, col1, col2), setOf(1L)))
        assertTrue(isAllCollectionsSelected(listOf(uncategorised, col1, col2), setOf(1L, 2L)))
    }

    @Test
    fun testCanSortCurrentScreen_onlyEnabledOnHomeAndCollectionDetail() {
        assertTrue(canSortCurrentScreen(currentTab = 0, isCollectionDetail = false))
        assertFalse(canSortCurrentScreen(currentTab = 1, isCollectionDetail = false))
        assertTrue(canSortCurrentScreen(currentTab = 1, isCollectionDetail = true))
        assertFalse(canSortCurrentScreen(currentTab = 2, isCollectionDetail = false))
    }

    @Test
    fun testFilterCollectionSearchBookmarks_matchesTitleUrlDescriptionAndNote() {
        val items = listOf(
            Bookmark(id = 1L, url = "https://kotlinlang.org", title = "Kotlin", description = "Language docs", imageUrl = null, note = null),
            Bookmark(id = 2L, url = "https://developer.android.com", title = "Android", description = null, imageUrl = null, note = "Compose navigation tip")
        )

        assertEquals(2, filterCollectionSearchBookmarks(items, "  ").size)
        assertEquals(listOf(1L), filterCollectionSearchBookmarks(items, "DOCS").map { it.id })
        assertEquals(listOf(2L), filterCollectionSearchBookmarks(items, "NAVIGATION").map { it.id })
        assertEquals(listOf(1L), filterCollectionSearchBookmarks(items, "kotlinlang").map { it.id })
        assertTrue(filterCollectionSearchBookmarks(items, "nonexistent").isEmpty())
    }

    @Test
    fun testIsAllCollectionsSelected_handlesEmptyRealCollectionsAndVirtualSelectedIdEdgeCase() {
        val uncategorised = Collection(id = Collection.UNCATEGORISED_ID, name = "Uncategorised", isVirtual = true)
        val col1 = Collection(id = 1L, name = "Reading")

        assertFalse(isAllCollectionsSelected(emptyList(), emptySet()))
        assertFalse(isAllCollectionsSelected(listOf(uncategorised), setOf(Collection.UNCATEGORISED_ID)))
        assertTrue(isAllCollectionsSelected(listOf(col1), setOf(1L, Collection.UNCATEGORISED_ID)))
    }

    @Test
    fun testCanSortCurrentScreen_handlesNegativeAndOutOfRangeTabs() {
        assertFalse(canSortCurrentScreen(currentTab = -1, isCollectionDetail = false))
        assertTrue(canSortCurrentScreen(currentTab = -1, isCollectionDetail = true))
        assertFalse(canSortCurrentScreen(currentTab = 99, isCollectionDetail = false))
        assertTrue(canSortCurrentScreen(currentTab = 0, isCollectionDetail = true))
    }

    @Test
    fun testFilterCollectionSearchBookmarks_handlesEmptyListNullFieldsAndTitleMatches() {
        assertTrue(filterCollectionSearchBookmarks(emptyList(), "anything").isEmpty())

        val items = listOf(
            Bookmark(id = 10L, url = "https://plain.org", title = null, description = null, imageUrl = null, note = null),
            Bookmark(id = 11L, url = "https://kotlin.dev", title = "Multiplatform Guide", description = "", imageUrl = null, note = "")
        )
        assertEquals(listOf(11L), filterCollectionSearchBookmarks(items, "  multiplatform  ".trim()).map { it.id })
        assertEquals(listOf(10L), filterCollectionSearchBookmarks(items, "PLAIN.ORG").map { it.id })
        assertEquals(2, filterCollectionSearchBookmarks(items, "").size)
    }

    @Test
    fun testHeaderTitleEndPaddingAndNavGuards_preventOverlapAndStaleTransitions() {
        assertEquals(156.dp, resolveHeaderTitleEndPadding(isSelectionMode = true, isCollectionDetail = true, isTrashScreen = false, isDeepScreen = true))
        assertEquals(112.dp, resolveHeaderTitleEndPadding(isSelectionMode = false, isCollectionDetail = true, isTrashScreen = false, isDeepScreen = true))
        assertEquals(64.dp, resolveHeaderTitleEndPadding(isSelectionMode = false, isCollectionDetail = false, isTrashScreen = true, isDeepScreen = true))
        assertEquals(24.dp, resolveHeaderTitleEndPadding(isSelectionMode = false, isCollectionDetail = false, isTrashScreen = false, isDeepScreen = true))
        assertEquals(0.dp, resolveHeaderTitleEndPadding(isSelectionMode = false, isCollectionDetail = false, isTrashScreen = false, isDeepScreen = false))

        assertTrue(canNavigateFromEntry("entry-1", "entry-1"))
        assertFalse(canNavigateFromEntry("entry-2", "entry-1"))
        assertTrue(canPopDeepScreen(isDeepScreen = true, currentEntryId = "entry-2", displayedEntryId = "entry-2"))
        assertFalse(canPopDeepScreen(isDeepScreen = false, currentEntryId = "entry-2", displayedEntryId = "entry-2"))
        assertFalse(canPopDeepScreen(isDeepScreen = true, currentEntryId = "entry-1", displayedEntryId = "entry-2"))
    }
}
