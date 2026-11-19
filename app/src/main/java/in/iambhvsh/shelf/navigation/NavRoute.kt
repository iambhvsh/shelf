package `in`.iambhvsh.shelf.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute {
    @Serializable data object Home : AppRoute
    @Serializable data object Collections : AppRoute
    @Serializable data object Settings : AppRoute
    @Serializable data class CollectionDetail(val collectionId: Long) : AppRoute

    // Settings sub-routes
    @Serializable data object SettingsAppearance : AppRoute
    @Serializable data object SettingsGeneral : AppRoute
    @Serializable data object SettingsSecurity : AppRoute
    @Serializable data object SettingsData : AppRoute
    @Serializable data object SettingsAbout : AppRoute
}
