package `in`.iambhvsh.shelf.data

import kotlinx.serialization.Serializable

data class Bookmark(
    val id: Long = 0,
    val url: String,
    val title: String?,
    val description: String?,
    val imageUrl: String?,
    val isPinned: Boolean = false,
    val note: String? = null,
    val reminderTime: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class Collection(
    val id: Long = 0,
    val name: String,
    val bookmarkCount: Int = 0,
    val previewUrls: List<String> = emptyList(),
    val isVirtual: Boolean = false
) {
    companion object {
        const val UNCATEGORISED_ID = -1L
    }
}

enum class SortOrder {
    DATE_NEWEST,
    DATE_OLDEST,
    TITLE_ASC,
    TITLE_DESC
}

data class Tag(
    val id: Long = 0,
    val name: String
)

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
