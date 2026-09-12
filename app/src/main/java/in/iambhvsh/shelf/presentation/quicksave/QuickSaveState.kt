package `in`.iambhvsh.shelf.presentation.quicksave

import `in`.iambhvsh.shelf.domain.model.Collection
import `in`.iambhvsh.shelf.domain.model.Tag

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

sealed class QuickSaveEvents {
    data class Init(val url: String) : QuickSaveEvents()
    data class SelectCollection(val collectionId: Long) : QuickSaveEvents()
    data class ToggleTag(val tagId: Long) : QuickSaveEvents()
    data class CreateTag(val name: String) : QuickSaveEvents()
    data class DeleteTag(val tagId: Long) : QuickSaveEvents()
    object CancelSave : QuickSaveEvents()
    object MessageShown : QuickSaveEvents()
    
    object ShowCollectionPicker : QuickSaveEvents()
    object HideCollectionPicker : QuickSaveEvents()
    object ShowTagPicker : QuickSaveEvents()
    object HideTagPicker : QuickSaveEvents()
}
