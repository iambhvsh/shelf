package `in`.iambhvsh.shelf.presentation.setting

import `in`.iambhvsh.shelf.ui.theme.AccentColor
import `in`.iambhvsh.shelf.ui.theme.ThemeMode

sealed class SettingEvents {
    data class SelectTheme(val themeMode: ThemeMode) : SettingEvents()
    object ShowThemeSheet : SettingEvents()
    object HideThemeSheet : SettingEvents()
    data class SelectTapAction(val action: TapAction) : SettingEvents()
    object ShowTapActionSheet : SettingEvents()
    object HideTapActionSheet : SettingEvents()
    data class ToggleDynamicColor(val enabled: Boolean) : SettingEvents()
    data class SelectAccentColor(val accentColor: AccentColor) : SettingEvents()
    object ShowAccentColorSheet : SettingEvents()
    object HideAccentColorSheet : SettingEvents()
    data class ToggleViewMode(val viewMode: ViewMode) : SettingEvents()
    object ShowViewModeSheet : SettingEvents()
    object HideViewModeSheet : SettingEvents()
    object ExportData : SettingEvents()
    object DismissExport : SettingEvents()
    data class ImportData(val json: String) : SettingEvents()
    object DismissImportResult : SettingEvents()
    data class ToggleAutoBackup(val enabled: Boolean) : SettingEvents()
    object ConfirmAutoBackupEnable : SettingEvents()
    object DismissAutoBackupInfoDialog : SettingEvents()
    data class ImportBrowserBookmarks(val html: String) : SettingEvents()
    object DismissBrowserImportResult : SettingEvents()
    object ExportBrowserBookmarks : SettingEvents()
    object DismissBrowserExport : SettingEvents()
    object ShowExportSheet : SettingEvents()
    object HideExportSheet : SettingEvents()
    object ShowImportSheet : SettingEvents()
    object HideImportSheet : SettingEvents()
    object ShowAboutDialog : SettingEvents()
    object HideAboutDialog : SettingEvents()
}
