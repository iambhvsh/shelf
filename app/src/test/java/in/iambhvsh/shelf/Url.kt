package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.features.fetch.ContentTypeDetector
import `in`.iambhvsh.shelf.features.fetch.LinkPreviewType
import `in`.iambhvsh.shelf.features.fetch.UrlUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class UrlAndDetectorTest {

    private val detector = ContentTypeDetector()

    @Test
    fun testMakeAbsoluteUrl_handlesVariousFormats() {
        assertEquals("https://example.com/pic.jpg", UrlUtils.makeAbsoluteUrl("https://example.com/pic.jpg", "https://example.com"))
        assertEquals("https://cdn.example.com/pic.jpg", UrlUtils.makeAbsoluteUrl("//cdn.example.com/pic.jpg", "https://example.com"))
        assertEquals("https://example.com/static/style.css", UrlUtils.makeAbsoluteUrl("/static/style.css", "https://example.com/blog/2026/my-post"))
        assertEquals("https://example.com/blog/2026/header.png", UrlUtils.makeAbsoluteUrl("header.png", "https://example.com/blog/2026/my-post"))
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
}
