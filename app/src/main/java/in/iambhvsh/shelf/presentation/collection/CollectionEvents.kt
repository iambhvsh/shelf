package `in`.iambhvsh.shelf.presentation.collection

import `in`.iambhvsh.shelf.domain.model.Bookmark
import `in`.iambhvsh.shelf.domain.model.Collection
import `in`.iambhvsh.shelf.domain.model.SortOrder

sealed class CollectionEvents {
    data class InputNameChanged(val name: String) : CollectionEvents()
    object ShowCreateDialog : CollectionEvents()
    object HideCreateDialog : CollectionEvents()
    object CreateCollection : CollectionEvents()
    data class SelectCollection(val collection: Collection) : CollectionEvents()
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
    data class SetSortOrder(val sortOrder: SortOrder) : CollectionEvents()
    object ShowSortSheet : CollectionEvents()
    object HideSortSheet : CollectionEvents()

    data class ShowRenameCollectionDialog(val initialName: String?) : CollectionEvents()
    object HideRenameCollectionDialog : CollectionEvents()
    data class UpdateCollectionName(val id: Long, val name: String) : CollectionEvents()

    data class TogglePin(val bookmark: Bookmark) : CollectionEvents()
    data class ShowRenameBookmarkDialog(val initialTitle: String?) : CollectionEvents()
    object HideRenameBookmarkDialog : CollectionEvents()
    data class UpdateBookmarkTitle(val id: Long, val title: String) : CollectionEvents()

    object ShowTagManager : CollectionEvents()
    object HideTagManager : CollectionEvents()
    data class CreateTag(val name: String) : CollectionEvents()
    data class ToggleTagForBookmark(val tag: `in`.iambhvsh.shelf.domain.model.Tag, val isChecked: Boolean) : CollectionEvents()
    data class DeleteTag(val tagId: Long) : CollectionEvents()

    data class ShowNoteEditor(val initialNote: String?) : CollectionEvents()
    object HideNoteEditor : CollectionEvents()
    data class UpdateNote(val id: Long, val note: String?) : CollectionEvents()

    object ShowReminderPicker : CollectionEvents()
    object HideReminderPicker : CollectionEvents()
    data class SetReminder(val id: Long, val timeInMillis: Long) : CollectionEvents()
    data class CancelReminder(val id: Long) : CollectionEvents()
}
