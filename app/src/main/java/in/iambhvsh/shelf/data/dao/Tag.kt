package `in`.iambhvsh.shelf.data.dao

import `in`.iambhvsh.shelf.data.BookmarkTagCrossRef
import `in`.iambhvsh.shelf.data.TagEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): TagEntity?

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags ORDER BY name ASC")
    suspend fun getAllTagsOnce(): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagToBookmark(crossRef: BookmarkTagCrossRef)

    @Query("DELETE FROM bookmark_tag_cross_ref WHERE bookmarkId = :bookmarkId AND tagId = :tagId")
    suspend fun removeTagFromBookmark(bookmarkId: Long, tagId: Long)

    @Query("SELECT tags.* FROM tags INNER JOIN bookmark_tag_cross_ref ON tags.id = bookmark_tag_cross_ref.tagId WHERE bookmark_tag_cross_ref.bookmarkId = :bookmarkId")
    fun getTagsForBookmark(bookmarkId: Long): Flow<List<TagEntity>>

    @Query("SELECT tags.* FROM tags INNER JOIN bookmark_tag_cross_ref ON tags.id = bookmark_tag_cross_ref.tagId WHERE bookmark_tag_cross_ref.bookmarkId = :bookmarkId")
    suspend fun getTagsForBookmarkOnce(bookmarkId: Long): List<TagEntity>
    
    @Query("""
        SELECT ref.bookmarkId FROM bookmark_tag_cross_ref ref
        INNER JOIN bookmarks b ON ref.bookmarkId = b.id
        WHERE ref.tagId = :tagId AND b.isHidden = 0
    """)
    fun getBookmarksForTag(tagId: Long): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM bookmark_tag_cross_ref")
    fun getTagCrossRefCount(): Flow<Int>
    
    @Query("DELETE FROM bookmark_tag_cross_ref WHERE tagId = :tagId")
    suspend fun deleteTagCrossRefs(tagId: Long)

    @Transaction
    suspend fun purgeTag(tagId: Long) {
        deleteTagCrossRefs(tagId)
        deleteTag(tagId)
    }

    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun deleteTag(tagId: Long)
}
