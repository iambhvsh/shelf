package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.features.fetch.ContentTypeDetector
import `in`.iambhvsh.shelf.features.fetch.LinkMetadataParser
import `in`.iambhvsh.shelf.features.fetch.LinkPreviewData
import `in`.iambhvsh.shelf.features.fetch.LinkPreviewService
import `in`.iambhvsh.shelf.features.fetch.LinkPreviewType
import `in`.iambhvsh.shelf.features.fetch.MetadataExtractor
import `in`.iambhvsh.shelf.features.fetch.NetworkFetcher
import `in`.iambhvsh.shelf.features.fetch.refreshStaleImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkTest {

    private fun clientWithHandler(handler: (Interceptor.Chain) -> Response): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(Interceptor(handler))
            .build()
    }

    private fun htmlResponse(
        chain: Interceptor.Chain,
        code: Int = 200,
        contentType: String = "text/html; charset=utf-8",
        body: String = "<html><head><title>Test</title></head><body></body></html>"
    ): Response {
        val builder = Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("OK")
            .body(body.toResponseBody(contentType.toMediaType()))
        if (contentType.isNotEmpty()) {
            builder.header("Content-Type", contentType)
        }
        return builder.build()
    }

    @Test
    fun testFetchDocument_sendsUserAgentAndAcceptLanguageAndParsesHtml() {
        var seenAgent: String? = null
        var seenLang: String? = null
        val client = clientWithHandler { chain ->
            seenAgent = chain.request().header("User-Agent")
            seenLang = chain.request().header("Accept-Language")
            htmlResponse(
                chain,
                body = """
                    <html>
                      <head>
                        <meta property="og:title" content="Kotlin Docs"/>
                        <meta property="og:description" content="Official docs"/>
                        <meta property="og:image" content="/banner.png"/>
                      </head>
                    </html>
                """.trimIndent()
            )
        }

        val fetcher = NetworkFetcher(client)
        val doc = fetcher.fetchDocument("https://kotlinlang.org/docs", "CustomAgent/1.0")

        assertNotNull(doc)
        assertEquals("CustomAgent/1.0", seenAgent)
        assertEquals("en-US,en;q=0.9", seenLang)
        assertEquals("Kotlin Docs", doc!!.select("meta[property=og:title]").attr("content"))
    }

    @Test
    fun testFetchDocument_returnsNullForHttpErrorsNonHtmlAndExceptions() {
        val client404 = clientWithHandler { chain -> htmlResponse(chain, code = 404) }
        assertNull(NetworkFetcher(client404).fetchDocument("https://example.com", "UA"))

        val clientJson = clientWithHandler { chain ->
            htmlResponse(chain, code = 200, contentType = "application/json", body = "{}")
        }
        assertNull(NetworkFetcher(clientJson).fetchDocument("https://example.com/api", "UA"))

        val clientFail = clientWithHandler { throw IOException("network down") }
        assertNull(NetworkFetcher(clientFail).fetchDocument("https://example.com", "UA"))

        assertNull(NetworkFetcher(client404).fetchDocument("not a url", "UA"))
    }

    @Test
    fun testIsImageUrlReachable_handlesHeadRangeFallbackErrorsAndNetworkFailure() {
        val methods = mutableListOf<String>()
        val client200 = clientWithHandler { chain ->
            methods.add(chain.request().method)
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ByteArray(0).toResponseBody("image/png".toMediaType()))
                .build()
        }
        assertTrue(NetworkFetcher(client200).isImageUrlReachable("https://cdn.example.com/a.png"))
        assertEquals(listOf("HEAD"), methods)

        assertFalse(NetworkFetcher(client200).isImageUrlReachable("   "))

        val fallbackCalls = mutableListOf<Pair<String, String?>>()
        val client405 = clientWithHandler { chain ->
            fallbackCalls.add(chain.request().method to chain.request().header("Range"))
            val code = if (chain.request().method == "HEAD") 405 else 206
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("Status")
                .body(ByteArray(0).toResponseBody("image/png".toMediaType()))
                .build()
        }
        assertTrue(NetworkFetcher(client405).isImageUrlReachable("https://cdn.example.com/b.png"))
        assertEquals(listOf("HEAD" to null, "GET" to "bytes=0-0"), fallbackCalls)

        val client404 = clientWithHandler { chain ->
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(404)
                .message("Not Found")
                .body(ByteArray(0).toResponseBody("text/plain".toMediaType()))
                .build()
        }
        assertFalse(NetworkFetcher(client404).isImageUrlReachable("https://cdn.example.com/missing.png"))

        val clientOffline = clientWithHandler { throw IOException("timeout") }
        assertTrue(NetworkFetcher(clientOffline).isImageUrlReachable("https://cdn.example.com/c.png"))
    }

    @Test
    fun testLinkPreviewService_buildsTrimmedDataAndDetectsType() {
        val client = clientWithHandler { chain ->
            htmlResponse(
                chain,
                body = """
                    <html>
                      <head>
                        <meta property="og:title" content="  Compose Guide  "/>
                        <meta property="og:description" content="  UI toolkit  "/>
                        <meta property="og:image" content="/thumb.jpg"/>
                      </head>
                    </html>
                """.trimIndent()
            )
        }
        val service = LinkPreviewService(
            networkFetcher = NetworkFetcher(client),
            metadataExtractor = MetadataExtractor(),
            contentTypeDetector = ContentTypeDetector()
        )

        val data = service.getLinkPreviewData("  https://youtube.com/watch?v=123  ", "Agent")
        assertNotNull(data)
        assertEquals("Compose Guide", data!!.title)
        assertEquals("UI toolkit", data.description)
        assertEquals("https://youtube.com/thumb.jpg", data.imageUrl)
        assertEquals("https://youtube.com/watch?v=123", data.url)
        assertEquals(LinkPreviewType.VIDEO, data.linkContentType)

        val failClient = clientWithHandler { chain -> htmlResponse(chain, code = 500) }
        val failingService = LinkPreviewService(networkFetcher = NetworkFetcher(failClient))
        assertNull(failingService.getLinkPreviewData("https://example.com", "Agent"))
    }

    @Test
    fun testLinkMetadataParser_rotatesAgentsPreservesPartialAndPropagatesCancellation() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val seenAgents = mutableListOf<String>()

        val rotatingClient = clientWithHandler { chain ->
            val ua = chain.request().header("User-Agent")!!
            seenAgents.add(ua)
            if (ua.startsWith("WhatsApp")) {
                htmlResponse(chain, body = "<html><head></head><body></body></html>")
            } else {
                htmlResponse(
                    chain,
                    body = """
                        <html>
                          <head>
                            <title>Chrome Fallback Title</title>
                            <meta name="description" content="Fallback description"/>
                            <meta property="og:image" content="https://example.com/fallback.png"/>
                          </head>
                        </html>
                    """.trimIndent()
                )
            }
        }
        val parser = LinkMetadataParser(
            service = LinkPreviewService(networkFetcher = NetworkFetcher(rotatingClient)),
            ioDispatcher = dispatcher
        )
        val rotated = parser.parse("https://example.com/post")
        assertEquals(2, seenAgents.size)
        assertTrue(seenAgents[0].startsWith("WhatsApp"))
        assertTrue(seenAgents[1].contains("Mozilla"))
        assertEquals("Chrome Fallback Title", rotated?.title)
        assertEquals("https://example.com/fallback.png", rotated?.imageUrl)

        seenAgents.clear()
        val partialKeepClient = clientWithHandler { chain ->
            val ua = chain.request().header("User-Agent")!!
            seenAgents.add(ua)
            if (ua.startsWith("WhatsApp")) {
                htmlResponse(
                    chain,
                    body = """
                        <html><head><meta property="og:image" content="https://example.com/only-image.png"/></head></html>
                    """.trimIndent()
                )
            } else {
                htmlResponse(chain, code = 503)
            }
        }
        val partialParser = LinkMetadataParser(
            service = LinkPreviewService(networkFetcher = NetworkFetcher(partialKeepClient)),
            ioDispatcher = dispatcher
        )
        val partialResult = partialParser.parse("https://example.com/partial")
        assertNotNull(partialResult)
        assertEquals("https://example.com/only-image.png", partialResult?.imageUrl)

        val cancellingClient = clientWithHandler { throw CancellationException("cancelled") }
        val cancelParser = LinkMetadataParser(
            service = LinkPreviewService(networkFetcher = NetworkFetcher(cancellingClient)),
            ioDispatcher = dispatcher
        )
        var cancelledCaught = false
        try {
            cancelParser.parse("https://example.com/cancel")
        } catch (_: CancellationException) {
            cancelledCaught = true
        }
        assertTrue(cancelledCaught)
    }

    @Test
    fun testRefreshStaleImage_deduplicatesAndFiltersBlankOrUnchangedUrls() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val parser = object : LinkMetadataParser(ioDispatcher = dispatcher) {
            var calls = 0
            override suspend fun parse(url: String): LinkPreviewData {
                calls++
                return LinkPreviewData(imageUrl = "https://cdn.example.com/new.png")
            }
        }
        val attempted = mutableSetOf<String>()
        val updates = mutableListOf<Pair<Long, String>>()

        refreshStaleImage(1L, "https://a.com", "https://old.png", parser, attempted) { id, newUrl ->
            updates.add(id to newUrl)
        }
        refreshStaleImage(1L, "https://a.com", "https://old.png", parser, attempted) { id, newUrl ->
            updates.add(id to newUrl)
        }
        assertEquals(1, parser.calls)
        assertEquals(listOf(1L to "https://cdn.example.com/new.png"), updates)

        val sameParser = object : LinkMetadataParser(ioDispatcher = dispatcher) {
            override suspend fun parse(url: String): LinkPreviewData =
                LinkPreviewData(imageUrl = "https://same.png")
        }
        refreshStaleImage(2L, "https://b.com", "https://same.png", sameParser, attempted) { id, newUrl ->
            updates.add(id to newUrl)
        }
        refreshStaleImage(3L, "https://c.com", null, object : LinkMetadataParser(ioDispatcher = dispatcher) {
            override suspend fun parse(url: String): LinkPreviewData = LinkPreviewData(imageUrl = "   ")
        }, attempted) { id, newUrl ->
            updates.add(id to newUrl)
        }
        assertEquals(1, updates.size)
    }
}
