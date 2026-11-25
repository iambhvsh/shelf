package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.data.BookmarkEntity
import `in`.iambhvsh.shelf.data.BookmarkTagCrossRef
import `in`.iambhvsh.shelf.data.CollectionEntity
import `in`.iambhvsh.shelf.data.SortOrder
import `in`.iambhvsh.shelf.data.TagEntity
import `in`.iambhvsh.shelf.features.fetch.LinkPreviewData
import `in`.iambhvsh.shelf.features.home.HomeEvents
import `in`.iambhvsh.shelf.features.home.HomeState
import `in`.iambhvsh.shelf.features.home.HomeViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeStateTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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

    @Test
    fun testSelectionMode_togglesWithSelectedIds() {
        var state = HomeState()
        assertFalse(state.isSelectionMode)
        assertTrue(state.selectedIds.isEmpty())

        val selected = setOf(1L, 2L, 3L)
        state = state.copy(selectedIds = selected, isSelectionMode = true)
        assertTrue(state.isSelectionMode)
        assertEquals(3, state.selectedIds.size)

        state = state.copy(selectedIds = emptySet(), isSelectionMode = false)
        assertFalse(state.isSelectionMode)
        assertTrue(state.selectedIds.isEmpty())
    }

    @Test
    fun testSortOrderTransitions_inHomeState() {
        var state = HomeState()
        assertEquals(SortOrder.DATE_NEWEST, state.sortOrder)

        state = state.copy(sortOrder = SortOrder.TITLE_ASC)
        assertEquals(SortOrder.TITLE_ASC, state.sortOrder)

        state = state.copy(sortOrder = SortOrder.TITLE_DESC)
        assertEquals(SortOrder.TITLE_DESC, state.sortOrder)

        state = state.copy(sortOrder = SortOrder.DATE_OLDEST)
        assertEquals(SortOrder.DATE_OLDEST, state.sortOrder)
    }

    @Test
    fun testHomeViewModel_dialogDismissIsIdempotentAndSaveErrorResetsLoading() = runTest {
        val repo = ControllableBookmarkRepository()
        val parser = FakeLinkMetadataParser { throw IllegalStateException("Network down") }
        val vm = HomeViewModel(repo, FakeReminderManager(), FakeUpdateManager(), parser, mainDispatcherRule.dispatcher, mainDispatcherRule.dispatcher)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.homeEvents(HomeEvents.OnDialogDismissClick)
        assertFalse(vm.state.value.isDialog)

        vm.homeEvents(HomeEvents.FabClick)
        assertTrue(vm.state.value.isDialog)
        vm.homeEvents(HomeEvents.OnDialogDismissClick)
        vm.homeEvents(HomeEvents.OnDialogDismissClick)
        assertFalse(vm.state.value.isDialog)

        vm.homeEvents(HomeEvents.OnTextFieldValueChange("example.com/fail"))
        vm.homeEvents(HomeEvents.SaveBookmark)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.state.value.isLoading)
        assertEquals("Network down", vm.state.value.error)
    }

    @Test
    fun testHomeViewModel_duplicateDetectionNormalizesAndResetsDuplicateToastKey() = runTest {
        val repo = ControllableBookmarkRepository()
        repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://shelf.app", title = "Shelf", description = null, imageUrl = "https://shelf.app/i.png")
        )
        val vm = HomeViewModel(repo, FakeReminderManager(), FakeUpdateManager(), FakeLinkMetadataParser(), mainDispatcherRule.dispatcher, mainDispatcherRule.dispatcher)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.homeEvents(HomeEvents.FabClick)
        vm.homeEvents(HomeEvents.OnTextFieldValueChange("HTTPS://shelf.app/"))
        vm.homeEvents(HomeEvents.SaveBookmark)

        assertEquals(1, vm.state.value.duplicateToastKey)
        assertFalse(vm.state.value.isDialog)
        assertEquals("", vm.state.value.inputUrl)

        vm.homeEvents(HomeEvents.ClearDuplicateToast)
        assertEquals(0, vm.state.value.duplicateToastKey)
    }

    @Test
    fun testHomeViewModel_deletingFilteredTagClearsFilterAndReloadsBookmarks() = runTest {
        val repo = ControllableBookmarkRepository()
        val tagId = repo.store.tagDao.insertTag(TagEntity(name = "Kotlin"))
        val bm1 = repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://kotlinlang.org", title = "Kotlin", description = null, imageUrl = "https://a.png")
        )
        val bm2 = repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://other.org", title = "Other", description = null, imageUrl = "https://b.png")
        )
        repo.store.tagDao.addTagToBookmark(BookmarkTagCrossRef(bm1, tagId))

        val vm = HomeViewModel(repo, FakeReminderManager(), FakeUpdateManager(), FakeLinkMetadataParser(), mainDispatcherRule.dispatcher, mainDispatcherRule.dispatcher)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, vm.state.value.bookmarkData.size)

        vm.homeEvents(HomeEvents.ToggleTagFilter(tagId))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(setOf(tagId), vm.state.value.activeTagFilters)
        assertEquals(listOf(bm1), vm.state.value.bookmarkData.map { it.id })

        vm.homeEvents(HomeEvents.DeleteTag(tagId))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.activeTagFilters.isEmpty())
        assertEquals(setOf(bm1, bm2), vm.state.value.bookmarkData.map { it.id }.toSet())
    }

    @Test
    fun testHomeViewModel_startupMetadataDoesNotRefetchImageLessItemsRepeatedly() = runTest {
        val repo = ControllableBookmarkRepository()
        val bmId = repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://textonly.example.com", title = "Text Only", description = null, imageUrl = null)
        )
        val parser = FakeLinkMetadataParser { url ->
            LinkPreviewData(title = "Text Only", description = null, imageUrl = null, url = url)
        }
        val vm = HomeViewModel(repo, FakeReminderManager(), FakeUpdateManager(), parser, mainDispatcherRule.dispatcher, mainDispatcherRule.dispatcher)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("https://textonly.example.com"), parser.parsedUrls)

        val currentBm = vm.state.value.bookmarkData.first { it.id == bmId }
        vm.homeEvents(HomeEvents.TogglePin(currentBm))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, parser.parsedUrls.size)
    }

    @Test
    fun testHomeViewModel_selectionDeleteRemindersNotesAndTransientReset() = runTest {
        val repo = ControllableBookmarkRepository()
        val reminders = FakeReminderManager()
        val updates = FakeUpdateManager(hasUpdateResult = true)
        val colId = repo.store.collectionDao.insertCollection(CollectionEntity(name = "Read"))
        val bm1 = repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://1.com", title = "One", description = null, imageUrl = "https://1.png", reminderTime = 1234L)
        )
        val bm2 = repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://2.com", title = "Two", description = null, imageUrl = "https://2.png")
        )

        val vm = HomeViewModel(repo, reminders, updates, FakeLinkMetadataParser(), mainDispatcherRule.dispatcher, mainDispatcherRule.dispatcher)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.showUpdateSheet)

        vm.homeEvents(HomeEvents.UpdateNote(bm1, "   "))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertNull(repo.store.bookmarkDao.getBookmarkById(bm1)?.note)

        vm.homeEvents(HomeEvents.SetReminder(bm2, 9_999L))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(reminders.scheduled.contains(bm2 to 9_999L))

        vm.homeEvents(HomeEvents.SelectAll)
        assertEquals(setOf(bm1, bm2), vm.state.value.selectedIds)
        vm.homeEvents(HomeEvents.AddToCollection(colId))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertFalse(vm.state.value.isSelectionMode)
        assertEquals("Added to Read", vm.state.value.toastMessage)

        vm.homeEvents(HomeEvents.ToggleSelection(bm1))
        vm.homeEvents(HomeEvents.DeleteSelected)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(reminders.cancelled.contains(bm1))
        assertTrue(repo.store.bookmarkDao.getBookmarkById(bm1)!!.isHidden)

        vm.clearTransientState()
        assertTrue(vm.state.value.activeTagFilters.isEmpty())
        assertTrue(vm.state.value.selectedIds.isEmpty())
        assertNotNull(repo.store.bookmarkDao.getBookmarkById(bm2))
    }

    @Test
    fun testHomeViewModel_externalTagDeletionPrunesActiveFilterAndImageRefreshDeduplicates() = runTest {
        val repo = ControllableBookmarkRepository()
        val tagId = repo.store.tagDao.insertTag(TagEntity(name = "ExternalTag"))
        val bm1 = repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://img1.dev", title = "One", description = null, imageUrl = "https://stale.dev/1.png")
        )
        repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://img2.dev", title = "Two", description = null, imageUrl = "https://ok.dev/2.png")
        )
        repo.store.tagDao.addTagToBookmark(BookmarkTagCrossRef(bm1, tagId))

        val parser = FakeLinkMetadataParser { url ->
            LinkPreviewData(title = "One", description = null, imageUrl = "https://fresh.dev/1.png", url = url)
        }
        val vm = HomeViewModel(repo, FakeReminderManager(), FakeUpdateManager(), parser, mainDispatcherRule.dispatcher, mainDispatcherRule.dispatcher)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.homeEvents(HomeEvents.ToggleTagFilter(tagId))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(bm1), vm.state.value.bookmarkData.map { it.id })

        repo.deleteTag(tagId)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.activeTagFilters.isEmpty())
        assertEquals(2, vm.state.value.bookmarkData.size)

        vm.homeEvents(HomeEvents.RefreshBookmarkImage(bm1, "https://img1.dev", "https://stale.dev/1.png"))
        vm.homeEvents(HomeEvents.RefreshBookmarkImage(bm1, "https://img1.dev", "https://stale.dev/1.png"))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals("https://fresh.dev/1.png", repo.store.bookmarkDao.getBookmarkById(bm1)?.imageUrl)
        assertEquals(listOf("https://img1.dev"), parser.parsedUrls)
    }

    @Test
    fun testHomeViewModel_everyRemainingEventSortOrderAndLiveSheetSync() = runTest {
        val repo = ControllableBookmarkRepository()
        val reminders = FakeReminderManager()
        val updates = FakeUpdateManager(hasUpdateResult = true)
        val parser = FakeLinkMetadataParser { url ->
            if (url.contains("nullmeta")) null else LinkPreviewData(title = "Title $url", imageUrl = "https://img.dev/x.png", url = url)
        }
        val bOld = repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://z-old.dev", title = "Zebra", description = "D1", imageUrl = "https://i/1.png", createdAt = 100L, isPinned = false)
        )
        val bNewPinned = repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://a-new.dev", title = "alpha", description = "D2", imageUrl = "https://i/2.png", createdAt = 300L, isPinned = true, reminderTime = 50_000L)
        )
        val bMid = repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://m-mid.dev", title = "Beta", description = "D3", imageUrl = "https://i/3.png", createdAt = 200L, isPinned = false)
        )
        val colWork = repo.store.collectionDao.insertCollection(CollectionEntity(name = "Work"))
        val tagRead = repo.store.tagDao.insertTag(TagEntity(name = "Read"))

        val vm = HomeViewModel(repo, reminders, updates, parser, mainDispatcherRule.dispatcher, mainDispatcherRule.dispatcher)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state.value.showUpdateSheet)
        vm.homeEvents(HomeEvents.DismissUpdateSheet)
        assertFalse(vm.state.value.showUpdateSheet)
        vm.homeEvents(HomeEvents.InstallUpdate)
        assertEquals(1, updates.downloadAndInstallCalls)

        vm.homeEvents(HomeEvents.ShowSortSheet)
        assertTrue(vm.state.value.showSortSheet)
        vm.homeEvents(HomeEvents.HideSortSheet)
        assertFalse(vm.state.value.showSortSheet)

        vm.homeEvents(HomeEvents.SetSortOrder(SortOrder.DATE_NEWEST))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(bNewPinned, bMid, bOld), vm.state.value.bookmarkData.map { it.id })

        vm.homeEvents(HomeEvents.SetSortOrder(SortOrder.DATE_OLDEST))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(bNewPinned, bOld, bMid), vm.state.value.bookmarkData.map { it.id })

        vm.homeEvents(HomeEvents.SetSortOrder(SortOrder.TITLE_ASC))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(bNewPinned, bMid, bOld), vm.state.value.bookmarkData.map { it.id })

        vm.homeEvents(HomeEvents.SetSortOrder(SortOrder.TITLE_DESC))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(bNewPinned, bOld, bMid), vm.state.value.bookmarkData.map { it.id })

        vm.homeEvents(HomeEvents.OnTextFieldValueChange("   "))
        vm.homeEvents(HomeEvents.SaveBookmark)
        vm.homeEvents(HomeEvents.OnTextFieldValueChange("nullmeta.dev"))
        vm.homeEvents(HomeEvents.SaveBookmark)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(repo.existsByUrl("https://nullmeta.dev"))

        vm.homeEvents(HomeEvents.PreviewImageClick("https://i/1.png"))
        assertTrue(vm.state.value.isPhotoPreviewDialog)
        assertEquals("https://i/1.png", vm.state.value.dialogPhotoUrl)
        vm.homeEvents(HomeEvents.PreviewImageDismissClick)
        assertFalse(vm.state.value.isPhotoPreviewDialog)

        val target = vm.state.value.bookmarkData.first { it.id == bOld }
        vm.homeEvents(HomeEvents.BookmarkPreviewClick(target))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.isBodySheet)

        vm.homeEvents(HomeEvents.TogglePin(target))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.tempBookmark?.isPinned == true)

        vm.homeEvents(HomeEvents.ShowTagManager)
        assertTrue(vm.state.value.showTagManager)
        assertFalse(vm.state.value.isBodySheet)
        vm.homeEvents(HomeEvents.CreateTag("Swift"))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        val tagObj = vm.state.value.tags.first { it.id == tagRead }
        vm.homeEvents(HomeEvents.ToggleTagForBookmark(tagObj, true))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(listOf(tagRead), vm.state.value.tempBookmarkTags.map { it.id })
        vm.homeEvents(HomeEvents.ToggleTagForBookmark(tagObj, false))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.tempBookmarkTags.isEmpty())
        vm.homeEvents(HomeEvents.HideTagManager)
        assertFalse(vm.state.value.showTagManager)
        assertNull(vm.state.value.tempBookmark)

        vm.homeEvents(HomeEvents.OpenBookmarkById(999_999L))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertNull(vm.state.value.tempBookmark)

        vm.homeEvents(HomeEvents.OpenBookmarkById(bNewPinned))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.isBodySheet)
        assertEquals(bNewPinned, vm.state.value.tempBookmark?.id)
        assertNull(vm.state.value.tempBookmark?.reminderTime)
        assertNull(repo.store.bookmarkDao.getBookmarkById(bNewPinned)?.reminderTime)
        assertTrue(reminders.cancelled.contains(bNewPinned))

        vm.homeEvents(HomeEvents.ShowEditDialog(vm.state.value.tempBookmark!!))
        assertTrue(vm.state.value.showRenameDialog)
        vm.homeEvents(HomeEvents.HideEditDialog)
        assertFalse(vm.state.value.showRenameDialog)

        val currentTarget = vm.state.value.bookmarkData.first { it.id == bOld }
        vm.homeEvents(HomeEvents.BookmarkPreviewClick(currentTarget))
        vm.homeEvents(HomeEvents.ShowEditDialog(currentTarget))
        vm.homeEvents(HomeEvents.UpdateBookmarkDetails(bOld, "Renamed Old", "Updated Desc"))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals("Renamed Old", repo.store.bookmarkDao.getBookmarkById(bOld)?.title)

        vm.homeEvents(HomeEvents.BookmarkPreviewClick(currentTarget))
        vm.homeEvents(HomeEvents.ShowNoteEditor("init note"))
        assertEquals("init note", vm.state.value.noteEditorText)
        vm.homeEvents(HomeEvents.HideNoteEditor)
        assertFalse(vm.state.value.showNoteEditor)

        vm.homeEvents(HomeEvents.BookmarkPreviewClick(currentTarget))
        vm.homeEvents(HomeEvents.ShowReminderPicker)
        assertTrue(vm.state.value.showReminderPicker)
        vm.homeEvents(HomeEvents.HideReminderPicker)
        assertFalse(vm.state.value.showReminderPicker)

        vm.homeEvents(HomeEvents.BookmarkPreviewClick(currentTarget))
        vm.homeEvents(HomeEvents.DeleteBookmark(currentTarget))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(repo.store.bookmarkDao.getBookmarkById(bOld)!!.isHidden)
        assertEquals("Deleted bookmark", vm.state.value.toastMessage)
        vm.homeEvents(HomeEvents.ClearToast)
        assertNull(vm.state.value.toastMessage)

        vm.homeEvents(HomeEvents.SelectAll)
        assertTrue(vm.state.value.isSelectionMode)
        vm.homeEvents(HomeEvents.ShowCollectionPicker)
        assertTrue(vm.state.value.showCollectionPicker)
        vm.homeEvents(HomeEvents.HideCollectionPicker)
        assertFalse(vm.state.value.showCollectionPicker)
        vm.homeEvents(HomeEvents.AddToCollection(colWork))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals("Added to Work", vm.state.value.toastMessage)
        assertFalse(vm.state.value.isSelectionMode)

        vm.homeEvents(HomeEvents.ToggleSelection(bMid))
        vm.homeEvents(HomeEvents.ToggleSelection(bMid))
        assertFalse(vm.state.value.isSelectionMode)
        vm.homeEvents(HomeEvents.SelectAll)
        vm.homeEvents(HomeEvents.DeselectAll)
        assertFalse(vm.state.value.isSelectionMode)

        repo.forceHideError = IllegalStateException("Hide failed")
        vm.homeEvents(HomeEvents.ToggleSelection(bMid))
        vm.homeEvents(HomeEvents.DeleteSelected)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals("Hide failed", vm.state.value.error)

        vm.clearTransientState()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertFalse(vm.state.value.isSelectionMode)
        assertTrue(vm.attemptedStartupMetadataIds.isNotEmpty() || vm.state.value.bookmarkData.all { it.imageUrl != null })
    }
}
