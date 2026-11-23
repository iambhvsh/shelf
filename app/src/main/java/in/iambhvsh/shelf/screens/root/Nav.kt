package `in`.iambhvsh.shelf.screens.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import `in`.iambhvsh.shelf.data.Collection
import `in`.iambhvsh.shelf.features.collection.CollectionState
import `in`.iambhvsh.shelf.features.home.HomeState
import `in`.iambhvsh.shelf.nav.AppRoute

data class RootNavState(
    val currentRoute: String?,
    val isCollectionDetail: Boolean,
    val isSettingsSubScreen: Boolean,
    val isDeepScreen: Boolean,
    val currentTab: Int,
    val deepScreenTitle: String?,
    val showDock: Boolean
)

@Composable
fun rememberRootNavState(
    navBackStackEntry: NavBackStackEntry?,
    state: HomeState,
    collectionState: CollectionState,
    isSearching: Boolean,
    isCollectionSearching: Boolean
): RootNavState {
    val currentRoute = navBackStackEntry?.destination?.route

    return remember(currentRoute, state.isSelectionMode, collectionState.isSelectionMode,
        collectionState.isDetailSelectionMode, isSearching, isCollectionSearching,
        collectionState.collections, collectionState.selectedCollection) {

        val isCollectionDetail = currentRoute?.startsWith("collection_detail") == true
        val isSettingsSubScreen = currentRoute?.startsWith("settings_") == true
        val isDeepScreen = isCollectionDetail || isSettingsSubScreen

        val currentTab = when {
            currentRoute == AppRoute.Home.route -> 0
            currentRoute == AppRoute.Collections.route || isCollectionDetail -> 1
            currentRoute == AppRoute.Settings.route || isSettingsSubScreen -> 2
            else -> 0
        }

        val currentCollectionId = if (isCollectionDetail) {
            navBackStackEntry.arguments?.getLong("collectionId")
                ?: currentRoute.substringAfter("collection_detail/").toLongOrNull()
        } else null

        val collectionName = if (isCollectionDetail) {
            val matched = if (currentCollectionId == Collection.UNCATEGORISED_ID) {
                "Uncategorised"
            } else {
                collectionState.collections.find { it.id == currentCollectionId }?.name
                    ?: collectionState.selectedCollection?.takeIf { it.id == currentCollectionId }?.name
            }
            matched ?: collectionState.selectedCollection?.name ?: "Collection"
        } else null

        val settingsSubScreenTitle = when (currentRoute) {
            AppRoute.SettingsAppearance.route -> "Appearance"
            AppRoute.SettingsGeneral.route -> "General"
            AppRoute.SettingsSecurity.route -> "Security"
            AppRoute.SettingsData.route -> "Data & Storage"
            AppRoute.SettingsAbout.route -> "About"
            else -> null
        }

        val deepScreenTitle = if (isCollectionDetail) collectionName else settingsSubScreenTitle
        val showDock = !state.isSelectionMode && !collectionState.isSelectionMode &&
                !collectionState.isDetailSelectionMode && !isSearching && !isCollectionSearching && !isDeepScreen

        RootNavState(
            currentRoute = currentRoute,
            isCollectionDetail = isCollectionDetail,
            isSettingsSubScreen = isSettingsSubScreen,
            isDeepScreen = isDeepScreen,
            currentTab = currentTab,
            deepScreenTitle = deepScreenTitle,
            showDock = showDock
        )
    }
}

fun navigateRootTab(navController: NavController, currentRoute: String?, tab: Int) {
    val targetRoute = when (tab) {
        0 -> AppRoute.Home.route
        1 -> AppRoute.Collections.route
        else -> AppRoute.Settings.route
    }
    if (currentRoute != targetRoute) {
        navController.navigate(targetRoute) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}
