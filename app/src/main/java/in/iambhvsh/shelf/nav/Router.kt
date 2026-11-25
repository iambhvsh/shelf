package `in`.iambhvsh.shelf.nav

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import `in`.iambhvsh.shelf.screens.root.canNavigateFromEntry

private val navItems = listOf(
    AppRoute.Home.route,
    AppRoute.Collections.route,
    AppRoute.Settings.route
)

private fun isDeepRoute(route: String?): Boolean =
    route?.startsWith("collection_detail") == true ||
    route?.startsWith("settings_") == true

private val navEnterAnimationSpec = spring<IntOffset>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessLow
)

private val navExitAnimationSpec = spring<IntOffset>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessLow
)

@Composable
fun Router(
    navController: NavHostController,
    homeScreen: @Composable () -> Unit,
    collectionsScreen: @Composable (onNavigateToDetail: (Long) -> Unit) -> Unit,
    collectionDetailScreen: @Composable (collectionId: Long) -> Unit,
    settingsMainScreen: @Composable (onNavigate: (AppRoute) -> Unit) -> Unit,
    settingsAppearanceScreen: @Composable () -> Unit,
    settingsGeneralScreen: @Composable () -> Unit,
    settingsSecurityScreen: @Composable () -> Unit,
    settingsDataScreen: @Composable (onNavigateToTrash: () -> Unit) -> Unit,
    settingsAboutScreen: @Composable () -> Unit,
    settingsTrashScreen: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Home.route,
        modifier = modifier.fillMaxSize(),
        enterTransition = {
            val initialRoute = initialState.destination.route
            val targetRoute = targetState.destination.route

            val isTargetDeep = isDeepRoute(targetRoute)

            if (isTargetDeep) {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = navEnterAnimationSpec
                ) + fadeIn()
            } else {
                val initialIndex = navItems.indexOfFirst { it == initialRoute }
                val targetIndex = navItems.indexOfFirst { it == targetRoute }

                if (targetIndex > initialIndex && initialIndex != -1) {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = navEnterAnimationSpec
                    ) + fadeIn()
                } else if (targetIndex < initialIndex && targetIndex != -1) {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = navEnterAnimationSpec
                    ) + fadeIn()
                } else {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = navEnterAnimationSpec
                    ) + fadeIn()
                }
            }
        },
        exitTransition = {
            val initialRoute = initialState.destination.route
            val targetRoute = targetState.destination.route

            val isTargetDeep = isDeepRoute(targetRoute)
            val isInitialDeep = isDeepRoute(initialRoute)

            if (isTargetDeep) {
                slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = navExitAnimationSpec
                ) + fadeOut()
            } else if (isInitialDeep) {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = navExitAnimationSpec
                ) + fadeOut()
            } else {
                val initialIndex = navItems.indexOfFirst { it == initialRoute }
                val targetIndex = navItems.indexOfFirst { it == targetRoute }

                if (targetIndex > initialIndex) {
                    slideOutHorizontally(
                        targetOffsetX = { -it / 3 },
                        animationSpec = navExitAnimationSpec
                    ) + fadeOut()
                } else {
                    slideOutHorizontally(
                        targetOffsetX = { it / 3 },
                        animationSpec = navExitAnimationSpec
                    ) + fadeOut()
                }
            }
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = navEnterAnimationSpec
            ) + fadeIn()
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = navEnterAnimationSpec
            ) + fadeOut()
        }
    ) {
        composable(AppRoute.Home.route) { backStackEntry ->
            RouteSurface(backStackEntry, navController) {
                homeScreen()
            }
        }
        composable(AppRoute.Collections.route) { backStackEntry ->
            RouteSurface(backStackEntry, navController) {
                collectionsScreen { id ->
                    if (canNavigateFromEntry(navController.currentBackStackEntry?.id, backStackEntry.id)) {
                        navController.navigate(AppRoute.CollectionDetail.createRoute(id)) {
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
        composable(
            route = AppRoute.CollectionDetail.ROUTE,
            arguments = listOf(
                navArgument("collectionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val collectionId = backStackEntry.arguments?.getLong("collectionId") ?: 0L
            RouteSurface(backStackEntry, navController) {
                collectionDetailScreen(collectionId)
            }
        }
        composable(AppRoute.Settings.route) { backStackEntry ->
            RouteSurface(backStackEntry, navController) {
                settingsMainScreen { targetRoute ->
                    if (canNavigateFromEntry(navController.currentBackStackEntry?.id, backStackEntry.id)) {
                        navController.navigate(targetRoute.route) {
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
        composable(AppRoute.SettingsAppearance.route) { backStackEntry ->
            RouteSurface(backStackEntry, navController) {
                settingsAppearanceScreen()
            }
        }
        composable(AppRoute.SettingsGeneral.route) { backStackEntry ->
            RouteSurface(backStackEntry, navController) {
                settingsGeneralScreen()
            }
        }
        composable(AppRoute.SettingsSecurity.route) { backStackEntry ->
            RouteSurface(backStackEntry, navController) {
                settingsSecurityScreen()
            }
        }
        composable(AppRoute.SettingsData.route) { backStackEntry ->
            RouteSurface(backStackEntry, navController) {
                settingsDataScreen {
                    if (canNavigateFromEntry(navController.currentBackStackEntry?.id, backStackEntry.id)) {
                        navController.navigate(AppRoute.SettingsTrash.route) {
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
        composable(AppRoute.SettingsAbout.route) { backStackEntry ->
            RouteSurface(backStackEntry, navController) {
                settingsAboutScreen()
            }
        }
        composable(AppRoute.SettingsTrash.route) { backStackEntry ->
            RouteSurface(backStackEntry, navController) {
                settingsTrashScreen()
            }
        }
    }
}

@Composable
private fun RouteSurface(
    entry: NavBackStackEntry,
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(entry, navController) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (navController.currentBackStackEntry?.id != entry.id) {
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
            }
    ) {
        content()
    }
}
