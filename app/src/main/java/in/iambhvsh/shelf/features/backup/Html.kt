package `in`.iambhvsh.shelf.features.backup

import androidx.room.withTransaction
import `in`.iambhvsh.shelf.data.BookmarkCollectionCrossRef
import `in`.iambhvsh.shelf.data.BookmarkDatabase
import `in`.iambhvsh.shelf.data.BookmarkEntity
import `in`.iambhvsh.shelf.data.CollectionEntity
import `in`.iambhvsh.shelf.data.dao.BookmarkDao
import `in`.iambhvsh.shelf.data.dao.CollectionDao
import `in`.iambhvsh.shelf.features.fetch.UrlUtils
import kotlinx.coroutines.flow.first

internal suspend fun exportBookmarksToHtml(
    bookmarkDao: BookmarkDao,
    collectionDao: CollectionDao
): String {
    val bookmarks = bookmarkDao.getBookmarksOnce()
    val collections = collectionDao.getAllCollectionsRaw().first()
    val sb = StringBuilder()
    sb.appendLine("<!DOCTYPE NETSCAPE-Bookmark-file-1>")
    sb.appendLine("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">")
    sb.appendLine("<TITLE>Shelf</TITLE>")
    sb.appendLine("<H1>Shelf</H1>")
    sb.appendLine("<DL><p>")

    val collectionBookmarkIds = mutableMapOf<Long, MutableSet<Long>>()
    for (c in collections) {
        val urlKeys = collectionDao.getBookmarkUrlsForCollection(c.id).map { UrlUtils.normalizeUrlKey(it) }.toSet()
        val ids = bookmarks.filter { UrlUtils.normalizeUrlKey(it.url) in urlKeys }.map { it.id }.toMutableSet()
        collectionBookmarkIds[c.id] = ids
    }

    val assigned = mutableSetOf<Long>()
    for (c in collections) {
        val ids = collectionBookmarkIds[c.id] ?: continue
        if (ids.isEmpty()) continue
        assigned.addAll(ids)
        sb.appendLine("<DT><H3>${escapeHtml(c.name)}</H3>")
        sb.appendLine("<DL><p>")
        for (bm in bookmarks.filter { it.id in ids }) {
            sb.appendLine("<DT><A HREF=\"${escapeHtml(bm.url)}\"${if (bm.title != null) ">${escapeHtml(bm.title)}" else ">${escapeHtml(bm.url)}"}</A>")
        }
        sb.appendLine("</DL><p>")
    }

    val unassigned = bookmarks.filter { it.id !in assigned }
    if (unassigned.isNotEmpty()) {
        sb.appendLine("<DT><H3>Uncategorized</H3>")
        sb.appendLine("<DL><p>")
        for (bm in unassigned) {
            sb.appendLine("<DT><A HREF=\"${escapeHtml(bm.url)}\"${if (bm.title != null) ">${escapeHtml(bm.title)}" else ">${escapeHtml(bm.url)}"}</A>")
        }
        sb.appendLine("</DL><p>")
    }

    sb.appendLine("</DL><p>")
    return sb.toString()
}

internal fun escapeHtml(text: String): String {
    return text
        .replace("&", "&amp;")
        .replace("\"", "&quot;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
}

internal suspend fun importBookmarksFromHtml(
    html: String,
    bookmarkDao: BookmarkDao,
    collectionDao: CollectionDao,
    database: BookmarkDatabase? = null
): BrowserImportResult {
    val block: suspend () -> BrowserImportResult = {
        val parser = BookmarkParser()
        val (parsedBookmarks, collectionNames) = parser.parse(html)

        val existingInDb = mutableMapOf<String, BookmarkEntity>()
        for (entity in bookmarkDao.getAllBookmarksOnce()
            .sortedWith(compareBy<BookmarkEntity> { it.isHidden }.thenByDescending { it.id })) {
            existingInDb.putIfAbsent(UrlUtils.normalizeUrlKey(entity.url), entity)
        }
        var imported = 0
        var skipped = 0

        val bookmarkMap = mutableMapOf<String, Long>()

        for (pb in parsedBookmarks) {
            val normalizedKey = UrlUtils.normalizeUrlKey(pb.url)
            val existing = existingInDb[normalizedKey]
            if (existing != null) {
                if (existing.isHidden) {
                    val resolvedTitle = if (pb.title.isNotBlank()) pb.title else existing.title
                    bookmarkDao.restoreBookmark(
                        id = existing.id,
                        title = resolvedTitle,
                        description = existing.description,
                        imageUrl = existing.imageUrl,
                        createdAt = existing.createdAt,
                        isPinned = existing.isPinned,
                        note = existing.note,
                        reminderTime = existing.reminderTime
                    )
                    existingInDb[normalizedKey] = existing.copy(isHidden = false, title = resolvedTitle)
                    imported++
                } else {
                    if ((existing.title.isNullOrBlank() || existing.title == existing.url) && pb.title.isNotBlank()) {
                        bookmarkDao.updateTitle(existing.id, pb.title)
                    }
                    skipped++
                }
                bookmarkMap[normalizedKey] = existing.id
            } else {
                val newId = bookmarkDao.insertWithReturn(
                    BookmarkEntity(
                        url = pb.url,
                        title = pb.title,
                        description = null,
                        imageUrl = null
                    )
                )
                bookmarkMap[normalizedKey] = newId
                existingInDb[normalizedKey] = BookmarkEntity(id = newId, url = pb.url, title = pb.title, description = null, imageUrl = null)
                imported++
            }
        }

        val existingCollections = collectionDao.getAllCollectionsRaw().first().associateBy { it.name }
        val nameToId = existingCollections.mapValues { it.value.id }.toMutableMap()

        for (pb in parsedBookmarks) {
            val bmId = bookmarkMap[UrlUtils.normalizeUrlKey(pb.url)] ?: continue
            for (cName in pb.collections) {
                val trimmed = cName.trim()
                if (trimmed.isEmpty()) continue
                val colId = nameToId.getOrPut(trimmed) {
                    collectionDao.insertCollection(CollectionEntity(name = trimmed))
                }
                if (colId > 0) {
                    collectionDao.addBookmarkToCollection(BookmarkCollectionCrossRef(bmId, colId))
                }
            }
        }

        BrowserImportResult(imported = imported, skipped = skipped, collections = collectionNames.size)
    }

    return if (database != null) {
        database.withTransaction { block() }
    } else {
        block()
    }
}
