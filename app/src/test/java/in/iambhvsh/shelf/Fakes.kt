package `in`.iambhvsh.shelf

import android.content.Context
import android.content.ContextWrapper
import android.database.AbstractCursor
import android.database.Cursor
import androidx.room.InvalidationTracker
import androidx.sqlite.db.SupportSQLiteDatabase
import java.lang.reflect.Proxy
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.data.BookmarkCollectionCrossRef
import `in`.iambhvsh.shelf.data.BookmarkDatabase
import `in`.iambhvsh.shelf.data.BookmarkEntity
import `in`.iambhvsh.shelf.data.BookmarkRepository
import `in`.iambhvsh.shelf.data.BookmarkRepositoryImpl
import `in`.iambhvsh.shelf.data.BookmarkStatus
import `in`.iambhvsh.shelf.data.BookmarkTagCrossRef
import `in`.iambhvsh.shelf.data.Collection
import `in`.iambhvsh.shelf.data.CollectionEntity
import `in`.iambhvsh.shelf.data.CollectionWithBookmarks
import `in`.iambhvsh.shelf.data.SettingsRepository
import `in`.iambhvsh.shelf.data.Tag
import `in`.iambhvsh.shelf.data.TagEntity
import `in`.iambhvsh.shelf.data.UpdateManager
import `in`.iambhvsh.shelf.data.dao.BookmarkDao
import `in`.iambhvsh.shelf.data.dao.CollectionDao
import `in`.iambhvsh.shelf.data.dao.CollectionWithCount
import `in`.iambhvsh.shelf.data.dao.TagDao
import `in`.iambhvsh.shelf.features.backup.BackupManager
import `in`.iambhvsh.shelf.features.backup.BrowserImportResult
import `in`.iambhvsh.shelf.features.backup.JsonImportResult
import `in`.iambhvsh.shelf.features.fetch.LinkMetadataParser
import `in`.iambhvsh.shelf.features.fetch.LinkPreviewData
import `in`.iambhvsh.shelf.features.fetch.UrlUtils
import `in`.iambhvsh.shelf.features.reminder.ReminderManager
import `in`.iambhvsh.shelf.features.setting.ChangelogLoader
import `in`.iambhvsh.shelf.features.setting.TapAction
import `in`.iambhvsh.shelf.features.setting.ViewMode
import `in`.iambhvsh.shelf.theme.AccentColor
import `in`.iambhvsh.shelf.theme.ThemeMode
import `in`.iambhvsh.shelf.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class FakeContext : ContextWrapper(null) {
    override fun getApplicationContext(): Context = this
    override fun getSystemService(name: String): Any? = null
}

class InMemoryDaoStore {
    var nextBookmarkId = 1L
    var nextCollectionId = 1L
    var nextTagId = 1L

    val bookmarksState = MutableStateFlow<List<BookmarkEntity>>(emptyList())
    val collectionsState = MutableStateFlow<List<CollectionEntity>>(emptyList())
    val tagsState = MutableStateFlow<List<TagEntity>>(emptyList())
    val collectionCrossRefsState = MutableStateFlow<List<BookmarkCollectionCrossRef>>(emptyList())
    val tagCrossRefsState = MutableStateFlow<List<BookmarkTagCrossRef>>(emptyList())

    val hideBatchSizes = mutableListOf<Int>()
    val unhideBatchSizes = mutableListOf<Int>()
    val purgeBatchSizes = mutableListOf<Int>()
    val addToCollectionBatchSizes = mutableListOf<Int>()

    fun clearAll() {
        bookmarksState.value = emptyList()
        collectionsState.value = emptyList()
        tagsState.value = emptyList()
        collectionCrossRefsState.value = emptyList()
        tagCrossRefsState.value = emptyList()
    }

