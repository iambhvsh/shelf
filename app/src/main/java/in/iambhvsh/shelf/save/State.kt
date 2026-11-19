package `in`.iambhvsh.shelf.save

import `in`.iambhvsh.shelf.data.Collection
import `in`.iambhvsh.shelf.data.Tag

data class QuickSaveState(
    val url: String = "",
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val bookmarkId: Long? = null,
    val isSaving: Boolean = true,
    val message: String? = null,
    val selectedCollection: Collection? = null,
    val selectedTags: List<Tag> = emptyList(),
    val collections: List<Collection> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val showCollectionPicker: Boolean = false,
    val showTagPicker: Boolean = false
)
