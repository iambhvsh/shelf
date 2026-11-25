package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.features.fetch.ContentTypeDetector
import `in`.iambhvsh.shelf.features.fetch.LinkPreviewData
import `in`.iambhvsh.shelf.features.fetch.LinkPreviewType
import org.junit.Assert.assertEquals
import org.junit.Test

class DetectorTest {

    private val detector = ContentTypeDetector()

    @Test
    fun testDetectContentType_matchesAllVideoPatterns() {
        val videoUrls = listOf(
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ",
            "https://youtube.com/shorts/abc123",
            "https://vimeo.com/123456789",
            "https://instagram.com/reel/CxYz123/",
            "https://instagram.com/p/CxYz123/video",
            "https://facebook.com/watch/?v=123",
            "https://facebook.com/user/videos/123",
            "https://fb.watch/abc123/",
            "https://twitter.com/jack/status/12345/video/1",
            "https://x.com/user/status/999/video/1",
            "https://www.twitch.tv/clip/CoolClipName",
            "https://www.twitch.tv/videos/123456",
            "https://www.dailymotion.com/video/x8abcde",
            "https://v.redd.it/abcdef",
            "https://www.reddit.com/r/kotlin/comments/abc123/video/title",
            "https://www.linkedin.com/posts/user_activity-12345",
            "https://www.linkedin.com/feed/update/urn:li:video:123",
            "https://cdn.example.com/demo.mp4",
            "https://cdn.example.com/demo.MKV?token=abc",
            "https://cdn.example.com/demo.webm",
            "https://example.com/embed/123",
            "https://example.com/player/stream1",
            "https://example.com/stream/live"
        )

        videoUrls.forEach { url ->
            assertEquals("Expected VIDEO for $url", LinkPreviewType.VIDEO, detector.detectContentType(url))
        }
    }

    @Test
    fun testDetectContentType_matchesAllProfilePatternsAndHandlesEncodedUrls() {
        val profileUrls = listOf(
            "https://twitter.com/kotlin",
            "https://x.com/androiddev",
            "https://instagram.com/natgeo",
            "https://facebook.com/meta",
            "https://fb.com/meta",
            "https://www.linkedin.com/in/johndoe/",
            "https://www.linkedin.com/profile/view",
            "https://www.pinterest.com/design/",
            "https://www.reddit.com/user/spez",
            "https://www.reddit.com/u/kotlin_dev",
            "https://github.com/octocat",
            "https://github.com/octocat/",
            "https://medium.com/@author",
            "https://threads.net/@creator",
            "https://twitch.tv/streamer",
            "https://wa.me/15551234567",
            "https://chat.whatsapp.com/InviteCode123",
            "https://example.com/users/42",
            "https://example.com/members/alice",
            "https://mastodon.social/@user",
            "https://mastodon.social/%40encodeduser",
            "https://tilde.club/~alice",
            "https://tilde.club/~alice/",
            "https://example.com/u/alice"
        )

        profileUrls.forEach { url ->
            assertEquals("Expected PROFILE for $url", LinkPreviewType.PROFILE, detector.detectContentType(url))
        }
    }

    @Test
    fun testDetectContentType_respectsNegativeLookaheadsAndFallsBackToWebpage() {
        val webpageUrls = listOf(
            "https://twitter.com/kotlin/status/12345",
            "https://x.com/kotlin/lists/android",
            "https://instagram.com/p/CxYz123/",
            "https://facebook.com/posts/123",
            "https://fb.com/events/456",
            "https://pinterest.com/pin/12345/",
            "https://github.com/octocat/Hello-World",
            "https://twitch.tv/streamer/about",
            "https://example.com/upload/document.pdf",
            "https://kotlinlang.org/docs/home.html",
            "https://example.com/watch",
            "   "
        )

        webpageUrls.forEach { url ->
            assertEquals("Expected WEBPAGE for $url", LinkPreviewType.WEBPAGE, detector.detectContentType(url))
        }
    }

    @Test
    fun testLinkPreviewDataDefaults() {
        val data = LinkPreviewData()
        assertEquals(null, data.title)
        assertEquals(null, data.description)
        assertEquals(null, data.imageUrl)
        assertEquals(null, data.url)
        assertEquals(LinkPreviewType.WEBPAGE, data.linkContentType)
    }
}
