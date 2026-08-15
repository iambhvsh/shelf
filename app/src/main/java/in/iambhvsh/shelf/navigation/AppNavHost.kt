package `in`.iambhvsh.shelf.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay

@Composable
fun AppNavHost(
    currentTab: Int,
    onTabChange: (Int) -> Unit,
    homeScreen: @Composable () -> Unit,
    collectionsScreen: @Composable (onNavigateToDetail: (Long) -> Unit) -> Unit,
    collectionDetailScreen: @Composable (collectionId: Long) -> Unit,
    settingsScreen: @Composable () -> Unit,
    backStacks: List<MutableList<AppRoute>>,
    modifier: Modifier = Modifier
) {
    val currentBackStack = backStacks[currentTab]

    BackHandler(enabled = currentTab != 0 || currentBackStack.size > 1) {
        when {
            currentBackStack.size > 1 -> currentBackStack.removeLastOrNull()
            currentTab != 0 -> onTabChange(0)
        }
    }

    NavDisplay(
        modifier = modifier,
        backStack = currentBackStack,
        onBack = { currentBackStack.removeLastOrNull() },
        transitionSpec = {
            if ((initialState as Any) is AppRoute.Collections && (targetState as Any) is AppRoute.CollectionDetail) {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(tween(300)) togetherWith slideOutHorizontally(
                    targetOffsetX = { -it / 2 },
                    animationSpec = tween(300)
                ) + fadeOut(tween(300))
            } else {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            }
        },
        popTransitionSpec = {
            if ((initialState as Any) is AppRoute.CollectionDetail && (targetState as Any) is AppRoute.Collections) {
                slideInHorizontally(
                    initialOffsetX = { -it / 2 },
                    animationSpec = tween(300)
                ) + fadeIn(tween(300)) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(tween(300))
            } else {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            }
        },
        entryProvider = { route ->
            when (route) {
                is AppRoute.Home -> NavEntry(route) { homeScreen() }
                is AppRoute.Collections -> NavEntry(route) {
                    collectionsScreen { id -> currentBackStack.add(AppRoute.CollectionDetail(id)) }
                }
                is AppRoute.CollectionDetail -> NavEntry(route) { collectionDetailScreen(route.collectionId) }
                is AppRoute.Settings -> NavEntry(route) { settingsScreen() }
            }
        }
    )
}
