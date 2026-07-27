package `in`.iambhvsh.shelf.presentation.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material3.TopAppBarDefaults
import `in`.iambhvsh.shelf.domain.model.SortOrder
import `in`.iambhvsh.shelf.navigation.AppNavHost
import `in`.iambhvsh.shelf.presentation.collection.CollectionDetailScreen
import `in`.iambhvsh.shelf.presentation.collection.CollectionEvents
import `in`.iambhvsh.shelf.presentation.collection.CollectionScreen
import `in`.iambhvsh.shelf.presentation.collection.CollectionViewModel
import `in`.iambhvsh.shelf.presentation.collection.components.CollectionPickerSheet
import `in`.iambhvsh.shelf.presentation.home.HomeEvents
import `in`.iambhvsh.shelf.presentation.home.HomeScreen
import `in`.iambhvsh.shelf.presentation.home.HomeViewModel
import `in`.iambhvsh.shelf.presentation.home.components.TagFilterRow
import `in`.iambhvsh.shelf.presentation.root.components.DefaultTopBar
import `in`.iambhvsh.shelf.presentation.root.components.RootBottomBar
import `in`.iambhvsh.shelf.presentation.root.components.RootFab
import `in`.iambhvsh.shelf.presentation.root.components.SearchTopBar
import `in`.iambhvsh.shelf.presentation.root.components.SelectionTopBar
import `in`.iambhvsh.shelf.presentation.search.SearchViewModel
import `in`.iambhvsh.shelf.presentation.home.components.LoadingProgress
import `in`.iambhvsh.shelf.presentation.setting.BrowserImportState
import `in`.iambhvsh.shelf.presentation.setting.ImportState
import `in`.iambhvsh.shelf.presentation.setting.SettingScreen
import `in`.iambhvsh.shelf.presentation.setting.SettingViewModel
import `in`.iambhvsh.shelf.presentation.setting.components.RadioOptionSheet
import `in`.iambhvsh.shelf.ui.theme.ShelfTheme
import org.koin.androidx.compose.koinViewModel

