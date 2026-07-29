package `in`.iambhvsh.shelf.data.local.repository

import `in`.iambhvsh.shelf.data.local.dao.BookmarkDao
import `in`.iambhvsh.shelf.data.local.dao.CollectionDao
import `in`.iambhvsh.shelf.data.local.dao.TagDao
import `in`.iambhvsh.shelf.data.local.entity.BookmarkCollectionCrossRef
import `in`.iambhvsh.shelf.data.local.entity.BookmarkTagCrossRef
import `in`.iambhvsh.shelf.data.local.entity.CollectionEntity
import `in`.iambhvsh.shelf.data.local.entity.TagEntity
import `in`.iambhvsh.shelf.data.local.mapper.toDomain
import `in`.iambhvsh.shelf.data.local.mapper.toEntity
import `in`.iambhvsh.shelf.domain.model.Bookmark
import `in`.iambhvsh.shelf.domain.model.Collection
import `in`.iambhvsh.shelf.domain.model.Tag
import `in`.iambhvsh.shelf.domain.repository.BookmarkRepository
import `in`.iambhvsh.shelf.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class BookmarkRepositoryImpl(
    private val dao: BookmarkDao,
    private val collectionDao: CollectionDao,
    private val tagDao: TagDao
) : BookmarkRepository {

    override suspend fun insert(bookmark: Bookmark): Boolean {
        return dao.insertOrUnhide(bookmark.toEntity())
    }

    override suspend fun deleteBookmark(bookmark: Bookmark) {
        dao.deleteBookmark(bookmark.toEntity())
    }

    override suspend fun getBookmarksWithoutImage(): List<Bookmark> {
        return dao.getBookmarksWithoutImageOnce().map { it.toDomain() }
    }

    override suspend fun hideBookmarks(ids: List<Long>) {
        dao.hideBookmarks(ids)
    }

    override suspend fun togglePinStatus(id: Long, isPinned: Boolean) {
        dao.updatePinStatus(id, isPinned)
    }

    override suspend fun updateNote(id: Long, note: String?) {
        dao.updateNote(id, note)
    }

    override suspend fun updateReminderTime(id: Long, reminderTime: Long?) {
        dao.updateReminderTime(id, reminderTime)
    }

    override fun getAllTags(): Flow<Resource<List<Tag>>> = flow {
        emit(Resource.Loading())
        try {
            tagDao.getAllTags().collect { tags ->
                emit(Resource.Success(tags.map { it.toDomain() }))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Failed to fetch tags: ${e.localizedMessage}"))
        }
    }

    override suspend fun insertTag(name: String): Long {
        return tagDao.insertTag(TagEntity(name = name))
    }

    override suspend fun deleteTag(tagId: Long) {
        tagDao.deleteTag(tagId)
    }

    override suspend fun addTagToBookmark(bookmarkId: Long, tagId: Long) {
        tagDao.addTagToBookmark(BookmarkTagCrossRef(bookmarkId, tagId))
    }

    override suspend fun removeTagFromBookmark(bookmarkId: Long, tagId: Long) {
        tagDao.removeTagFromBookmark(bookmarkId, tagId)
    }

    override fun getTagsForBookmark(bookmarkId: Long): Flow<Resource<List<Tag>>> = flow {
        emit(Resource.Loading())
        try {
            tagDao.getTagsForBookmark(bookmarkId).collect { tags ->
                emit(Resource.Success(tags.map { it.toDomain() }))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unknown Error"))
        }
    }

    override fun getBookmarksForTag(tagId: Long): Flow<Resource<List<Long>>> = flow {
        emit(Resource.Loading())
        try {
            tagDao.getBookmarksForTag(tagId).collect { list ->
                emit(Resource.Success(list))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unknown Error"))
        }
    }

    override suspend fun searchBookmarks(text: String): Flow<Resource<List<Bookmark>>> {
        return dao.searchBookmarks(text)
            .map { list -> Resource.Success(list.map { it.toDomain() }) as Resource<List<Bookmark>> }
            .onStart { emit(Resource.Loading()) }
            .catch { e -> emit(Resource.Error(e.message ?: "Unknown error")) }
    }

    override suspend fun searchBookmarksWithTags(text: String, tagIds: List<Long>): Flow<Resource<List<Bookmark>>> {
        return dao.searchBookmarksWithTags(text, tagIds)
            .map { list -> Resource.Success(list.map { it.toDomain() }) as Resource<List<Bookmark>> }
            .onStart { emit(Resource.Loading()) }
            .catch { e -> emit(Resource.Error(e.message ?: "Unknown error")) }
    }

    override fun getBookmarks(): Flow<Resource<List<Bookmark>>> {
        return dao.getBookmarks()
            .map { list -> Resource.Success(list.map { it.toDomain() }) as Resource<List<Bookmark>> }
            .onStart { emit(Resource.Loading()) }
            .catch { e -> emit(Resource.Error(e.message ?: "Unknown error")) }
    }

    override fun getAllBookmarks(): Flow<Resource<List<Bookmark>>> {
        return dao.getAllBookmarks()
            .map { list -> Resource.Success(list.map { it.toDomain() }) as Resource<List<Bookmark>> }
            .catch { emit(Resource.Error(it.localizedMessage ?: "Unknown Error")) }
    }

    override fun getBookmarksByTags(tagIds: List<Long>): Flow<Resource<List<Bookmark>>> {
        return dao.getBookmarksByTags(tagIds)
            .map { list -> Resource.Success(list.map { it.toDomain() }) as Resource<List<Bookmark>> }
            .catch { emit(Resource.Error(it.localizedMessage ?: "Unknown Error")) }
    }

    override suspend fun createCollection(name: String): Long {
        return collectionDao.insertCollection(
            CollectionEntity(name = name)
        )
    }

    override suspend fun deleteCollection(collection: Collection) {
        collectionDao.deleteCollection(collection.toEntity())
    }

    override fun getAllCollections(): Flow<Resource<List<Collection>>> {
        return collectionDao.getAllCollections()
            .map { list -> Resource.Success(list.map { it.toDomain() }) as Resource<List<Collection>> }
            .onStart { emit(Resource.Loading()) }
            .catch { e -> emit(Resource.Error(e.message ?: "Unknown error")) }
    }

    override fun getBookmarksInCollection(collectionId: Long): Flow<Resource<List<Bookmark>>> {
        return collectionDao.getCollectionWithBookmarks(collectionId)
            .map { result ->
                Resource.Success(result?.bookmarks?.map { it.toDomain() } ?: emptyList()) as Resource<List<Bookmark>>
            }
            .onStart { emit(Resource.Loading()) }
            .catch { e -> emit(Resource.Error(e.message ?: "Unknown error")) }
    }

    override suspend fun addBookmarkToCollection(bookmarkId: Long, collectionId: Long) {
        collectionDao.addBookmarkToCollection(BookmarkCollectionCrossRef(bookmarkId, collectionId))
    }

    override suspend fun addBookmarksToCollection(bookmarkIds: List<Long>, collectionId: Long) {
        collectionDao.addBookmarksToCollection(bookmarkIds.map { BookmarkCollectionCrossRef(it, collectionId) })
    }

    override suspend fun removeBookmarkFromCollection(bookmarkId: Long, collectionId: Long) {
        collectionDao.removeBookmarkFromCollection(BookmarkCollectionCrossRef(bookmarkId, collectionId))
    }

    override suspend fun updateImageUrl(id: Long, imageUrl: String?) {
        dao.updateImageUrl(id, imageUrl)
    }
}
