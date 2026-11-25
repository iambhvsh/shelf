package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.data.BookmarkCollectionCrossRef
import `in`.iambhvsh.shelf.data.BackupData
import `in`.iambhvsh.shelf.data.BookmarkEntity
import `in`.iambhvsh.shelf.data.BookmarkTagCrossRef
import `in`.iambhvsh.shelf.data.CollectionEntity
import `in`.iambhvsh.shelf.data.TagEntity
import `in`.iambhvsh.shelf.features.backup.BackupManager
import `in`.iambhvsh.shelf.features.fetch.LinkMetadataParser
import `in`.iambhvsh.shelf.features.fetch.LinkPreviewData
import `in`.iambhvsh.shelf.features.fetch.NetworkFetcher
import `in`.iambhvsh.shelf.features.setting.ChangelogLoader
import `in`.iambhvsh.shelf.features.setting.formatLastBackupTime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChangelogAndBackupManagerTest {

    private val backupJson = Json { ignoreUnknownKeys = true }

    @Test
    fun testChangelogLoader_returnsFriendlyMessageWhenAssetsUnavailable() {
        val text = ChangelogLoader(FakeContext()).load()
        assertTrue(text.startsWith("Error loading changelog:"))
    }

    @Test
    fun testFormatLastBackupTime_handlesNonPositiveAndPositiveTimestamps() {
        assertEquals("", formatLastBackupTime(0L))
        assertEquals("", formatLastBackupTime(-500L))

        val formatted = formatLastBackupTime(1_700_000_000_000L)
        assertTrue(formatted.startsWith("Last backup: "))
        assertTrue(formatted.length > "Last backup: ".length)
    }

    @Test
    fun testRealBackupManager_generateBackupJsonAndExportHtml_captureAllRelations() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val db = FakeBookmarkDatabase()
        val store = db.store
        val b1 = store.bookmarkDao.insertWithReturn(
            BookmarkEntity(
                url = "https://kotlinlang.org",
                title = "Kotlin",
                description = "Docs",
                imageUrl = "https://kotlinlang.org/img.png",
                isHidden = false,
                isPinned = true,
                note = "Pinned doc",
                reminderTime = 999L
            )
        )
        val bHidden = store.bookmarkDao.insertWithReturn(
            BookmarkEntity(
                url = "https://trashed.dev",
                title = "Trashed",
                description = null,
                imageUrl = null,
                isHidden = true
            )
        )
        val colId = store.collectionDao.insertCollection(CollectionEntity(name = "Dev"))
        store.collectionDao.insertCollection(CollectionEntity(name = "   "))
        store.collectionDao.addBookmarkToCollection(BookmarkCollectionCrossRef(b1, colId))
        store.collectionDao.addBookmarkToCollection(BookmarkCollectionCrossRef(bHidden, colId))
        val tagId = store.tagDao.insertTag(TagEntity(name = "Android"))
        store.tagDao.addTagToBookmark(BookmarkTagCrossRef(b1, tagId))

        val manager = BackupManager(
            database = db,
            bookmarkDao = store.bookmarkDao,
            collectionDao = store.collectionDao,
            tagDao = store.tagDao,
            settingsRepository = FakeSettingsRepository(),
            context = FakeContext(),
            ioDispatcher = dispatcher
        )

        val jsonStr = manager.generateBackupJson()
        val decoded = backupJson.decodeFromString<BackupData>(jsonStr)
        assertEquals(2, decoded.bookmarks.size)
        assertEquals(1, decoded.collections.size)
        assertEquals("Dev", decoded.collections.single().name)
        assertEquals(2, decoded.collections.single().bookmarkUrls.size)
        assertEquals(listOf("Android"), decoded.bookmarks.first { it.url == "https://kotlinlang.org" }.tags)
        assertTrue(decoded.bookmarks.first { it.url == "https://trashed.dev" }.isHidden)
        assertEquals(listOf("Android"), decoded.tags.map { it.name })

        val html = manager.generateExportHtml()
        assertTrue(html.contains("<H3>Dev</H3>"))
        assertTrue(html.contains("https://kotlinlang.org"))
        assertFalse(html.contains("https://trashed.dev"))
    }

    @Test
    fun testRealBackupManager_fetchMissingImages_updatesUnreachableAndMissingAndRethrowsCancellation() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val db = FakeBookmarkDatabase()
        val store = db.store

        val bMissing = store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://missing.example.com", title = "https://missing.example.com", description = null, imageUrl = null)
        )
        val bBroken = store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://broken.example.com", title = "Broken Title", description = null, imageUrl = "https://broken.example.com/old.png")
        )
        val bHealthy = store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://healthy.example.com", title = "Healthy", description = null, imageUrl = "https://healthy.example.com/ok.png")
        )

        val okClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val url = chain.request().url.toString()
                val code = if (url.contains("broken.example.com/old.png")) 404 else 200
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(code)
                    .message("Status")
                    .body(ByteArray(0).toResponseBody("image/png".toMediaType()))
                    .build()
            })
            .build()

        val fakeParser = FakeLinkMetadataParser { url ->
            when (url) {
                "https://missing.example.com" -> LinkPreviewData(title = "Fetched Title", imageUrl = "https://missing.example.com/new.png", url = url)
                "https://broken.example.com" -> LinkPreviewData(title = "Ignored Overwrite", imageUrl = "https://broken.example.com/fixed.png", url = url)
                else -> LinkPreviewData(title = "Should Not Parse", imageUrl = "https://other.png", url = url)
            }
        }

        val manager = BackupManager(
            database = db,
            bookmarkDao = store.bookmarkDao,
            collectionDao = store.collectionDao,
            tagDao = store.tagDao,
            settingsRepository = FakeSettingsRepository(),
            context = FakeContext(),
            linkMetadataParser = fakeParser,
            networkFetcher = NetworkFetcher(okClient),
            ioDispatcher = dispatcher
        )

        manager.fetchMissingImages(checkExistingImages = false)
        assertEquals("https://missing.example.com/new.png", store.bookmarkDao.getBookmarkById(bMissing)?.imageUrl)
        assertEquals("Fetched Title", store.bookmarkDao.getBookmarkById(bMissing)?.title)
        assertEquals("https://broken.example.com/old.png", store.bookmarkDao.getBookmarkById(bBroken)?.imageUrl)

        fakeParser.parsedUrls.clear()
        manager.fetchMissingImages(checkExistingImages = true)
        assertEquals("https://broken.example.com/fixed.png", store.bookmarkDao.getBookmarkById(bBroken)?.imageUrl)
        assertEquals("Broken Title", store.bookmarkDao.getBookmarkById(bBroken)?.title)
        assertEquals("https://healthy.example.com/ok.png", store.bookmarkDao.getBookmarkById(bHealthy)?.imageUrl)
        assertFalse(fakeParser.parsedUrls.contains("https://healthy.example.com"))

        val cancellingParser = object : LinkMetadataParser(ioDispatcher = dispatcher) {
            override suspend fun parse(url: String): LinkPreviewData {
                throw CancellationException("abort")
            }
        }
        val bBlank = store.bookmarkDao.insertWithReturn(
            BookmarkEntity(url = "https://cancel.example.com", title = null, description = null, imageUrl = null)
        )
        assertNull(store.bookmarkDao.getBookmarkById(bBlank)?.imageUrl)
        val cancelManager = BackupManager(
            database = db,
            bookmarkDao = store.bookmarkDao,
            collectionDao = store.collectionDao,
            tagDao = store.tagDao,
            settingsRepository = FakeSettingsRepository(),
            context = FakeContext(),
            linkMetadataParser = cancellingParser,
            networkFetcher = NetworkFetcher(okClient),
            ioDispatcher = dispatcher
        )
        var cancelled = false
        try {
            cancelManager.fetchMissingImages(checkExistingImages = false)
        } catch (_: CancellationException) {
            cancelled = true
        }
        assertTrue(cancelled)
    }
}
