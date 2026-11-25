package `in`.iambhvsh.shelf

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import `in`.iambhvsh.shelf.data.SettingsRepositoryImpl
import `in`.iambhvsh.shelf.features.setting.TapAction
import `in`.iambhvsh.shelf.features.setting.ViewMode
import `in`.iambhvsh.shelf.theme.AccentColor
import `in`.iambhvsh.shelf.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PreferencesTest {

    private class InMemorySharedPreferences : SharedPreferences {
        private val store = linkedMapOf<String, Any?>()
        private val listeners = mutableSetOf<SharedPreferences.OnSharedPreferenceChangeListener>()

        fun putRaw(key: String, value: Any?) {
            store[key] = value
        }

        override fun getAll(): MutableMap<String, *> = store.toMutableMap()

        override fun getString(key: String?, defValue: String?): String? =
            (store[key] as? String) ?: defValue

        @Suppress("UNCHECKED_CAST")
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
            (store[key] as? Set<String>)?.toMutableSet() ?: defValues

        override fun getInt(key: String?, defValue: Int): Int =
            (store[key] as? Int) ?: defValue

        override fun getLong(key: String?, defValue: Long): Long =
            (store[key] as? Long) ?: defValue

        override fun getFloat(key: String?, defValue: Float): Float =
            (store[key] as? Float) ?: defValue

        override fun getBoolean(key: String?, defValue: Boolean): Boolean =
            (store[key] as? Boolean) ?: defValue

        override fun contains(key: String?): Boolean = store.containsKey(key)

        override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
            private val staged = linkedMapOf<String, Any?>()
            private val removed = mutableSetOf<String>()
            private var clearAll = false

            override fun putString(key: String?, value: String?) = apply {
                if (key != null) {
                    staged[key] = value
                    removed.remove(key)
                }
            }

            override fun putStringSet(key: String?, values: MutableSet<String>?) = apply {
                if (key != null) {
                    staged[key] = values?.toSet()
                    removed.remove(key)
                }
            }

            override fun putInt(key: String?, value: Int) = apply {
                if (key != null) {
                    staged[key] = value
                    removed.remove(key)
                }
            }

            override fun putLong(key: String?, value: Long) = apply {
                if (key != null) {
                    staged[key] = value
                    removed.remove(key)
                }
            }

            override fun putFloat(key: String?, value: Float) = apply {
                if (key != null) {
                    staged[key] = value
                    removed.remove(key)
                }
            }

            override fun putBoolean(key: String?, value: Boolean) = apply {
                if (key != null) {
                    staged[key] = value
                    removed.remove(key)
                }
            }

            override fun remove(key: String?) = apply {
                if (key != null) {
                    removed.add(key)
                    staged.remove(key)
                }
            }

            override fun clear() = apply {
                clearAll = true
                staged.clear()
                removed.clear()
            }

            override fun commit(): Boolean {
                apply()
                return true
            }

            override fun apply() {
                if (clearAll) {
                    store.clear()
                }
                removed.forEach { store.remove(it) }
                staged.forEach { (k, v) ->
                    if (v == null) store.remove(k) else store[k] = v
                }
            }
        }

        override fun registerOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener?
        ) {
            if (listener != null) listeners.add(listener)
        }

        override fun unregisterOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener?
        ) {
            if (listener != null) listeners.remove(listener)
        }
    }

    private fun createContextAndPrefs(): Pair<Context, InMemorySharedPreferences> {
        val prefs = InMemorySharedPreferences()
        val context = object : ContextWrapper(null) {
            override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences {
                assertEquals("shelf_settings", name)
                assertEquals(Context.MODE_PRIVATE, mode)
                return prefs
            }
        }
        return context to prefs
    }

    @Test
    fun testDefaultsMatchExpectedContract() {
        val (context, _) = createContextAndPrefs()
        val repo = SettingsRepositoryImpl(context)

        assertEquals(ThemeMode.SYSTEM, repo.getThemeMode())
        assertEquals(TapAction.SHOW_PREVIEW, repo.getTapAction())
        assertTrue(repo.getDynamicColor())
        assertEquals(AccentColor.PERIWINKLE, repo.getAccentColor())
        assertEquals(ViewMode.GRID, repo.getViewMode())
        assertFalse(repo.getAutoBackupEnabled())
        assertNull(repo.getAutoBackupUri())
        assertFalse(repo.getAppLockEnabled())
        assertTrue(repo.getAppLockUsePinEnabled())
        assertEquals(0L, repo.getAppLockTimeout())
        assertEquals(0L, repo.getLastUpdateCheckTime())
        assertNull(repo.getLatestAvailableVersion())
        assertNull(repo.getLatestReleaseUrl())
        assertFalse(repo.isFirstLaunchPermissionsShown())
    }

    @Test
    fun testSettersPersistAllValuesAndSupportNullableClears() {
        val (context, _) = createContextAndPrefs()
        val repo = SettingsRepositoryImpl(context)

        ThemeMode.entries.forEach { mode ->
            repo.setThemeMode(mode)
            assertEquals(mode, repo.getThemeMode())
        }
        TapAction.entries.forEach { action ->
            repo.setTapAction(action)
            assertEquals(action, repo.getTapAction())
        }
        AccentColor.entries.forEach { color ->
            repo.setAccentColor(color)
            assertEquals(color, repo.getAccentColor())
        }
        ViewMode.entries.forEach { viewMode ->
            repo.setViewMode(viewMode)
            assertEquals(viewMode, repo.getViewMode())
        }

        repo.setDynamicColor(false)
        assertFalse(repo.getDynamicColor())

        repo.setAutoBackupEnabled(true)
        assertTrue(repo.getAutoBackupEnabled())

        repo.setAutoBackupUri("content://tree/backup")
        assertEquals("content://tree/backup", repo.getAutoBackupUri())
        repo.setAutoBackupUri(null)
        assertNull(repo.getAutoBackupUri())

        repo.setAppLockEnabled(true)
        assertTrue(repo.getAppLockEnabled())

        repo.setAppLockUsePinEnabled(false)
        assertFalse(repo.getAppLockUsePinEnabled())

        repo.setAppLockTimeout(300_000L)
        assertEquals(300_000L, repo.getAppLockTimeout())

        repo.setLastUpdateCheckTime(1_700_000_000L)
        assertEquals(1_700_000_000L, repo.getLastUpdateCheckTime())

        repo.setLatestAvailableVersion("v2.5.0")
        assertEquals("v2.5.0", repo.getLatestAvailableVersion())
        repo.setLatestAvailableVersion(null)
        assertNull(repo.getLatestAvailableVersion())

        repo.setLatestReleaseUrl("https://github.com/org/repo/releases/download/v2.5.0/app.apk")
        assertEquals("https://github.com/org/repo/releases/download/v2.5.0/app.apk", repo.getLatestReleaseUrl())
        repo.setLatestReleaseUrl(null)
        assertNull(repo.getLatestReleaseUrl())

        repo.setFirstLaunchPermissionsShown(true)
        assertTrue(repo.isFirstLaunchPermissionsShown())
    }

    @Test
    fun testCorruptedOutOfRangeEnumOrdinalsFallBackSafely() {
        val (context, prefs) = createContextAndPrefs()
        prefs.putRaw("theme_mode", 99)
        prefs.putRaw("tap_action", -1)
        prefs.putRaw("accent_color", 42)
        prefs.putRaw("view_mode", 7)

        val repo = SettingsRepositoryImpl(context)
        assertEquals(ThemeMode.SYSTEM, repo.getThemeMode())
        assertEquals(TapAction.SHOW_PREVIEW, repo.getTapAction())
        assertEquals(AccentColor.PERIWINKLE, repo.getAccentColor())
        assertEquals(ViewMode.GRID, repo.getViewMode())
    }

    @Test
    fun testResetToDefaults_clearsEveryPersistedSetting() {
        val (context, _) = createContextAndPrefs()
        val repo = SettingsRepositoryImpl(context)

        repo.setThemeMode(ThemeMode.OLED)
        repo.setTapAction(TapAction.OPEN_BROWSER)
        repo.setDynamicColor(false)
        repo.setAccentColor(AccentColor.TEAL)
        repo.setViewMode(ViewMode.LIST)
        repo.setAutoBackupEnabled(true)
        repo.setAutoBackupUri("content://tree/backup")
        repo.setAppLockEnabled(true)
        repo.setAppLockUsePinEnabled(false)
        repo.setAppLockTimeout(60_000L)
        repo.setLastUpdateCheckTime(999L)
        repo.setLatestAvailableVersion("v9.9.9")
        repo.setLatestReleaseUrl("https://example.com/app.apk")
        repo.setFirstLaunchPermissionsShown(true)

        repo.resetToDefaults()

        assertEquals(ThemeMode.SYSTEM, repo.getThemeMode())
        assertEquals(TapAction.SHOW_PREVIEW, repo.getTapAction())
        assertTrue(repo.getDynamicColor())
        assertEquals(AccentColor.PERIWINKLE, repo.getAccentColor())
        assertEquals(ViewMode.GRID, repo.getViewMode())
        assertFalse(repo.getAutoBackupEnabled())
        assertNull(repo.getAutoBackupUri())
        assertFalse(repo.getAppLockEnabled())
        assertTrue(repo.getAppLockUsePinEnabled())
        assertEquals(0L, repo.getAppLockTimeout())
        assertEquals(0L, repo.getLastUpdateCheckTime())
        assertNull(repo.getLatestAvailableVersion())
        assertNull(repo.getLatestReleaseUrl())
        assertFalse(repo.isFirstLaunchPermissionsShown())
    }
}
