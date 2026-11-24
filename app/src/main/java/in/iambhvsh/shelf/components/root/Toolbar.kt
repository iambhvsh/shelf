package `in`.iambhvsh.shelf.components.root

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.zIndex
import `in`.iambhvsh.shelf.data.Collection
import `in`.iambhvsh.shelf.nav.AppRoute
import `in`.iambhvsh.shelf.features.collection.CollectionEvents
import `in`.iambhvsh.shelf.features.collection.CollectionState
import `in`.iambhvsh.shelf.features.collection.CollectionViewModel
import `in`.iambhvsh.shelf.features.home.HomeEvents
import `in`.iambhvsh.shelf.features.home.HomeState
import `in`.iambhvsh.shelf.features.home.HomeViewModel
import `in`.iambhvsh.shelf.features.search.SearchState
import `in`.iambhvsh.shelf.features.search.SearchViewModel
import `in`.iambhvsh.shelf.features.setting.SettingEvents
import `in`.iambhvsh.shelf.features.setting.SettingState
import `in`.iambhvsh.shelf.features.setting.SettingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootToolbar(
    currentTab: Int,
    scrollBehavior: TopAppBarScrollBehavior,
    focusRequester: FocusRequester,
    focusManager: FocusManager,
    isDeepScreen: Boolean,
    deepScreenTitle: String?,
    isCollectionDetail: Boolean,
    isSettingsSubScreen: Boolean,
    state: HomeState,
    viewModel: HomeViewModel,
    collectionState: CollectionState,
    collectionViewModel: CollectionViewModel,
    searchState: SearchState,
    searchViewModel: SearchViewModel,
    settingState: SettingState? = null,
    settingViewModel: SettingViewModel? = null,
    isSearching: Boolean,
    onSetSearching: (Boolean) -> Unit,
    isCollectionSearching: Boolean,
    onSetCollectionSearching: (Boolean) -> Unit,
    collectionSearchQuery: String,
    onSetCollectionSearchQuery: (String) -> Unit,
    onBack: () -> Unit,
    onConfirmDelete: (() -> Unit) -> Unit
) {
    val isSettingsTrash = isSettingsSubScreen && deepScreenTitle == "Recently Deleted"
    val isTrashSelection = isSettingsTrash && (settingState?.selectedTrashIds?.isNotEmpty() == true)
    val isAnySearching = isSearching || isCollectionSearching
    val isAnySelection = state.isSelectionMode || collectionState.isSelectionMode || collectionState.isDetailSelectionMode || isTrashSelection
    val selectedCount = when {
        isTrashSelection -> settingState.selectedTrashIds.size
        state.isSelectionMode -> state.selectedIds.size
        collectionState.isSelectionMode -> collectionState.selectedIds.size
        collectionState.isDetailSelectionMode -> collectionState.detailSelectedIds.size
        else -> 0
    }
    val isAllSelected = when {
        isTrashSelection -> {
            val trash = settingState.trashedBookmarks
            trash.isNotEmpty() && settingState.selectedTrashIds.size == trash.size
        }
        state.isSelectionMode -> state.bookmarkData.isNotEmpty() && state.selectedIds.size == state.bookmarkData.size
        collectionState.isSelectionMode -> collectionState.collections.isNotEmpty() && collectionState.selectedIds.size == collectionState.collections.size
        collectionState.isDetailSelectionMode -> collectionState.collectionBookmarks.isNotEmpty() && collectionState.detailSelectedIds.size == collectionState.collectionBookmarks.size
        else -> false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(10f)
    ) {
        ShelfHeader(
            currentTab = currentTab,
            isDeepScreen = isDeepScreen,
            deepScreenTitle = deepScreenTitle,
            isCollectionDetail = isCollectionDetail,
            isSelectionMode = isAnySelection,
            selectedCount = selectedCount,
            isAllSelected = isAllSelected,
            isSearching = isAnySearching,
            searchQuery = if (isCollectionSearching) collectionSearchQuery else searchState.searchQuery,
            searchPlaceholder = when {
                isCollectionDetail -> "Search in collection\u2026"
                currentTab == 1 -> "Search collections\u2026"
                else -> "Search bookmarks\u2026"
            },
            onSearchQueryChange = {
                if (isCollectionSearching) onSetCollectionSearchQuery(it)
                else searchViewModel.onQueryChange(it)
            },
            focusRequester = focusRequester,
            scrollBehavior = scrollBehavior,
            onBack = onBack,
            onNewBookmarkClick = {
                if (isCollectionDetail && collectionState.selectedCollection != null) {
                    collectionViewModel.onEvent(CollectionEvents.ShowAddBookmarkDialog)
                } else {
                    viewModel.homeEvents(HomeEvents.FabClick)
                }
            },
            onNewCollectionClick = { collectionViewModel.onEvent(CollectionEvents.ShowCreateDialog) },
            onSearchClick = {
                scrollBehavior.state.heightOffset = 0f
                scrollBehavior.state.contentOffset = 0f
                if (currentTab == 0) {
                    onSetSearching(true)
                    searchViewModel.onQueryChange("")
                    viewModel.homeEvents(HomeEvents.ClearSelection)
                } else {
                    onSetCollectionSearching(true)
                    onSetCollectionSearchQuery("")
                    if (isCollectionDetail) {
                        collectionViewModel.onEvent(CollectionEvents.ClearDetailSelection)
                    } else {
                        collectionViewModel.onEvent(CollectionEvents.ClearSelection)
                    }
                }
            },
            onSortClick = {
                if (currentTab == 0) viewModel.homeEvents(HomeEvents.ShowSortSheet)
                else collectionViewModel.onEvent(CollectionEvents.ShowSortSheet)
            },
            onCloseSelection = {
                when {
                    isTrashSelection -> settingViewModel?.onEvent(SettingEvents.ClearTrashSelection)
                    state.isSelectionMode -> viewModel.homeEvents(HomeEvents.ClearSelection)
                    collectionState.isSelectionMode -> collectionViewModel.onEvent(CollectionEvents.ClearSelection)
                    collectionState.isDetailSelectionMode -> collectionViewModel.onEvent(CollectionEvents.ClearDetailSelection)
                }
            },
            onSelectAll = {
                when {
                    isSettingsTrash -> settingViewModel?.onEvent(SettingEvents.SelectAllTrash)
                    state.isSelectionMode -> viewModel.homeEvents(HomeEvents.SelectAll)
                    collectionState.isSelectionMode -> collectionViewModel.onEvent(CollectionEvents.SelectAll)
                    collectionState.isDetailSelectionMode -> collectionViewModel.onEvent(CollectionEvents.SelectAllDetail)
                }
            },
            onDeselectAll = {
                when {
                    isTrashSelection -> settingViewModel?.onEvent(SettingEvents.ClearTrashSelection)
                    state.isSelectionMode -> viewModel.homeEvents(HomeEvents.DeselectAll)
                    collectionState.isSelectionMode -> collectionViewModel.onEvent(CollectionEvents.DeselectAll)
                    collectionState.isDetailSelectionMode -> collectionViewModel.onEvent(CollectionEvents.DeselectAllDetail)
                }
            },
            onDeleteSelected = when {
                isTrashSelection -> {
                    { settingViewModel?.onEvent(SettingEvents.ShowDeletePermanentlyDialog) }
                }
                state.isSelectionMode -> { { onConfirmDelete { viewModel.homeEvents(HomeEvents.DeleteSelected) } } }
                collectionState.isSelectionMode -> { { onConfirmDelete { collectionViewModel.onEvent(CollectionEvents.DeleteSelected) } } }
                collectionState.isDetailSelectionMode && collectionState.selectedCollection?.id != Collection.UNCATEGORISED_ID -> {
                    {
                        val id = collectionState.selectedCollection?.id
                        if (id != null) {
                            collectionViewModel.onEvent(CollectionEvents.RemoveSelectedFromCollection(id))
                        }
                    }
                }
                else -> null
            },
            onAddToCollection = if (state.isSelectionMode) {
                { viewModel.homeEvents(HomeEvents.ShowCollectionPicker) }
            } else null,
            onRenameSelected = if (collectionState.isSelectionMode && collectionState.selectedIds.size == 1) {
                {
                    val id = collectionState.selectedIds.first()
                    val collection = collectionState.collections.find { it.id == id }
                    if (collection != null) {
                        collectionViewModel.onEvent(CollectionEvents.ShowRenameCollectionDialog(collection.name))
                    }
                }
            } else null,
            isTrashScreen = isSettingsTrash && (settingState?.trashedBookmarks?.isNotEmpty() == true),
            onRestoreSelected = if (isTrashSelection) {
                { settingViewModel?.onEvent(SettingEvents.RestoreSelectedTrash) }
            } else null,
            onEmptyTrash = if (isSettingsTrash && (settingState?.trashedBookmarks?.isNotEmpty() == true)) {
                { settingViewModel?.onEvent(SettingEvents.ShowEmptyTrashDialog) }
            } else null,
            onCloseSearch = {
                scrollBehavior.state.heightOffset = 0f
                scrollBehavior.state.contentOffset = 0f
                if (isCollectionSearching) {
                    onSetCollectionSearching(false)
                    onSetCollectionSearchQuery("")
                } else {
                    onSetSearching(false)
                    searchViewModel.onQueryChange("")
                }
                focusManager.clearFocus()
            }
        )
    }
}
