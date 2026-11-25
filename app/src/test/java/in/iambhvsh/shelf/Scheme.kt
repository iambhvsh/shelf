package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.data.BookmarkStatus
import `in`.iambhvsh.shelf.data.Collection
import `in`.iambhvsh.shelf.data.SortOrder
import `in`.iambhvsh.shelf.data.Tag
import `in`.iambhvsh.shelf.theme.AccentColor
import `in`.iambhvsh.shelf.theme.ThemeMode
import `in`.iambhvsh.shelf.util.Resource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SchemeAndDomainTest {

    @Test
    fun testIsHttpOrHttpsUrl_allowsOnlyHttpAndHttpsSchemesCaseInsensitively() {
        assertTrue(isHttpOrHttpsUrl("http://example.com"))
        assertTrue(isHttpOrHttpsUrl("https://example.com/path?x=1"))
        assertTrue(isHttpOrHttpsUrl("  HTTPS://EXAMPLE.COM/Docs  "))
        assertTrue(isHttpOrHttpsUrl("Http://127.0.0.1:8080"))

        assertFalse(isHttpOrHttpsUrl(""))
        assertFalse(isHttpOrHttpsUrl("   "))
        assertFalse(isHttpOrHttpsUrl("javascript:alert(1)"))
        assertFalse(isHttpOrHttpsUrl("file:///etc/passwd"))
        assertFalse(isHttpOrHttpsUrl("content://com.android.provider/item"))
        assertFalse(isHttpOrHttpsUrl("intent://scan/#Intent;end"))
        assertFalse(isHttpOrHttpsUrl("mailto:user@example.com"))
        assertFalse(isHttpOrHttpsUrl("tel:+1234567890"))
        assertFalse(isHttpOrHttpsUrl("ftp://example.com/file"))
        assertFalse(isHttpOrHttpsUrl("example.com"))
    }

    @Test
    fun testResourceVariants_exposeExpectedDataAndErrorMessages() {
        val loading = Resource.Loading<List<String>>()
        assertNull(loading.data)
        assertNull(loading.errorMessage)

        val success = Resource.Success(listOf("a", "b"))
        assertEquals(listOf("a", "b"), success.data)
        assertNull(success.errorMessage)

        val error = Resource.Error<List<String>>("Something broke")
        assertNull(error.data)
        assertEquals("Something broke", error.errorMessage)
    }

    @Test
    fun testEnumsAndDomainDefaults_coverAllEntries() {
        assertEquals(listOf("LIGHT", "DARK", "SYSTEM", "OLED"), ThemeMode.entries.map { it.name })
        assertEquals(
            listOf("DATE_NEWEST", "DATE_OLDEST", "TITLE_ASC", "TITLE_DESC"),
            SortOrder.entries.map { it.name }
        )
        assertEquals(9, AccentColor.entries.size)
        assertEquals(AccentColor.entries.map { it.label }.toSet().size, AccentColor.entries.size)
        assertTrue(AccentColor.entries.all { it.label.isNotBlank() })

        val col = Collection(name = "Work")
        assertEquals(0L, col.id)
        assertEquals(0, col.bookmarkCount)
        assertTrue(col.previewUrls.isEmpty())
        assertFalse(col.isVirtual)
        assertEquals(-1L, Collection.UNCATEGORISED_ID)

        val bm = Bookmark(url = "https://shelf.app", title = "Shelf", description = null, imageUrl = null, createdAt = 42L)
        assertFalse(bm.isPinned)
        assertNull(bm.note)
        assertNull(bm.reminderTime)
        val status = BookmarkStatus(bookmark = bm, isHidden = true)
        assertTrue(status.isHidden)

        val tag = Tag(name = "Kotlin")
        assertEquals(0L, tag.id)
        assertEquals("Kotlin", tag.name)
    }
}
