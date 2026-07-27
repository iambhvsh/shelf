package `in`.iambhvsh.shelf.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "bookmark_tag_cross_ref",
    primaryKeys = ["bookmarkId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = BookmarkEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookmarkId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("bookmarkId"),
        Index("tagId")
    ]
)
data class BookmarkTagCrossRef(
    val bookmarkId: Long,
    val tagId: Long
)
