package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.data.BookmarkCollectionCrossRef
import `in`.iambhvsh.shelf.data.BookmarkEntity
import `in`.iambhvsh.shelf.data.BookmarkRepositoryImpl
import `in`.iambhvsh.shelf.data.CollectionEntity
import `in`.iambhvsh.shelf.data.TagEntity
import `in`.iambhvsh.shelf.util.Resource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RepositoryTest {

    @Test
    fun testBatchOperations_chunkIdsAboveSqliteParameterLimitAndSkipEmptyLists() = runTest {
        val store = InMemoryDaoStore()
        val repo = BookmarkRepositoryImpl(store.bookmarkDao, store.collectionDao, store.tagDao)

        val ids = (1L..1005L).toList()
        ids.forEach { id ->
            store.bookmarkDao.insertWithReturn(
                BookmarkEntity(id = id, url = "https://example.com/$id", title = "T$id", description = null, imageUrl = null)
            )
        }
        val colId = store.collectionDao.insertCollection(CollectionEntity(name = "Huge"))

        repo.hideBookmarks(emptyList())
        repo.unhideBookmarks(emptyList())
        repo.permanentlyDeleteBookmarks(emptyList())
        repo.addBookmarksToCollection(emptyList(), colId)
        assertTrue(store.hideBatchSizes.isEmpty())
        assertTrue(store.unhideBatchSizes.isEmpty())
        assertTrue(store.purgeBatchSizes.isEmpty())
        assertTrue(store.addToCollectionBatchSizes.isEmpty())

        repo.addBookmarksToCollection(ids + 1L, colId)
        assertEquals(listOf(900, 105), store.addToCollectionBatchSizes)

        repo.hideBookmarks(ids)
        assertEquals(listOf(900, 105), store.hideBatchSizes)
        assertTrue(store.bookmarksState.value.all { it.isHidden })

        repo.unhideBookmarks(ids)
        assertEquals(listOf(900, 105), store.unhideBatchSizes)
        assertTrue(store.bookmarksState.value.none { it.isHidden })

        repo.permanentlyDeleteBookmarks(ids)
        assertEquals(listOf(900, 105), store.purgeBatchSizes)
        assertTrue(store.bookmarksState.value.isEmpty())
    }

    @Test
    fun testFindBookmarkStatusByUrl_prefersVisibleRowOverTrashedDuplicate() = runTest {
        val store = InMemoryDaoStore()
        val repo = BookmarkRepositoryImpl(store.bookmarkDao, store.collectionDao, store.tagDao)

        store.bookmarkDao.insertWithReturn(
            BookmarkEntity(id = 1L, url = "https://example.com/", title = "Old Trashed", description = null, imageUrl = null, isHidden = true)
        )
        store.bookmarkDao.insertWithReturn(
            BookmarkEntity(id = 2L, url = "https://example.com", title = "Active Copy", description = null, imageUrl = null, isHidden = false)
        )

        val status = repo.findBookmarkStatusByUrl("https://example.com/")
        assertNotNull(status)
        assertFalse(status!!.isHidden)
        assertEquals(2L, status.bookmark.id)
    }

    @Test
    fun testEmptyTagListQueries_fallBackToUntaggedFlows() = runTest {
        val store = InMemoryDaoStore()
        val repo = BookmarkRepositoryImpl(store.bookmarkDao, store.collectionDao, store.tagDao)
        store.bookmarkDao.insertWithReturn(
            BookmarkEntity(id = 1L, url = "https://kotlinlang.org", title = "Kotlin", description = null, imageUrl = null)
        )

        val byTags = repo.getBookmarksByTags(emptyList()).first { it !is Resource.Loading } as Resource.Success
        assertEquals(1, byTags.data?.size)

        val search = repo.searchBookmarksWithTags("kotlin", emptyList()).first { it !is Resource.Loading } as Resource.Success
        assertEquals(1, search.data?.size)
    }

    @Test
    fun testInsertTagAndCreateCollection_trimWhitespaceAndDeduplicate() = runTest {
        val store = InMemoryDaoStore()
        val repo = BookmarkRepositoryImpl(store.bookmarkDao, store.collectionDao, store.tagDao)

        assertEquals(-1L, repo.insertTag("   "))
        assertEquals(-1L, repo.createCollection("   "))

        val firstTagId = repo.insertTag("  Compose  ")
        val duplicateTagId = repo.insertTag("Compose")
        assertTrue(firstTagId > 0L)
        assertEquals(firstTagId, duplicateTagId)
        assertEquals(listOf(TagEntity(id = firstTagId, name = "Compose")), store.tagsState.value)

        val colId = repo.createCollection("  Design  ")
        assertTrue(colId > 0L)
        assertEquals("Design", store.collectionsState.value.first().name)
    }

    @Test
    fun testFlowResourceCancellation_doesNotViolateExceptionTransparency() = runTest {
        val store = InMemoryDaoStore()
        val repo = BookmarkRepositoryImpl(store.bookmarkDao, store.collectionDao, store.tagDao)
        val bmId = store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://flow.dev", title = "Flow", description = null, imageUrl = null)
        )
        val tagId = store.tagDao.insertTag(TagEntity(name = "Coroutines"))
        store.tagDao.addTagToBookmark(`in`.iambhvsh.shelf.data.BookmarkTagCrossRef(bmId, tagId))

        val allTags = repo.getAllTags().first { it !is Resource.Loading } as Resource.Success
        assertEquals(1, allTags.data?.size)

        val tagsForBookmark = repo.getTagsForBookmark(bmId).first { it !is Resource.Loading } as Resource.Success
        assertEquals(1, tagsForBookmark.data?.size)

        val bookmarksForTag = repo.getBookmarksForTag(tagId).first { it !is Resource.Loading } as Resource.Success
        assertEquals(listOf(bmId), bookmarksForTag.data)
    }

    @Test
    fun testCrudMetadataCollectionsAndTrashLifecycle_coverAllRemainingRepositoryMethods() = runTest {
        val store = InMemoryDaoStore()
        val repo = BookmarkRepositoryImpl(store.bookmarkDao, store.collectionDao, store.tagDao)

        assertTrue(repo.insert(Bookmark(url = "https://a.dev", title = "A", description = "Desc A", imageUrl = null)))
        val idA = repo.findBookmarkStatusByUrl("https://a.dev")!!.bookmark.id
        assertTrue(repo.existsByUrl("https://a.dev"))

        assertFalse(repo.insert(Bookmark(url = "HTTPS://a.dev/", title = "Dup", description = null, imageUrl = null)))

        val idB = repo.saveAndReturnId(Bookmark(url = "https://b.dev", title = "B", description = "Desc B", imageUrl = "https://b.dev/i.png"))
        assertEquals(listOf(idA), repo.getBookmarksWithoutImage().map { it.id })

        repo.togglePinStatus(idA, true)
        repo.updateNote(idA, "Keep handy")
        repo.updateReminderTime(idA, 123_456L)
        repo.updateBookmarkTitle(idA, "Title A2")
        repo.updateBookmarkDetails(idA, "Title A3", "Desc A3")
        repo.updateImageUrl(idA, "https://a.dev/i.png")

        val updatedA = repo.getBookmarkById(idA)!!
        assertTrue(updatedA.isPinned)
        assertEquals("Keep handy", updatedA.note)
        assertEquals(123_456L, updatedA.reminderTime)
        assertEquals("Title A3", updatedA.title)
        assertEquals("Desc A3", updatedA.description)
        assertEquals("https://a.dev/i.png", updatedA.imageUrl)
        assertTrue(repo.getBookmarksWithoutImage().isEmpty())

        val activeList = (repo.getBookmarks().first { it !is Resource.Loading } as Resource.Success).data!!
        assertEquals(setOf(idA, idB), activeList.map { it.id }.toSet())

        val searched = (repo.searchBookmarks("handy").first { it !is Resource.Loading } as Resource.Success).data!!
        assertEquals(listOf(idA), searched.map { it.id })

        val tagId = repo.insertTag("Backend")
        repo.addTagToBookmark(idA, tagId)
        val byTag = (repo.getBookmarksByTags(listOf(tagId)).first { it !is Resource.Loading } as Resource.Success).data!!
        assertEquals(listOf(idA), byTag.map { it.id })
        val searchByTag = (repo.searchBookmarksWithTags("title a3", listOf(tagId)).first { it !is Resource.Loading } as Resource.Success).data!!
        assertEquals(listOf(idA), searchByTag.map { it.id })

        repo.removeTagFromBookmark(idA, tagId)
        val afterRemoveTag = (repo.getTagsForBookmark(idA).first { it !is Resource.Loading } as Resource.Success).data!!
        assertTrue(afterRemoveTag.isEmpty())
        repo.deleteTag(tagId)
        val allTags = (repo.getAllTags().first { it !is Resource.Loading } as Resource.Success).data!!
        assertTrue(allTags.isEmpty())

        val colId = repo.createCollection("Reading")
        repo.updateCollectionName(colId, "  Reading List  ")
        repo.updateCollectionName(colId, "   ")
        repo.addBookmarkToCollection(idA, colId)
        assertEquals(colId, repo.getCollectionIdForBookmark(idA))

        val colsWithUncat = (repo.getAllCollections().first { it !is Resource.Loading } as Resource.Success).data!!
        assertEquals(2, colsWithUncat.size)
        assertTrue(colsWithUncat.any { it.isVirtual && it.bookmarkCount == 1 })
        assertTrue(colsWithUncat.any { it.id == colId && it.name == "Reading List" && it.bookmarkCount == 1 })

        val inCol = (repo.getBookmarksInCollection(colId).first { it !is Resource.Loading } as Resource.Success).data!!
        assertEquals(listOf(idA), inCol.map { it.id })
        val inUncat = (repo.getBookmarksInCollection(`in`.iambhvsh.shelf.data.Collection.UNCATEGORISED_ID).first { it !is Resource.Loading } as Resource.Success).data!!
        assertEquals(listOf(idB), inUncat.map { it.id })

        repo.hideBookmarks(listOf(idA, idB))
        assertEquals(2, repo.getTrashedCount().first())
        assertEquals(1, repo.getTrashedCountForCollection(colId).first())
        assertEquals(1, repo.getTrashedCountForCollection(`in`.iambhvsh.shelf.data.Collection.UNCATEGORISED_ID).first())
        val trashed = (repo.getTrashedBookmarks().first { it !is Resource.Loading } as Resource.Success).data!!
        assertEquals(setOf(idA, idB), trashed.map { it.id }.toSet())

        repo.restoreBookmarksInCollection(colId)
        assertEquals(1, repo.getTrashedCount().first())
        repo.restoreBookmarksInCollection(`in`.iambhvsh.shelf.data.Collection.UNCATEGORISED_ID)
        assertEquals(0, repo.getTrashedCount().first())

        repo.removeBookmarkFromCollection(idA, colId)
        assertEquals(null, repo.getCollectionIdForBookmark(idA))
        repo.deleteCollection(`in`.iambhvsh.shelf.data.Collection(id = colId, name = "Reading List"))
        val onlyUncat = (repo.getAllCollections().first { it !is Resource.Loading } as Resource.Success).data!!
        assertTrue(onlyUncat.all { it.isVirtual })

        repo.hideBookmarks(listOf(idA))
        repo.emptyTrash()
        assertEquals(null, repo.getBookmarkById(idA))

        val allRemaining = (repo.getAllBookmarks().first() as Resource.Success).data!!
        assertEquals(listOf(idB), allRemaining.map { it.id })

        repo.deleteBookmark(updatedA.copy(id = idB))
        assertEquals(null, repo.getBookmarkById(idB))
    }
}