    val bookmarkDao: BookmarkDao = object : BookmarkDao {
        override suspend fun insert(bookmarkEntity: BookmarkEntity) {
            insertWithReturn(bookmarkEntity)
        }

        override suspend fun insertWithReturn(bookmarkEntity: BookmarkEntity): Long {
            val id = if (bookmarkEntity.id > 0L) bookmarkEntity.id else nextBookmarkId++
            val stored = bookmarkEntity.copy(id = id)
            bookmarksState.value = bookmarksState.value.filterNot { it.id == id } + stored
            return id
        }

        override suspend fun existsByUrl(url: String): Boolean {
            val key = UrlUtils.normalizeUrlKey(url)
            return bookmarksState.value.any { !it.isHidden && UrlUtils.normalizeUrlKey(it.url) == key }
        }

        override fun getBookmarks(): Flow<List<BookmarkEntity>> =
            bookmarksState.map { list -> list.filter { !it.isHidden } }

        override fun getAllBookmarks(): Flow<List<BookmarkEntity>> = bookmarksState

        override suspend fun deleteBookmark(bookmarkEntity: BookmarkEntity) {
            bookmarksState.value = bookmarksState.value.filterNot { it.id == bookmarkEntity.id }
        }

        override fun searchBookmarks(searchQuery: String): Flow<List<BookmarkEntity>> =
            bookmarksState.map { list ->
                list.filter { bm ->
                    !bm.isHidden && (
                        (bm.title?.contains(searchQuery, ignoreCase = true) == true) ||
                            bm.url.contains(searchQuery, ignoreCase = true) ||
                            (bm.description?.contains(searchQuery, ignoreCase = true) == true) ||
                            (bm.note?.contains(searchQuery, ignoreCase = true) == true)
                        )
                }
            }

        override fun searchBookmarksWithTags(searchQuery: String, tagIds: List<Long>): Flow<List<BookmarkEntity>> =
            combine(bookmarksState, tagCrossRefsState) { list, refs ->
                val matchingIds = refs.filter { it.tagId in tagIds }.map { it.bookmarkId }.toSet()
                list.filter { bm ->
                    !bm.isHidden && bm.id in matchingIds && (
                        (bm.title?.contains(searchQuery, ignoreCase = true) == true) ||
                            bm.url.contains(searchQuery, ignoreCase = true) ||
                            (bm.description?.contains(searchQuery, ignoreCase = true) == true) ||
                            (bm.note?.contains(searchQuery, ignoreCase = true) == true)
                        )
                }
            }

        override suspend fun hideBookmarks(ids: List<Long>) {
            hideBatchSizes.add(ids.size)
            val idSet = ids.toSet()
            bookmarksState.value = bookmarksState.value.map {
                if (it.id in idSet) it.copy(isHidden = true) else it
            }
        }

        override suspend fun updateNote(id: Long, note: String?) {
            bookmarksState.value = bookmarksState.value.map {
                if (it.id == id) it.copy(note = note) else it
            }
        }

        override suspend fun updateReminderTime(id: Long, reminderTime: Long?) {
            bookmarksState.value = bookmarksState.value.map {
                if (it.id == id) it.copy(reminderTime = reminderTime) else it
            }
        }

        override suspend fun unhideBookmarks(ids: List<Long>) {
            unhideBatchSizes.add(ids.size)
            val idSet = ids.toSet()
            bookmarksState.value = bookmarksState.value.map {
                if (it.id in idSet) it.copy(isHidden = false) else it
            }
        }

        override suspend fun updatePinStatus(id: Long, isPinned: Boolean) {
            bookmarksState.value = bookmarksState.value.map {
                if (it.id == id) it.copy(isPinned = isPinned) else it
            }
        }

        override suspend fun findHiddenByUrl(url: String): BookmarkEntity? {
            val key = UrlUtils.normalizeUrlKey(url)
            return bookmarksState.value.firstOrNull { it.isHidden && UrlUtils.normalizeUrlKey(it.url) == key }
        }

        override suspend fun findByUrl(url: String): BookmarkEntity? {
            val key = UrlUtils.normalizeUrlKey(url)
            return bookmarksState.value
                .filter { UrlUtils.normalizeUrlKey(it.url) == key }
                .sortedWith(compareBy<BookmarkEntity> { it.isHidden }.thenByDescending { it.id })
                .firstOrNull()
        }

        override suspend fun updateTitleAndDescription(id: Long, title: String?, description: String?) {
            bookmarksState.value = bookmarksState.value.map {
                if (it.id == id) it.copy(title = title, description = description) else it
            }
        }

        override suspend fun getBookmarkById(id: Long): BookmarkEntity? =
            bookmarksState.value.firstOrNull { it.id == id }

        override fun getBookmarksByTags(tagIds: List<Long>): Flow<List<BookmarkEntity>> =
            combine(bookmarksState, tagCrossRefsState) { list, refs ->
                val ids = refs.filter { it.tagId in tagIds }.map { it.bookmarkId }.toSet()
                list.filter { !it.isHidden && it.id in ids }
            }

        override suspend fun unhideBookmark(
            id: Long,
            title: String?,
            description: String?,
            imageUrl: String?,
            createdAt: Long
        ) {
            bookmarksState.value = bookmarksState.value.map {
                if (it.id == id) {
                    it.copy(
                        isHidden = false,
                        title = title,
                        description = description,
                        imageUrl = imageUrl,
                        createdAt = createdAt,
                        isPinned = false,
                        note = null,
                        reminderTime = null
                    )
                } else it
            }
        }

        override suspend fun updateTitle(id: Long, title: String?) {
            bookmarksState.value = bookmarksState.value.map {
                if (it.id == id) it.copy(title = title) else it
            }
        }

        override suspend fun updateImageUrl(id: Long, imageUrl: String?) {
            bookmarksState.value = bookmarksState.value.map {
                if (it.id == id) it.copy(imageUrl = imageUrl) else it
            }
        }

        override suspend fun getBookmarksOnce(): List<BookmarkEntity> =
            bookmarksState.value.filter { !it.isHidden }

        override suspend fun getAllBookmarksOnce(): List<BookmarkEntity> = bookmarksState.value

        override suspend fun restoreBookmark(
            id: Long,
            title: String?,
            description: String?,
            imageUrl: String?,
            createdAt: Long,
            isPinned: Boolean,
            note: String?,
            reminderTime: Long?
        ) {
            bookmarksState.value = bookmarksState.value.map {
                if (it.id == id) {
                    it.copy(
                        isHidden = false,
                        title = title,
                        description = description,
                        imageUrl = imageUrl,
                        createdAt = createdAt,
                        isPinned = isPinned,
                        note = note,
                        reminderTime = reminderTime
                    )
                } else it
            }
        }

        override suspend fun updateExistingBookmarkMetadata(
            id: Long,
            title: String?,
            description: String?,
            imageUrl: String?,
            isPinned: Boolean,
            note: String?,
            reminderTime: Long?
        ) {
            bookmarksState.value = bookmarksState.value.map {
                if (it.id == id) {
                    it.copy(
                        title = title?.takeIf { s -> s.isNotEmpty() } ?: it.title,
                        description = description?.takeIf { s -> s.isNotEmpty() } ?: it.description,
                        imageUrl = imageUrl?.takeIf { s -> s.isNotEmpty() } ?: it.imageUrl,
                        isPinned = if (isPinned) true else it.isPinned,
                        note = note?.takeIf { s -> s.isNotEmpty() } ?: it.note,
                        reminderTime = reminderTime ?: it.reminderTime
                    )
                } else it
            }
        }

        override suspend fun getBookmarksWithoutImageOnce(): List<BookmarkEntity> =
            bookmarksState.value.filter { !it.isHidden && it.imageUrl.isNullOrEmpty() }

        override fun getUnassignedBookmarks(): Flow<List<BookmarkEntity>> =
            combine(bookmarksState, collectionsState, collectionCrossRefsState) { list, cols, refs ->
                val validColIds = cols.map { it.id }.toSet()
                val assigned = refs
                    .filter { it.collectionId in validColIds }
                    .map { it.bookmarkId }
                    .toSet()
                list.filter { !it.isHidden && it.id !in assigned }
            }

        override fun getTrashedBookmarks(): Flow<List<BookmarkEntity>> =
            bookmarksState.map { list -> list.filter { it.isHidden }.sortedByDescending { it.createdAt } }

        override fun getTrashedCount(): Flow<Int> =
            bookmarksState.map { list -> list.count { it.isHidden } }

        override fun getTrashedCountForCollection(collectionId: Long): Flow<Int> =
            combine(bookmarksState, collectionCrossRefsState) { list, refs ->
                val ids = refs
                    .filter { it.collectionId == collectionId }
                    .map { it.bookmarkId }
                    .toSet()
                list.count { it.isHidden && it.id in ids }
            }

        override fun getUnassignedTrashedCount(): Flow<Int> =
            combine(bookmarksState, collectionsState, collectionCrossRefsState) { list, cols, refs ->
                val validColIds = cols.map { it.id }.toSet()
                val assigned = refs
                    .filter { it.collectionId in validColIds }
                    .map { it.bookmarkId }
                    .toSet()
                list.count { it.isHidden && it.id !in assigned }
            }

        override suspend fun restoreBookmarksInCollection(collectionId: Long) {
            val ids = collectionCrossRefsState.value
                .filter { it.collectionId == collectionId }
                .map { it.bookmarkId }
                .toSet()
            bookmarksState.value = bookmarksState.value.map {
                if (it.id in ids && it.isHidden) it.copy(isHidden = false) else it
            }
        }

        override suspend fun restoreUnassignedBookmarks() {
            val validColIds = collectionsState.value.map { it.id }.toSet()
            val assigned = collectionCrossRefsState.value
                .filter { it.collectionId in validColIds }
                .map { it.bookmarkId }
                .toSet()
            bookmarksState.value = bookmarksState.value.map {
                if (it.id !in assigned && it.isHidden) it.copy(isHidden = false) else it
            }
        }

        override suspend fun deleteBookmarksPermanently(ids: List<Long>) {
            purgeBatchSizes.add(ids.size)
            val idSet = ids.toSet()
            bookmarksState.value = bookmarksState.value.filterNot { it.id in idSet }
        }

        override suspend fun deleteBookmarkCollectionCrossRefs(ids: List<Long>) {
            val idSet = ids.toSet()
            collectionCrossRefsState.value = collectionCrossRefsState.value.filterNot { it.bookmarkId in idSet }
        }

        override suspend fun deleteBookmarkTagCrossRefs(ids: List<Long>) {
            val idSet = ids.toSet()
            tagCrossRefsState.value = tagCrossRefsState.value.filterNot { it.bookmarkId in idSet }
        }

        override suspend fun emptyTrashCollectionCrossRefs() {
            val hiddenIds = bookmarksState.value.filter { it.isHidden }.map { it.id }.toSet()
            collectionCrossRefsState.value = collectionCrossRefsState.value.filterNot { it.bookmarkId in hiddenIds }
        }

        override suspend fun emptyTrashTagCrossRefs() {
            val hiddenIds = bookmarksState.value.filter { it.isHidden }.map { it.id }.toSet()
            tagCrossRefsState.value = tagCrossRefsState.value.filterNot { it.bookmarkId in hiddenIds }
        }

        override suspend fun emptyTrashBookmarks() {
            bookmarksState.value = bookmarksState.value.filterNot { it.isHidden }
        }
    }

