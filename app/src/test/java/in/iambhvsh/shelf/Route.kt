package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.nav.AppRoute
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationRouteTest {

    @Test
    fun testCoreRoutes_matchExpectedRouteNames() {
        assertEquals("home", AppRoute.Home.route)
        assertEquals("collections", AppRoute.Collections.route)
        assertEquals("settings", AppRoute.Settings.route)
    }

    @Test
    fun testCollectionDetailRoute_generatesParametrizedPath() {
        val detailRoute = AppRoute.CollectionDetail(99L)
        assertEquals("collection_detail/99", detailRoute.route)
        assertEquals("collection_detail/99", AppRoute.CollectionDetail.createRoute(99L))
        assertEquals("collection_detail/{collectionId}", AppRoute.CollectionDetail.ROUTE)
    }

    @Test
    fun testSettingsSubRoutes_matchExpectedKeys() {
        assertEquals("settings_appearance", AppRoute.SettingsAppearance.route)
        assertEquals("settings_general", AppRoute.SettingsGeneral.route)
        assertEquals("settings_security", AppRoute.SettingsSecurity.route)
        assertEquals("settings_data", AppRoute.SettingsData.route)
        assertEquals("settings_about", AppRoute.SettingsAbout.route)
        assertEquals("settings_trash", AppRoute.SettingsTrash.route)
    }
}
