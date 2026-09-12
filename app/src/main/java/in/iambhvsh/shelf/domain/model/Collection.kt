package `in`.iambhvsh.shelf.domain.model

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