    val collectionDao: CollectionDao = object : CollectionDao {
        override suspend fun insertCollection(collection: CollectionEntity): Long {
            val id = if (collection.id > 0L) collection.id else nextCollectionId++
            val stored = collection.copy(id = id)
            collectionsState.value = collectionsState.value.filterNot { it.id == id } + stored
            return id
        }

        override suspend fun deleteCollection(collection: CollectionEntity) {
            collectionsState.value = collectionsState.value.filterNot { it.id == collection.id }
        }

        override suspend fun deleteCrossRefsForCollection(collectionId: Long) {
            collectionCrossRefsState.value = collectionCrossRefsState.value.filterNot { it.collectionId == collectionId }
        }

        override suspend fun updateName(id: Long, name: String) {
            collectionsState.value = collectionsState.value.map {
                if (it.id == id) it.copy(name = name) else it
            }
        }

        override fun getAllCollections(): Flow<List<CollectionWithCount>> =
            combine(collectionsState, bookmarksState, collectionCrossRefsState) { cols, bms, refs ->
                cols.sortedByDescending { it.createdAt }.map { c ->
                    val bmIds = refs.filter { it.collectionId == c.id }.map { it.bookmarkId }.toSet()
                    val visible = bms.filter { !it.isHidden && it.id in bmIds }
                    val previews = visible.mapNotNull { it.imageUrl?.takeIf { url -> url.isNotEmpty() } }
                        .take(4)
                        .joinToString("|||")
                        .ifEmpty { null }
                    CollectionWithCount(c.id, c.name, c.createdAt, visible.size, previews)
                }
            }

        override fun getCollectionWithBookmarks(collectionId: Long): Flow<CollectionWithBookmarks?> =
            combine(collectionsState, bookmarksState, collectionCrossRefsState) { cols, bms, refs ->
                val c = cols.firstOrNull { it.id == collectionId } ?: return@combine null
                val ids = refs.filter { it.collectionId == collectionId }.map { it.bookmarkId }.toSet()
                CollectionWithBookmarks(c, bms.filter { it.id in ids })
            }

        override fun getBookmarksForCollection(collectionId: Long): Flow<List<BookmarkEntity>> =
            combine(bookmarksState, collectionCrossRefsState) { bms, refs ->
                val ids = refs
                    .filter { it.collectionId == collectionId }
                    .map { it.bookmarkId }
                    .toSet()
                bms.filter { !it.isHidden && it.id in ids }.sortedByDescending { it.createdAt }
            }

        override suspend fun addBookmarkToCollection(crossRef: BookmarkCollectionCrossRef) {
            val current = collectionCrossRefsState.value
            if (current.none { it.bookmarkId == crossRef.bookmarkId && it.collectionId == crossRef.collectionId }) {
                collectionCrossRefsState.value = current + crossRef
            }
        }

        override suspend fun addBookmarksToCollection(crossRefs: List<BookmarkCollectionCrossRef>) {
            addToCollectionBatchSizes.add(crossRefs.size)
            val current = collectionCrossRefsState.value.toMutableList()
            for (ref in crossRefs) {
                if (current.none { it.bookmarkId == ref.bookmarkId && it.collectionId == ref.collectionId }) {
                    current.add(ref)
                }
            }
            collectionCrossRefsState.value = current
        }

        override suspend fun removeBookmarkFromCollection(crossRef: BookmarkCollectionCrossRef) {
            collectionCrossRefsState.value = collectionCrossRefsState.value.filterNot {
                it.bookmarkId == crossRef.bookmarkId && it.collectionId == crossRef.collectionId
            }
        }

        override fun getAllCollectionsRaw(): Flow<List<CollectionEntity>> =
            collectionsState.map { it.sortedByDescending { c -> c.createdAt } }

        override suspend fun getBookmarkUrlsForCollection(collectionId: Long): List<String> {
            val ids = collectionCrossRefsState.value.filter { it.collectionId == collectionId }.map { it.bookmarkId }.toSet()
            return bookmarksState.value.filter { !it.isHidden && it.id in ids }.map { it.url }
        }

        override suspend fun getAllBookmarkUrlsForCollection(collectionId: Long): List<String> {
            val ids = collectionCrossRefsState.value.filter { it.collectionId == collectionId }.map { it.bookmarkId }.toSet()
            return bookmarksState.value.filter { it.id in ids }.map { it.url }
        }

        override suspend fun getCollectionIdForBookmark(bookmarkId: Long): Long? =
            collectionCrossRefsState.value.firstOrNull { it.bookmarkId == bookmarkId }?.collectionId

        override fun getCollectionCrossRefCount(): Flow<Int> =
            collectionCrossRefsState.map { it.size }

        override fun getUnassignedCollectionStats(): Flow<CollectionWithCount> =
            combine(bookmarksState, collectionsState, collectionCrossRefsState) { bms, cols, refs ->
                val validColIds = cols.map { it.id }.toSet()
                val assigned = refs
                    .filter { it.collectionId in validColIds }
                    .map { it.bookmarkId }
                    .toSet()
                val unassigned = bms.filter { !it.isHidden && it.id !in assigned }
                val previews = unassigned.mapNotNull { it.imageUrl?.takeIf { u -> u.isNotEmpty() } }
                    .take(4)
                    .joinToString("|||")
                    .ifEmpty { null }
                CollectionWithCount(Collection.UNCATEGORISED_ID, "Uncategorised", 0L, unassigned.size, previews)
            }
    }

