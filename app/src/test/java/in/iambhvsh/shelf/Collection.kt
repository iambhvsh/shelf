package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.data.BookmarkCollectionCrossRef
import `in`.iambhvsh.shelf.data.BookmarkEntity
import `in`.iambhvsh.shelf.data.Collection
import `in`.iambhvsh.shelf.data.CollectionEntity
import `in`.iambhvsh.shelf.data.SortOrder
import `in`.iambhvsh.shelf.features.collection.CollectionEvents
import `in`.iambhvsh.shelf.features.collection.CollectionState
import `in`.iambhvsh.shelf.features.collection.CollectionViewModel
import `in`.iambhvsh.shelf.features.fetch.LinkPreviewData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionStateTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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

        val state = CollectionState(collections = listOf(virtualCol, normalCol))
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

    @Test
    fun testDeleteSelectedDetail_clearsSelectionStateCleanly() {
        var state = CollectionState(
            detailSelectedIds = setOf(10L, 20L),
            isDetailSelectionMode = true
        )
        assertTrue(state.isDetailSelectionMode)
        assertEquals(2, state.detailSelectedIds.size)

        state = state.copy(
            detailSelectedIds = emptySet(),
            isDetailSelectionMode = false,
            toastMessage = "Deleted bookmarks"
        )
        assertFalse(state.isDetailSelectionMode)
        assertTrue(state.detailSelectedIds.isEmpty())
        assertEquals("Deleted bookmarks", state.toastMessage)
    }

    @Test
    fun testCollectionViewModel_restoreCollectionBookmarksReschedulesOnlyFutureReminders() = runTest {
        val repo = ControllableBookmarkRepository()
        val reminders = FakeReminderManager()
        val colId = repo.store.collectionDao.insertCollection(CollectionEntity(name = "Docs"))
        val futureTime = System.currentTimeMillis() + 3600_000L
        val pastTime = System.currentTimeMillis() - 3600_000L

        val bmFuture = repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://future.dev", title = "Future", description = null, imageUrl = null, isHidden = true, reminderTime = futureTime)
        )
        val bmPast = repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://past.dev", title = "Past", description = null, imageUrl = null, isHidden = true, reminderTime = pastTime)
        )
        repo.store.collectionDao.addBookmarkToCollection(BookmarkCollectionCrossRef(bmFuture, colId))
        repo.store.collectionDao.addBookmarkToCollection(BookmarkCollectionCrossRef(bmPast, colId))

        val vm = CollectionViewModel(repo, reminders, FakeLinkMetadataParser(), mainDispatcherRule.dispatcher)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.onEvent(CollectionEvents.RestoreCollectionBookmarks(colId))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(repo.store.bookmarkDao.getBookmarkById(bmFuture)!!.isHidden)
        assertFalse(repo.store.bookmarkDao.getBookmarkById(bmPast)!!.isHidden)
        assertEquals(listOf(bmFuture to futureTime), reminders.scheduled)
        assertEquals("Restored bookmarks", vm.state.value.toastMessage)
    }

    @Test
    fun testCollectionViewModel_uncategorisedSaveDuplicateAndVirtualSelectionGuards() = runTest {
        val repo = ControllableBookmarkRepository()
        val reminders = FakeReminderManager()
        val existingColId = repo.store.collectionDao.insertCollection(CollectionEntity(name = "Existing"))
        val existingBm = repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://already-categorized.dev", title = "Cat", description = null, imageUrl = null)
        )
        repo.store.collectionDao.addBookmarkToCollection(BookmarkCollectionCrossRef(existingBm, existingColId))

        repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://unassigned.dev", title = "Unassigned", description = null, imageUrl = null)
        )

        val parser = FakeLinkMetadataParser { url ->
            LinkPreviewData(title = "Saved", description = null, imageUrl = null, url = url)
        }
        val vm = CollectionViewModel(repo, reminders, parser, mainDispatcherRule.dispatcher)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.onEvent(CollectionEvents.ToggleSelection(Collection.UNCATEGORISED_ID))
        assertFalse(vm.state.value.isSelectionMode)

        vm.onEvent(CollectionEvents.SelectAll)
        assertEquals(setOf(existingColId), vm.state.value.selectedIds)

        vm.onEvent(CollectionEvents.SelectCollectionById(Collection.UNCATEGORISED_ID))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(Collection.UNCATEGORISED_ID, vm.state.value.selectedCollection?.id)

        vm.onEvent(CollectionEvents.ShowAddBookmarkDialog)
        vm.onEvent(CollectionEvents.OnAddBookmarkUrlChange("already-categorized.dev"))
        vm.onEvent(CollectionEvents.SaveBookmarkInCollection)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals("Link already exists in your bookmarks", vm.state.value.toastMessage)

        vm.onEvent(CollectionEvents.ShowAddBookmarkDialog)
        vm.onEvent(CollectionEvents.OnAddBookmarkUrlChange("brandnew.dev"))
        vm.onEvent(CollectionEvents.SaveBookmarkInCollection)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals("Bookmark saved", vm.state.value.toastMessage)
    }

    @Test
    fun testCollectionViewModel_clearSelectedCollectionCancelsJobsAndResetsSelection() = runTest {
        val repo = ControllableBookmarkRepository()
        val colId = repo.store.collectionDao.insertCollection(CollectionEntity(name = "Reading"))
        val bmId = repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://a.dev", title = "A", description = null, imageUrl = null)
        )
        repo.store.collectionDao.addBookmarkToCollection(BookmarkCollectionCrossRef(bmId, colId))

        val vm = CollectionViewModel(repo, FakeReminderManager(), FakeLinkMetadataParser(), mainDispatcherRule.dispatcher)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.onEvent(CollectionEvents.SelectCollectionById(colId))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, vm.state.value.collectionBookmarks.size)

        vm.onEvent(CollectionEvents.ToggleDetailSelection(bmId))
        assertTrue(vm.state.value.isDetailSelectionMode)

        vm.onEvent(CollectionEvents.ClearSelectedCollection)
        assertNull(vm.state.value.selectedCollection)
        assertTrue(vm.state.value.collectionBookmarks.isEmpty())
        assertFalse(vm.state.value.isDetailSelectionMode)
        assertTrue(vm.state.value.detailSelectedIds.isEmpty())
    }

    @Test
    fun testCollectionViewModel_renameSortRemoveAndFailureReset() = runTest {
        val repo = ControllableBookmarkRepository()
        val reminders = FakeReminderManager()
        val colId = repo.store.collectionDao.insertCollection(CollectionEntity(name = "Tech"))
        val bmPinned = repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://z.dev", title = "Zeta", description = null, imageUrl = null, isPinned = true, createdAt = 100L)
        )
        val bmNormal = repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://a.dev", title = "Alpha", description = null, imageUrl = null, isPinned = false, createdAt = 200L)
        )
        repo.store.collectionDao.addBookmarkToCollection(BookmarkCollectionCrossRef(bmPinned, colId))
        repo.store.collectionDao.addBookmarkToCollection(BookmarkCollectionCrossRef(bmNormal, colId))

        val parser = FakeLinkMetadataParser { throw IllegalStateException("Timeout") }
        val vm = CollectionViewModel(repo, reminders, parser, mainDispatcherRule.dispatcher)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.onEvent(CollectionEvents.UpdateCollectionName(colId, "  Engineering  "))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals("Engineering", vm.state.value.collections.first { it.id == colId }.name)

        vm.onEvent(CollectionEvents.SelectCollectionById(colId))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.onEvent(CollectionEvents.SetSortOrder(SortOrder.TITLE_ASC))
        assertEquals(listOf(bmPinned, bmNormal), vm.state.value.collectionBookmarks.map { it.id })

        vm.onEvent(CollectionEvents.ShowAddBookmarkDialog)
        vm.onEvent(CollectionEvents.OnAddBookmarkUrlChange("https://fail.dev"))
        vm.onEvent(CollectionEvents.SaveBookmarkInCollection)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertFalse(vm.state.value.isSavingBookmark)
        assertEquals("Timeout", vm.state.value.error)

        vm.onEvent(CollectionEvents.ToggleDetailSelection(bmNormal))
        vm.onEvent(CollectionEvents.RemoveSelectedFromCollection(colId))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(bmPinned), vm.state.value.collectionBookmarks.map { it.id })
        assertFalse(repo.store.bookmarkDao.getBookmarkById(bmNormal)!!.isHidden)
    }

    @Test
    fun testCollectionViewModel_everyRemainingEventSheetTagNoteReminderAndDelete() = runTest {
        val repo = ControllableBookmarkRepository()
        val reminders = FakeReminderManager()
        val parser = FakeLinkMetadataParser { url ->
            LinkPreviewData(title = "Parsed", description = "Desc", imageUrl = "https://img.dev/c.png", url = url)
        }
        val colA = repo.store.collectionDao.insertCollection(CollectionEntity(name = "Alpha"))
        val colB = repo.store.collectionDao.insertCollection(CollectionEntity(name = "Beta"))
        val bm1 = repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://one.dev", title = "One", description = null, imageUrl = "https://i/1.png", createdAt = 100L)
        )
        val bm2 = repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://two.dev", title = "Two", description = null, imageUrl = null, createdAt = 200L)
        )
        repo.store.collectionDao.addBookmarkToCollection(`in`.iambhvsh.shelf.data.BookmarkCollectionCrossRef(bm1, colA))
        repo.store.collectionDao.addBookmarkToCollection(`in`.iambhvsh.shelf.data.BookmarkCollectionCrossRef(bm2, colA))
        val tagId = repo.store.tagDao.insertTag(`in`.iambhvsh.shelf.data.TagEntity(name = "Tag1"))

        val vm = CollectionViewModel(repo, reminders, parser, mainDispatcherRule.dispatcher)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.onEvent(CollectionEvents.ShowCreateDialog)
        assertTrue(vm.state.value.showCreateDialog)
        vm.onEvent(CollectionEvents.InputNameChanged("   "))
        vm.onEvent(CollectionEvents.CreateCollection)
        vm.onEvent(CollectionEvents.HideCreateDialog)
        assertFalse(vm.state.value.showCreateDialog)
        vm.onEvent(CollectionEvents.ShowCreateDialog)
        vm.onEvent(CollectionEvents.InputNameChanged("  Gamma  "))
        vm.onEvent(CollectionEvents.CreateCollection)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.collections.any { it.name == "Gamma" })

        vm.onEvent(CollectionEvents.SelectAll)
        assertFalse(vm.state.value.selectedIds.contains(Collection.UNCATEGORISED_ID))
        assertTrue(vm.state.value.selectedIds.containsAll(listOf(colA, colB)))
        vm.onEvent(CollectionEvents.DeselectAll)
        assertFalse(vm.state.value.isSelectionMode)
        vm.onEvent(CollectionEvents.ToggleSelection(colB))
        vm.onEvent(CollectionEvents.ClearSelection)
        assertTrue(vm.state.value.selectedIds.isEmpty())

        vm.onEvent(CollectionEvents.SelectCollectionById(colA))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, vm.state.value.collectionBookmarks.size)

        vm.onEvent(CollectionEvents.ShowSortSheet)
        assertTrue(vm.state.value.showSortSheet)
        vm.onEvent(CollectionEvents.HideSortSheet)
        assertFalse(vm.state.value.showSortSheet)
        vm.onEvent(CollectionEvents.SetSortOrder(SortOrder.DATE_OLDEST))
        assertEquals(listOf(bm1, bm2), vm.state.value.collectionBookmarks.map { it.id })
        vm.onEvent(CollectionEvents.SetSortOrder(SortOrder.TITLE_DESC))
        assertEquals(listOf(bm2, bm1), vm.state.value.collectionBookmarks.map { it.id })
        vm.onEvent(CollectionEvents.SetSortOrder(SortOrder.TITLE_ASC))
        assertEquals(listOf(bm1, bm2), vm.state.value.collectionBookmarks.map { it.id })

        val detailBm = vm.state.value.collectionBookmarks.first { it.id == bm1 }
        vm.onEvent(CollectionEvents.ShowDetailBodySheet(detailBm))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.isDetailBodySheet)

        vm.onEvent(CollectionEvents.TogglePin(detailBm))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.tempBookmark?.isPinned == true)

        vm.onEvent(CollectionEvents.ShowTagManager)
        assertTrue(vm.state.value.showTagManager)
        vm.onEvent(CollectionEvents.CreateTag("CollectionTag"))
        val tObj = repo.store.tagDao.getAllTagsOnce().first { it.id == tagId }.let {
            `in`.iambhvsh.shelf.data.Tag(it.id, it.name)
        }
        vm.onEvent(CollectionEvents.ToggleTagForBookmark(tObj, true))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(tagId), vm.state.value.tempBookmarkTags.map { it.id })
        vm.onEvent(CollectionEvents.ToggleTagForBookmark(tObj, false))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.tempBookmarkTags.isEmpty())
        vm.onEvent(CollectionEvents.DeleteTag(tagId))
        vm.onEvent(CollectionEvents.HideTagManager)

        vm.onEvent(CollectionEvents.ShowDetailBodySheet(detailBm))
        vm.onEvent(CollectionEvents.ShowNoteEditor("Old note"))
        assertEquals("Old note", vm.state.value.noteEditorText)
        vm.onEvent(CollectionEvents.UpdateNote(bm1, "   "))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertNull(repo.store.bookmarkDao.getBookmarkById(bm1)?.note)
        vm.onEvent(CollectionEvents.HideNoteEditor)

        vm.onEvent(CollectionEvents.ShowDetailBodySheet(detailBm))
        vm.onEvent(CollectionEvents.ShowReminderPicker)
        assertTrue(vm.state.value.showReminderPicker)
        vm.onEvent(CollectionEvents.SetReminder(bm1, 90_000L))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(bm1 to 90_000L), reminders.scheduled)
        vm.onEvent(CollectionEvents.ShowDetailBodySheet(detailBm))
        vm.onEvent(CollectionEvents.CancelReminder(bm1))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(bm1), reminders.cancelled)
        vm.onEvent(CollectionEvents.HideReminderPicker)

        vm.onEvent(CollectionEvents.ShowDetailBodySheet(detailBm))
        vm.onEvent(CollectionEvents.ShowEditDialog(detailBm))
        assertTrue(vm.state.value.showRenameBookmarkDialog)
        vm.onEvent(CollectionEvents.HideEditDialog)
        assertFalse(vm.state.value.showRenameBookmarkDialog)
        vm.onEvent(CollectionEvents.UpdateBookmarkDetails(bm1, "  New T  ", "  New D  "))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals("New T", repo.store.bookmarkDao.getBookmarkById(bm1)?.title)

        vm.onEvent(CollectionEvents.ShowDetailBodySheet(detailBm))
        vm.onEvent(CollectionEvents.DismissDetailBodySheet)
        assertFalse(vm.state.value.isDetailBodySheet)
        assertNull(vm.state.value.tempBookmark)

        vm.onEvent(CollectionEvents.PreviewImageClick("https://img.dev/p.png"))
        assertTrue(vm.state.value.isPhotoPreviewDialog)
        vm.onEvent(CollectionEvents.DismissPreviewDialog)
        assertFalse(vm.state.value.isPhotoPreviewDialog)

        vm.onEvent(CollectionEvents.RefreshBookmarkImage(bm2, "https://two.dev", null))
        vm.onEvent(CollectionEvents.RefreshBookmarkImage(bm2, "https://two.dev", null))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals("https://img.dev/c.png", repo.store.bookmarkDao.getBookmarkById(bm2)?.imageUrl)

        vm.onEvent(CollectionEvents.ShowAddBookmarkDialog)
        vm.onEvent(CollectionEvents.HideAddBookmarkDialog)
        vm.onEvent(CollectionEvents.ShowAddBookmarkDialog)
        vm.onEvent(CollectionEvents.SaveBookmarkInCollection)
        assertFalse(vm.state.value.showAddBookmarkDialog)

        vm.onEvent(CollectionEvents.ShowAddBookmarkDialog)
        vm.onEvent(CollectionEvents.OnAddBookmarkUrlChange("added.dev"))
        vm.onEvent(CollectionEvents.SaveBookmarkInCollection)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(repo.existsByUrl("https://added.dev"))

        vm.onEvent(CollectionEvents.SelectAllDetail)
        vm.onEvent(CollectionEvents.DeselectAllDetail)
        assertFalse(vm.state.value.isDetailSelectionMode)
        vm.onEvent(CollectionEvents.ToggleDetailSelection(bm1))
        vm.onEvent(CollectionEvents.ClearDetailSelection)
        assertTrue(vm.state.value.detailSelectedIds.isEmpty())

        vm.onEvent(CollectionEvents.DeleteBookmark(detailBm))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(repo.store.bookmarkDao.getBookmarkById(bm1)!!.isHidden)
        vm.onEvent(CollectionEvents.ClearToast)
        assertNull(vm.state.value.toastMessage)

        vm.onEvent(CollectionEvents.DeleteCollectionById(Collection.UNCATEGORISED_ID))
        vm.onEvent(CollectionEvents.DeleteCollectionById(colA))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertNull(vm.state.value.selectedCollection)

        vm.onEvent(CollectionEvents.ToggleSelection(colB))
        vm.onEvent(CollectionEvents.DeleteSelected)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertFalse(vm.state.value.collections.any { it.id == colB })

        vm.backToCollections()
        vm.clearTransientState()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertNull(vm.state.value.selectedCollection)
    }
}
