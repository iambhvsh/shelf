package `in`.iambhvsh.shelf.data.dao

import `in`.iambhvsh.shelf.data.BookmarkCollectionCrossRef
import `in`.iambhvsh.shelf.data.BookmarkEntity
import `in`.iambhvsh.shelf.data.CollectionEntity
import `in`.iambhvsh.shelf.data.CollectionWithBookmarks
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

data class CollectionWithCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val bookmarkCount: Int,
    val previewUrls: String?
)

@Dao
interface CollectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity): Long

    @Delete
    suspend fun deleteCollection(collection: CollectionEntity)

    @Query("DELETE FROM bookmark_collection_cross_ref WHERE collectionId = :collectionId")
    suspend fun deleteCrossRefsForCollection(collectionId: Long)

    @Transaction
    suspend fun purgeCollection(collection: CollectionEntity) {
        deleteCrossRefsForCollection(collection.id)
        deleteCollection(collection)
    }

    @Query("UPDATE collections SET name = :name WHERE id = :id")
    suspend fun updateName(id: Long, name: String)

    @Query("""
        SELECT c.id, c.name, c.createdAt, 
               (SELECT COUNT(*) FROM bookmark_collection_cross_ref bcc_cnt
                INNER JOIN bookmarks b_cnt ON bcc_cnt.bookmarkId = b_cnt.id
                WHERE bcc_cnt.collectionId = c.id AND b_cnt.isHidden = 0) AS bookmarkCount,
               (SELECT GROUP_CONCAT(img, '|||')
                FROM (
                    SELECT b.imageUrl AS img
                    FROM bookmark_collection_cross_ref bcc2
                    INNER JOIN bookmarks b ON bcc2.bookmarkId = b.id
                    WHERE bcc2.collectionId = c.id AND b.imageUrl IS NOT NULL AND b.imageUrl != '' AND b.isHidden = 0
                    LIMIT 4
                )) AS previewUrls
        FROM collections c
        ORDER BY c.createdAt DESC
    """)
    fun getAllCollections(): Flow<List<CollectionWithCount>>

    @Transaction
    @Query("SELECT * FROM collections WHERE id = :collectionId")
    fun getCollectionWithBookmarks(collectionId: Long): Flow<CollectionWithBookmarks?>

    @Query("""
        SELECT b.* FROM bookmarks b
        INNER JOIN bookmark_collection_cross_ref bcc ON b.id = bcc.bookmarkId
        WHERE bcc.collectionId = :collectionId AND b.isHidden = 0
        ORDER BY b.createdAt DESC
    """)
    fun getBookmarksForCollection(collectionId: Long): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addBookmarkToCollection(crossRef: BookmarkCollectionCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addBookmarksToCollection(crossRefs: List<BookmarkCollectionCrossRef>)

    @Delete
    suspend fun removeBookmarkFromCollection(crossRef: BookmarkCollectionCrossRef)

    @Query("SELECT * FROM collections ORDER BY createdAt DESC")
    fun getAllCollectionsRaw(): Flow<List<CollectionEntity>>

    @Query("""
        SELECT b.url FROM bookmarks b
        INNER JOIN bookmark_collection_cross_ref bcc ON b.id = bcc.bookmarkId
        WHERE bcc.collectionId = :collectionId AND b.isHidden = 0
    """)
    suspend fun getBookmarkUrlsForCollection(collectionId: Long): List<String>

    @Query("""
        SELECT b.url FROM bookmarks b
        INNER JOIN bookmark_collection_cross_ref bcc ON b.id = bcc.bookmarkId
        WHERE bcc.collectionId = :collectionId
    """)
    suspend fun getAllBookmarkUrlsForCollection(collectionId: Long): List<String>

    @Query("SELECT collectionId FROM bookmark_collection_cross_ref WHERE bookmarkId = :bookmarkId LIMIT 1")
    suspend fun getCollectionIdForBookmark(bookmarkId: Long): Long?

    @Query("SELECT COUNT(*) FROM bookmark_collection_cross_ref")
    fun getCollectionCrossRefCount(): Flow<Int>

    @Query("""
        SELECT -1 AS id, 'Uncategorised' AS name, 0 AS createdAt, 
               COUNT(b.id) AS bookmarkCount,
               (SELECT GROUP_CONCAT(img, '|||') 
                FROM (
                    SELECT b2.imageUrl AS img
                    FROM bookmarks b2 
                    LEFT JOIN (
                        SELECT bcc2.bookmarkId FROM bookmark_collection_cross_ref bcc2
                        INNER JOIN collections c2 ON bcc2.collectionId = c2.id
                    ) active_bcc2 ON b2.id = active_bcc2.bookmarkId 
                    WHERE active_bcc2.bookmarkId IS NULL AND b2.isHidden = 0 AND b2.imageUrl IS NOT NULL AND b2.imageUrl != '' 
                    LIMIT 4
                )) AS previewUrls
        FROM bookmarks b
        LEFT JOIN (
            SELECT bcc.bookmarkId FROM bookmark_collection_cross_ref bcc
            INNER JOIN collections c ON bcc.collectionId = c.id
        ) active_bcc ON b.id = active_bcc.bookmarkId
        WHERE active_bcc.bookmarkId IS NULL AND b.isHidden = 0
    """)
    fun getUnassignedCollectionStats(): Flow<CollectionWithCount>
}
