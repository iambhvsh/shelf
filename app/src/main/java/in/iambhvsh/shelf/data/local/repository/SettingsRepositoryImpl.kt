package `in`.iambhvsh.shelf.data.local.repository

import android.content.Context
import `in`.iambhvsh.shelf.domain.repository.SettingsRepository
import `in`.iambhvsh.shelf.presentation.setting.TapAction
import `in`.iambhvsh.shelf.presentation.setting.ViewMode
import `in`.iambhvsh.shelf.ui.theme.AccentColor
import `in`.iambhvsh.shelf.ui.theme.ThemeMode
import androidx.core.content.edit

class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {

    companion object {
        private const val PREFS_NAME = "shelf_settings"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_TAP_ACTION = "tap_action"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_ACCENT_COLOR = "accent_color"
        private const val KEY_VIEW_MODE = "view_mode"
        private const val KEY_AUTO_BACKUP = "auto_backup"
        private const val KEY_APP_LOCK = "app_lock"
        private const val KEY_LAST_UPDATE_CHECK_TIME = "last_update_check_time"
        private const val KEY_LATEST_AVAILABLE_VERSION = "latest_available_version"
        private const val KEY_LATEST_RELEASE_URL = "latest_release_url"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getThemeMode(): ThemeMode {
        val ordinal = prefs.getInt(KEY_THEME_MODE, ThemeMode.SYSTEM.ordinal)
        return ThemeMode.entries.getOrElse(ordinal) { ThemeMode.SYSTEM }
    }

    override fun setThemeMode(mode: ThemeMode) {
        prefs.edit { putInt(KEY_THEME_MODE, mode.ordinal) }
    }

    override fun getTapAction(): TapAction {
        val ordinal = prefs.getInt(KEY_TAP_ACTION, TapAction.SHOW_PREVIEW.ordinal)
        return TapAction.entries.getOrElse(ordinal) { TapAction.SHOW_PREVIEW }
    }

    override fun setTapAction(action: TapAction) {
        prefs.edit { putInt(KEY_TAP_ACTION, action.ordinal) }
    }

    override fun getDynamicColor(): Boolean {
        return prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
    }

    override fun setDynamicColor(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_DYNAMIC_COLOR, enabled) }
    }

    override fun getAccentColor(): AccentColor {
        val ordinal = prefs.getInt(KEY_ACCENT_COLOR, AccentColor.BLUE.ordinal)
        return AccentColor.entries.getOrElse(ordinal) { AccentColor.BLUE }
    }

    override fun setAccentColor(color: AccentColor) {
        prefs.edit { putInt(KEY_ACCENT_COLOR, color.ordinal) }
    }

    override fun getViewMode(): ViewMode {
        val ordinal = prefs.getInt(KEY_VIEW_MODE, ViewMode.GRID.ordinal)
        return ViewMode.entries.getOrElse(ordinal) { ViewMode.GRID }
    }

    override fun setViewMode(mode: ViewMode) {
        prefs.edit { putInt(KEY_VIEW_MODE, mode.ordinal) }
    }

    override fun getAutoBackupEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_BACKUP, false)
    }

    override fun setAutoBackupEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_AUTO_BACKUP, enabled) }
    }

    override fun getAppLockEnabled(): Boolean {
        return prefs.getBoolean(KEY_APP_LOCK, false)
    }

    override fun setAppLockEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_APP_LOCK, enabled) }
    }

    override fun getLastUpdateCheckTime(): Long {
        return prefs.getLong(KEY_LAST_UPDATE_CHECK_TIME, 0L)
    }

    override fun setLastUpdateCheckTime(time: Long) {
        prefs.edit { putLong(KEY_LAST_UPDATE_CHECK_TIME, time) }
    }

    override fun getLatestAvailableVersion(): String? {
        return prefs.getString(KEY_LATEST_AVAILABLE_VERSION, null)
    }

    override fun setLatestAvailableVersion(version: String?) {
        prefs.edit { putString(KEY_LATEST_AVAILABLE_VERSION, version) }
    }

    override fun getLatestReleaseUrl(): String? {
        return prefs.getString(KEY_LATEST_RELEASE_URL, null)
    }

    override fun setLatestReleaseUrl(url: String?) {
        prefs.edit { putString(KEY_LATEST_RELEASE_URL, url) }
    }
}
