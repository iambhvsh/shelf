package `in`.iambhvsh.shelf.domain.repository

import `in`.iambhvsh.shelf.domain.model.Bookmark
import `in`.iambhvsh.shelf.domain.model.Collection
import `in`.iambhvsh.shelf.domain.model.Tag
import `in`.iambhvsh.shelf.utils.Resource
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    suspend fun insert(bookmark: Bookmark): Boolean
    suspend fun deleteBookmark(bookmark: Bookmark)
    fun getAllBookmarks(): Flow<Resource<List<Bookmark>>>
    fun getBookmarks(): Flow<Resource<List<Bookmark>>>
    fun getBookmarksByTags(tagIds: List<Long>): Flow<Resource<List<Bookmark>>>
    suspend fun getBookmarksWithoutImage(): List<Bookmark>
    suspend fun hideBookmarks(ids: List<Long>)
    suspend fun searchBookmarks(text: String): Flow<Resource<List<Bookmark>>>
    suspend fun searchBookmarksWithTags(text: String, tagIds: List<Long>): Flow<Resource<List<Bookmark>>>
    suspend fun togglePinStatus(id: Long, isPinned: Boolean)
    suspend fun updateNote(id: Long, note: String?)
    suspend fun updateReminderTime(id: Long, reminderTime: Long?)

    fun getAllTags(): Flow<Resource<List<Tag>>>
    suspend fun insertTag(name: String): Long
    suspend fun deleteTag(tagId: Long)
    suspend fun addTagToBookmark(bookmarkId: Long, tagId: Long)
    suspend fun removeTagFromBookmark(bookmarkId: Long, tagId: Long)
    fun getTagsForBookmark(bookmarkId: Long): Flow<Resource<List<Tag>>>
    fun getBookmarksForTag(tagId: Long): Flow<Resource<List<Long>>>

    suspend fun createCollection(name: String): Long
    suspend fun deleteCollection(collection: Collection)
    fun getAllCollections(): Flow<Resource<List<Collection>>>
    fun getBookmarksInCollection(collectionId: Long): Flow<Resource<List<Bookmark>>>
    suspend fun addBookmarkToCollection(bookmarkId: Long, collectionId: Long)
    suspend fun addBookmarksToCollection(bookmarkIds: List<Long>, collectionId: Long)
    suspend fun removeBookmarkFromCollection(bookmarkId: Long, collectionId: Long)
    suspend fun updateImageUrl(id: Long, imageUrl: String?)
}
