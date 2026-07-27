package `in`.iambhvsh.shelf.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import `in`.iambhvsh.shelf.data.local.entity.BookmarkTagCrossRef
import `in`.iambhvsh.shelf.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagToBookmark(crossRef: BookmarkTagCrossRef)

    @Query("DELETE FROM bookmark_tag_cross_ref WHERE bookmarkId = :bookmarkId AND tagId = :tagId")
    suspend fun removeTagFromBookmark(bookmarkId: Long, tagId: Long)

    @Query("SELECT tags.* FROM tags INNER JOIN bookmark_tag_cross_ref ON tags.id = bookmark_tag_cross_ref.tagId WHERE bookmark_tag_cross_ref.bookmarkId = :bookmarkId")
    fun getTagsForBookmark(bookmarkId: Long): Flow<List<TagEntity>>
    
    @Query("SELECT bookmarkId FROM bookmark_tag_cross_ref WHERE tagId = :tagId")
    fun getBookmarksForTag(tagId: Long): Flow<List<Long>>
    
    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun deleteTag(tagId: Long)
}
