@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package `in`.iambhvsh.shelf.presentation.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.ui.res.painterResource
import `in`.iambhvsh.shelf.R
import `in`.iambhvsh.shelf.ui.components.expressiveBounce
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
import `in`.iambhvsh.shelf.presentation.collection.components.CollectionInputSheet
import `in`.iambhvsh.shelf.presentation.collection.components.CollectionPickerSheet
import `in`.iambhvsh.shelf.presentation.home.HomeEvents
import `in`.iambhvsh.shelf.presentation.home.HomeScreen
import `in`.iambhvsh.shelf.presentation.home.HomeViewModel
import `in`.iambhvsh.shelf.presentation.home.components.HomeInputSheet
import `in`.iambhvsh.shelf.presentation.home.components.TagFilterRow
import `in`.iambhvsh.shelf.presentation.root.components.RootBottomBar
import `in`.iambhvsh.shelf.presentation.root.components.RootFab
import `in`.iambhvsh.shelf.presentation.root.components.ShelfHeader
import `in`.iambhvsh.shelf.presentation.search.SearchViewModel
import `in`.iambhvsh.shelf.presentation.home.components.LoadingProgress
import `in`.iambhvsh.shelf.presentation.setting.BrowserImportState
import `in`.iambhvsh.shelf.presentation.setting.ExportState
import `in`.iambhvsh.shelf.navigation.AppRoute
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalContext
import `in`.iambhvsh.shelf.presentation.setting.ImportState
import `in`.iambhvsh.shelf.presentation.setting.SettingDialogsAndSheets
import `in`.iambhvsh.shelf.presentation.setting.SettingViewModel
import `in`.iambhvsh.shelf.presentation.setting.screens.SettingsAboutScreen
import `in`.iambhvsh.shelf.presentation.setting.screens.SettingsAppearanceScreen
import `in`.iambhvsh.shelf.presentation.setting.screens.SettingsDataScreen
import `in`.iambhvsh.shelf.presentation.setting.screens.SettingsGeneralScreen
import `in`.iambhvsh.shelf.presentation.setting.screens.SettingsMainScreen
import `in`.iambhvsh.shelf.presentation.setting.screens.SettingsSecurityScreen
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
    openBookmarkId: Long? = null,
    intentId: Int = 0,
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
    
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var pendingDeleteAction: (() -> Unit)? by remember { mutableStateOf(null) }
    
    val backStacks: List<MutableList<AppRoute>> = remember {
        listOf(
            mutableStateListOf<AppRoute>(AppRoute.Home),
            mutableStateListOf<AppRoute>(AppRoute.Collections),
            mutableStateListOf<AppRoute>(AppRoute.Settings)
        )
    }
    val pendingSharedUrl = remember(intentId) { mutableStateOf(sharedUrl) }
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

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    LaunchedEffect(isSearching) {
        if (isSearching) focusRequester.requestFocus()
    }
    
    LaunchedEffect(state.activeTagFilters) {
        searchViewModel.onTagsChange(state.activeTagFilters)
    }
    
    LaunchedEffect(openBookmarkId, intentId) {
        if (openBookmarkId != null && !state.isBodySheet) {
            viewModel.homeEvents(HomeEvents.OpenBookmarkById(openBookmarkId))
        }
    }

    LaunchedEffect(isCollectionSearching) {
        if (isCollectionSearching) focusRequester.requestFocus()
    }

    LaunchedEffect(currentTab) {
        scrollBehavior.state.heightOffset = 0f
        scrollBehavior.state.contentOffset = 0f
    }

    val showTopBarActions =
        !state.isSelectionMode && !collectionState.isSelectionMode &&
        !collectionState.isDetailSelectionMode && !isSearching && !isCollectionSearching

    ShelfTheme(
        themeMode = settingState.themeMode,
        dynamicColor = settingState.dynamicColor,
        accentColor = settingState.accentColor
    ) {
        val context = LocalContext.current
        val versionName = remember {
            runCatching {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            }.getOrNull().orEmpty()
        }

        val isCollectionDetail = currentTab == 1 && backStacks[1].lastOrNull() is AppRoute.CollectionDetail
        val collectionName = if (isCollectionDetail) {
            val rawName = collectionState.selectedCollection?.name ?: "Collection"
            if (rawName.length > 13) "${rawName.take(13)}..." else rawName
        } else null

        val currentSettingsRoute = if (currentTab == 2) backStacks[2].lastOrNull() else null
        val isSettingsSubScreen = currentTab == 2 && currentSettingsRoute != null && currentSettingsRoute != AppRoute.Settings
        val settingsSubScreenTitle = when (currentSettingsRoute) {
            is AppRoute.SettingsAppearance -> "Appearance"
            is AppRoute.SettingsGeneral -> "General"
            is AppRoute.SettingsSecurity -> "Security"
            is AppRoute.SettingsData -> "Data & Storage"
            is AppRoute.SettingsAbout -> "About"
            else -> null
        }

        val isDeepScreen = isCollectionDetail || isSettingsSubScreen
        val deepScreenTitle = if (isCollectionDetail) collectionName else settingsSubScreenTitle
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            AppNavHost(
                modifier = Modifier.fillMaxSize(),
                currentTab = currentTab,
                onTabChange = { currentTab = it },
                backStacks = backStacks,
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
                settingsMainScreen = { onNavigate ->
                    SettingsMainScreen(
                        state = settingState,
                        versionName = versionName,
                        onNavigate = onNavigate
                    )
                },
                settingsAppearanceScreen = {
                    SettingsAppearanceScreen(
                        state = settingState,
                        viewModel = settingViewModel
                    )
                },
                settingsGeneralScreen = {
                    SettingsGeneralScreen(
                        state = settingState,
                        viewModel = settingViewModel
                    )
                },
                settingsSecurityScreen = {
                    SettingsSecurityScreen(
                        state = settingState,
                        viewModel = settingViewModel
                    )
                },
                settingsDataScreen = {
                    SettingsDataScreen(
                        state = settingState,
                        viewModel = settingViewModel
                    )
                },
                settingsAboutScreen = {
                    SettingsAboutScreen(
                        state = settingState,
                        viewModel = settingViewModel,
                        versionName = versionName,
                        context = context
                    )
                }
            )

            // Top Header (Floating over content with zIndex)
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
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
                        if (isCollectionSearching) collectionSearchQuery = it
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
                    onNewCollectionClick = {
                        collectionViewModel.onEvent(CollectionEvents.ShowCreateDialog)
                    },
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
                        state.isSelectionMode -> {
                            {
                                pendingDeleteAction = { viewModel.homeEvents(HomeEvents.DeleteSelected) }
                                showDeleteConfirmDialog = true
                            }
                        }
                        collectionState.isSelectionMode -> {
                            {
                                pendingDeleteAction = { collectionViewModel.onEvent(CollectionEvents.DeleteSelected) }
                                showDeleteConfirmDialog = true
                            }
                        }
                        collectionState.isDetailSelectionMode && collectionState.selectedCollection?.id != `in`.iambhvsh.shelf.domain.model.Collection.UNCATEGORISED_ID -> {
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
                            isCollectionSearching = false
                            collectionSearchQuery = ""
                        } else {
                            isSearching = false
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

            // Bottom Toolbar (Floating over content with zIndex)
            AnimatedVisibility(
                visible = showTopBarActions && !isDeepScreen,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
                    .zIndex(10f),
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeOut()
            ) {
                RootBottomBar(
                    currentTab = currentTab,
                    onTabChange = { tab ->
                        if (currentTab == tab) {
                            if (tab == 1 && backStacks[1].size > 1) {
                                backStacks[1].subList(1, backStacks[1].size).clear()
                                collectionViewModel.onEvent(CollectionEvents.ClearSelectedCollection)
                            } else if (tab == 2 && backStacks[2].size > 1) {
                                backStacks[2].subList(1, backStacks[2].size).clear()
                            }
                        } else {
                            currentTab = tab
                        }
                    }
                )
            }
        }

        BackHandler(enabled = isSearching) {
            isSearching = false
            searchViewModel.onQueryChange("")
        }

        BackHandler(enabled = isCollectionSearching) {
            isCollectionSearching = false
            collectionSearchQuery = ""
        }

        val isSettingBusy =
            settingState.browserImportState is BrowserImportState.Loading ||
            settingState.importState is ImportState.Loading ||
            settingState.exportState is ExportState.Loading ||
            settingState.browserExportState is ExportState.Loading ||
            settingState.isCheckingForUpdates

        LoadingProgress(isLoading = isSettingBusy, blockTouch = true)

        HomeInputSheet(
            showBottomSheet = state.isDialog,
            onDismissRequest = { viewModel.homeEvents(HomeEvents.OnDialogDismissClick) },
            value = state.inputUrl,
            onTextChange = { viewModel.homeEvents(HomeEvents.OnTextFieldValueChange(it)) },
            onSaveClick = { viewModel.homeEvents(HomeEvents.SaveBookmark) }
        )

        CollectionInputSheet(
            showBottomSheet = collectionState.showCreateDialog,
            onDismissRequest = { collectionViewModel.onEvent(CollectionEvents.HideCreateDialog) },
            value = collectionState.inputName,
            onTextChange = { collectionViewModel.onEvent(CollectionEvents.InputNameChanged(it)) },
            onSaveClick = { collectionViewModel.onEvent(CollectionEvents.CreateCollection) }
        )

        if (state.showCollectionPicker) {
            CollectionPickerSheet(
                collections = state.collections.filter { !it.isVirtual },
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

        if (showDeleteConfirmDialog) {
            `in`.iambhvsh.shelf.presentation.home.components.DeleteConfirmationDialog(
                title = "Delete Selected",
                text = "Are you sure you want to permanently delete these items?",
                onConfirm = {
                    pendingDeleteAction?.invoke()
                    showDeleteConfirmDialog = false
                    pendingDeleteAction = null
                },
                onDismiss = {
                    showDeleteConfirmDialog = false
                    pendingDeleteAction = null
                }
            )
        }

        SettingDialogsAndSheets(
            state = settingState,
            viewModel = settingViewModel,
            versionName = versionName,
            context = context
        )
    }
}
