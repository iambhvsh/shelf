package `in`.iambhvsh.shelf.data

import `in`.iambhvsh.shelf.features.setting.TapAction
import `in`.iambhvsh.shelf.features.setting.ViewMode
import `in`.iambhvsh.shelf.theme.AccentColor
import `in`.iambhvsh.shelf.theme.ThemeMode
import `in`.iambhvsh.shelf.util.Resource
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    suspend fun insert(bookmark: Bookmark): Boolean
    suspend fun saveAndReturnId(bookmark: Bookmark): Long
    suspend fun deleteBookmark(bookmark: Bookmark)
    fun getAllBookmarks(): Flow<Resource<List<Bookmark>>>
    fun getBookmarks(): Flow<Resource<List<Bookmark>>>
    fun getBookmarksByTags(tagIds: List<Long>): Flow<Resource<List<Bookmark>>>
    suspend fun existsByUrl(url: String): Boolean
    suspend fun findBookmarkStatusByUrl(url: String): BookmarkStatus?
    suspend fun getBookmarksWithoutImage(): List<Bookmark>
    suspend fun hideBookmarks(ids: List<Long>)
    suspend fun getBookmarkById(id: Long): Bookmark?
    suspend fun searchBookmarks(text: String): Flow<Resource<List<Bookmark>>>
    suspend fun searchBookmarksWithTags(text: String, tagIds: List<Long>): Flow<Resource<List<Bookmark>>>
    suspend fun togglePinStatus(id: Long, isPinned: Boolean)
    suspend fun updateNote(id: Long, note: String?)
    suspend fun updateReminderTime(id: Long, reminderTime: Long?)
    suspend fun updateBookmarkTitle(id: Long, title: String?)
    suspend fun updateBookmarkDetails(id: Long, title: String?, description: String?)

    fun getAllTags(): Flow<Resource<List<Tag>>>
    suspend fun insertTag(name: String): Long
    suspend fun deleteTag(tagId: Long)
    suspend fun addTagToBookmark(bookmarkId: Long, tagId: Long)
    suspend fun removeTagFromBookmark(bookmarkId: Long, tagId: Long)
    fun getTagsForBookmark(bookmarkId: Long): Flow<Resource<List<Tag>>>
    fun getBookmarksForTag(tagId: Long): Flow<Resource<List<Long>>>

    suspend fun createCollection(name: String): Long
    suspend fun updateCollectionName(id: Long, name: String)
    suspend fun deleteCollection(collection: Collection)
    fun getAllCollections(): Flow<Resource<List<Collection>>>
    fun getBookmarksInCollection(collectionId: Long): Flow<Resource<List<Bookmark>>>
    suspend fun addBookmarkToCollection(bookmarkId: Long, collectionId: Long)
    suspend fun addBookmarksToCollection(bookmarkIds: List<Long>, collectionId: Long)
    suspend fun removeBookmarkFromCollection(bookmarkId: Long, collectionId: Long)
    suspend fun getCollectionIdForBookmark(bookmarkId: Long): Long?
    suspend fun updateImageUrl(id: Long, imageUrl: String?)

    fun getTrashedBookmarks(): Flow<Resource<List<Bookmark>>>
    fun getTrashedCount(): Flow<Int>
    fun getTrashedCountForCollection(collectionId: Long): Flow<Int>
    suspend fun unhideBookmarks(ids: List<Long>)
    suspend fun restoreBookmarksInCollection(collectionId: Long)
    suspend fun permanentlyDeleteBookmarks(ids: List<Long>)
    suspend fun emptyTrash()
}

interface SettingsRepository {
    fun getThemeMode(): ThemeMode
    fun setThemeMode(mode: ThemeMode)
    fun getTapAction(): TapAction
    fun setTapAction(action: TapAction)
    fun getDynamicColor(): Boolean
    fun setDynamicColor(enabled: Boolean)
    fun getAccentColor(): AccentColor
    fun setAccentColor(color: AccentColor)
    fun getViewMode(): ViewMode
    fun setViewMode(mode: ViewMode)
    fun getAutoBackupEnabled(): Boolean
    fun setAutoBackupEnabled(enabled: Boolean)
    fun getAutoBackupUri(): String?
    fun setAutoBackupUri(uri: String?)
    fun getAppLockEnabled(): Boolean
    fun setAppLockEnabled(enabled: Boolean)
    fun getAppLockUsePinEnabled(): Boolean
    fun setAppLockUsePinEnabled(enabled: Boolean)
    fun getAppLockTimeout(): Long
    fun setAppLockTimeout(timeout: Long)
    fun getLastUpdateCheckTime(): Long
    fun setLastUpdateCheckTime(time: Long)
    fun getLatestAvailableVersion(): String?
    fun setLatestAvailableVersion(version: String?)
    fun getLatestReleaseUrl(): String?
    fun setLatestReleaseUrl(url: String?)
    fun isFirstLaunchPermissionsShown(): Boolean
    fun setFirstLaunchPermissionsShown(shown: Boolean)
    fun resetToDefaults()
}

interface UpdateManager {
    suspend fun checkForUpdates(force: Boolean = false): Boolean
    fun downloadAndInstallUpdate()
}
