package `in`.iambhvsh.shelf.features.backup

import android.content.Context
import `in`.iambhvsh.shelf.data.BackupBookmark
import `in`.iambhvsh.shelf.data.BackupCollection
import `in`.iambhvsh.shelf.data.BackupData
import `in`.iambhvsh.shelf.data.BackupTag
import `in`.iambhvsh.shelf.data.dao.BookmarkDao
import `in`.iambhvsh.shelf.data.dao.CollectionDao
import `in`.iambhvsh.shelf.data.dao.TagDao
import `in`.iambhvsh.shelf.data.BookmarkCollectionCrossRef
import `in`.iambhvsh.shelf.data.BookmarkEntity
import `in`.iambhvsh.shelf.data.BookmarkTagCrossRef
import `in`.iambhvsh.shelf.data.CollectionEntity
import `in`.iambhvsh.shelf.data.TagEntity
import `in`.iambhvsh.shelf.data.SettingsRepository
import `in`.iambhvsh.shelf.features.fetch.LinkMetadataParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
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

@OptIn(FlowPreview::class)
class BackupManager(
    private val bookmarkDao: BookmarkDao,
    private val collectionDao: CollectionDao,
    private val tagDao: TagDao,
    private val settingsRepository: SettingsRepository,
    private val context: Context
) {
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
        val file = getInternalBackupFile(context)
        _lastBackupTimeMillis.value = if (file.exists()) file.lastModified() else 0L
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
                        val urls = collectionDao.getBookmarkUrlsForCollection(collection.id)
                        if (collection.name.isNotBlank()) BackupCollection(name = collection.name, bookmarkUrls = urls) else null
                    }
                    BackupData(bookmarks = backupBookmarks, collections = backupCollections, tags = backupTags)
                }
                .collect { data ->
                    val jsonString = json.encodeToString(data)
                    _lastBackupTimeMillis.value = writeBackupToStorage(
                        context = context,
                        jsonString = jsonString,
                        customUriStr = settingsRepository.getAutoBackupUri()
                    )
                }
        }
    }

    fun stopAutoBackup() {
        autoBackupJob?.cancel()
        autoBackupJob = null
    }

    fun readInternalBackup(): String? = readInternalBackupFile(context)

    suspend fun generateBackupJson(): String {
        val bookmarks = bookmarkDao.getBookmarksOnce()
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
            val urls = collectionDao.getBookmarkUrlsForCollection(collection.id)
            if (collection.name.isNotBlank()) BackupCollection(name = collection.name, bookmarkUrls = urls) else null
        }

        val allTags = tagDao.getAllTagsOnce()
        val backupTags = allTags.map { BackupTag(it.name) }

        val data = BackupData(bookmarks = backupBookmarks, collections = backupCollections, tags = backupTags)
        return json.encodeToString(data)
    }

    suspend fun generateExportHtml(): String = exportBookmarksToHtml(bookmarkDao, collectionDao)

    suspend fun importFromBrowserBookmarks(html: String): BrowserImportResult =
        importBookmarksFromHtml(html, bookmarkDao, collectionDao)

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

        val existingTags = tagDao.getAllTagsOnce().associateBy { it.name }
        val tagNameToId = existingTags.mapValues { it.value.id }.toMutableMap()

        for (t in backupData.tags) {
            if (!tagNameToId.containsKey(t.name)) {
                val tagId = tagDao.insertTag(TagEntity(name = t.name))
                tagNameToId[t.name] = tagId
            }
        }

        val existingUrls = bookmarkDao.getBookmarksOnce().map { it.url }.toSet()

        for (b in backupData.bookmarks) {
            if (b.url !in existingUrls) {
                var imgUrl = b.imageUrl
                if (imgUrl != null && imgUrl.contains("scontent") && imgUrl.contains("instagram")) {
                    imgUrl = null
                }
                val bookmarkId = bookmarkDao.insertWithReturn(
                    BookmarkEntity(
                        url = b.url,
                        title = b.title,
                        description = b.description,
                        imageUrl = imgUrl,
                        createdAt = b.createdAt,
                        isHidden = b.isHidden,
                        isPinned = b.isPinned,
                        note = b.note,
                        reminderTime = b.reminderTime
                    )
                )

                b.tags.forEach { tagName ->
                    val tagId = tagNameToId[tagName]
                    if (tagId != null) {
                        tagDao.addTagToBookmark(BookmarkTagCrossRef(bookmarkId, tagId))
                    }
                }
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