    val tagDao: TagDao = object : TagDao {
        override suspend fun insertTag(tag: TagEntity): Long {
            val existing = tagsState.value.firstOrNull { it.name == tag.name }
            if (existing != null) return -1L
            val id = if (tag.id > 0L) tag.id else nextTagId++
            tagsState.value = (tagsState.value + tag.copy(id = id)).sortedBy { it.name }
            return id
        }

        override suspend fun findByName(name: String): TagEntity? =
            tagsState.value.firstOrNull { it.name == name }

        override fun getAllTags(): Flow<List<TagEntity>> = tagsState

        override suspend fun getAllTagsOnce(): List<TagEntity> = tagsState.value

        override suspend fun addTagToBookmark(crossRef: BookmarkTagCrossRef) {
            val current = tagCrossRefsState.value
            if (current.none { it.bookmarkId == crossRef.bookmarkId && it.tagId == crossRef.tagId }) {
                tagCrossRefsState.value = current + crossRef
            }
        }

        override suspend fun removeTagFromBookmark(bookmarkId: Long, tagId: Long) {
            tagCrossRefsState.value = tagCrossRefsState.value.filterNot {
                it.bookmarkId == bookmarkId && it.tagId == tagId
            }
        }

        override fun getTagsForBookmark(bookmarkId: Long): Flow<List<TagEntity>> =
            combine(tagCrossRefsState, tagsState) { refs, tags ->
                val tagIds = refs.filter { it.bookmarkId == bookmarkId }.map { it.tagId }.toSet()
                tags.filter { it.id in tagIds }
            }

        override suspend fun getTagsForBookmarkOnce(bookmarkId: Long): List<TagEntity> {
            val tagIds = tagCrossRefsState.value.filter { it.bookmarkId == bookmarkId }.map { it.tagId }.toSet()
            return tagsState.value.filter { it.id in tagIds }
        }

        override fun getBookmarksForTag(tagId: Long): Flow<List<Long>> =
            combine(tagCrossRefsState, bookmarksState) { refs, bms ->
                val visibleIds = bms.filter { !it.isHidden }.map { it.id }.toSet()
                refs.filter { it.tagId == tagId && it.bookmarkId in visibleIds }.map { it.bookmarkId }
            }

        override fun getTagCrossRefCount(): Flow<Int> = tagCrossRefsState.map { it.size }

        override suspend fun deleteTagCrossRefs(tagId: Long) {
            tagCrossRefsState.value = tagCrossRefsState.value.filterNot { it.tagId == tagId }
        }

        override suspend fun deleteTag(tagId: Long) {
            tagsState.value = tagsState.value.filterNot { it.id == tagId }
        }
    }
}

