package com.iambhvsh.shelf.presentation.home

import com.iambhvsh.shelf.domain.model.Bookmark
import com.iambhvsh.shelf.domain.model.SortOrder

sealed class HomeEvents {
    data class OnTextFieldValueChange(val text: String) : HomeEvents()
    object SaveBookmark : HomeEvents()
    object OnDialogDismissClick : HomeEvents()
    object FabClick : HomeEvents()
    data class PreviewImageClick(val url: String) : HomeEvents()
    object PreviewImageDismissClick : HomeEvents()

    data class BookmarkPreviewClick(val bookmark: Bookmark) : HomeEvents()
    object BookmarkPreviewDismissClick : HomeEvents()

    data class ToggleSelection(val id: Long) : HomeEvents()
    object SelectAll : HomeEvents()
    object DeselectAll : HomeEvents()
    object DeleteSelected : HomeEvents()
    object ClearSelection : HomeEvents()
    object ShowCollectionPicker : HomeEvents()
    object HideCollectionPicker : HomeEvents()
    data class AddToCollection(val collectionId: Long) : HomeEvents()
    data class SetSortOrder(val sortOrder: SortOrder) : HomeEvents()
    object ShowSortSheet : HomeEvents()
    object HideSortSheet : HomeEvents()
}