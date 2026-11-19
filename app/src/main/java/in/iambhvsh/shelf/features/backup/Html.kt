package `in`.iambhvsh.shelf.features.backup

import `in`.iambhvsh.shelf.data.dao.BookmarkDao
import `in`.iambhvsh.shelf.data.dao.CollectionDao
import `in`.iambhvsh.shelf.data.BookmarkCollectionCrossRef
import `in`.iambhvsh.shelf.data.BookmarkEntity
import `in`.iambhvsh.shelf.data.CollectionEntity
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
        val urls = collectionDao.getBookmarkUrlsForCollection(c.id)
        val ids = bookmarks.filter { it.url in urls }.map { it.id }.toMutableSet()
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
    collectionDao: CollectionDao
): BrowserImportResult {
    val parser = BookmarkParser()
    val (parsedBookmarks, collectionNames) = parser.parse(html)

    val existingUrls = bookmarkDao.getBookmarksOnce().map { it.url }.toSet()
    var imported = 0
    var skipped = 0

    for (pb in parsedBookmarks) {
        if (pb.url in existingUrls) {
            skipped++
            continue
        }
        bookmarkDao.insertWithReturn(BookmarkEntity(url = pb.url, title = pb.title, description = null, imageUrl = null))
        imported++
    }

    val existingCollections = collectionDao.getAllCollectionsRaw().first().associateBy { it.name }
    val nameToId = existingCollections.mapValues { it.value.id }.toMutableMap()

    val bookmarkMap = bookmarkDao.getBookmarksOnce().associateBy { it.url }

    for (name in collectionNames) {
        val id = nameToId.getOrPut(name) {
            collectionDao.insertCollection(CollectionEntity(name = name))
        }
        parsedBookmarks.filter { pb -> pb.collection == name }.forEach { pb ->
            bookmarkMap[pb.url]?.let { bm ->
                collectionDao.addBookmarkToCollection(BookmarkCollectionCrossRef(bm.id, id))
            }
        }
    }

    return BrowserImportResult(imported = imported, skipped = skipped, collections = collectionNames.size)
}