class ControllableBookmarkRepository(
    val store: InMemoryDaoStore = InMemoryDaoStore(),
    private val delegate: BookmarkRepositoryImpl = BookmarkRepositoryImpl(
        store.bookmarkDao,
        store.collectionDao,
        store.tagDao
    )
) : BookmarkRepository by delegate {
    var forceInsertReturn: Boolean? = null
    var forceHideError: Throwable? = null
    var onBeforeSaveReturnId: (suspend () -> Unit)? = null

    override suspend fun insert(bookmark: Bookmark): Boolean {
        forceInsertReturn?.let { return it }
        return delegate.insert(bookmark)
    }

    override suspend fun saveAndReturnId(bookmark: Bookmark): Long {
        onBeforeSaveReturnId?.invoke()
        return delegate.saveAndReturnId(bookmark)
    }

    override suspend fun hideBookmarks(ids: List<Long>) {
        forceHideError?.let { throw it }
        delegate.hideBookmarks(ids)
    }
}

class FakeSettingsRepository : SettingsRepository {
    @JvmField var themeMode: ThemeMode = ThemeMode.SYSTEM
    @JvmField var tapAction: TapAction = TapAction.SHOW_PREVIEW
    @JvmField var dynamicColor: Boolean = true
    @JvmField var accentColor: AccentColor = AccentColor.PERIWINKLE
    @JvmField var viewMode: ViewMode = ViewMode.GRID
    @JvmField var autoBackupEnabled: Boolean = false
    @JvmField var autoBackupUri: String? = null
    @JvmField var appLockEnabled: Boolean = false
    @JvmField var appLockUsePinEnabled: Boolean = false
    @JvmField var appLockTimeout: Long = 0L
    @JvmField var lastUpdateCheckTime: Long = 0L
    @JvmField var latestAvailableVersion: String? = null
    @JvmField var latestReleaseUrl: String? = null
    @JvmField var firstLaunchPermissionsShown: Boolean = false

