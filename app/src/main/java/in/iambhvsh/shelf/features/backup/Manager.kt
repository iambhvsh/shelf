package `in`.iambhvsh.shelf.features.backup

import android.content.Context
import `in`.iambhvsh.shelf.data.BackupBookmark
import `in`.iambhvsh.shelf.data.BackupCollection
import `in`.iambhvsh.shelf.data.BackupData
import `in`.iambhvsh.shelf.data.BackupTag
import `in`.iambhvsh.shelf.data.BookmarkDatabase
import `in`.iambhvsh.shelf.data.dao.BookmarkDao
import `in`.iambhvsh.shelf.data.dao.CollectionDao
import `in`.iambhvsh.shelf.data.dao.TagDao
import `in`.iambhvsh.shelf.data.SettingsRepository
import `in`.iambhvsh.shelf.features.fetch.LinkMetadataParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(FlowPreview::class)
open class BackupManager(
    private val database: BookmarkDatabase,
    private val bookmarkDao: BookmarkDao,
    private val collectionDao: CollectionDao,
    private val tagDao: TagDao,
    private val settingsRepository: SettingsRepository,
    private val context: Context,
    private val linkMetadataParser: LinkMetadataParser = LinkMetadataParser(),
    private val networkFetcher: `in`.iambhvsh.shelf.features.fetch.NetworkFetcher = `in`.iambhvsh.shelf.features.fetch.NetworkFetcher(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val reminderManager = `in`.iambhvsh.shelf.features.reminder.ReminderManager(context)
    private var autoBackupJob: Job? = null
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val _lastBackupTimeMillis = MutableStateFlow(0L)
    val lastBackupTimeMillis: StateFlow<Long> = _lastBackupTimeMillis.asStateFlow()

    init {
        if (settingsRepository.getAutoBackupEnabled()) {
            refreshLastBackupTime()
            startAutoBackup()
        }
    }

    open fun refreshLastBackupTime() {
        val file = getInternalBackupFile(context)
        _lastBackupTimeMillis.value = if (file.exists()) file.lastModified() else 0L
    }

    open fun clearInternalBackup() {
        runCatching {
            val file = getInternalBackupFile(context)
            if (file.exists()) file.delete()
        }
        _lastBackupTimeMillis.value = 0L
    }

    open fun startAutoBackup() {
        stopAutoBackup()
        autoBackupJob = scope.launch {
            combine(
                bookmarkDao.getAllBookmarks(),
                collectionDao.getAllCollections(),
                tagDao.getAllTags(),
                tagDao.getTagCrossRefCount(),
                collectionDao.getCollectionCrossRefCount()
            ) { bookmarks, collections, _, _, _ ->
                Pair(bookmarks, collections)
            }
                .debounce(500)
                .map { (bookmarks, collections) ->
                    val allTags = tagDao.getAllTagsOnce()
                    val backupTags = allTags.map { BackupTag(it.name) }

                    val backupBookmarks = bookmarks.map { bookmark ->
                        val tagEntities = tagDao.getTagsForBookmarkOnce(bookmark.id)
                        val tagNames = tagEntities.map { it.name }
                        BackupBookmark(
                            url = bookmark.url,
                            title = bookmark.title,
                            description = bookmark.description,
                            imageUrl = bookmark.imageUrl,
                            createdAt = bookmark.createdAt,
                            isHidden = bookmark.isHidden,
                            isPinned = bookmark.isPinned,
                            note = bookmark.note,
                            reminderTime = bookmark.reminderTime,
                            tags = tagNames
                        )
                    }
                    val backupCollections = collections.mapNotNull { collection ->
                        val urls = collectionDao.getAllBookmarkUrlsForCollection(collection.id)
                        if (collection.name.isNotBlank()) BackupCollection(name = collection.name, bookmarkUrls = urls) else null
                    }
                    BackupData(bookmarks = backupBookmarks, collections = backupCollections, tags = backupTags)
                }
                .collect { data ->
                    try {
                        val jsonString = json.encodeToString(data)
                        val timestamp = writeBackupToStorage(
                            context = context,
                            jsonString = jsonString,
                            customUriStr = settingsRepository.getAutoBackupUri()
                        )
                        if (timestamp != null) {
                            _lastBackupTimeMillis.value = timestamp
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                    }
                }
        }
    }

    open fun stopAutoBackup() {
        autoBackupJob?.cancel()
        autoBackupJob = null
    }

    open fun readInternalBackup(): String? = readInternalBackupFile(context)

    open suspend fun generateBackupJson(): String = withContext(ioDispatcher) {
        val bookmarks = bookmarkDao.getAllBookmarksOnce()
        val collections = collectionDao.getAllCollectionsRaw().first()

        val backupBookmarks = bookmarks.map { bookmark ->
            val tagEntities = tagDao.getTagsForBookmarkOnce(bookmark.id)
            val tagNames = tagEntities.map { it.name }
            BackupBookmark(
                url = bookmark.url,
                title = bookmark.title,
                description = bookmark.description,
                imageUrl = bookmark.imageUrl,
                createdAt = bookmark.createdAt,
                isHidden = bookmark.isHidden,
                isPinned = bookmark.isPinned,
                note = bookmark.note,
                reminderTime = bookmark.reminderTime,
                tags = tagNames
            )
        }
        val backupCollections = collections.mapNotNull { collection ->
            val urls = collectionDao.getAllBookmarkUrlsForCollection(collection.id)
            if (collection.name.isNotBlank()) BackupCollection(name = collection.name, bookmarkUrls = urls) else null
        }

        val allTags = tagDao.getAllTagsOnce()
        val backupTags = allTags.map { BackupTag(it.name) }

        val data = BackupData(bookmarks = backupBookmarks, collections = backupCollections, tags = backupTags)
        json.encodeToString(data)
    }

    open suspend fun generateExportHtml(): String = withContext(ioDispatcher) {
        exportBookmarksToHtml(bookmarkDao, collectionDao)
    }

    open suspend fun importFromBrowserBookmarks(html: String): BrowserImportResult = withContext(ioDispatcher) {
        importBookmarksFromHtml(html, bookmarkDao, collectionDao, database)
    }

    open fun refreshImportedThumbnailsAsync(checkExistingImages: Boolean) {
        scope.launch {
            fetchMissingImages(checkExistingImages = checkExistingImages)
        }
    }

    suspend fun fetchMissingImages(checkExistingImages: Boolean = false) {
        val candidates = if (checkExistingImages) {
            bookmarkDao.getBookmarksOnce()
        } else {
            bookmarkDao.getBookmarksWithoutImageOnce()
        }
        if (candidates.isEmpty()) return

        val semaphore = Semaphore(4)
        coroutineScope {
            candidates.map { bm ->
                async(ioDispatcher) {
                    semaphore.withPermit {
                        try {
                            val currentImage = bm.imageUrl
                            val needsRefresh = currentImage.isNullOrBlank() ||
                                (checkExistingImages && !networkFetcher.isImageUrlReachable(currentImage))
                            if (!needsRefresh) return@withPermit

                            val meta = linkMetadataParser.parse(bm.url) ?: return@withPermit
                            val newImage = meta.imageUrl?.trim()
                            if (!newImage.isNullOrBlank() && newImage != currentImage) {
                                bookmarkDao.updateImageUrl(bm.id, newImage)
                            }
                            if ((bm.title.isNullOrBlank() || bm.title == bm.url) && !meta.title.isNullOrBlank()) {
                                bookmarkDao.updateTitle(bm.id, meta.title)
                            }
                        } catch (e: Exception) {
                            if (e is CancellationException) throw e
                        }
                    }
                }
            }.awaitAll()
        }
    }

    open suspend fun importFromJson(jsonString: String): JsonImportResult = withContext(ioDispatcher) {
        val result = importBackupFromJson(
            jsonString = jsonString,
            json = json,
            bookmarkDao = bookmarkDao,
            collectionDao = collectionDao,
            tagDao = tagDao,
            database = database
        )
        val now = System.currentTimeMillis()
        val all = bookmarkDao.getAllBookmarksOnce()
        for (bm in all) {
            val time = bm.reminderTime
            if (time != null && time > now && !bm.isHidden) {
                reminderManager.scheduleReminder(bm.id, time)
            }
        }
        result
    }
}
