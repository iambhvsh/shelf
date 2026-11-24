package `in`.iambhvsh.shelf.nav

import kotlinx.serialization.Serializable

sealed interface AppRoute {
    val route: String

    @Serializable
    data object Home : AppRoute {
        override val route: String = "home"
    }

    @Serializable
    data object Collections : AppRoute {
        override val route: String = "collections"
    }

    @Serializable
    data object Settings : AppRoute {
        override val route: String = "settings"
    }

    @Serializable
    data class CollectionDetail(val collectionId: Long) : AppRoute {
        override val route: String = createRoute(collectionId)

        companion object {
            const val ROUTE = "collection_detail/{collectionId}"
            fun createRoute(collectionId: Long): String = "collection_detail/$collectionId"
        }
    }

    @Serializable
    data object SettingsAppearance : AppRoute {
        override val route: String = "settings_appearance"
    }

    @Serializable
    data object SettingsGeneral : AppRoute {
        override val route: String = "settings_general"
    }

    @Serializable
    data object SettingsSecurity : AppRoute {
        override val route: String = "settings_security"
    }

    @Serializable
    data object SettingsData : AppRoute {
        override val route: String = "settings_data"
    }

    @Serializable
    data object SettingsAbout : AppRoute {
        override val route: String = "settings_about"
    }

    @Serializable
    data object SettingsTrash : AppRoute {
        override val route: String = "settings_trash"
    }
}

