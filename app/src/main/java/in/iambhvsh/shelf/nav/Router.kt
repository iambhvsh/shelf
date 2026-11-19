package `in`.iambhvsh.shelf.nav

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay

private fun parseRoute(raw: Any?): AppRoute? = when (raw) {
    is AppRoute -> raw
    is Scene<*> -> parseRoute(raw.key) ?: raw.entries.lastOrNull()?.let { parseRoute(it.contentKey) }
    is String -> when {
        raw == "Home" || raw.endsWith(".Home") -> AppRoute.Home
        raw == "Collections" || raw.endsWith(".Collections") -> AppRoute.Collections
        raw == "Settings" || raw.endsWith(".Settings") -> AppRoute.Settings
        raw == "SettingsAppearance" || raw.endsWith(".SettingsAppearance") -> AppRoute.SettingsAppearance
        raw == "SettingsGeneral" || raw.endsWith(".SettingsGeneral") -> AppRoute.SettingsGeneral
        raw == "SettingsSecurity" || raw.endsWith(".SettingsSecurity") -> AppRoute.SettingsSecurity
        raw == "SettingsData" || raw.endsWith(".SettingsData") -> AppRoute.SettingsData
        raw == "SettingsAbout" || raw.endsWith(".SettingsAbout") -> AppRoute.SettingsAbout
        raw.contains("CollectionDetail") -> {
            val id = raw.substringAfter("collectionId=").substringBefore(")").toLongOrNull() ?: 0L
            AppRoute.CollectionDetail(id)
        }
        else -> null
    }
    else -> null
}

private fun extractRoute(state: Any?): AppRoute? = parseRoute(state)

private fun getRouteIndex(route: Any?): Int = when (route) {
    is AppRoute.Home -> 0
    is AppRoute.Collections -> 1
    is AppRoute.CollectionDetail -> 1
    is AppRoute.Settings,
    is AppRoute.SettingsAppearance,
    is AppRoute.SettingsGeneral,
    is AppRoute.SettingsSecurity,
    is AppRoute.SettingsData,
    is AppRoute.SettingsAbout -> 2
    else -> -1
}

private fun isDeepRoute(route: Any?): Boolean =
    route is AppRoute.CollectionDetail ||
    route is AppRoute.SettingsAppearance ||
    route is AppRoute.SettingsGeneral ||
    route is AppRoute.SettingsSecurity ||
    route is AppRoute.SettingsData ||
    route is AppRoute.SettingsAbout

private val navEnterAnimationSpec = spring<IntOffset>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessLow
)

private val navExitAnimationSpec = spring<IntOffset>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessLow
)

private fun resolveForwardTransition(initialState: AppRoute?, targetState: AppRoute?): ContentTransform {
    val initialIndex = getRouteIndex(initialState)
    val targetIndex = getRouteIndex(targetState)

    if (isDeepRoute(targetState) && (initialIndex == targetIndex || initialIndex == -1)) {
        return (slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = navEnterAnimationSpec
        ) + fadeIn()) togetherWith (slideOutHorizontally(
            targetOffsetX = { -it / 3 },
            animationSpec = navExitAnimationSpec
        ) + fadeOut())
    }

    return if (targetIndex > initialIndex && initialIndex != -1) {
        (slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = navEnterAnimationSpec
        ) + fadeIn()) togetherWith (slideOutHorizontally(
            targetOffsetX = { -it / 3 },
            animationSpec = navExitAnimationSpec
        ) + fadeOut())
    } else if (targetIndex < initialIndex && targetIndex != -1) {
        (slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = navEnterAnimationSpec
        ) + fadeIn()) togetherWith (slideOutHorizontally(
            targetOffsetX = { it / 3 },
            animationSpec = navExitAnimationSpec
        ) + fadeOut())
    } else {
        (slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = navEnterAnimationSpec
        ) + fadeIn()) togetherWith (slideOutHorizontally(
            targetOffsetX = { -it / 3 },
            animationSpec = navExitAnimationSpec
        ) + fadeOut())
    }
}

