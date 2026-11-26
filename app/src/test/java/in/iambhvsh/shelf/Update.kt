package `in`.iambhvsh.shelf

import android.app.DownloadManager
import `in`.iambhvsh.shelf.data.UpdateManagerImpl
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.BufferedSource
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateTest {

    @Test
    fun testIsVersionNewer_higherMajorVersionReturnsTrue() {
        assertTrue(UpdateManagerImpl.isVersionNewer("1.0.0", "2.0.0"))
        assertTrue(UpdateManagerImpl.isVersionNewer("2.0.4", "3.0.0"))
    }

    @Test
    fun testIsVersionNewer_higherMinorVersionReturnsTrue() {
        assertTrue(UpdateManagerImpl.isVersionNewer("2.0.4", "2.1.0"))
        assertTrue(UpdateManagerImpl.isVersionNewer("2.0.0", "2.0.1"))
    }

    @Test
    fun testIsVersionNewer_higherPatchVersionReturnsTrue() {
        assertTrue(UpdateManagerImpl.isVersionNewer("2.0.4", "2.0.5"))
        assertTrue(UpdateManagerImpl.isVersionNewer("2.0.4", "2.0.10"))
    }

    @Test
    fun testIsVersionNewer_equalOrLowerVersionReturnsFalse() {
        assertFalse(UpdateManagerImpl.isVersionNewer("2.0.4", "2.0.4"))
        assertFalse(UpdateManagerImpl.isVersionNewer("2.0.4", "2.0.3"))
        assertFalse(UpdateManagerImpl.isVersionNewer("2.1.0", "2.0.9"))
        assertFalse(UpdateManagerImpl.isVersionNewer("3.0.0", "2.9.9"))
    }

    @Test
    fun testIsVersionNewer_handlesTagPrefixAndPrereleaseSuffix() {
        assertTrue(UpdateManagerImpl.isVersionNewer("2.0.4", "v2.0.5"))
        assertTrue(UpdateManagerImpl.isVersionNewer("v2.0.4", "2.0.5"))
        assertTrue(UpdateManagerImpl.isVersionNewer("2.0.4", "v2.0.5-beta"))
        assertFalse(UpdateManagerImpl.isVersionNewer("2.0.4", "v2.0.4-beta"))
        assertFalse(UpdateManagerImpl.isVersionNewer("2.0.5", "v2.0.4-beta"))
    }

    @Test
    fun testIsVersionNewer_handlesDifferentSegmentCounts() {
        assertTrue(UpdateManagerImpl.isVersionNewer("2.0", "2.0.1"))
        assertTrue(UpdateManagerImpl.isVersionNewer("2.0.0", "2.0.0.1"))
        assertFalse(UpdateManagerImpl.isVersionNewer("2.0.1", "2.0"))
    }

    @Test
    fun testParseLatestRelease_validatesHttpsApkAssetAndVersion() {
        val json = """
            {
                "tag_name": "v2.1.0",
                "assets": [
                    {"name": "checksums.txt", "browser_download_url": "https://github.com/r/checksums.txt"},
                    {"name": "shelf-release.APK", "browser_download_url": "https://github.com/r/shelf.apk"}
                ]
            }
        """.trimIndent()
        val parsed = UpdateManagerImpl.parseLatestRelease(json, "2.0.5")
        assertNotNull(parsed)
        assertEquals("2.1.0", parsed?.version)
        assertEquals("https://github.com/r/shelf.apk", parsed?.downloadUrl)

        val cleartextJson = """
            {
                "tag_name": "v2.1.0",
                "assets": [
                    {"name": "shelf.apk", "browser_download_url": "http://insecure.example.com/shelf.apk"}
                ]
            }
        """.trimIndent()
        assertNull(UpdateManagerImpl.parseLatestRelease(cleartextJson, "2.0.5"))
        assertNull(UpdateManagerImpl.parseLatestRelease(json, "2.1.0"))
    }

    @Test
    fun testIsDownloadSuccessful_closesCursorAcrossAllPaths() {
        assertFalse(UpdateManagerImpl.isDownloadSuccessful(null))

        val emptyCursor = FakeStatusCursor(arrayOf(DownloadManager.COLUMN_STATUS), emptyList())
        assertFalse(UpdateManagerImpl.isDownloadSuccessful(emptyCursor.cursor))
        assertTrue(emptyCursor.wasClosed)

        val failedCursor = FakeStatusCursor(
            arrayOf(DownloadManager.COLUMN_STATUS),
            listOf(listOf(DownloadManager.STATUS_FAILED))
        )
        assertFalse(UpdateManagerImpl.isDownloadSuccessful(failedCursor.cursor))
        assertTrue(failedCursor.wasClosed)

        val successCursor = FakeStatusCursor(
            arrayOf(DownloadManager.COLUMN_STATUS),
            listOf(listOf(DownloadManager.STATUS_SUCCESSFUL))
        )
        assertTrue(UpdateManagerImpl.isDownloadSuccessful(successCursor.cursor))
        assertTrue(successCursor.wasClosed)
    }

    @Test
    fun testCheckForUpdates_closesHttpBodyOnFailureAndStoresValidRelease() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        var bodyClosedCount = 0
        var responseCode = 404
        var responseJson = "{}"

        val client = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val buffer = Buffer().writeUtf8(responseJson)
                val trackedBody = object : ResponseBody() {
                    override fun contentType() = "application/json".toMediaType()
                    override fun contentLength() = buffer.size
                    override fun source(): BufferedSource = buffer
                    override fun close() {
                        bodyClosedCount++
                        super.close()
                    }
                }
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(responseCode)
                    .message("Test")
                    .body(trackedBody)
                    .build()
            })
            .build()

        val settings = FakeSettingsRepository()
        val manager = UpdateManagerImpl(FakeContext(), settings, client, dispatcher)

        assertFalse(manager.checkForUpdates(force = true))
        assertEquals(1, bodyClosedCount)
        assertNull(settings.latestAvailableVersion)

        responseCode = 200
        responseJson = """
            {
                "tag_name": "v99.0.0",
                "assets": [
                    {"name": "shelf.apk", "browser_download_url": "https://example.com/shelf-99.apk"}
                ]
            }
        """.trimIndent()

        assertTrue(manager.checkForUpdates(force = true))
        assertEquals(2, bodyClosedCount)
        assertEquals("99.0.0", settings.latestAvailableVersion)
        assertEquals("https://example.com/shelf-99.apk", settings.latestReleaseUrl)

        assertTrue(manager.checkForUpdates(force = false))
        assertEquals(2, bodyClosedCount)

        settings.latestReleaseUrl = "http://insecure.example.com/shelf-99.apk"
        assertFalse(manager.checkForUpdates(force = false))

        settings.latestAvailableVersion = "0.0.0"
        settings.latestReleaseUrl = "https://example.com/old.apk"
        assertFalse(manager.checkForUpdates(force = false))
        assertNull(settings.latestAvailableVersion)
        assertNull(settings.latestReleaseUrl)
    }

    @Test
    fun testUpdateEdgeCases_cooldownCacheHttpsErrorCursorsAndCancellation() = runTest {
        assertTrue(UpdateManagerImpl.isVersionNewer("V1.0.0", "V1.0.1"))
        assertFalse(UpdateManagerImpl.isVersionNewer("v1.0.0", "invalid"))
        assertNull(UpdateManagerImpl.parseLatestRelease("", "1.0.0"))
        assertNull(UpdateManagerImpl.parseLatestRelease("not-json", "1.0.0"))
        assertNull(
            UpdateManagerImpl.parseLatestRelease(
                """{"tag_name":"v9.0.0","assets":[{"name":"notes.txt","browser_download_url":"https://a.dev/notes.txt"}]}""",
                "1.0.0"
            )
        )
        assertNull(
            UpdateManagerImpl.parseLatestRelease(
                """{"tag_name":"   ","assets":[{"name":"app.apk","browser_download_url":"https://a.dev/app.apk"}]}""",
                "1.0.0"
            )
        )

        assertFalse(UpdateManagerImpl.isDownloadSuccessful(null))
        val emptyCursor = FakeStatusCursor(arrayOf(DownloadManager.COLUMN_STATUS), emptyList())
        assertFalse(UpdateManagerImpl.isDownloadSuccessful(emptyCursor.cursor))
        assertTrue(emptyCursor.wasClosed)
        val failedCursor = FakeStatusCursor(arrayOf(DownloadManager.COLUMN_STATUS), listOf(listOf(DownloadManager.STATUS_FAILED)))
        assertFalse(UpdateManagerImpl.isDownloadSuccessful(failedCursor.cursor))
        assertTrue(failedCursor.wasClosed)
        val noColCursor = FakeStatusCursor(arrayOf("other"), listOf(listOf(1)))
        assertFalse(UpdateManagerImpl.isDownloadSuccessful(noColCursor.cursor))
        assertTrue(noColCursor.wasClosed)

        val settings = FakeSettingsRepository()
        settings.lastUpdateCheckTime = System.currentTimeMillis()
        settings.latestAvailableVersion = "99.0.0"
        settings.latestReleaseUrl = "https://example.com/cached.apk"

        var networkCalls = 0
        val dispatcher = StandardTestDispatcher(testScheduler)
        val cachedClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor { throw AssertionError("Should not hit network within cooldown") })
            .build()
        val cachedManager = UpdateManagerImpl(FakeContext(), settings, cachedClient, dispatcher)
        assertTrue(cachedManager.checkForUpdates(force = false))

        val errorClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                networkCalls++
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(500)
                    .message("Err")
                    .body(ByteArray(0).toResponseBody("text/plain".toMediaType()))
                    .build()
            })
            .build()
        val errorManager = UpdateManagerImpl(FakeContext(), settings, errorClient, dispatcher)
        assertFalse(errorManager.checkForUpdates(force = true))
        assertEquals(1, networkCalls)

        val cancelClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor { throw kotlinx.coroutines.CancellationException("cancel") })
            .build()
        val cancelManager = UpdateManagerImpl(FakeContext(), settings, cancelClient, dispatcher)
        var cancelledCaught = false
        try {
            cancelManager.checkForUpdates(force = true)
        } catch (_: kotlinx.coroutines.CancellationException) {
            cancelledCaught = true
        }
        assertTrue(cancelledCaught)
    }

    @Test
    fun testIsVersionNewer_prereleaseVsStableAndBuildMetadata() {
        assertTrue(UpdateManagerImpl.isVersionNewer("2.0.5-beta", "2.0.5"))
        assertTrue(UpdateManagerImpl.isVersionNewer("2.0.5-beta", "v2.0.5"))
        assertFalse(UpdateManagerImpl.isVersionNewer("2.0.5", "2.0.5-beta"))
        assertTrue(UpdateManagerImpl.isVersionNewer("2.0.5-alpha", "2.0.5-beta"))
        assertTrue(UpdateManagerImpl.isVersionNewer("2.0.5+32", "2.0.6+33"))
        assertFalse(UpdateManagerImpl.isVersionNewer("2.0.5+32", "2.0.5+33"))
    }
}
