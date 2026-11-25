package `in`.iambhvsh.shelf

import `in`.iambhvsh.shelf.features.setting.SettingState
import `in`.iambhvsh.shelf.features.setting.TapAction
import `in`.iambhvsh.shelf.features.setting.ViewMode
import `in`.iambhvsh.shelf.theme.AccentColor
import `in`.iambhvsh.shelf.theme.ThemeMode
import org.junit.Assert.*
import org.junit.Test

class SettingStateTest {

    @Test
    fun testDefaultSettingState_hasStandardDefaults() {
        val state = SettingState()
        assertEquals(ThemeMode.SYSTEM, state.themeMode)
        assertEquals(TapAction.SHOW_PREVIEW, state.tapAction)
        assertEquals(ViewMode.GRID, state.viewMode)
        assertTrue(state.dynamicColor)
        assertEquals(AccentColor.PERIWINKLE, state.accentColor)
        assertFalse(state.appLockEnabled)
        assertEquals(0L, state.appLockTimeout)
    }

    @Test
    fun testThemeModeTransitions() {
        var state = SettingState()
        state = state.copy(themeMode = ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, state.themeMode)

        state = state.copy(themeMode = ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, state.themeMode)

        state = state.copy(themeMode = ThemeMode.OLED)
        assertEquals(ThemeMode.OLED, state.themeMode)
    }

    @Test
    fun testTapActionTransitions() {
        var state = SettingState()
        state = state.copy(tapAction = TapAction.OPEN_BROWSER)
        assertEquals(TapAction.OPEN_BROWSER, state.tapAction)

        state = state.copy(tapAction = TapAction.COPY_LINK)
        assertEquals(TapAction.COPY_LINK, state.tapAction)
    }

    @Test
    fun testViewModeToggle() {
        var state = SettingState(viewMode = ViewMode.GRID)
        state = state.copy(viewMode = ViewMode.LIST)
        assertEquals(ViewMode.LIST, state.viewMode)
    }

    @Test
    fun testAppLockTimeoutConfigurations() {
        var state = SettingState()
        state = state.copy(appLockEnabled = true, appLockTimeout = 15000L)
        assertTrue(state.appLockEnabled)
        assertEquals(15000L, state.appLockTimeout)

        state = state.copy(appLockTimeout = 60000L)
        assertEquals(60000L, state.appLockTimeout)
    }

    @Test
    fun testAccentColorsHaveDefinedLabelsAndValues() {
        assertEquals(9, AccentColor.values().size)
        for (accent in AccentColor.values()) {
            assertNotNull(accent.label)
            assertTrue(accent.label.isNotBlank())
            assertNotNull(accent.seed)
        }
    }

    @Test
    fun testResetPreservesDynamicColorCapability() {
        val current = SettingState(
            dynamicColor = false,
            isDynamicColorSupported = true
        )
        val resetState = SettingState(
            dynamicColor = true,
            isDynamicColorSupported = current.isDynamicColorSupported,
            resetSuccessToast = true
        )
        assertTrue(resetState.dynamicColor)
        assertTrue(resetState.isDynamicColorSupported)
        assertTrue(resetState.resetSuccessToast)
    }
}
