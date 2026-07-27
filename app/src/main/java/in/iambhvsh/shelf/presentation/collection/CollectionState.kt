package `in`.iambhvsh.shelf.presentation.collection

import `in`.iambhvsh.shelf.domain.model.Bookmark
import `in`.iambhvsh.shelf.domain.model.Collection
import `in`.iambhvsh.shelf.domain.model.SortOrder

data class CollectionState(
    val collections: List<Collection> = emptyList(),
    val isLoading: Boolean = false,
    val error: String = "",
    val showCreateDialog: Boolean = false,
    val inputName: String = "",
    val selectedCollection: Collection? = null,
    val collectionBookmarks: List<Bookmark> = emptyList(),
    val isDetailLoading: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val detailSelectedIds: Set<Long> = emptySet(),
    val isDetailSelectionMode: Boolean = false,
    val tempBookmark: Bookmark? = null,
    val isDetailBodySheet: Boolean = false,
    val sortOrder: SortOrder = SortOrder.DATE_NEWEST,
    val showSortSheet: Boolean = false
)
