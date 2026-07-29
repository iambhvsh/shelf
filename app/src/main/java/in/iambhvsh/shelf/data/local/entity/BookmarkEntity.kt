package `in`.iambhvsh.shelf.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val title: String?,
    val description: String?,
    val imageUrl: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val isHidden: Boolean = false,
    val isPinned: Boolean = false,
    val note: String? = null,
    val reminderTime: Long? = null
)
