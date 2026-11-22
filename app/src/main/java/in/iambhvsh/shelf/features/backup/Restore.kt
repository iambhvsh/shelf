package `in`.iambhvsh.shelf.features.backup

import `in`.iambhvsh.shelf.data.BackupBookmark
import `in`.iambhvsh.shelf.data.BackupData
import `in`.iambhvsh.shelf.data.BookmarkCollectionCrossRef
import `in`.iambhvsh.shelf.data.BookmarkEntity
import `in`.iambhvsh.shelf.data.BookmarkTagCrossRef
import `in`.iambhvsh.shelf.data.CollectionEntity
import `in`.iambhvsh.shelf.data.TagEntity
import `in`.iambhvsh.shelf.data.dao.BookmarkDao
import `in`.iambhvsh.shelf.data.dao.CollectionDao
import `in`.iambhvsh.shelf.data.dao.TagDao
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

data class JsonImportResult(
    val imported: Int,
    val updated: Int,
    val skipped: Int,
    val collections: Int,
    val skippedUrls: List<String> = emptyList()
)

fun sanitizeJson(input: String): String? {
    val startIndex = input.indexOf('{')
    if (startIndex == -1) return null
    var depth = 0
    var inString = false
    var isEscaped = false
    for (i in startIndex until input.length) {
        val c = input[i]
        if (isEscaped) {
            isEscaped = false
            continue
        }
        if (c == '\\') {
            if (inString) {
                isEscaped = true
            }
            continue
        }
        if (c == '"') {
            inString = !inString
            continue
        }
        if (!inString) {
            if (c == '{') {
                depth++
            } else if (c == '}') {
                depth--
                if (depth == 0) {
                    return input.substring(startIndex, i + 1)
                }
            }
        }
    }
    return null
}

fun extractTrailingBookmarks(trailingText: String, json: Json): List<BackupBookmark> {
    val results = mutableListOf<BackupBookmark>()
    val regex = Regex("""\{[^{}]*"url"\s*:[^{}]*\}""", RegexOption.DOT_MATCHES_ALL)
    for (match in regex.findAll(trailingText)) {
        try {
            val bm = json.decodeFromString<BackupBookmark>(match.value)
            results.add(bm)
        } catch (_: Exception) {
        }
    }
    return results
}

suspend fun importBackupFromJson(
    jsonString: String,
    json: Json,
    bookmarkDao: BookmarkDao,
    collectionDao: CollectionDao,
    tagDao: TagDao
): JsonImportResult {
    val trimmed = jsonString.trim()
    val (backupData, trailingText) = try {
        Pair(json.decodeFromString<BackupData>(trimmed), "")
    } catch (e: Exception) {
        val sanitized = sanitizeJson(trimmed)
        if (sanitized != null) {
            val trailing = trimmed.substring(sanitized.length).trim()
            Pair(json.decodeFromString<BackupData>(sanitized), trailing)
        } else {
            throw e
        }
    }

    val allBookmarks = backupData.bookmarks.toMutableList()
    if (trailingText.isNotEmpty()) {
        val extra = extractTrailingBookmarks(trailingText, json)
        for (eb in extra) {
            if (allBookmarks.none { it.url == eb.url }) {
                allBookmarks.add(eb)
            }
        }
    }

    val existingInDb = bookmarkDao.getAllBookmarksOnce().associateBy { it.url }.toMutableMap()

    val existingTags = tagDao.getAllTagsOnce().associateBy { it.name }
    val tagNameToId = existingTags.mapValues { it.value.id }.toMutableMap()

    for (t in backupData.tags) {
        if (!tagNameToId.containsKey(t.name)) {
            val tagId = tagDao.insertTag(TagEntity(name = t.name))
            tagNameToId[t.name] = tagId
        }
    }

    var importedCount = 0
    var updatedCount = 0
    var skippedCount = 0
    val urlToBookmarkId = mutableMapOf<String, Long>()

    for (b in allBookmarks) {
        val existing = existingInDb[b.url]
        val bookmarkId: Long
        if (existing != null) {
            bookmarkId = existing.id
            urlToBookmarkId[b.url] = bookmarkId
            if (existing.isHidden) {
                bookmarkDao.restoreBookmark(
                    id = existing.id,
                    title = b.title,
                    description = b.description,
                    imageUrl = b.imageUrl,
                    createdAt = b.createdAt,
                    isPinned = b.isPinned,
                    note = b.note,
                    reminderTime = b.reminderTime
                )
                updatedCount++
            } else {
                bookmarkDao.updateExistingBookmarkMetadata(
                    id = existing.id,
                    title = b.title,
                    description = b.description,
                    imageUrl = b.imageUrl,
                    isPinned = b.isPinned,
                    note = b.note,
                    reminderTime = b.reminderTime
                )
                skippedCount++
            }
        } else {
            bookmarkId = bookmarkDao.insertWithReturn(
                BookmarkEntity(
                    url = b.url,
                    title = b.title,
                    description = b.description,
                    imageUrl = b.imageUrl,
                    createdAt = b.createdAt,
                    isHidden = b.isHidden,
                    isPinned = b.isPinned,
                    note = b.note,
                    reminderTime = b.reminderTime
                )
            )
            urlToBookmarkId[b.url] = bookmarkId
            existingInDb[b.url] = BookmarkEntity(
                id = bookmarkId,
                url = b.url,
                title = b.title,
                description = b.description,
                imageUrl = b.imageUrl,
                createdAt = b.createdAt,
                isHidden = b.isHidden,
                isPinned = b.isPinned,
                note = b.note,
                reminderTime = b.reminderTime
            )
            importedCount++
        }

        b.tags.forEach { tagName ->
            val tagId = tagNameToId.getOrPut(tagName) {
                tagDao.insertTag(TagEntity(name = tagName))
            }
            tagDao.addTagToBookmark(BookmarkTagCrossRef(bookmarkId, tagId))
        }
    }

    val existingCollections = collectionDao.getAllCollectionsRaw().first().associateBy { it.name }
    val collectionNameToId = existingCollections.mapValues { it.value.id }.toMutableMap()
    val skippedUrls = mutableListOf<String>()
    var restoredCollections = 0

    for (c in backupData.collections) {
        val collectionId = collectionNameToId.getOrPut(c.name) {
            collectionDao.insertCollection(CollectionEntity(name = c.name))
        }
        restoredCollections++
        c.bookmarkUrls.forEach { url ->
            val bmId = urlToBookmarkId[url] ?: existingInDb[url]?.id
            if (bmId != null) {
                collectionDao.addBookmarkToCollection(BookmarkCollectionCrossRef(bmId, collectionId))
            } else {
                skippedUrls.add(url)
            }
        }
    }

    return JsonImportResult(
        imported = importedCount,
        updated = updatedCount,
        skipped = skippedCount,
        collections = restoredCollections,
        skippedUrls = skippedUrls
    )
}
