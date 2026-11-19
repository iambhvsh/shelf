@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package `in`.iambhvsh.shelf.root

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import `in`.iambhvsh.shelf.nav.AppRoute
import `in`.iambhvsh.shelf.collection.CollectionEvents
import `in`.iambhvsh.shelf.collection.CollectionViewModel
import `in`.iambhvsh.shelf.home.HomeEvents
import `in`.iambhvsh.shelf.home.HomeViewModel
import `in`.iambhvsh.shelf.search.SearchViewModel
import `in`.iambhvsh.shelf.setting.SettingViewModel
import `in`.iambhvsh.shelf.ui.theme.ShelfTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Root(
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
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
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
    val showDock = !state.isSelectionMode && !collectionState.isSelectionMode &&
            !collectionState.isDetailSelectionMode && !isSearching && !isCollectionSearching && !isDeepScreen

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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                RootBody(
                    currentTab = currentTab,
                    onTabChange = { currentTab = it },
                    sharedUrl = sharedUrl,
                    viewModel = viewModel,
                    collectionViewModel = collectionViewModel,
                    settingViewModel = settingViewModel,
                    collectionState = collectionState,
                    settingState = settingState,
                    searchState = searchState,
                    versionName = versionName,
                    context = context,
                    backStacks = backStacks,
                    isSearching = isSearching,
                    isCollectionSearching = isCollectionSearching,
                    collectionSearchQuery = collectionSearchQuery
                )
            }

            Box(modifier = Modifier.align(Alignment.TopCenter)) {
                RootToolbar(
                    currentTab = currentTab,
                    scrollBehavior = scrollBehavior,
                    focusRequester = focusRequester,
                    focusManager = focusManager,
                    isDeepScreen = isDeepScreen,
                    deepScreenTitle = deepScreenTitle,
                    isCollectionDetail = isCollectionDetail,
                    isSettingsSubScreen = isSettingsSubScreen,
                    state = state,
                    viewModel = viewModel,
                    collectionState = collectionState,
                    collectionViewModel = collectionViewModel,
                    searchState = searchState,
                    searchViewModel = searchViewModel,
                    isSearching = isSearching,
                    onSetSearching = { isSearching = it },
                    isCollectionSearching = isCollectionSearching,
                    onSetCollectionSearching = { isCollectionSearching = it },
                    collectionSearchQuery = collectionSearchQuery,
                    onSetCollectionSearchQuery = { collectionSearchQuery = it },
                    backStacks = backStacks,
                    onConfirmDelete = { action ->
                        pendingDeleteAction = action
                        showDeleteConfirmDialog = true
                    }
                )
            }

            AnimatedDock(
                visible = showDock,
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
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        BackHandler(enabled = isSearching) {
            isSearching = false
            searchViewModel.onQueryChange("")
        }

        BackHandler(enabled = isCollectionSearching) {
            isCollectionSearching = false
            collectionSearchQuery = ""
        }

        RootOverlays(
            state = state,
            viewModel = viewModel,
            collectionState = collectionState,
            collectionViewModel = collectionViewModel,
            settingState = settingState,
            settingViewModel = settingViewModel,
            showDeleteConfirmDialog = showDeleteConfirmDialog,
            onDismissDeleteConfirm = {
                showDeleteConfirmDialog = false
                pendingDeleteAction = null
            },
            pendingDeleteAction = pendingDeleteAction,
            versionName = versionName,
            context = context
        )
    }
}

@Composable
fun RootScreen(
    sharedUrl: String? = null,
    openBookmarkId: Long? = null,
    intentId: Int = 0,
    viewModel: HomeViewModel = koinViewModel(),
    collectionViewModel: CollectionViewModel = koinViewModel(),
    settingViewModel: SettingViewModel = koinViewModel(),
    searchViewModel: SearchViewModel = koinViewModel()
) = Root(
    sharedUrl = sharedUrl,
    openBookmarkId = openBookmarkId,
    intentId = intentId,
    viewModel = viewModel,
    collectionViewModel = collectionViewModel,
    settingViewModel = settingViewModel,
    searchViewModel = searchViewModel
)
