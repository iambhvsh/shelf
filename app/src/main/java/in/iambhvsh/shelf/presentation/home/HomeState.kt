package `in`.iambhvsh.shelf.presentation.home

import `in`.iambhvsh.shelf.domain.model.Bookmark
import `in`.iambhvsh.shelf.domain.model.SortOrder
import `in`.iambhvsh.shelf.domain.model.Collection
import `in`.iambhvsh.shelf.domain.model.Tag

data class HomeState(
    val isLoading: Boolean = false,
    val error: String = "",
    val bookmarkData: List<Bookmark> = emptyList(),
    val inputUrl: String = "",
    val isDialog: Boolean = false,
    val isPhotoPreviewDialog: Boolean = false,
    val dialogPhotoUrl: String = "",
    val tempBookmark: Bookmark? = null,
    val isBodySheet: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val showCollectionPicker: Boolean = false,
    val collections: List<Collection> = emptyList(),
    val sortOrder: SortOrder = SortOrder.DATE_NEWEST,
    val showSortSheet: Boolean = false,
    val duplicateToastKey: Int = 0,
    val tags: List<Tag> = emptyList(),
    val tempBookmarkTags: List<Tag> = emptyList(),
    val showTagManager: Boolean = false,
    val activeTagFilters: Set<Long> = emptySet(),
    val showNoteEditor: Boolean = false,
    val noteEditorText: String? = null,
    val showReminderPicker: Boolean = false,
    val showUpdateSheet: Boolean = false
)