private fun resolvePopTransition(initialState: AppRoute?, targetState: AppRoute?): ContentTransform {
    val initialIndex = getRouteIndex(initialState)
    val targetIndex = getRouteIndex(targetState)

    if (isDeepRoute(initialState) && (targetIndex == initialIndex || targetIndex == -1)) {
        return (slideInHorizontally(
            initialOffsetX = { -it / 3 },
            animationSpec = navEnterAnimationSpec
        ) + fadeIn()) togetherWith (slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = navEnterAnimationSpec
        ) + fadeOut())
    }

    return if (targetIndex < initialIndex && targetIndex != -1) {
        (slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = navEnterAnimationSpec
        ) + fadeIn()) togetherWith (slideOutHorizontally(
            targetOffsetX = { it / 3 },
            animationSpec = navExitAnimationSpec
        ) + fadeOut())
    } else {
        (slideInHorizontally(
            initialOffsetX = { -it / 3 },
            animationSpec = navEnterAnimationSpec
        ) + fadeIn()) togetherWith (slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = navEnterAnimationSpec
        ) + fadeOut())
    }
}

@Composable
fun Router(
    currentTab: Int,
    onTabChange: (Int) -> Unit,
    homeScreen: @Composable () -> Unit,
    collectionsScreen: @Composable (onNavigateToDetail: (Long) -> Unit) -> Unit,
    collectionDetailScreen: @Composable (collectionId: Long) -> Unit,
    settingsMainScreen: @Composable (onNavigate: (AppRoute) -> Unit) -> Unit,
    settingsAppearanceScreen: @Composable () -> Unit,
    settingsGeneralScreen: @Composable () -> Unit,
    settingsSecurityScreen: @Composable () -> Unit,
    settingsDataScreen: @Composable () -> Unit,
    settingsAboutScreen: @Composable () -> Unit,
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
            resolveForwardTransition(extractRoute(initialState), extractRoute(targetState))
        },
        popTransitionSpec = {
            resolvePopTransition(extractRoute(initialState), extractRoute(targetState))
        },
        predictivePopTransitionSpec = {
            resolvePopTransition(extractRoute(initialState), extractRoute(targetState))
        },
        entryProvider = { route ->
            when (route) {
                is AppRoute.Home -> NavEntry(route) { homeScreen() }
                is AppRoute.Collections -> NavEntry(route) {
                    collectionsScreen { id -> currentBackStack.add(AppRoute.CollectionDetail(id)) }
                }
                is AppRoute.CollectionDetail -> NavEntry(route) { collectionDetailScreen(route.collectionId) }
                is AppRoute.Settings -> NavEntry(route) {
                    settingsMainScreen { subRoute -> currentBackStack.add(subRoute) }
                }
                is AppRoute.SettingsAppearance -> NavEntry(route) { settingsAppearanceScreen() }
                is AppRoute.SettingsGeneral -> NavEntry(route) { settingsGeneralScreen() }
                is AppRoute.SettingsSecurity -> NavEntry(route) { settingsSecurityScreen() }
                is AppRoute.SettingsData -> NavEntry(route) { settingsDataScreen() }
                is AppRoute.SettingsAbout -> NavEntry(route) { settingsAboutScreen() }
            }
        }
    )
}

@Composable
fun AppNavHost(
    currentTab: Int,
    onTabChange: (Int) -> Unit,
    homeScreen: @Composable () -> Unit,
    collectionsScreen: @Composable (onNavigateToDetail: (Long) -> Unit) -> Unit,
    collectionDetailScreen: @Composable (collectionId: Long) -> Unit,
    settingsMainScreen: @Composable (onNavigate: (AppRoute) -> Unit) -> Unit,
    settingsAppearanceScreen: @Composable () -> Unit,
    settingsGeneralScreen: @Composable () -> Unit,
    settingsSecurityScreen: @Composable () -> Unit,
    settingsDataScreen: @Composable () -> Unit,
    settingsAboutScreen: @Composable () -> Unit,
    backStacks: List<MutableList<AppRoute>>,
    modifier: Modifier = Modifier
) = Router(
    currentTab = currentTab,
    onTabChange = onTabChange,
    homeScreen = homeScreen,
    collectionsScreen = collectionsScreen,
    collectionDetailScreen = collectionDetailScreen,
    settingsMainScreen = settingsMainScreen,
    settingsAppearanceScreen = settingsAppearanceScreen,
    settingsGeneralScreen = settingsGeneralScreen,
    settingsSecurityScreen = settingsSecurityScreen,
    settingsDataScreen = settingsDataScreen,
    settingsAboutScreen = settingsAboutScreen,
    backStacks = backStacks,
    modifier = modifier
)

