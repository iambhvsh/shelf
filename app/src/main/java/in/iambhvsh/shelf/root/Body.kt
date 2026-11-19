package `in`.iambhvsh.shelf.root

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import `in`.iambhvsh.shelf.nav.AppRoute
import `in`.iambhvsh.shelf.nav.Router
import `in`.iambhvsh.shelf.collection.CollectionDetailScreen
import `in`.iambhvsh.shelf.collection.CollectionScreen
import `in`.iambhvsh.shelf.collection.CollectionState
import `in`.iambhvsh.shelf.collection.CollectionViewModel
import `in`.iambhvsh.shelf.home.HomeScreen
import `in`.iambhvsh.shelf.home.HomeViewModel
import `in`.iambhvsh.shelf.search.SearchState
import `in`.iambhvsh.shelf.setting.SettingState
import `in`.iambhvsh.shelf.setting.SettingViewModel
import `in`.iambhvsh.shelf.setting.screens.SettingsAboutScreen
import `in`.iambhvsh.shelf.setting.screens.SettingsAppearanceScreen
import `in`.iambhvsh.shelf.setting.screens.SettingsDataScreen
import `in`.iambhvsh.shelf.setting.screens.SettingsGeneralScreen
import `in`.iambhvsh.shelf.setting.screens.SettingsMainScreen
import `in`.iambhvsh.shelf.setting.screens.SettingsSecurityScreen

@Composable
fun RootBody(
    currentTab: Int,
    onTabChange: (Int) -> Unit,
    sharedUrl: String?,
    viewModel: HomeViewModel,
    collectionViewModel: CollectionViewModel,
    settingViewModel: SettingViewModel,
    collectionState: CollectionState,
    settingState: SettingState,
    searchState: SearchState,
    versionName: String,
    context: Context,
    backStacks: List<MutableList<AppRoute>>,
    isSearching: Boolean,
    isCollectionSearching: Boolean,
    collectionSearchQuery: String,
    modifier: Modifier = Modifier
) {
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

    Router(
        currentTab = currentTab,
        onTabChange = onTabChange,
        homeScreen = {
            HomeScreen(
                sharedUrl = sharedUrl,
                tapAction = settingState.tapAction,
                viewMode = settingState.viewMode,
                searchResults = if (isSearching) searchState.searchResults else null,
                searchQuery = searchState.searchQuery,
                viewModel = viewModel
            )
        },
        collectionsScreen = { onNavigateToDetail ->
            CollectionScreen(
                onCollectionClick = onNavigateToDetail,
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
                onResetClick = { settingViewModel.onEvent(`in`.iambhvsh.shelf.setting.SettingEvents.ShowResetDialog) }
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
        },
        backStacks = backStacks,
        modifier = modifier.fillMaxSize()
    )
}
