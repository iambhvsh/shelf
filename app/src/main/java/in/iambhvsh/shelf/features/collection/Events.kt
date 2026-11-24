package `in`.iambhvsh.shelf.features.collection

import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.data.Collection
import `in`.iambhvsh.shelf.data.SortOrder

sealed class CollectionEvents {
    data class InputNameChanged(val name: String) : CollectionEvents()
    object ShowCreateDialog : CollectionEvents()
    object HideCreateDialog : CollectionEvents()
    object CreateCollection : CollectionEvents()
    data class SelectCollection(val collection: Collection) : CollectionEvents()
    data class SelectCollectionById(val collectionId: Long) : CollectionEvents()
    object ClearSelectedCollection : CollectionEvents()
    data class ToggleSelection(val id: Long) : CollectionEvents()
    object SelectAll : CollectionEvents()
    object DeselectAll : CollectionEvents()
    object ClearSelection : CollectionEvents()
    object DeleteSelected : CollectionEvents()
    data class DeleteCollectionById(val collectionId: Long) : CollectionEvents()
    data class ShowDetailBodySheet(val bookmark: Bookmark) : CollectionEvents()
    object DismissDetailBodySheet : CollectionEvents()
    data class ToggleDetailSelection(val id: Long) : CollectionEvents()
    object SelectAllDetail : CollectionEvents()
    object DeselectAllDetail : CollectionEvents()
    object ClearDetailSelection : CollectionEvents()
    data class RemoveSelectedFromCollection(val collectionId: Long) : CollectionEvents()
    data class DeleteBookmark(val bookmark: Bookmark) : CollectionEvents()
    data class SetSortOrder(val sortOrder: SortOrder) : CollectionEvents()
    object ShowSortSheet : CollectionEvents()
    object HideSortSheet : CollectionEvents()

    data class ShowRenameCollectionDialog(val initialName: String?) : CollectionEvents()
    object HideRenameCollectionDialog : CollectionEvents()
    data class UpdateCollectionName(val id: Long, val name: String) : CollectionEvents()

    data class TogglePin(val bookmark: Bookmark) : CollectionEvents()
    data class ShowEditDialog(val bookmark: Bookmark) : CollectionEvents()
    object HideEditDialog : CollectionEvents()
    data class UpdateBookmarkDetails(val id: Long, val title: String?, val description: String?) : CollectionEvents()

    object ShowTagManager : CollectionEvents()
    object HideTagManager : CollectionEvents()
    data class CreateTag(val name: String) : CollectionEvents()
    data class ToggleTagForBookmark(val tag: `in`.iambhvsh.shelf.data.Tag, val isChecked: Boolean) : CollectionEvents()
    data class DeleteTag(val tagId: Long) : CollectionEvents()

    data class ShowNoteEditor(val initialNote: String?) : CollectionEvents()
    object HideNoteEditor : CollectionEvents()
    data class UpdateNote(val id: Long, val note: String?) : CollectionEvents()

    object ShowReminderPicker : CollectionEvents()
    object HideReminderPicker : CollectionEvents()
    data class SetReminder(val id: Long, val timeInMillis: Long) : CollectionEvents()
    data class CancelReminder(val id: Long) : CollectionEvents()
    object ShowAddBookmarkDialog : CollectionEvents()
    object HideAddBookmarkDialog : CollectionEvents()
    data class OnAddBookmarkUrlChange(val url: String) : CollectionEvents()
    object SaveBookmarkInCollection : CollectionEvents()
    object ClearToast : CollectionEvents()
    data class RestoreCollectionBookmarks(val collectionId: Long) : CollectionEvents()
    data class PreviewImageClick(val url: String) : CollectionEvents()
    object DismissPreviewDialog : CollectionEvents()
}
