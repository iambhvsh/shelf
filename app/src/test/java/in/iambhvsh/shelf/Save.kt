package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.data.BookmarkEntity
import `in`.iambhvsh.shelf.data.BookmarkTagCrossRef
import `in`.iambhvsh.shelf.data.Collection
import `in`.iambhvsh.shelf.data.CollectionEntity
import `in`.iambhvsh.shelf.data.Tag
import `in`.iambhvsh.shelf.data.TagEntity
import `in`.iambhvsh.shelf.features.fetch.LinkPreviewData
import `in`.iambhvsh.shelf.features.save.QuickSaveEvents
import `in`.iambhvsh.shelf.features.save.QuickSaveState
import `in`.iambhvsh.shelf.features.save.QuickSaveViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SaveTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun testQuickSaveState_defaultsToSaving() {
        val state = QuickSaveState(url = "https://example.com")
        assertEquals("https://example.com", state.url)
        assertTrue(state.isSaving)
        assertFalse(state.isAlreadySaved)
        assertFalse(state.wasRestoredFromTrash)
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

    @Test
    fun testQuickSaveViewModel_newUrlEnrichesMetadataViaExternalScopeAfterViewModelIdleOrDismiss() = runTest {
        val repo = ControllableBookmarkRepository()
        val externalScope = TestScope(mainDispatcherRule.dispatcher + SupervisorJob())
        val parser = FakeLinkMetadataParser { url ->
            delay(500)
            LinkPreviewData(
                title = "Fetched Title",
                description = "Fetched Desc",
                imageUrl = "https://cdn.example.com/cover.png",
                url = url
            )
        }
        val vm = QuickSaveViewModel(repo, parser = parser, externalScope = externalScope)

        vm.onEvent(QuickSaveEvents.Init("Share link: https://example.com/post."))
        mainDispatcherRule.dispatcher.scheduler.runCurrent()

        val initialState = vm.state.value
        assertEquals("https://example.com/post", initialState.url)
        assertNotNull(initialState.bookmarkId)
        assertFalse(initialState.isAlreadySaved)
        assertEquals("Saved successfully", initialState.message)

        externalScope.advanceUntilIdle()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val saved = repo.store.bookmarkDao.getBookmarkById(initialState.bookmarkId!!)!!
        assertEquals("Fetched Title", saved.title)
        assertEquals("Fetched Desc", saved.description)
        assertEquals("https://cdn.example.com/cover.png", saved.imageUrl)
        assertFalse(vm.state.value.isSaving)
        externalScope.cancel()
    }

    @Test
    fun testQuickSaveViewModel_cancelRestoredFromTrashReHidesAndPreservesUserMetadata() = runTest {
        val repo = ControllableBookmarkRepository()
        val colId = repo.store.collectionDao.insertCollection(CollectionEntity(name = "Saved"))
        val tagId = repo.store.tagDao.insertTag(TagEntity(name = "Important"))
        val trashedId = repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(
                url = "https://trash.example.com",
                title = "Original Trashed Title",
                description = "Original Desc",
                imageUrl = "https://trash.example.com/thumb.png",
                isHidden = true,
                isPinned = true,
                note = "Keep my note",
                reminderTime = 999_999L
            )
        )
        repo.store.collectionDao.addBookmarkToCollection(
            `in`.iambhvsh.shelf.data.BookmarkCollectionCrossRef(trashedId, colId)
        )
        repo.store.tagDao.addTagToBookmark(BookmarkTagCrossRef(trashedId, tagId))

        val externalScope = TestScope(mainDispatcherRule.dispatcher + SupervisorJob())
        val parser = FakeLinkMetadataParser()
        val vm = QuickSaveViewModel(repo, parser = parser, externalScope = externalScope)

        vm.onEvent(QuickSaveEvents.Init("https://trash.example.com"))
        mainDispatcherRule.dispatcher.scheduler.runCurrent()

        assertTrue(vm.state.value.wasRestoredFromTrash)
        assertFalse(vm.state.value.isAlreadySaved)
        assertEquals(trashedId, vm.state.value.bookmarkId)

        vm.onEvent(QuickSaveEvents.CancelSave)
        externalScope.advanceUntilIdle()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val afterCancel = repo.store.bookmarkDao.getBookmarkById(trashedId)
        assertNotNull("Trashed bookmark must not be permanently purged on Cancel", afterCancel)
        assertTrue(afterCancel!!.isHidden)
        assertEquals("Original Trashed Title", afterCancel.title)
        assertTrue(afterCancel.isPinned)
        assertEquals("Keep my note", afterCancel.note)
        assertEquals(999_999L, afterCancel.reminderTime)
        assertEquals(colId, repo.store.collectionDao.getCollectionIdForBookmark(trashedId))
        assertEquals(listOf(tagId), repo.store.tagDao.getTagsForBookmarkOnce(trashedId).map { it.id })
        externalScope.cancel()
    }

    @Test
    fun testQuickSaveViewModel_alreadySavedDoesNotDeleteOnCancelAndCancelBeforeSaveAbortsInsert() = runTest {
        val repo = ControllableBookmarkRepository()
        val existingId = repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://active.example.com", title = "Existing", description = null, imageUrl = null)
        )
        val externalScope = TestScope(mainDispatcherRule.dispatcher + SupervisorJob())
        val parser = FakeLinkMetadataParser()
        val vm1 = QuickSaveViewModel(repo, parser = parser, externalScope = externalScope)

        vm1.onEvent(QuickSaveEvents.Init("https://active.example.com"))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm1.state.value.isAlreadySaved)
        assertEquals("Already saved", vm1.state.value.message)
        assertTrue(parser.parsedUrls.isEmpty())

        vm1.onEvent(QuickSaveEvents.CancelSave)
        externalScope.advanceUntilIdle()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertNotNull(repo.store.bookmarkDao.getBookmarkById(existingId))

        var cancelTriggered = false
        val vm2 = QuickSaveViewModel(repo, parser = parser, externalScope = externalScope)
        repo.onBeforeSaveReturnId = {
            if (!cancelTriggered) {
                cancelTriggered = true
                vm2.onEvent(QuickSaveEvents.CancelSave)
            }
        }
        vm2.onEvent(QuickSaveEvents.Init("https://race.example.com"))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        externalScope.advanceUntilIdle()
        assertNull(repo.store.bookmarkDao.findByUrl("https://race.example.com"))
        externalScope.cancel()
    }

    @Test
    fun testQuickSaveViewModel_tagAndCollectionMutations() = runTest {
        val repo = ControllableBookmarkRepository()
        val col1 = repo.store.collectionDao.insertCollection(CollectionEntity(name = "Work"))
        val col2 = repo.store.collectionDao.insertCollection(CollectionEntity(name = "Home"))
        val externalScope = TestScope(mainDispatcherRule.dispatcher + SupervisorJob())
        val vm = QuickSaveViewModel(repo, parser = FakeLinkMetadataParser(), externalScope = externalScope)

        vm.onEvent(QuickSaveEvents.Init("https://multi.example.com"))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        externalScope.advanceUntilIdle()

        val id = vm.state.value.bookmarkId!!
        vm.onEvent(QuickSaveEvents.SelectCollection(col1))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(col1, vm.state.value.selectedCollection?.id)

        vm.onEvent(QuickSaveEvents.SelectCollection(col2))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(col2, vm.state.value.selectedCollection?.id)
        assertEquals(col2, repo.store.collectionDao.getCollectionIdForBookmark(id))

        vm.onEvent(QuickSaveEvents.CreateTag("ReadLater"))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, vm.state.value.selectedTags.size)

        val createdTagId = vm.state.value.selectedTags.first().id
        vm.onEvent(QuickSaveEvents.ToggleTag(createdTagId))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.selectedTags.isEmpty())

        vm.onEvent(QuickSaveEvents.ToggleTag(createdTagId))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, vm.state.value.selectedTags.size)

        vm.onEvent(QuickSaveEvents.DeleteTag(createdTagId))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.selectedTags.isEmpty())
        assertTrue(vm.state.value.allTags.isEmpty())

        vm.onEvent(QuickSaveEvents.MessageShown)
        assertNull(vm.state.value.message)
        vm.onEvent(QuickSaveEvents.Init("https://multi.example.com"))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals("https://multi.example.com", vm.state.value.url)
        externalScope.cancel()
    }

    @Test
    fun testQuickSaveViewModel_blankInitPickerStatesAndTrashCancelCleanup() = runTest {
        val repo = ControllableBookmarkRepository()
        val colOld = repo.store.collectionDao.insertCollection(CollectionEntity(name = "OldCol"))
        val colNew = repo.store.collectionDao.insertCollection(CollectionEntity(name = "NewCol"))
        val tagOrig = repo.store.tagDao.insertTag(TagEntity(name = "Orig"))
        val trashId = repo.store.bookmarkDao.insertWithReturn(
            BookmarkEntity(
                url = "https://trash-edit.dev",
                title = "Orig Title",
                description = "Orig Desc",
                imageUrl = "https://trash-edit.dev/o.png",
                isHidden = true,
                isPinned = true,
                note = "Orig Note",
                reminderTime = 999_000L
            )
        )
        repo.store.collectionDao.addBookmarkToCollection(`in`.iambhvsh.shelf.data.BookmarkCollectionCrossRef(trashId, colOld))
        repo.store.tagDao.addTagToBookmark(BookmarkTagCrossRef(trashId, tagOrig))

        val externalScope = TestScope(mainDispatcherRule.dispatcher + SupervisorJob())
        val vm = QuickSaveViewModel(repo, FakeLinkMetadataParser(), externalScope)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.onEvent(QuickSaveEvents.Init("   "))
        assertFalse(vm.state.value.isSaving)
        assertEquals("No valid URL found", vm.state.value.message)

        vm.onEvent(QuickSaveEvents.ShowCollectionPicker)
        assertTrue(vm.state.value.showCollectionPicker)
        vm.onEvent(QuickSaveEvents.HideCollectionPicker)
        assertFalse(vm.state.value.showCollectionPicker)

        vm.onEvent(QuickSaveEvents.ShowTagPicker)
        assertTrue(vm.state.value.showTagPicker)
        vm.onEvent(QuickSaveEvents.HideTagPicker)
        assertFalse(vm.state.value.showTagPicker)

        vm.onEvent(QuickSaveEvents.Init("https://trash-edit.dev/"))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.wasRestoredFromTrash)

        vm.onEvent(QuickSaveEvents.SelectCollection(colNew))
        vm.onEvent(QuickSaveEvents.CreateTag("   "))
        vm.onEvent(QuickSaveEvents.CreateTag("TempExtraTag"))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        assertEquals(colNew, repo.getCollectionIdForBookmark(trashId))

        vm.onEvent(QuickSaveEvents.CancelSave)
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        externalScope.advanceUntilIdle()

        val reverted = repo.store.bookmarkDao.getBookmarkById(trashId)!!
        assertTrue(reverted.isHidden)
        assertEquals("Orig Title", reverted.title)
        assertEquals("Orig Note", reverted.note)
        assertEquals(colOld, repo.getCollectionIdForBookmark(trashId))
        val revertedTags = repo.store.tagDao.getTagsForBookmarkOnce(trashId)
        assertEquals(listOf(tagOrig), revertedTags.map { it.id })
        externalScope.cancel()
    }
}
