package `in`.iambhvsh.shelf.features.setting

import `in`.iambhvsh.shelf.theme.AccentColor
import `in`.iambhvsh.shelf.theme.ThemeMode

enum class TapAction { SHOW_PREVIEW, OPEN_BROWSER, COPY_LINK }

enum class ViewMode { GRID, LIST }

sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    data class Ready(val json: String) : ExportState()
    data class Error(val message: String) : ExportState()
}

sealed class ImportState {
    object Idle : ImportState()
    object Loading : ImportState()
    data class Success(
        val imported: Int,
        val updated: Int,
        val skipped: Int,
        val collections: Int,
        val unresolvable: Int = 0
    ) : ImportState()
    data class Error(val message: String) : ImportState()
}

sealed class BrowserImportState {
    object Idle : BrowserImportState()
    object Loading : BrowserImportState()
    data class Success(val imported: Int, val skipped: Int, val collections: Int) : BrowserImportState()
    data class Error(val message: String) : BrowserImportState()
}

data class SettingState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showThemeSheet: Boolean = false,
    val tapAction: TapAction = TapAction.SHOW_PREVIEW,
    val showTapActionSheet: Boolean = false,
    val dynamicColor: Boolean = true,
    val isDynamicColorSupported: Boolean = false,
    val accentColor: AccentColor = AccentColor.PERIWINKLE,
    val showAccentColorSheet: Boolean = false,
    val viewMode: ViewMode = ViewMode.GRID,
    val showViewModeSheet: Boolean = false,
    val autoBackupEnabled: Boolean = false,
    val autoBackupUri: String? = null,
    val lastBackupTimeText: String = "",
    val showAutoBackupInfoDialog: Boolean = false,
    val appLockEnabled: Boolean = false,
    val appLockUsePinEnabled: Boolean = true,
    val appLockTimeout: Long = 0L,
    val showAppLockTimeoutSheet: Boolean = false,
    val exportState: ExportState = ExportState.Idle,
    val importState: ImportState = ImportState.Idle,
    val browserImportState: BrowserImportState = BrowserImportState.Idle,
    val browserExportState: ExportState = ExportState.Idle,
    val showExportSheet: Boolean = false,
    val showImportSheet: Boolean = false,
    val showAboutSheet: Boolean = false,
    val showAboutDialog: Boolean = false,
    val showUpdateSheet: Boolean = false,
    val isCheckingForUpdates: Boolean = false,
    val showNoUpdateToast: Boolean = false,
    val showChangelogSheet: Boolean = false,
    val changelogText: String = "",
    val showResetDialog: Boolean = false,
    val resetSuccessToast: Boolean = false,
    val showPermissionsSheet: Boolean = false,
    val trashedBookmarks: List<`in`.iambhvsh.shelf.data.Bookmark> = emptyList(),
    val trashedCount: Int = 0,
    val selectedTrashIds: Set<Long> = emptySet(),
    val isTrashLoading: Boolean = false,
    val showEmptyTrashDialog: Boolean = false,
    val showDeletePermanentlyDialog: Boolean = false,
    val trashToastMessage: String? = null
)