    override fun getThemeMode() = themeMode
    override fun setThemeMode(mode: ThemeMode) { themeMode = mode }
    override fun getTapAction() = tapAction
    override fun setTapAction(action: TapAction) { tapAction = action }
    override fun getDynamicColor() = dynamicColor
    override fun setDynamicColor(enabled: Boolean) { dynamicColor = enabled }
    override fun getAccentColor() = accentColor
    override fun setAccentColor(color: AccentColor) { accentColor = color }
    override fun getViewMode() = viewMode
    override fun setViewMode(mode: ViewMode) { viewMode = mode }
    override fun getAutoBackupEnabled() = autoBackupEnabled
    override fun setAutoBackupEnabled(enabled: Boolean) { autoBackupEnabled = enabled }
    override fun getAutoBackupUri() = autoBackupUri
    override fun setAutoBackupUri(uri: String?) { autoBackupUri = uri }
    override fun getAppLockEnabled() = appLockEnabled
    override fun setAppLockEnabled(enabled: Boolean) { appLockEnabled = enabled }
    override fun getAppLockUsePinEnabled() = appLockUsePinEnabled
    override fun setAppLockUsePinEnabled(enabled: Boolean) { appLockUsePinEnabled = enabled }
    override fun getAppLockTimeout() = appLockTimeout
    override fun setAppLockTimeout(timeout: Long) { appLockTimeout = timeout }
    override fun getLastUpdateCheckTime() = lastUpdateCheckTime
    override fun setLastUpdateCheckTime(time: Long) { lastUpdateCheckTime = time }
    override fun getLatestAvailableVersion() = latestAvailableVersion
    override fun setLatestAvailableVersion(version: String?) { latestAvailableVersion = version }
    override fun getLatestReleaseUrl() = latestReleaseUrl
    override fun setLatestReleaseUrl(url: String?) { latestReleaseUrl = url }
    override fun isFirstLaunchPermissionsShown() = firstLaunchPermissionsShown
    override fun setFirstLaunchPermissionsShown(shown: Boolean) { firstLaunchPermissionsShown = shown }

