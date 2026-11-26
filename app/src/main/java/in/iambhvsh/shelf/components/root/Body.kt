package `in`.iambhvsh.shelf.components.root

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.nav.AppRoute
import `in`.iambhvsh.shelf.nav.Router
import `in`.iambhvsh.shelf.screens.collection.CollectionDetailScreen
import `in`.iambhvsh.shelf.screens.collection.CollectionScreen
import `in`.iambhvsh.shelf.features.collection.CollectionState
import `in`.iambhvsh.shelf.features.collection.CollectionViewModel
import `in`.iambhvsh.shelf.screens.home.HomeScreen
import `in`.iambhvsh.shelf.features.home.HomeViewModel
import `in`.iambhvsh.shelf.features.search.SearchState
import `in`.iambhvsh.shelf.features.setting.SettingState
import `in`.iambhvsh.shelf.features.setting.SettingViewModel
import `in`.iambhvsh.shelf.screens.setting.SettingsAboutScreen
import `in`.iambhvsh.shelf.screens.setting.SettingsAppearanceScreen
import `in`.iambhvsh.shelf.screens.setting.SettingsDataScreen
import `in`.iambhvsh.shelf.screens.setting.SettingsGeneralScreen
import `in`.iambhvsh.shelf.screens.setting.SettingsMainScreen
import `in`.iambhvsh.shelf.screens.setting.SettingsSecurityScreen
import `in`.iambhvsh.shelf.screens.setting.SettingsTrashScreen

internal fun filterCollectionSearchBookmarks(
    bookmarks: List<Bookmark>,
    query: String
): List<Bookmark> {
    if (query.isBlank()) return bookmarks
    return bookmarks.filter { bm ->
        (bm.title?.contains(query, ignoreCase = true) == true) ||
            bm.url.contains(query, ignoreCase = true) ||
            (bm.description?.contains(query, ignoreCase = true) == true) ||
            (bm.note?.contains(query, ignoreCase = true) == true)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootBody(
    navController: NavHostController,
    sharedUrl: String?,
    viewModel: HomeViewModel,
    collectionViewModel: CollectionViewModel,
    settingViewModel: SettingViewModel,
    collectionState: CollectionState,
    settingState: SettingState,
    searchState: SearchState,
    versionName: String,
    context: Context,
    isSearching: Boolean,
    isCollectionSearching: Boolean,
    isCollectionDetail: Boolean,
    collectionSearchQuery: String,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier
) {
    val collectionSearchResults = remember(
        collectionState.collectionBookmarks,
        collectionSearchQuery,
        isCollectionSearching,
        isCollectionDetail
    ) {
        if (!isCollectionSearching || !isCollectionDetail) null
        else filterCollectionSearchBookmarks(collectionState.collectionBookmarks, collectionSearchQuery)
    }

    Router(
        navController = navController,
        homeScreen = {
            HomeScreen(
                sharedUrl = null,
                tapAction = settingState.tapAction,
                viewMode = settingState.viewMode,
                isSearching = isSearching,
                searchResults = searchState.searchResults,
                searchQuery = searchState.searchQuery,
                isSearchLoading = searchState.isLoading,
                viewModel = viewModel,
                scrollBehavior = scrollBehavior
            )
        },
        collectionsScreen = { onNavigateToDetail ->
            CollectionScreen(
                onCollectionClick = onNavigateToDetail,
                searchQuery = collectionSearchQuery,
                isSearching = isCollectionSearching && !isCollectionDetail,
                viewModel = collectionViewModel
            )
        },
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
                onNavigate = onNavigate,
                onResetClick = { settingViewModel.onEvent(`in`.iambhvsh.shelf.features.setting.SettingEvents.ShowResetDialog) }
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
        settingsDataScreen = { onNavigateToTrash ->
            SettingsDataScreen(
                state = settingState,
                viewModel = settingViewModel,
                onNavigateToTrash = onNavigateToTrash
            )
        },
        settingsAboutScreen = {
            SettingsAboutScreen(
                state = settingState,
                viewModel = settingViewModel,
                versionName = versionName,
                context = context
            )
        },
        settingsTrashScreen = {
            SettingsTrashScreen(
                state = settingState,
                viewModel = settingViewModel
            )
        },
        modifier = modifier.fillMaxSize()
    )
}
