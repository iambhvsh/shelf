package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.features.backup.BookmarkParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ParserTest {

    private val parser = BookmarkParser()

    @Test
    fun testNestedFolderPreservation() {
        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <TITLE>Bookmarks</TITLE>
            <H1>Bookmarks</H1>
            <DL><p>
                <DT><H3>Bookmarks bar</H3>
                <DL><p>
                    <DT><H3>Dev</H3>
                    <DL><p>
                        <DT><A HREF="https://github.com">GitHub</A>
                    </DL><p>
                </DL><p>
            </DL><p>
        """.trimIndent()

        val (bookmarks, collections) = parser.parse(html)
        assertEquals(1, bookmarks.size)
        assertEquals("https://github.com", bookmarks[0].url)
        assertEquals("GitHub", bookmarks[0].title)
        assertEquals(listOf("Bookmarks bar/Dev"), bookmarks[0].collections)
        assertEquals(listOf("Bookmarks bar/Dev"), collections)
    }

    @Test
    fun testManyToManyCollectionPreservation() {
        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
                <DT><H3>Dev</H3>
                <DL><p>
                    <DT><A HREF="https://github.com">GitHub</A>
                </DL><p>
                <DT><H3>Work</H3>
                <DL><p>
                    <DT><A HREF="https://github.com">GitHub Work</A>
                </DL><p>
            </DL><p>
        """.trimIndent()

        val (bookmarks, collections) = parser.parse(html)
        assertEquals(1, bookmarks.size)
        val bm = bookmarks[0]
        assertEquals("https://github.com", bm.url)
        assertTrue(bm.collections.contains("Dev"))
        assertTrue(bm.collections.contains("Work"))
        assertEquals(2, bm.collections.size)
        assertTrue(collections.contains("Dev"))
        assertTrue(collections.contains("Work"))
    }

    @Test
    fun testEmptyFoldersNotCreated() {
        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
                <DT><H3>EmptyFolder1</H3>
                <DL><p>
                    <DT><H3>SubEmptyFolder</H3>
                    <DL><p></DL><p>
                </DL><p>
                <DT><H3>Dev</H3>
                <DL><p>
                    <DT><A HREF="https://kotlinlang.org">Kotlin</A>
                </DL><p>
            </DL><p>
        """.trimIndent()

        val (bookmarks, collections) = parser.parse(html)
        assertEquals(1, bookmarks.size)
        assertEquals(listOf("Dev"), collections)
        assertFalse(collections.contains("EmptyFolder1"))
        assertFalse(collections.contains("EmptyFolder1/SubEmptyFolder"))
    }

    @Test
    fun testHtmlEntitiesUnescaped() {
        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
                <DT><H3>Q&amp;A &lt;Folder&gt;</H3>
                <DL><p>
                    <DT><A HREF="https://example.com/test?a=1&amp;b=2">Tom &amp; Jerry &#39;Special&#39;</A>
                </DL><p>
            </DL><p>
        """.trimIndent()

        val (bookmarks, collections) = parser.parse(html)
        assertEquals(1, bookmarks.size)
        assertEquals("https://example.com/test?a=1&b=2", bookmarks[0].url)
        assertEquals("Tom & Jerry 'Special'", bookmarks[0].title)
        assertEquals(listOf("Q&A <Folder>"), bookmarks[0].collections)
        assertEquals(listOf("Q&A <Folder>"), collections)
    }

    @Test
    fun testSingleQuotedHrefAndBlankFolderStack() {
        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
                <DT><H3>Dev</H3>
                <DL><p>
                    <DT><H3></H3>
                    <DL><p>
                        <DT><A HREF='https://example.com/single?x=1&amp;y=2'>Single Quote Link</A>
                    </DL><p>
                    <DT><A HREF="https://example.com/next">Next Link</A>
                </DL><p>
            </DL><p>
        """.trimIndent()

        val (bookmarks, collections) = parser.parse(html)
        assertEquals(2, bookmarks.size)
        assertEquals("https://example.com/single?x=1&y=2", bookmarks[0].url)
        assertEquals(listOf("Dev"), bookmarks[0].collections)
        assertEquals("https://example.com/next", bookmarks[1].url)
        assertEquals(listOf("Dev"), bookmarks[1].collections)
        assertEquals(listOf("Dev"), collections)
    }

    @Test
    fun testDelimiterInUrlAndUnassignedBookmarks() {
        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
                <DT><A HREF="https://example.com/item|||special">Pipes In Url</A>
            </DL><p>
        """.trimIndent()

        val (bookmarks, collections) = parser.parse(html)
        assertEquals(1, bookmarks.size)
        assertEquals("https://example.com/item|||special", bookmarks[0].url)
        assertEquals("Pipes In Url", bookmarks[0].title)
        assertTrue(bookmarks[0].collections.isEmpty())
        assertTrue(collections.isEmpty())
    }

    @Test
    fun testMalformedHtml_returnsEmptyListsGracefully() {
        val (bookmarks, collections) = parser.parse("Not a bookmark file at all")
        assertTrue(bookmarks.isEmpty())
        assertTrue(collections.isEmpty())
    }

    @Test
    fun testUnsafeSchemes_areFilteredAndDoNotCreateEmptyFolders() {
        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
                <DT><H3>UnsafeOnly</H3>
                <DL><p>
                    <DT><A HREF="javascript:alert(1)">Bookmarklet</A>
                    <DT><A HREF="file:///etc/passwd">Local</A>
                    <DT><A HREF="chrome://settings">Settings</A>
                    <DT><A HREF="data:text/html,hi">Data</A>
                    <DT><A HREF="about:blank">Blank</A>
                </DL><p>
                <DT><H3>Safe</H3>
                <DL><p>
                    <DT><A HREF="HTTP://example.com/ok">Safe Link</A>
                </DL><p>
            </DL><p>
        """.trimIndent()

        val (bookmarks, collections) = parser.parse(html)
        assertEquals(1, bookmarks.size)
        assertEquals("HTTP://example.com/ok", bookmarks[0].url)
        assertEquals(listOf("Safe"), collections)
    }

    @Test
    fun testDeepNestedFoldersAndFallbackUrlTitle() {
        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <dl>
                <dt><h3 add_date="1700">Engineering</h3>
                <dl>
                    <dt><h3>Kotlin</h3>
                    <dl>
                        <dt><h3>Coroutines</h3>
                        <dl>
                            <dt><a href="https://kotlinlang.org/coroutines" add_date="1701">   </a>
                        </dl>
                    </dl>
                </dl>
            </dl>
        """.trimIndent()

        val (bookmarks, collections) = parser.parse(html)
        assertEquals(listOf("Engineering/Kotlin/Coroutines"), collections)
        assertEquals(1, bookmarks.size)
        assertEquals("https://kotlinlang.org/coroutines", bookmarks[0].url)
        assertEquals(listOf("Engineering/Kotlin/Coroutines"), bookmarks[0].collections)
    }

    @Test
    fun testEntityDoubleUnescapeNumericEntitiesAndNormalizedUrlMerge() {
        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
                <DT><H3>Docs</H3>
                <DL><p>
                    <DT><A HREF="https://a.dev">It&#8217;s &amp;quot;Quoted&amp;quot;&nbsp;Here</A>
                </DL><p>
                <DT><H3>Archive</H3>
                <DL><p>
                    <DT><A HREF="HTTPS://a.dev/">Second Copy</A>
                </DL><p>
            </DL><p>
        """.trimIndent()

        val (bookmarks, collections) = parser.parse(html)
        assertEquals(1, bookmarks.size)
        assertEquals("It’s &quot;Quoted&quot; Here", bookmarks[0].title)
        assertEquals(listOf("Docs", "Archive"), bookmarks[0].collections)
        assertEquals(listOf("Docs", "Archive"), collections)
    }
}
