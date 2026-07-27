package com.iambhvsh.shelf.presentation.collection

import com.iambhvsh.shelf.domain.model.Bookmark
import com.iambhvsh.shelf.domain.model.Collection
import com.iambhvsh.shelf.domain.model.SortOrder

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
}
