package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.data.BackupBookmark
import `in`.iambhvsh.shelf.data.BackupCollection
import `in`.iambhvsh.shelf.data.BackupData
import `in`.iambhvsh.shelf.data.BackupTag
import `in`.iambhvsh.shelf.features.backup.extractTrailingBookmarks
import `in`.iambhvsh.shelf.features.backup.sanitizeJson
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class JsonTest {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun testSchemaVersionInExportedJson() {
        val data = BackupData(
            bookmarks = listOf(
                BackupBookmark(
                    url = "https://shelf.app",
                    title = "Shelf",
                    description = "Bookmark manager",
                    imageUrl = "https://shelf.app/icon.png",
                    isPinned = true,
                    note = "Personal note",
                    reminderTime = 1700000000L,
                    tags = listOf("Android", "Productivity")
                )
            ),
            collections = listOf(
                BackupCollection(name = "Dev", bookmarkUrls = listOf("https://shelf.app"))
            ),
            tags = listOf(BackupTag("Android"), BackupTag("Productivity"))
        )

        val jsonString = json.encodeToString(data)
        assertTrue(jsonString.contains("\"schemaVersion\": 3"))
        assertTrue(jsonString.contains("\"version\": 3"))

        val decoded = json.decodeFromString<BackupData>(jsonString)
        assertEquals(3, decoded.schemaVersion)
        assertEquals(3, decoded.version)
        assertEquals(1, decoded.bookmarks.size)
        val bm = decoded.bookmarks[0]
        assertEquals("https://shelf.app", bm.url)
        assertEquals("Shelf", bm.title)
        assertEquals("Personal note", bm.note)
        assertEquals(1700000000L, bm.reminderTime)
        assertEquals(true, bm.isPinned)
        assertEquals("https://shelf.app/icon.png", bm.imageUrl)
        assertEquals(listOf("Android", "Productivity"), bm.tags)
    }

    @Test
    fun testBackwardCompatibilityWithV2Json() {
        val legacyJson = """
            {
                "version": 2,
                "exportedAt": 1699999999,
                "bookmarks": [
                    {
                        "url": "https://example.com",
                        "title": "Example",
                        "tags": ["Tools"]
                    }
                ],
                "collections": [
                    {
                        "name": "General",
                        "bookmarkUrls": ["https://example.com"]
                    }
                ],
                "tags": [
                    { "name": "Tools" }
                ]
            }
        """.trimIndent()

        val decoded = json.decodeFromString<BackupData>(legacyJson)
        assertEquals(3, decoded.schemaVersion)
        assertEquals(2, decoded.version)
        assertEquals(1, decoded.bookmarks.size)
        assertEquals("https://example.com", decoded.bookmarks[0].url)
        assertEquals(listOf("Tools"), decoded.bookmarks[0].tags)
        assertEquals(1, decoded.collections.size)
        assertEquals("General", decoded.collections[0].name)
    }

    @Test
    fun testSanitizeJson_extractsValidJsonObjectFromSurroundingNoise() {
        val input = "junk prefix {\"version\": 3, \"bookmarks\": []} trailing junk"
        val sanitized = sanitizeJson(input)
        assertNotNull(sanitized)
        assertEquals("{\"version\": 3, \"bookmarks\": []}", sanitized)
    }

    @Test
    fun testSanitizeJson_returnsNullWhenNoOpeningOrClosingBrace() {
        assertNull(sanitizeJson("no braces here"))
        assertNull(sanitizeJson("only opening brace {"))
    }

    @Test
    fun testExtractTrailingBookmarks_extractsValidFragments() {
        val trailing = """
            some corrupt text
            {"url":"https://extra1.com","title":"Extra 1","tags":["tag1"]}
            middle junk
            {"url":"https://extra2.com","title":"Extra 2"}
        """.trimIndent()

        val extracted = extractTrailingBookmarks(trailing, json)
        assertEquals(2, extracted.size)
        assertEquals("https://extra1.com", extracted[0].url)
        assertEquals("Extra 1", extracted[0].title)
        assertEquals(listOf("tag1"), extracted[0].tags)
        assertEquals("https://extra2.com", extracted[1].url)
        assertEquals("Extra 2", extracted[1].title)
    }
}
