package `in`.iambhvsh.shelf.data.local.repository

import `in`.iambhvsh.shelf.data.local.dao.BookmarkDao
import `in`.iambhvsh.shelf.data.local.dao.CollectionDao
import `in`.iambhvsh.shelf.data.local.entity.BookmarkCollectionCrossRef
import `in`.iambhvsh.shelf.data.local.mapper.toDomain
import `in`.iambhvsh.shelf.data.local.mapper.toEntity
import `in`.iambhvsh.shelf.domain.model.Bookmark
import `in`.iambhvsh.shelf.domain.model.Collection
import `in`.iambhvsh.shelf.domain.repository.BookmarkRepository
import `in`.iambhvsh.shelf.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class BookmarkRepositoryImpl(
    private val dao: BookmarkDao,
    private val collectionDao: CollectionDao
) : BookmarkRepository {

    override suspend fun insert(bookmark: Bookmark): Boolean {
        return dao.insertOrUnhide(bookmark.toEntity())
    }

    override suspend fun getBookmarksWithoutImage(): List<Bookmark> {
        return dao.getBookmarksWithoutImageOnce().map { it.toDomain() }
    }

    override suspend fun hideBookmarks(ids: List<Long>) {
        dao.hideBookmarks(ids)
    }

    override suspend fun searchBookmarks(text: String): Flow<Resource<List<Bookmark>>> {
        return dao.searchBookmarks(text)
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

    override suspend fun createCollection(name: String): Long {
        return collectionDao.insertCollection(
            in.iambhvsh.shelf.data.local.entity.CollectionEntity(name = name)
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