    override fun resetToDefaults() {
        themeMode = ThemeMode.SYSTEM
        tapAction = TapAction.SHOW_PREVIEW
        dynamicColor = true
        accentColor = AccentColor.PERIWINKLE
        viewMode = ViewMode.GRID
        autoBackupEnabled = false
        autoBackupUri = null
        appLockEnabled = false
        appLockUsePinEnabled = false
        appLockTimeout = 0L
        lastUpdateCheckTime = 0L
        latestAvailableVersion = null
        latestReleaseUrl = null
    }
}

class FakeReminderManager : ReminderManager(FakeContext()) {
    val scheduled = mutableListOf<Pair<Long, Long>>()
    val cancelled = mutableListOf<Long>()

    override fun scheduleReminder(bookmarkId: Long, timeInMillis: Long) {
        scheduled.add(bookmarkId to timeInMillis)
    }

    override fun cancelReminder(bookmarkId: Long) {
        cancelled.add(bookmarkId)
    }
}

class FakeUpdateManager(
    var hasUpdateResult: Boolean = false
) : UpdateManager {
    var forceCheckCalls = 0
    var autoCheckCalls = 0
    var downloadAndInstallCalls = 0

    override suspend fun checkForUpdates(force: Boolean): Boolean {
        if (force) forceCheckCalls++ else autoCheckCalls++
        return hasUpdateResult
    }

    override fun downloadAndInstallUpdate() {
        downloadAndInstallCalls++
    }
}

class FakeLinkMetadataParser(
    private val responder: suspend (String) -> LinkPreviewData? = { url ->
        LinkPreviewData(
            title = "Parsed Title",
            description = "Parsed Description",
            imageUrl = "https://example.com/image.png",
            url = url
        )
    }
) : LinkMetadataParser() {
    val parsedUrls = mutableListOf<String>()

    override suspend fun parse(url: String): LinkPreviewData? {
        parsedUrls.add(url)
        return responder(url)
    }
}

class FakeBookmarkDatabase(
    val store: InMemoryDaoStore = InMemoryDaoStore()
) : BookmarkDatabase() {
    var clearedTablesCount = 0

    override fun bookmarkDao(): BookmarkDao = store.bookmarkDao
    override fun collectionDao(): CollectionDao = store.collectionDao
    override fun tagDao(): TagDao = store.tagDao

    override fun createInvalidationTracker(): InvalidationTracker {
        return InvalidationTracker(
            this,
            emptyMap(),
            emptyMap(),
            "bookmarks",
            "collections",
            "bookmark_collection_cross_ref",
            "tags",
            "bookmark_tag_cross_ref"
        )
    }

    override fun clearAllTables() {
        clearedTablesCount++
        store.clearAll()
    }
}

class FakeBackupManager(
    private val database: BookmarkDatabase,
    private val store: InMemoryDaoStore,
    settingsRepository: SettingsRepository = FakeSettingsRepository()
) : BackupManager(
    database = database,
    bookmarkDao = store.bookmarkDao,
    collectionDao = store.collectionDao,
    tagDao = store.tagDao,
    settingsRepository = settingsRepository,
    context = FakeContext()
) {
    var autoBackupStarted = false
    var autoBackupStopped = false
    var refreshTimeCalls = 0
    var refreshThumbnailsCalls = 0
    var jsonToExport: String? = null
    var htmlToExport: String? = null
    var jsonImportError: Throwable? = null
    var browserImportError: Throwable? = null

    override fun refreshLastBackupTime() {
        refreshTimeCalls++
    }

    override fun startAutoBackup() {
        autoBackupStarted = true
    }

    override fun stopAutoBackup() {
        autoBackupStopped = true
    }

    override suspend fun generateBackupJson(): String {
        return jsonToExport ?: super.generateBackupJson()
    }

    override suspend fun generateExportHtml(): String {
        return htmlToExport ?: super.generateExportHtml()
    }

    override suspend fun importFromJson(jsonString: String): JsonImportResult {
        jsonImportError?.let { throw it }
        return `in`.iambhvsh.shelf.features.backup.importBackupFromJson(
            jsonString = jsonString,
            json = kotlinx.serialization.json.Json {
                prettyPrint = false
                ignoreUnknownKeys = true
                encodeDefaults = true
            },
            bookmarkDao = store.bookmarkDao,
            collectionDao = store.collectionDao,
            tagDao = store.tagDao,
            database = null
        )
    }

    override suspend fun importFromBrowserBookmarks(html: String): BrowserImportResult {
        browserImportError?.let { throw it }
        return `in`.iambhvsh.shelf.features.backup.importBookmarksFromHtml(
            html = html,
            bookmarkDao = store.bookmarkDao,
            collectionDao = store.collectionDao,
            database = null
        )
    }

    override fun refreshImportedThumbnailsAsync(checkExistingImages: Boolean) {
        refreshThumbnailsCalls++
    }
}

