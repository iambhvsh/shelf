package `in`.iambhvsh.shelf.components.root

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import `in`.iambhvsh.shelf.components.home.TagFilterRow
import `in`.iambhvsh.shelf.features.search.SearchState
import `in`.iambhvsh.shelf.features.search.SearchViewModel

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
    isSearching: Boolean,
    onSetSearching: (Boolean) -> Unit,
    isCollectionSearching: Boolean,
    onSetCollectionSearching: (Boolean) -> Unit,
    collectionSearchQuery: String,
    onSetCollectionSearchQuery: (String) -> Unit,
    backStacks: List<MutableList<AppRoute>>,
    onConfirmDelete: (() -> Unit) -> Unit
) {
    val isAnySearching = isSearching || isCollectionSearching
    val isAnySelection = state.isSelectionMode || collectionState.isSelectionMode || collectionState.isDetailSelectionMode
    val selectedCount = when {
        state.isSelectionMode -> state.selectedIds.size
        collectionState.isSelectionMode -> collectionState.selectedIds.size
        collectionState.isDetailSelectionMode -> collectionState.detailSelectedIds.size
        else -> 0
    }
    val isAllSelected = when {
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
            isSelectionMode = isAnySelection,
            selectedCount = selectedCount,
            isAllSelected = isAllSelected,
            isSearching = isAnySearching,
            searchQuery = if (isCollectionSearching) collectionSearchQuery else searchState.searchQuery,
            searchPlaceholder = if (isCollectionSearching) "Search in collection\u2026" else "Search bookmarks\u2026",
            onSearchQueryChange = {
                if (isCollectionSearching) onSetCollectionSearchQuery(it)
                else searchViewModel.onQueryChange(it)
            },
            focusRequester = focusRequester,
            scrollBehavior = scrollBehavior,
            onBack = {
                if (isCollectionDetail) {
                    backStacks[1].removeAt(backStacks[1].size - 1)
                    collectionViewModel.onEvent(CollectionEvents.ClearSelectedCollection)
                } else if (isSettingsSubScreen) {
                    backStacks[2].removeAt(backStacks[2].size - 1)
                }
            },
            onNewBookmarkClick = {
                if (isCollectionDetail && collectionState.selectedCollection != null) {
                    collectionViewModel.onEvent(CollectionEvents.ShowAddBookmarkDialog)
                } else {
                    viewModel.homeEvents(HomeEvents.FabClick)
                }
            },
            onNewCollectionClick = { collectionViewModel.onEvent(CollectionEvents.ShowCreateDialog) },
            onSearchClick = {
                if (currentTab == 0) {
                    onSetSearching(true)
                    searchViewModel.onQueryChange("")
                    viewModel.homeEvents(HomeEvents.ClearSelection)
                } else {
                    onSetCollectionSearching(true)
                    onSetCollectionSearchQuery("")
                    collectionViewModel.onEvent(CollectionEvents.ClearDetailSelection)
                }
            },
            onSortClick = {
                if (currentTab == 0) viewModel.homeEvents(HomeEvents.ShowSortSheet)
                else collectionViewModel.onEvent(CollectionEvents.ShowSortSheet)
            },
            onCloseSelection = {
                when {
                    state.isSelectionMode -> viewModel.homeEvents(HomeEvents.ClearSelection)
                    collectionState.isSelectionMode -> collectionViewModel.onEvent(CollectionEvents.ClearSelection)
                    collectionState.isDetailSelectionMode -> collectionViewModel.onEvent(CollectionEvents.ClearDetailSelection)
                }
            },
            onSelectAll = {
                when {
                    state.isSelectionMode -> viewModel.homeEvents(HomeEvents.SelectAll)
                    collectionState.isSelectionMode -> collectionViewModel.onEvent(CollectionEvents.SelectAll)
                    collectionState.isDetailSelectionMode -> collectionViewModel.onEvent(CollectionEvents.SelectAllDetail)
                }
            },
            onDeselectAll = {
                when {
                    state.isSelectionMode -> viewModel.homeEvents(HomeEvents.DeselectAll)
                    collectionState.isSelectionMode -> collectionViewModel.onEvent(CollectionEvents.DeselectAll)
                    collectionState.isDetailSelectionMode -> collectionViewModel.onEvent(CollectionEvents.DeselectAllDetail)
                }
            },
            onDeleteSelected = when {
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
            onCloseSearch = {
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

        AnimatedVisibility(
            visible = currentTab == 0 && !isAnySelection && !isAnySearching && state.tags.isNotEmpty(),
            enter = expandVertically(tween(180)) + fadeIn(tween(180)),
            exit = shrinkVertically(tween(150)) + fadeOut(tween(150))
        ) {
            TagFilterRow(
                tags = state.tags,
                activeTagIds = state.activeTagFilters,
                onToggleTag = { viewModel.homeEvents(HomeEvents.ToggleTagFilter(it)) }
            )
        }
    }
}
