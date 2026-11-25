package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.data.*
import `in`.iambhvsh.shelf.data.Collection
import `in`.iambhvsh.shelf.data.dao.CollectionWithCount
import org.junit.Assert.*
import org.junit.Test

class MapperTest {

    @Test
    fun testBookmarkEntityToDomainAndBack() {
        val entity = BookmarkEntity(
            id = 101L,
            url = "https://kotlinlang.org",
            title = "Kotlin Language",
            description = "Modern Android & multiplatform language",
            imageUrl = "https://kotlinlang.org/icon.png",
            createdAt = 1700000000000L,
            isHidden = false,
            isPinned = true,
            note = "Study coroutines",
            reminderTime = 1700005000000L
        )

        val domain = entity.toDomain()
        assertEquals(101L, domain.id)
        assertEquals("https://kotlinlang.org", domain.url)
        assertEquals("Kotlin Language", domain.title)
        assertEquals("Modern Android & multiplatform language", domain.description)
        assertEquals("https://kotlinlang.org/icon.png", domain.imageUrl)
        assertEquals(1700000000000L, domain.createdAt)
        assertTrue(domain.isPinned)
        assertEquals("Study coroutines", domain.note)
        assertEquals(1700005000000L, domain.reminderTime)

        val mappedBack = domain.toEntity()
        assertEquals(entity.id, mappedBack.id)
        assertEquals(entity.url, mappedBack.url)
        assertEquals(entity.title, mappedBack.title)
        assertEquals(entity.description, mappedBack.description)
        assertEquals(entity.imageUrl, mappedBack.imageUrl)
        assertEquals(entity.createdAt, mappedBack.createdAt)
        assertEquals(entity.isPinned, mappedBack.isPinned)
        assertEquals(entity.note, mappedBack.note)
        assertEquals(entity.reminderTime, mappedBack.reminderTime)
    }

    @Test
    fun testCollectionWithCountToDomain_splitsPreviewUrlsAndIdentifiesVirtual() {
        val uncategorised = CollectionWithCount(
            id = Collection.UNCATEGORISED_ID,
            name = "Uncategorised",
            createdAt = 1000L,
            bookmarkCount = 5,
            previewUrls = "https://a.com/1.png|||https://b.com/2.png||||||"
        )

        val domainUncat = uncategorised.toDomain()
        assertEquals(Collection.UNCATEGORISED_ID, domainUncat.id)
        assertEquals("Uncategorised", domainUncat.name)
        assertEquals(5, domainUncat.bookmarkCount)
        assertTrue(domainUncat.isVirtual)
        assertEquals(listOf("https://a.com/1.png", "https://b.com/2.png"), domainUncat.previewUrls)

        val normalCollection = CollectionWithCount(
            id = 42L,
            name = "Articles",
            createdAt = 2000L,
            bookmarkCount = 0,
            previewUrls = null
        )

        val domainNormal = normalCollection.toDomain()
        assertEquals(42L, domainNormal.id)
        assertFalse(domainNormal.isVirtual)
        assertTrue(domainNormal.previewUrls.isEmpty())
    }

    @Test
    fun testCollectionWithBookmarksToDomain_excludesHiddenBookmarksFromCount() {
        val collection = CollectionEntity(id = 10L, name = "Dev")
        val bookmarks = listOf(
            BookmarkEntity(id = 1L, url = "https://a.com", title = "A", description = null, imageUrl = null, isHidden = false),
            BookmarkEntity(id = 2L, url = "https://b.com", title = "B", description = null, imageUrl = null, isHidden = true),
            BookmarkEntity(id = 3L, url = "https://c.com", title = "C", description = null, imageUrl = null, isHidden = false)
        )
        val relation = CollectionWithBookmarks(collection = collection, bookmarks = bookmarks)
        val domain = relation.toDomain()

        assertEquals(10L, domain.id)
        assertEquals("Dev", domain.name)
        assertEquals(2, domain.bookmarkCount)
    }

    @Test
    fun testTagEntityToDomainAndBack() {
        val entity = TagEntity(id = 7L, name = "Android")
        val domain = entity.toDomain()
        assertEquals(7L, domain.id)
        assertEquals("Android", domain.name)

        val mappedBack = domain.toEntity()
        assertEquals(entity.id, mappedBack.id)
        assertEquals(entity.name, mappedBack.name)
    }

    @Test
    fun testCollectionEntityAndNullBookmarkFieldsMapCleanly() {
        val colEntity = CollectionEntity(id = 99L, name = "Read Later", createdAt = 555L)
        val colDomain = colEntity.toDomain()
        assertEquals(99L, colDomain.id)
        assertEquals("Read Later", colDomain.name)
        assertEquals(0, colDomain.bookmarkCount)
        assertFalse(colDomain.isVirtual)

        val roundTrip = colDomain.toEntity()
        assertEquals(99L, roundTrip.id)
        assertEquals("Read Later", roundTrip.name)

        val sparseEntity = BookmarkEntity(
            id = 5L,
            url = "https://minimal.org",
            title = null,
            description = null,
            imageUrl = null,
            isHidden = true
        )
        val sparseDomain = sparseEntity.toDomain()
        assertNull(sparseDomain.title)
        assertNull(sparseDomain.description)
        assertNull(sparseDomain.imageUrl)
        assertFalse(sparseDomain.isPinned)
        assertNull(sparseDomain.note)
        assertNull(sparseDomain.reminderTime)
        assertFalse(sparseDomain.toEntity().isHidden)

        val delimiterOnlyPreviews = CollectionWithCount(
            id = 3L,
            name = "Blank Previews",
            createdAt = 1L,
            bookmarkCount = 1,
            previewUrls = "|||   |||"
        )
        assertTrue(delimiterOnlyPreviews.toDomain().previewUrls.isEmpty())

        val allHidden = CollectionWithBookmarks(
            collection = CollectionEntity(id = 4L, name = "Archived"),
            bookmarks = listOf(
                BookmarkEntity(id = 1L, url = "https://x.com", title = null, description = null, imageUrl = null, isHidden = true)
            )
        )
        assertEquals(0, allHidden.toDomain().bookmarkCount)
    }
}