private fun fastOutLinearIn(fraction: Float): Float {
    val easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
    return easing.transform(fraction.coerceIn(0f, 1f))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen(
    sharedUrl: String? = null,
    viewModel: HomeViewModel = koinViewModel(),
    collectionViewModel: CollectionViewModel = koinViewModel(),
    settingViewModel: SettingViewModel = koinViewModel(),
    searchViewModel: SearchViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val collectionState by collectionViewModel.state.collectAsState()
    val settingState by settingViewModel.state.collectAsState()
    val searchState by searchViewModel.state.collectAsState()
    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    var isSearching by remember { mutableStateOf(false) }
    var isCollectionSearching by remember { mutableStateOf(false) }
    var collectionSearchQuery by remember { mutableStateOf("") }
    val pendingSharedUrl = remember { mutableStateOf(sharedUrl) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val collectionSearchResults = remember(
        collectionState.collectionBookmarks,
        collectionSearchQuery,
        isCollectionSearching
    ) {
        if (!isCollectionSearching) null
        else if (collectionSearchQuery.isBlank()) collectionState.collectionBookmarks
        else collectionState.collectionBookmarks.filter { bm ->
            (bm.title?.contains(collectionSearchQuery, ignoreCase = true) ?: false) ||
            bm.url.contains(collectionSearchQuery, ignoreCase = true)
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    LaunchedEffect(isSearching) {
        if (isSearching) focusRequester.requestFocus()
    }

    LaunchedEffect(isCollectionSearching) {
        if (isCollectionSearching) focusRequester.requestFocus()
    }

    val showTopBarActions =
        !state.isSelectionMode && !collectionState.isSelectionMode &&
        !collectionState.isDetailSelectionMode && !isSearching && !isCollectionSearching

    ShelfTheme(
        themeMode = settingState.themeMode,
        dynamicColor = settingState.dynamicColor,
        accentColor = settingState.accentColor
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .background(MaterialTheme.colorScheme.surface),
            bottomBar = {
                if (showTopBarActions) {
                    RootBottomBar(
                        currentTab = currentTab,
                        onTabChange = { currentTab = it }
                    )
                }
            },
            topBar = {
                val fraction = scrollBehavior.state.collapsedFraction
                val topBarColor = androidx.compose.ui.graphics.lerp(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surfaceContainer,
                    fastOutLinearIn(fraction)
                )

                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.background(topBarColor)
                ) {
                    when {
                    state.isSelectionMode && !isSearching && !isCollectionSearching -> {
                        SelectionTopBar(
                            title = "Selected ${state.selectedIds.size}",
                            isAllSelected = state.bookmarkData.isNotEmpty() &&
                                state.selectedIds.size == state.bookmarkData.size,
                            onClose = { viewModel.homeEvents(HomeEvents.ClearSelection) },
                            onSelectAll = { viewModel.homeEvents(HomeEvents.SelectAll) },
                            onDeselectAll = { viewModel.homeEvents(HomeEvents.DeselectAll) },
                            onDelete = { viewModel.homeEvents(HomeEvents.DeleteSelected) },
                            onAddToCollection = { viewModel.homeEvents(HomeEvents.ShowCollectionPicker) },
                            scrollBehavior = scrollBehavior
                        )
                    }

                    collectionState.isSelectionMode && !isSearching && !isCollectionSearching -> {
                        SelectionTopBar(
                            title = "Selected ${collectionState.selectedIds.size}",
                            isAllSelected = collectionState.collections.isNotEmpty() &&
                                collectionState.selectedIds.size == collectionState.collections.size,
                            onClose = { collectionViewModel.onEvent(CollectionEvents.ClearSelection) },
                            onSelectAll = { collectionViewModel.onEvent(CollectionEvents.SelectAll) },
                            onDeselectAll = { collectionViewModel.onEvent(CollectionEvents.DeselectAll) },
                            onDelete = { collectionViewModel.onEvent(CollectionEvents.DeleteSelected) },
                            scrollBehavior = scrollBehavior
                        )
                    }

                    collectionState.isDetailSelectionMode && !isSearching && !isCollectionSearching -> {
                        SelectionTopBar(
                            title = "Selected ${collectionState.detailSelectedIds.size}",
                            isAllSelected = collectionState.collectionBookmarks.isNotEmpty() &&
                                collectionState.detailSelectedIds.size == collectionState.collectionBookmarks.size,
                            onClose = { collectionViewModel.onEvent(CollectionEvents.ClearDetailSelection) },
                            onSelectAll = { collectionViewModel.onEvent(CollectionEvents.SelectAllDetail) },
                            onDeselectAll = { collectionViewModel.onEvent(CollectionEvents.DeselectAllDetail) },
                            onDelete = {
                                val id = collectionState.selectedCollection?.id ?: return@SelectionTopBar
                                collectionViewModel.onEvent(CollectionEvents.RemoveSelectedFromCollection(id))
                            },
                            scrollBehavior = scrollBehavior
                        )
                    }

                    isSearching -> {
                        SearchTopBar(
                            query = searchState.searchQuery,
                            onQueryChange = { searchViewModel.onQueryChange(it) },
                            placeholder = "Search bookmarks\u2026",
                            onClose = {
                                isSearching = false
                                searchViewModel.onQueryChange("")
                                focusManager.clearFocus()
                            },
                            focusRequester = focusRequester
                        )
                    }

                    isCollectionSearching -> {
                        SearchTopBar(
                            query = collectionSearchQuery,
                            onQueryChange = { collectionSearchQuery = it },
                            placeholder = "Search in collection\u2026",
                            onClose = {
                                isCollectionSearching = false
                                collectionSearchQuery = ""
                                focusManager.clearFocus()
                            },
                            focusRequester = focusRequester
                        )
                    }

                    else -> {
                        val showSortButton =
                            currentTab == 0 || (currentTab == 1 && collectionState.selectedCollection != null)
                        val showSearchButton =
                            currentTab == 0 || (currentTab == 1 && collectionState.selectedCollection != null)
                        DefaultTopBar(
                            currentTab = currentTab,
                            showSearchButton = showSearchButton,
                            showSortButton = showSortButton,
                            scrollBehavior = scrollBehavior,
                            onSearchClick = {
                                if (currentTab == 0) {
                                    isSearching = true
                                    searchViewModel.onQueryChange("")
                                    viewModel.homeEvents(HomeEvents.ClearSelection)
                                } else {
                                    isCollectionSearching = true
                                    collectionSearchQuery = ""
                                    collectionViewModel.onEvent(CollectionEvents.ClearDetailSelection)
                                }
                            },
                            onSortClick = {
                                if (currentTab == 0) {
                                    viewModel.homeEvents(HomeEvents.ShowSortSheet)
                                } else {
                                    collectionViewModel.onEvent(CollectionEvents.ShowSortSheet)
                                }
                            }
                        )
                    }
                }

                if (currentTab == 0 && !isSearching && !state.isSelectionMode) {
                    TagFilterRow(
                        tags = state.tags,
                        activeTagIds = state.activeTagFilters,
                        onToggleTag = { viewModel.homeEvents(HomeEvents.ToggleTagFilter(it)) }
                    )
                }
                }
            },
            floatingActionButton = {
                RootFab(
                    currentTab = currentTab,
                    homeState = state,
                    collectionState = collectionState,
                    isSearching = isSearching,
                    onHomeFabClick = { viewModel.homeEvents(HomeEvents.FabClick) },
                    onCollectionFabClick = { collectionViewModel.onEvent(CollectionEvents.ShowCreateDialog) }
                )
            }
        ) { innerPadding ->
            AppNavHost(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                currentTab = currentTab,
                onTabChange = { currentTab = it },
                homeScreen = {
                    val url = pendingSharedUrl.value
                    if (url != null) pendingSharedUrl.value = null
                    HomeScreen(
                        sharedUrl = url,
                        tapAction = settingState.tapAction,
                        viewMode = settingState.viewMode,
                        searchResults = if (isSearching) searchState.searchResults else null,
                        searchQuery = searchState.searchQuery
                    )
                },
                collectionsScreen = { navigateToDetail -> CollectionScreen(onCollectionClick = navigateToDetail) },
                collectionDetailScreen = { collectionId ->
                    CollectionDetailScreen(
                        collectionId = collectionId,
                        tapAction = settingState.tapAction,
                        viewMode = settingState.viewMode,
                        viewModel = collectionViewModel,
                        searchResults = collectionSearchResults,
                        searchQuery = collectionSearchQuery
                    )
                },
                settingsScreen = { 
                    SettingScreen(
                        viewModel = settingViewModel
                    ) 
                }
            )

            BackHandler(enabled = isSearching) {
                isSearching = false
                searchViewModel.onQueryChange("")
            }

            BackHandler(enabled = isCollectionSearching) {
                isCollectionSearching = false
                collectionSearchQuery = ""
            }
        }

        val isImporting = settingState.browserImportState is BrowserImportState.Loading || settingState.importState is ImportState.Loading

        LoadingProgress(isLoading = isImporting, blockTouch = true)

        if (state.showCollectionPicker) {
            CollectionPickerSheet(
                collections = state.collections,
                onSelectCollection = { viewModel.homeEvents(HomeEvents.AddToCollection(it)) },
                onDismiss = { viewModel.homeEvents(HomeEvents.HideCollectionPicker) }
            )
        }

        if (state.showSortSheet) {
            RadioOptionSheet(
                title = "Sort by",
                options = listOf(
                    "Date added (newest first)" to SortOrder.DATE_NEWEST,
                    "Date added (oldest first)" to SortOrder.DATE_OLDEST,
                    "Title (A-Z)" to SortOrder.TITLE_ASC,
                    "Title (Z-A)" to SortOrder.TITLE_DESC
                ),
                current = state.sortOrder,
                onSelect = { viewModel.homeEvents(HomeEvents.SetSortOrder(it)) },
                onDismiss = { viewModel.homeEvents(HomeEvents.HideSortSheet) }
            )
        }

        if (collectionState.showSortSheet) {
            RadioOptionSheet(
                title = "Sort by",
                options = listOf(
                    "Date added (newest first)" to SortOrder.DATE_NEWEST,
                    "Date added (oldest first)" to SortOrder.DATE_OLDEST,
                    "Title (A-Z)" to SortOrder.TITLE_ASC,
                    "Title (Z-A)" to SortOrder.TITLE_DESC
                ),
                current = collectionState.sortOrder,
                onSelect = { collectionViewModel.onEvent(CollectionEvents.SetSortOrder(it)) },
                onDismiss = { collectionViewModel.onEvent(CollectionEvents.HideSortSheet) }
            )
        }
    }
}
