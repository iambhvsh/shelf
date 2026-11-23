package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.features.backup.BookmarkParser
import `in`.iambhvsh.shelf.features.backup.escapeHtml
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlEscapeAndParserTest {

    private val parser = BookmarkParser()

    @Test
    fun testEscapeHtml_escapesAllSpecialXmlCharacters() {
        val input = "Search <Kotlin> & \"Compose\" > Swift"
        val expected = "Search &lt;Kotlin&gt; &amp; &quot;Compose&quot; &gt; Swift"
        assertEquals(expected, escapeHtml(input))
    }

    @Test
    fun testExportHtmlTemplateRoundTrip() {
        val rawTitle = "Docs & Tutorials <Official>"
        val rawFolder = "Dev & Tools"
        val rawUrl = "https://example.com/tutorials/compose"

        val generatedHtml = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <TITLE>Shelf</TITLE>
            <H1>Shelf</H1>
            <DL><p>
                <DT><H3>${escapeHtml(rawFolder)}</H3>
                <DL><p>
                    <DT><A HREF="$rawUrl">${escapeHtml(rawTitle)}</A>
                </DL><p>
            </DL><p>
        """.trimIndent()

        val (bookmarks, collections) = parser.parse(generatedHtml)
        assertEquals(1, collections.size)
        assertEquals(rawFolder, collections[0])
        assertEquals(1, bookmarks.size)
        assertEquals(rawUrl, bookmarks[0].url)
        assertEquals(rawTitle, bookmarks[0].title)
        assertTrue(bookmarks[0].collections.contains(rawFolder))
    }
}
