package `in`.iambhvsh.shelf.domain.model

data class Bookmark(
    val id: Long = 0,
    val url: String,
    val title: String?,
    val description: String?,
    val imageUrl: String?,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)