package `in`.iambhvsh.shelf.save

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
