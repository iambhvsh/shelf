package `in`.iambhvsh.shelf.data.local

import kotlinx.serialization.Serializable

@Serializable
data class BackupTag(
    val name: String
)

@Serializable
data class BackupBookmark(
    val url: String,
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isHidden: Boolean = false,
    val isPinned: Boolean = false,
    val note: String? = null,
    val reminderTime: Long? = null,
    val tags: List<String> = emptyList()
)

@Serializable
data class BackupCollection(
    val name: String,
    val bookmarkUrls: List<String> = emptyList()
)

@Serializable
data class BackupData(
    val version: Int = 2,
    val exportedAt: Long = System.currentTimeMillis(),
    val bookmarks: List<BackupBookmark> = emptyList(),
    val collections: List<BackupCollection> = emptyList(),
    val tags: List<BackupTag> = emptyList()
)
