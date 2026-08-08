package `in`.iambhvsh.shelf.data.backup

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import `in`.iambhvsh.shelf.data.local.BackupBookmark
import `in`.iambhvsh.shelf.data.local.BackupCollection
import `in`.iambhvsh.shelf.data.local.BackupData
import `in`.iambhvsh.shelf.link_fetcher.LinkMetadataParser
import `in`.iambhvsh.shelf.data.local.dao.BookmarkDao
import `in`.iambhvsh.shelf.data.local.dao.CollectionDao
import `in`.iambhvsh.shelf.data.local.entity.BookmarkCollectionCrossRef
import `in`.iambhvsh.shelf.data.local.entity.BookmarkEntity
import `in`.iambhvsh.shelf.data.local.entity.CollectionEntity
import `in`.iambhvsh.shelf.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
class BackupManager(
    private val bookmarkDao: BookmarkDao,
    private val collectionDao: CollectionDao,
    private val settingsRepository: SettingsRepository,
    private val context: Context
) {
    companion object {
        private const val FILE_NAME = "shelf_autobackup.json"
        private const val BACKUP_DIR = "backups"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var autoBackupJob: Job? = null
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val _lastBackupTimeMillis = MutableStateFlow(0L)
    val lastBackupTimeMillis: StateFlow<Long> = _lastBackupTimeMillis.asStateFlow()

    init {
        if (settingsRepository.getAutoBackupEnabled()) {
            refreshLastBackupTime()
            startAutoBackup()
        }
    }

    fun refreshLastBackupTime() {
        val file = getInternalFile()
        _lastBackupTimeMillis.value = if (file.exists()) file.lastModified() else 0L
    }

    private fun getInternalFile(): File {
        return File(File(context.filesDir, BACKUP_DIR), FILE_NAME)
    }

    fun startAutoBackup() {
        stopAutoBackup()
        autoBackupJob = scope.launch {
            combine(
                bookmarkDao.getBookmarks(),
                collectionDao.getAllCollections()
            ) { bookmarks, collections ->
                Pair(bookmarks, collections)
            }
                .debounce(500)
                .map { (bookmarks, collections) ->
                    val backupBookmarks = bookmarks.map { BackupBookmark(url = it.url, title = it.title, description = it.description, imageUrl = it.imageUrl, createdAt = it.createdAt) }
                    val backupCollections = collections.mapNotNull { collection ->
                        val urls = collectionDao.getBookmarkUrlsForCollection(collection.id)
                        if (collection.name.isNotBlank()) BackupCollection(name = collection.name, bookmarkUrls = urls) else null
                    }
                    BackupData(bookmarks = backupBookmarks, collections = backupCollections)
                }
                .collect { data ->
                    val jsonString = json.encodeToString(data)
                    writeToBothLocations(jsonString)
                }
        }
    }

    fun stopAutoBackup() {
        autoBackupJob?.cancel()
        autoBackupJob = null
    }

    private fun writeToBothLocations(jsonString: String) {
        val backupDir = File(context.filesDir, BACKUP_DIR)
        backupDir.mkdirs()
        val internalFile = File(backupDir, FILE_NAME)
        internalFile.writeText(jsonString)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeToDownloads(jsonString)
        }

        _lastBackupTimeMillis.value = System.currentTimeMillis()
    }

    @SuppressLint("NewApi")
    private fun writeToDownloads(jsonString: String) {
        try {
            val collectionUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            val selection = """
                ${MediaStore.Downloads.DISPLAY_NAME} = ?
                AND
                ${MediaStore.Downloads.RELATIVE_PATH} = ?
            """.trimIndent()
            val selectionArgs = arrayOf(
                FILE_NAME,
                "${Environment.DIRECTORY_DOWNLOADS}/Shelf/"
            )
            
            var existingUri: android.net.Uri? = null
            context.contentResolver.query(
                collectionUri,
                arrayOf(MediaStore.Downloads._ID),
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                    existingUri = android.content.ContentUris.withAppendedId(collectionUri, id)
                }
            }

            val uri = existingUri ?: run {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, FILE_NAME)
                    put(MediaStore.Downloads.MIME_TYPE, "application/json")
                    put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Shelf")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Downloads.IS_PENDING, 0)
                    }
                }
                context.contentResolver.insert(collectionUri, contentValues)
            }

            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { output ->
                    output.write(jsonString.toByteArray())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun readInternalBackup(): String? {
        val file = getInternalFile()
        return if (file.exists()) file.readText() else null
    }

    suspend fun generateBackupJson(): String {
        val bookmarks = bookmarkDao.getBookmarksOnce()
        val collections = collectionDao.getAllCollectionsRaw().first()

        val backupBookmarks = bookmarks.map { BackupBookmark(url = it.url, title = it.title, description = it.description, imageUrl = it.imageUrl, createdAt = it.createdAt) }
        val backupCollections = collections.mapNotNull { collection ->
            val urls = collectionDao.getBookmarkUrlsForCollection(collection.id)
            if (collection.name.isNotBlank()) BackupCollection(name = collection.name, bookmarkUrls = urls) else null
        }

        val data = BackupData(bookmarks = backupBookmarks, collections = backupCollections)
        return json.encodeToString(data)
    }

    suspend fun generateExportHtml(): String {
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

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }

    suspend fun importFromBrowserBookmarks(html: String): BrowserImportResult {
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
            parsedBookmarks.filter { it.collection == name }.forEach { pb ->
                bookmarkMap[pb.url]?.let { bm ->
                    collectionDao.addBookmarkToCollection(BookmarkCollectionCrossRef(bm.id, id))
                }
            }
        }

        return BrowserImportResult(imported = imported, skipped = skipped, collections = collectionNames.size)
    }

    suspend fun fetchMissingImages() {
        val parser = LinkMetadataParser()
        val missing = bookmarkDao.getBookmarksWithoutImageOnce()
        for (bm in missing) {
            try {
                val meta = parser.parse(bm.url)
                if (!meta?.imageUrl.isNullOrBlank()) {
                    bookmarkDao.updateImageUrl(bm.id, meta.imageUrl)
                }
            } catch (_: Exception) { }
        }
    }

    suspend fun importFromJson(jsonString: String) {
        val backupData = json.decodeFromString<BackupData>(jsonString)

        val existingUrls = bookmarkDao.getBookmarksOnce().map { it.url }.toSet()

        for (b in backupData.bookmarks) {
            if (b.url !in existingUrls) {
                bookmarkDao.insertWithReturn(BookmarkEntity(url = b.url, title = b.title, description = b.description, imageUrl = b.imageUrl, createdAt = b.createdAt))
            }
        }

        val bookmarkMap = bookmarkDao.getBookmarksOnce().associateBy { it.url }

        val existingCollections = collectionDao.getAllCollectionsRaw().first().associateBy { it.name }
        val collectionNameToId = existingCollections.mapValues { it.value.id }.toMutableMap()

        for (c in backupData.collections) {
            val collectionId = collectionNameToId.getOrPut(c.name) {
                collectionDao.insertCollection(CollectionEntity(name = c.name))
            }
            c.bookmarkUrls.forEach { url ->
                bookmarkMap[url]?.let { bm ->
                    collectionDao.addBookmarkToCollection(BookmarkCollectionCrossRef(bm.id, collectionId))
                }
            }
        }
    }
}
