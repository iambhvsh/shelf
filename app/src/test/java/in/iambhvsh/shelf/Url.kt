package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.features.fetch.ContentTypeDetector
import `in`.iambhvsh.shelf.features.fetch.LinkPreviewType
import `in`.iambhvsh.shelf.features.fetch.UrlUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlAndDetectorTest {

    private val detector = ContentTypeDetector()

    @Test
    fun testMakeAbsoluteUrl_handlesVariousFormats() {
        assertEquals("https://example.com/pic.jpg", UrlUtils.makeAbsoluteUrl("https://example.com/pic.jpg", "https://example.com"))
        assertEquals("https://cdn.example.com/pic.jpg", UrlUtils.makeAbsoluteUrl("//cdn.example.com/pic.jpg", "https://example.com"))
        assertEquals("https://example.com/static/style.css", UrlUtils.makeAbsoluteUrl("/static/style.css", "https://example.com/blog/2026/my-post"))
        assertEquals("https://example.com/blog/2026/header.png", UrlUtils.makeAbsoluteUrl("header.png", "https://example.com/blog/2026/my-post"))
        assertEquals("https://example.com/blog/2026/http-equiv.png", UrlUtils.makeAbsoluteUrl("http-equiv.png", "https://example.com/blog/2026/my-post"))
        assertEquals("HTTPS://example.com/pic.jpg", UrlUtils.makeAbsoluteUrl("HTTPS://example.com/pic.jpg", "https://example.com"))
    }

    @Test
    fun testDecodeUrl_decodesCorrectly() {
        val encoded = "https%3A%2F%2Fshelf.app%2Fsearch%3Fq%3Dandroid%20compose"
        assertEquals("https://shelf.app/search?q=android compose", UrlUtils.decodeUrl(encoded))
    }

    @Test
    fun testDetectContentType_identifiesVideos() {
        assertEquals(LinkPreviewType.VIDEO, detector.detectContentType("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertEquals(LinkPreviewType.VIDEO, detector.detectContentType("https://youtu.be/dQw4w9WgXcQ"))
        assertEquals(LinkPreviewType.VIDEO, detector.detectContentType("https://youtube.com/shorts/abcd1234"))
        assertEquals(LinkPreviewType.VIDEO, detector.detectContentType("https://vimeo.com/123456789"))
        assertEquals(LinkPreviewType.VIDEO, detector.detectContentType("https://www.instagram.com/reel/C2xyz123/"))
        assertEquals(LinkPreviewType.VIDEO, detector.detectContentType("https://example.com/media/clip.mp4"))
    }

    @Test
    fun testDetectContentType_identifiesProfiles() {
        assertEquals(LinkPreviewType.PROFILE, detector.detectContentType("https://github.com/iambhvsh"))
        assertEquals(LinkPreviewType.PROFILE, detector.detectContentType("https://twitter.com/AndroidDev"))
        assertEquals(LinkPreviewType.PROFILE, detector.detectContentType("https://medium.com/@author"))
        assertEquals(LinkPreviewType.PROFILE, detector.detectContentType("https://threads.net/@user"))
        assertEquals(LinkPreviewType.PROFILE, detector.detectContentType("https://reddit.com/u/spez"))
    }

    @Test
    fun testDetectContentType_defaultsToWebpage() {
        assertEquals(LinkPreviewType.WEBPAGE, detector.detectContentType("https://kotlinlang.org/docs/home.html"))
        assertEquals(LinkPreviewType.WEBPAGE, detector.detectContentType("https://developer.android.com/jetpack/compose"))
    }

    @Test
    fun testDetectContentType_doesNotMisclassifySubstringDomains() {
        assertEquals(LinkPreviewType.WEBPAGE, detector.detectContentType("https://dropbox.com/s/sample123/document.pdf"))
        assertEquals(LinkPreviewType.WEBPAGE, detector.detectContentType("https://www.dropbox.com/sh/abc123xyz"))
        assertEquals(LinkPreviewType.WEBPAGE, detector.detectContentType("https://box.com/shared/static/file.zip"))
        assertEquals(LinkPreviewType.WEBPAGE, detector.detectContentType("https://wix.com/website/template"))
        assertEquals(LinkPreviewType.WEBPAGE, detector.detectContentType("https://myifb.com/post/123"))
    }

    @Test
    fun testDetectContentType_identifiesXAndTwitterCorrectly() {
        assertEquals(LinkPreviewType.PROFILE, detector.detectContentType("https://x.com/AndroidDev"))
        assertEquals(LinkPreviewType.PROFILE, detector.detectContentType("https://www.x.com/AndroidDev"))
        assertEquals(LinkPreviewType.PROFILE, detector.detectContentType("https://twitter.com/iambhvsh"))
    }

    @Test
    fun testExtractUrl_handlesPreamblesBareDomainsCaseAndBalancedParens() {
        assertEquals("https://youtu.be/dQw4w9WgXcQ", UrlUtils.extractUrl("Watch this video: https://youtu.be/dQw4w9WgXcQ"))
        assertEquals("https://x.com/AndroidDev/status/123", UrlUtils.extractUrl("Check out this post https://x.com/AndroidDev/status/123 from Twitter"))
        assertEquals("https://example.com", UrlUtils.extractUrl("example.com"))
        assertEquals("https://example.com/page", UrlUtils.extractUrl("https://example.com/page"))
        assertEquals("https://shelf.app/docs", UrlUtils.extractUrl("Check out https://shelf.app/docs."))
        assertEquals("http://localhost:3000/app", UrlUtils.extractUrl("http://localhost:3000/app"))
        assertEquals("https://example.com/wiki", UrlUtils.extractUrl("See (https://example.com/wiki)"))
        assertEquals(
            "https://en.wikipedia.org/wiki/Kotlin_(programming_language)",
            UrlUtils.extractUrl("Read https://en.wikipedia.org/wiki/Kotlin_(programming_language).")
        )
        assertEquals("https://example.com/test", UrlUtils.extractUrl("HTTPS://example.com/test"))
    }

    @Test
    fun testNormalizeAndSchemeGuards() {
        assertEquals("https://example.com/a", UrlUtils.normalizeUrlKey("HTTPS://example.com/a/"))
        assertEquals("http://example.com", UrlUtils.ensureHttpScheme("  HTTP://example.com "))
        assertEquals("https://example.com", UrlUtils.ensureHttpScheme("example.com"))
        assertTrue(isHttpOrHttpsUrl("https://shelf.app"))
        assertTrue(isHttpOrHttpsUrl("HTTP://shelf.app"))
        assertFalse(isHttpOrHttpsUrl("javascript:alert(1)"))
        assertFalse(isHttpOrHttpsUrl("file:///etc/hosts"))
    }

    @Test
    fun testUrlEdgeCases_malformedEscapesParensPunctuationAndFallbacks() {
        assertEquals("%ZZ-invalid", UrlUtils.decodeUrl("%ZZ-invalid"))
        assertEquals("a+b@c", UrlUtils.decodeUrl("a%2Bb%40c"))
        assertEquals("", UrlUtils.ensureHttpScheme("   "))
        assertEquals("https://example.com/a?b=1&c=2", UrlUtils.normalizeUrlKey("HTTPS://example.com/a?b=1&c=2/".replace("2/", "2/")))
        assertEquals("https://example.com/search?q=1", UrlUtils.normalizeUrlKey("https://example.com/search?q=1"))
        assertEquals("", UrlUtils.extractUrl("   "))
        assertEquals(
            "https://en.wikipedia.org/wiki/Kotlin_(programming_language)",
            UrlUtils.extractUrl("See [docs](https://en.wikipedia.org/wiki/Kotlin_(programming_language)).")
        )
        assertEquals("https://a.dev/1", UrlUtils.extractUrl("Check https://a.dev/1, then https://b.dev/2"))
        assertEquals("relative.png", UrlUtils.makeAbsoluteUrl("relative.png", "not valid base"))
    }
}
