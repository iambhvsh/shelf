package `in`.iambhvsh.shelf.features.backup

import androidx.room.withTransaction
import `in`.iambhvsh.shelf.data.BackupBookmark
import `in`.iambhvsh.shelf.data.BackupData
import `in`.iambhvsh.shelf.data.BookmarkCollectionCrossRef
import `in`.iambhvsh.shelf.data.BookmarkDatabase
import `in`.iambhvsh.shelf.data.BookmarkEntity
import `in`.iambhvsh.shelf.data.BookmarkTagCrossRef
import `in`.iambhvsh.shelf.data.CollectionEntity
import `in`.iambhvsh.shelf.data.TagEntity
import `in`.iambhvsh.shelf.data.dao.BookmarkDao
import `in`.iambhvsh.shelf.data.dao.CollectionDao
import `in`.iambhvsh.shelf.data.dao.TagDao
import `in`.iambhvsh.shelf.features.fetch.UrlUtils
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
    tagDao: TagDao,
    database: BookmarkDatabase? = null
): JsonImportResult {
    val block: suspend () -> JsonImportResult = {
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
                val ebKey = UrlUtils.normalizeUrlKey(eb.url)
                if (allBookmarks.none { UrlUtils.normalizeUrlKey(it.url) == ebKey }) {
                    allBookmarks.add(eb)
                }
            }
        }

        val existingInDb = mutableMapOf<String, BookmarkEntity>()
        for (entity in bookmarkDao.getAllBookmarksOnce()
            .sortedWith(compareBy<BookmarkEntity> { it.isHidden }.thenByDescending { it.id })) {
            existingInDb.putIfAbsent(UrlUtils.normalizeUrlKey(entity.url), entity)
        }

        val existingTags = tagDao.getAllTagsOnce().associateBy { it.name }
        val tagNameToId = existingTags.mapValues { it.value.id }.toMutableMap()

        for (t in backupData.tags) {
            val trimmedName = t.name.trim()
            if (trimmedName.isEmpty()) continue
            if (!tagNameToId.containsKey(trimmedName)) {
                val existing = tagDao.findByName(trimmedName)
                val tagId = if (existing != null) {
                    existing.id
                } else {
                    val inserted = tagDao.insertTag(TagEntity(name = trimmedName))
                    if (inserted > 0) inserted else (tagDao.findByName(trimmedName)?.id ?: -1L)
                }
                if (tagId > 0) {
                    tagNameToId[trimmedName] = tagId
                }
            }
        }

        var importedCount = 0
        var updatedCount = 0
        var skippedCount = 0
        val urlToBookmarkId = mutableMapOf<String, Long>()

        for (b in allBookmarks) {
            val normalizedKey = UrlUtils.normalizeUrlKey(b.url)
            val existing = existingInDb[normalizedKey]
            val bookmarkId: Long
            if (existing != null) {
                bookmarkId = existing.id
                urlToBookmarkId[b.url] = bookmarkId
                urlToBookmarkId[normalizedKey] = bookmarkId
                if (existing.isHidden && !b.isHidden) {
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
                    existingInDb[normalizedKey] = existing.copy(
                        isHidden = false,
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
                urlToBookmarkId[normalizedKey] = bookmarkId
                existingInDb[normalizedKey] = BookmarkEntity(
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

            b.tags.forEach { rawTagName ->
                val tagName = rawTagName.trim()
                if (tagName.isNotEmpty()) {
                    val tagId = tagNameToId.getOrPut(tagName) {
                        val existing = tagDao.findByName(tagName)
                        if (existing != null) {
                            existing.id
                        } else {
                            val inserted = tagDao.insertTag(TagEntity(name = tagName))
                            if (inserted > 0) inserted else (tagDao.findByName(tagName)?.id ?: -1L)
                        }
                    }
                    if (tagId > 0) {
                        tagDao.addTagToBookmark(BookmarkTagCrossRef(bookmarkId, tagId))
                    }
                }
            }
        }

        val existingCollections = collectionDao.getAllCollectionsRaw().first().associateBy { it.name }
        val collectionNameToId = existingCollections.mapValues { it.value.id }.toMutableMap()
        val skippedUrls = mutableListOf<String>()
        var restoredCollections = 0

        for (c in backupData.collections) {
            val trimmedColName = c.name.trim()
            if (trimmedColName.isEmpty()) continue
            val collectionId = collectionNameToId.getOrPut(trimmedColName) {
                collectionDao.insertCollection(CollectionEntity(name = trimmedColName))
            }
            if (collectionId > 0) {
                restoredCollections++
                c.bookmarkUrls.forEach { url ->
                    val normalizedUrl = UrlUtils.normalizeUrlKey(url)
                    val bmId = urlToBookmarkId[url]
                        ?: urlToBookmarkId[normalizedUrl]
                        ?: existingInDb[normalizedUrl]?.id
                    if (bmId != null && bmId > 0) {
                        collectionDao.addBookmarkToCollection(BookmarkCollectionCrossRef(bmId, collectionId))
                    } else {
                        skippedUrls.add(url)
                    }
                }
            }
        }

        JsonImportResult(
            imported = importedCount,
            updated = updatedCount,
            skipped = skippedCount,
            collections = restoredCollections,
            skippedUrls = skippedUrls
        )
    }

    return if (database != null) {
        database.withTransaction { block() }
    } else {
        block()
    }
}
