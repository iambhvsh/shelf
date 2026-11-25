package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.data.BackupBookmark
import `in`.iambhvsh.shelf.data.BackupCollection
import `in`.iambhvsh.shelf.data.BackupData
import `in`.iambhvsh.shelf.data.BookmarkEntity
import `in`.iambhvsh.shelf.data.CollectionEntity
import `in`.iambhvsh.shelf.features.backup.exportBookmarksToHtml
import `in`.iambhvsh.shelf.features.backup.importBackupFromJson
import `in`.iambhvsh.shelf.features.backup.importBookmarksFromHtml
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupTest {

    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun testJsonImport_respectsIsHiddenFlagWhenExistingHiddenBookmarkAlsoHiddenInBackup() = runTest {
        val store = InMemoryDaoStore()
        val bmId = store.bookmarkDao.insertWithReturn(
            BookmarkEntity(
                url = "https://hidden.example.com",
                title = "Hidden",
                description = null,
                imageUrl = null,
                isHidden = true,
                note = "Secret note"
            )
        )

        val backup = BackupData(
            bookmarks = listOf(
                BackupBookmark(
                    url = "https://hidden.example.com/",
                    title = "Hidden",
                    isHidden = true,
                    note = "Secret note"
                )
            )
        )

        val result = importBackupFromJson(
            jsonString = json.encodeToString(backup),
            json = json,
            bookmarkDao = store.bookmarkDao,
            collectionDao = store.collectionDao,
            tagDao = store.tagDao
        )

        assertEquals(0, result.imported)
        assertEquals(0, result.updated)
        assertEquals(1, result.skipped)
        assertTrue(store.bookmarkDao.getBookmarkById(bmId)!!.isHidden)
    }

    @Test
    fun testJsonImport_normalizesTrailingSlashesAndMixedCaseSchemesAcrossCollectionLinks() = runTest {
        val store = InMemoryDaoStore()
        store.bookmarkDao.insertWithReturn(
            BookmarkEntity(
                url = "https://kotlinlang.org",
                title = "Kotlin",
                description = null,
                imageUrl = null
            )
        )

        val backup = BackupData(
            bookmarks = listOf(
                BackupBookmark(url = "HTTPS://kotlinlang.org/", title = "Kotlin")
            ),
            collections = listOf(
                BackupCollection(name = "Languages", bookmarkUrls = listOf("https://kotlinlang.org/"))
            )
        )

        val result = importBackupFromJson(
            jsonString = json.encodeToString(backup),
            json = json,
            bookmarkDao = store.bookmarkDao,
            collectionDao = store.collectionDao,
            tagDao = store.tagDao
        )

        assertEquals(0, result.imported)
        assertEquals(1, result.skipped)
        assertEquals(1, result.collections)
        assertTrue(result.skippedUrls.isEmpty())
        assertEquals(1, store.collectionCrossRefsState.value.size)
    }

    @Test
    fun testHtmlImport_restoresHiddenBookmarkWithoutWipingPinNoteAndReminder() = runTest {
        val store = InMemoryDaoStore()
        val bmId = store.bookmarkDao.insertWithReturn(
            BookmarkEntity(
                url = "https://example.com/article/",
                title = "Saved Article",
                description = "Desc",
                imageUrl = "https://example.com/img.png",
                createdAt = 1234L,
                isHidden = true,
                isPinned = true,
                note = "Important highlight",
                reminderTime = 999_999L
            )
        )

        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
                <DT><H3>Reading</H3>
                <DL><p>
                    <DT><A HREF="HTTPS://example.com/article">Updated Title</A>
                </DL><p>
            </DL><p>
        """.trimIndent()

        val result = importBookmarksFromHtml(html, store.bookmarkDao, store.collectionDao)
        assertEquals(1, result.imported)
        assertEquals(0, result.skipped)

        val restored = store.bookmarkDao.getBookmarkById(bmId)!!
        assertFalse(restored.isHidden)
        assertTrue(restored.isPinned)
        assertEquals("Important highlight", restored.note)
        assertEquals(999_999L, restored.reminderTime)
        assertEquals("Updated Title", restored.title)
        assertEquals(1, store.collectionCrossRefsState.value.size)
    }

    @Test
    fun testHtmlExport_groupsCollectionsWithNormalizedUrlsAndSkipsHiddenItems() = runTest {
        val store = InMemoryDaoStore()
        val id1 = store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://a.com/", title = "A & B", description = null, imageUrl = null)
        )
        val id2 = store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://b.com", title = "Unassigned", description = null, imageUrl = null)
        )
        val hiddenId = store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://hidden.com", title = "Hidden", description = null, imageUrl = null, isHidden = true)
        )
        val colId = store.collectionDao.insertCollection(CollectionEntity(name = "Read <Now>"))
        store.collectionDao.addBookmarkToCollection(`in`.iambhvsh.shelf.data.BookmarkCollectionCrossRef(id1, colId))
        store.collectionDao.addBookmarkToCollection(`in`.iambhvsh.shelf.data.BookmarkCollectionCrossRef(hiddenId, colId))

        val html = exportBookmarksToHtml(store.bookmarkDao, store.collectionDao)
        assertTrue(html.contains("<H3>Read &lt;Now&gt;</H3>"))
        assertTrue(html.contains("A &amp; B"))
        assertTrue(html.contains("<H3>Uncategorized</H3>"))
        assertTrue(html.contains("https://b.com"))
        assertFalse(html.contains("https://hidden.com"))
    }

    @Test
    fun testJsonImport_recoversNoiseRestoresHiddenAndReportsSkippedCollectionUrls() = runTest {
        val store = InMemoryDaoStore()
        val activeId = store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://active.dev", title = "Existing Active", description = null, imageUrl = null, isHidden = false)
        )
        val trashedId = store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://trashed.dev", title = "Old Title", description = null, imageUrl = null, isHidden = true)
        )

        val backup = BackupData(
            bookmarks = listOf(
                BackupBookmark(url = "https://active.dev", title = "Backup Active", tags = listOf("  KMP  ", "  ")),
                BackupBookmark(url = "https://trashed.dev", title = "Restored Title", isHidden = false, isPinned = true, note = "Back!", reminderTime = 888L),
                BackupBookmark(url = "https://new.dev", title = "Brand New")
            ),
            collections = listOf(
                BackupCollection(name = "  ", bookmarkUrls = listOf("https://new.dev")),
                BackupCollection(name = "Mixed", bookmarkUrls = listOf("https://new.dev", "https://missing-bm.dev"))
            )
        )
        val wrapped = "CORRUPT_HEADER\n" + json.encodeToString(backup) + "\nNOISE_TAIL"

        val result = importBackupFromJson(
            jsonString = wrapped,
            json = json,
            bookmarkDao = store.bookmarkDao,
            collectionDao = store.collectionDao,
            tagDao = store.tagDao
        )

        assertEquals(1, result.imported)
        assertEquals(1, result.updated)
        assertEquals(1, result.skipped)
        assertEquals(1, result.collections)
        assertEquals(listOf("https://missing-bm.dev"), result.skippedUrls)

        assertEquals("Existing Active", store.bookmarkDao.getBookmarkById(activeId)?.title)
        val restored = store.bookmarkDao.getBookmarkById(trashedId)!!
        assertFalse(restored.isHidden)
        assertEquals("Restored Title", restored.title)
        assertTrue(restored.isPinned)
        assertEquals("Back!", restored.note)
        assertEquals(888L, restored.reminderTime)
        assertEquals(listOf("KMP"), store.tagsState.value.map { it.name })
    }

    @Test
    fun testJsonImport_throwsOnUnrecoverablePayloadAndEmptyBackupSucceeds() = runTest {
        val store = InMemoryDaoStore()
        var threw = false
        try {
            importBackupFromJson("not json at all", json, store.bookmarkDao, store.collectionDao, store.tagDao)
        } catch (_: Exception) {
            threw = true
        }
        assertTrue(threw)

        val emptyRes = importBackupFromJson("""{"bookmarks":[],"collections":[],"tags":[]}""", json, store.bookmarkDao, store.collectionDao, store.tagDao)
        assertEquals(0, emptyRes.imported)
        assertEquals(0, emptyRes.updated)
        assertEquals(0, emptyRes.skipped)
        assertEquals(0, emptyRes.collections)
        assertTrue(emptyRes.skippedUrls.isEmpty())
    }

    @Test
    fun testHtmlExportAndImport_handleEmptyDatabaseAndManyToManyFolders() = runTest {
        val store = InMemoryDaoStore()
        val emptyHtml = exportBookmarksToHtml(store.bookmarkDao, store.collectionDao)
        assertTrue(emptyHtml.contains("NETSCAPE-Bookmark-file-1"))
        assertTrue(emptyHtml.contains("<H1>Shelf</H1>"))

        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
                <DT><H3>Mobile</H3>
                <DL><p>
                    <DT><A HREF="https://shared.dev">Shared Link</A>
                </DL><p>
                <DT><H3>Web</H3>
                <DL><p>
                    <DT><A HREF="https://shared.dev/">Shared Link</A>
                    <DT><A HREF="javascript:alert(1)">Bad</A>
                </DL><p>
            </DL><p>
        """.trimIndent()
        val res = importBookmarksFromHtml(html, store.bookmarkDao, store.collectionDao)
        assertEquals(1, res.imported)
        assertEquals(2, res.collections)
        assertEquals(2, store.collectionCrossRefsState.value.size)
    }
}
