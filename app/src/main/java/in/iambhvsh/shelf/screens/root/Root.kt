@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package `in`.iambhvsh.shelf.screens.root

import `in`.iambhvsh.shelf.components.root.*
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import `in`.iambhvsh.shelf.features.collection.CollectionViewModel
import `in`.iambhvsh.shelf.features.home.HomeEvents
import `in`.iambhvsh.shelf.features.home.HomeViewModel
import `in`.iambhvsh.shelf.features.search.SearchViewModel
import `in`.iambhvsh.shelf.features.setting.SettingEvents
import `in`.iambhvsh.shelf.features.setting.SettingViewModel
import `in`.iambhvsh.shelf.theme.ShelfTheme
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

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    var isSearching by remember { mutableStateOf(false) }
    var isCollectionSearching by remember { mutableStateOf(false) }
    var collectionSearchQuery by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var pendingDeleteAction: (() -> Unit)? by remember { mutableStateOf(null) }

    val navState = rememberRootNavState(
        navBackStackEntry = navBackStackEntry,
        state = state,
        collectionState = collectionState,
        isSearching = isSearching,
        isCollectionSearching = isCollectionSearching
    )

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val enterAlwaysScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val pinnedScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scrollBehavior = if (navState.isDeepScreen) pinnedScrollBehavior else enterAlwaysScrollBehavior

    LaunchedEffect(isSearching) {
        if (isSearching) focusRequester.requestFocus()
    }
    LaunchedEffect(Unit) {
        if (!settingViewModel.settingsRepository.isFirstLaunchPermissionsShown()) {
            settingViewModel.onEvent(SettingEvents.ShowPermissionsSheet)
        }
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
    LaunchedEffect(navState.currentRoute) {
        if (!navState.isDeepScreen) {
            enterAlwaysScrollBehavior.state.heightOffset = 0f
            enterAlwaysScrollBehavior.state.contentOffset = 0f
        }
        if (navState.isCollectionDetail && isCollectionSearching) {
            isCollectionSearching = false
            collectionSearchQuery = ""
        }
    }

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
            RootBody(
                navController = navController,
                sharedUrl = sharedUrl,
                viewModel = viewModel,
                collectionViewModel = collectionViewModel,
                settingViewModel = settingViewModel,
                collectionState = collectionState,
                settingState = settingState,
                searchState = searchState,
                versionName = versionName,
                context = context,
                isSearching = isSearching,
                isCollectionSearching = isCollectionSearching,
                isCollectionDetail = navState.isCollectionDetail,
                collectionSearchQuery = collectionSearchQuery,
                scrollBehavior = scrollBehavior,
                modifier = Modifier.fillMaxSize()
            )

            Box(modifier = Modifier.align(Alignment.TopCenter)) {
                RootToolbar(
                    currentTab = navState.currentTab,
                    scrollBehavior = scrollBehavior,
                    focusRequester = focusRequester,
                    focusManager = focusManager,
                    isDeepScreen = navState.isDeepScreen,
                    deepScreenTitle = navState.deepScreenTitle,
                    isCollectionDetail = navState.isCollectionDetail,
                    isSettingsSubScreen = navState.isSettingsSubScreen,
                    state = state,
                    viewModel = viewModel,
                    collectionState = collectionState,
                    collectionViewModel = collectionViewModel,
                    searchState = searchState,
                    searchViewModel = searchViewModel,
                    settingState = settingState,
                    settingViewModel = settingViewModel,
                    isSearching = isSearching,
                    onSetSearching = { isSearching = it },
                    isCollectionSearching = isCollectionSearching,
                    onSetCollectionSearching = { isCollectionSearching = it },
                    collectionSearchQuery = collectionSearchQuery,
                    onSetCollectionSearchQuery = { collectionSearchQuery = it },
                    onBack = { navController.popBackStack() },
                    onConfirmDelete = { action ->
                        pendingDeleteAction = action
                        showDeleteConfirmDialog = true
                    }
                )
            }

            AnimatedDock(
                visible = navState.showDock,
                currentTab = navState.currentTab,
                onTabChange = { tab ->
                    enterAlwaysScrollBehavior.state.heightOffset = 0f
                    enterAlwaysScrollBehavior.state.contentOffset = 0f
                    navigateRootTab(navController, navState.currentRoute, tab)
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        BackHandler(enabled = isSearching) {
            enterAlwaysScrollBehavior.state.heightOffset = 0f
            enterAlwaysScrollBehavior.state.contentOffset = 0f
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