class FakeChangelogLoader(
    private val content: String = "## 2.0.5\n- Improvements"
) : ChangelogLoader(FakeContext()) {
    override fun load(): String = content
}

class FakeStatusCursor(
    private val columnNames: Array<String>,
    private val rows: List<List<Any?>>
) {
    var wasClosed = false
        private set
    private var position = -1

    val cursor: Cursor = Proxy.newProxyInstance(
        Cursor::class.java.classLoader,
        arrayOf(Cursor::class.java)
    ) { _, method, args ->
        when (method.name) {
            "moveToFirst" -> {
                if (rows.isEmpty()) {
                    position = -1
                    false
                } else {
                    position = 0
                    true
                }
            }
            "moveToNext" -> {
                if (position + 1 < rows.size) {
                    position++
                    true
                } else {
                    position = rows.size
                    false
                }
            }
            "getCount" -> rows.size
            "getColumnIndex" -> columnNames.indexOf(args?.firstOrNull() as? String)
            "getColumnNames" -> columnNames
            "getString" -> rows[position][(args!![0] as Int)]?.toString()
            "getInt" -> (rows[position][(args!![0] as Int)] as Number).toInt()
            "getLong" -> (rows[position][(args!![0] as Int)] as Number).toLong()
            "isNull" -> rows[position][(args!![0] as Int)] == null
            "isClosed" -> wasClosed
            "close" -> {
                wasClosed = true
                Unit
            }
            else -> null
        }
    } as Cursor
}

class RecordingSQLiteDatabase(
    private val preExistingTables: Map<String, List<String>> = emptyMap()
) {
    val executedSql = mutableListOf<String>()
    val queriedCursors = mutableListOf<FakeStatusCursor>()

    val db: SupportSQLiteDatabase = Proxy.newProxyInstance(
        SupportSQLiteDatabase::class.java.classLoader,
        arrayOf(SupportSQLiteDatabase::class.java)
    ) { _, method, args ->
        when (method.name) {
            "execSQL" -> {
                val sql = args?.firstOrNull() as? String
                if (sql != null) executedSql.add(sql)
                Unit
            }
            "query" -> {
                val raw = args?.firstOrNull()
                val query = when (raw) {
                    is String -> raw
                    else -> ""
                }
                val tableMatch = Regex("""PRAGMA\s+table_info\([`']?([^`'\)]+)[`']?\)""", RegexOption.IGNORE_CASE).find(query)
                val tableName = tableMatch?.groupValues?.get(1)
                val cols = tableName?.let { preExistingTables[it] }.orEmpty()
                val fakeCursor = FakeStatusCursor(
                    columnNames = arrayOf("name"),
                    rows = cols.map { listOf(it) }
                )
                queriedCursors.add(fakeCursor)
                fakeCursor.cursor
            }
            "isOpen", "isDatabaseIntegrityOk" -> true
            "isReadOnly", "inTransaction", "isDbLockedByCurrentThread", "yieldIfContendedSafely",
            "needUpgrade", "enableWriteAheadLogging", "isWriteAheadLoggingEnabled" -> false
            "getVersion" -> 1
            "getMaximumSize", "getPageSize", "insert" -> 0L
            "setMaximumSize" -> (args?.firstOrNull() as? Long) ?: 0L
            "delete", "update" -> 0
            "getPath" -> ":memory:"
            "getAttachedDbs" -> emptyList<Any>()
            else -> null
        }
    } as SupportSQLiteDatabase
}
