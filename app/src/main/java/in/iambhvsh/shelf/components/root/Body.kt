package `in`.iambhvsh.shelf.components.root

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
