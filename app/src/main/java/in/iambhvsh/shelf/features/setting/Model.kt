package `in`.iambhvsh.shelf.features.setting

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.iambhvsh.shelf.features.backup.BackupManager
import `in`.iambhvsh.shelf.features.reminder.ReminderManager
import `in`.iambhvsh.shelf.data.BookmarkDatabase
import `in`.iambhvsh.shelf.data.BookmarkRepository
import `in`.iambhvsh.shelf.data.UpdateManager
import `in`.iambhvsh.shelf.data.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingViewModel(
    internal val settingsRepository: SettingsRepository,
    internal val backupManager: BackupManager,
    internal val updateManager: UpdateManager,
    internal val changelogLoader: ChangelogLoader,
    internal val database: BookmarkDatabase,
    internal val bookmarkRepository: BookmarkRepository,
    internal val reminderManager: ReminderManager,
    internal val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    internal val _state = MutableStateFlow(
        SettingState(
            themeMode = settingsRepository.getThemeMode(),
            tapAction = settingsRepository.getTapAction(),
            dynamicColor = settingsRepository.getDynamicColor(),
            isDynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
            accentColor = settingsRepository.getAccentColor(),
            viewMode = settingsRepository.getViewMode(),
            autoBackupEnabled = settingsRepository.getAutoBackupEnabled(),
            autoBackupUri = settingsRepository.getAutoBackupUri(),
            appLockEnabled = settingsRepository.getAppLockEnabled(),
            appLockUsePinEnabled = settingsRepository.getAppLockUsePinEnabled(),
            appLockTimeout = settingsRepository.getAppLockTimeout()
        )
    )
    val state = _state.asStateFlow()
    internal var trashJob: kotlinx.coroutines.Job? = null

    init {
        if (settingsRepository.getAutoBackupEnabled()) {
            backupManager.refreshLastBackupTime()
        }
        viewModelScope.launch {
            backupManager.lastBackupTimeMillis.collect { millis ->
                if (_state.value.autoBackupEnabled) {
                    _state.update { it.copy(lastBackupTimeText = formatLastBackupTime(millis)) }
                }
            }
        }
        viewModelScope.launch {
            bookmarkRepository.getTrashedCount().collect { count ->
                _state.update { it.copy(trashedCount = count) }
            }
        }
    }

    fun onEvent(event: SettingEvents) {
        when (event) {
            is SettingEvents.SelectTheme -> {
                settingsRepository.setThemeMode(event.themeMode)
                _state.update { it.copy(themeMode = event.themeMode, showThemeSheet = false) }
            }
            SettingEvents.ShowThemeSheet -> _state.update { it.copy(showThemeSheet = true) }
            SettingEvents.HideThemeSheet -> _state.update { it.copy(showThemeSheet = false) }
            is SettingEvents.SelectTapAction -> {
                settingsRepository.setTapAction(event.action)
                _state.update { it.copy(tapAction = event.action, showTapActionSheet = false) }
            }
            SettingEvents.ShowTapActionSheet -> _state.update { it.copy(showTapActionSheet = true) }
            SettingEvents.HideTapActionSheet -> _state.update { it.copy(showTapActionSheet = false) }
            SettingEvents.ExportData -> exportData()
            is SettingEvents.ToggleDynamicColor -> {
                settingsRepository.setDynamicColor(event.enabled)
                _state.update { it.copy(dynamicColor = event.enabled) }
            }
            is SettingEvents.SelectAccentColor -> {
                settingsRepository.setAccentColor(event.accentColor)
                _state.update { it.copy(accentColor = event.accentColor, showAccentColorSheet = false) }
            }
            SettingEvents.ShowAccentColorSheet -> _state.update { it.copy(showAccentColorSheet = true) }
            SettingEvents.HideAccentColorSheet -> _state.update { it.copy(showAccentColorSheet = false) }
            is SettingEvents.ToggleViewMode -> {
                settingsRepository.setViewMode(event.viewMode)
                _state.update { it.copy(viewMode = event.viewMode, showViewModeSheet = false) }
            }
            SettingEvents.ShowViewModeSheet -> _state.update { it.copy(showViewModeSheet = true) }
            SettingEvents.HideViewModeSheet -> _state.update { it.copy(showViewModeSheet = false) }
            SettingEvents.DismissExport -> _state.update { it.copy(exportState = ExportState.Idle) }
            is SettingEvents.ImportData -> importData(event.json)
            SettingEvents.DismissImportResult -> _state.update { it.copy(importState = ImportState.Idle) }
            is SettingEvents.ToggleAutoBackup -> {
                if (event.enabled) {
                    _state.update { it.copy(showAutoBackupInfoDialog = true) }
                } else {
                    settingsRepository.setAutoBackupEnabled(false)
                    backupManager.stopAutoBackup()
                    _state.update { it.copy(autoBackupEnabled = false, lastBackupTimeText = "") }
                }
            }
            SettingEvents.ConfirmAutoBackupEnable -> {
                settingsRepository.setAutoBackupEnabled(true)
                _state.update { it.copy(autoBackupEnabled = true, showAutoBackupInfoDialog = false) }
                backupManager.startAutoBackup()
                backupManager.refreshLastBackupTime()
            }
            is SettingEvents.SetAutoBackupUri -> {
                settingsRepository.setAutoBackupUri(event.uri)
                _state.update { it.copy(autoBackupUri = event.uri) }
                if (event.uri != null && _state.value.autoBackupEnabled) {
                    backupManager.startAutoBackup()
                }
            }
            SettingEvents.DismissAutoBackupInfoDialog -> _state.update { it.copy(showAutoBackupInfoDialog = false) }
            is SettingEvents.ToggleAppLock -> {
                settingsRepository.setAppLockEnabled(event.enabled)
                `in`.iambhvsh.shelf.SessionManager.isUnlocked = true
                _state.update { it.copy(appLockEnabled = event.enabled) }
            }
            is SettingEvents.ToggleAppLockUsePin -> {
                settingsRepository.setAppLockUsePinEnabled(event.enabled)
                _state.update { it.copy(appLockUsePinEnabled = event.enabled) }
            }
            is SettingEvents.SetAppLockTimeout -> {
                settingsRepository.setAppLockTimeout(event.timeout)
                _state.update { it.copy(appLockTimeout = event.timeout, showAppLockTimeoutSheet = false) }
            }
            SettingEvents.SyncSecurityState -> {
                _state.update {
                    it.copy(
                        appLockEnabled = settingsRepository.getAppLockEnabled(),
                        appLockUsePinEnabled = settingsRepository.getAppLockUsePinEnabled(),
                        appLockTimeout = settingsRepository.getAppLockTimeout()
                    )
                }
            }
            SettingEvents.ShowAppLockTimeoutSheet -> _state.update { it.copy(showAppLockTimeoutSheet = true) }
            SettingEvents.HideAppLockTimeoutSheet -> _state.update { it.copy(showAppLockTimeoutSheet = false) }
            is SettingEvents.ImportBrowserBookmarks -> importBrowserBookmarks(event.html)
            SettingEvents.DismissBrowserImportResult -> {
                _state.update { it.copy(browserImportState = BrowserImportState.Idle) }
            }
            SettingEvents.ShowAboutSheet -> _state.update { it.copy(showAboutSheet = true) }
            SettingEvents.HideAboutSheet -> _state.update { it.copy(showAboutSheet = false) }
            SettingEvents.ExportBrowserBookmarks -> exportBrowserData()
            SettingEvents.DismissBrowserExport -> _state.update { it.copy(browserExportState = ExportState.Idle) }
            SettingEvents.ShowExportSheet -> _state.update { it.copy(showExportSheet = true) }
            SettingEvents.HideExportSheet -> _state.update { it.copy(showExportSheet = false) }
            SettingEvents.ShowImportSheet -> _state.update { it.copy(showImportSheet = true) }
            SettingEvents.HideImportSheet -> _state.update { it.copy(showImportSheet = false) }
            SettingEvents.ShowAboutDialog -> _state.update { it.copy(showAboutDialog = true) }
            SettingEvents.HideAboutDialog -> _state.update { it.copy(showAboutDialog = false) }
            SettingEvents.CheckForUpdates -> checkForUpdates()
            SettingEvents.DismissUpdateSheet -> _state.update { it.copy(showUpdateSheet = false) }
            SettingEvents.InstallUpdate -> {
                _state.update { it.copy(showUpdateSheet = false) }
                updateManager.downloadAndInstallUpdate()
            }
            SettingEvents.ResetNoUpdateToast -> _state.update { it.copy(showNoUpdateToast = false) }
            SettingEvents.ShowChangelogSheet -> {
                _state.update { it.copy(showChangelogSheet = true) }
                if (_state.value.changelogText.isEmpty()) {
                    viewModelScope.launch {
                        val text = changelogLoader.load()
                        _state.update { it.copy(changelogText = text) }
                    }
                }
            }
            SettingEvents.HideChangelogSheet -> _state.update { it.copy(showChangelogSheet = false) }
            SettingEvents.ShowResetDialog -> _state.update { it.copy(showResetDialog = true) }
            SettingEvents.HideResetDialog -> _state.update { it.copy(showResetDialog = false) }
            SettingEvents.ConfirmReset -> {
                _state.update { it.copy(showResetDialog = false) }
                resetEverything()
            }
            SettingEvents.ResetSuccessToastShown -> _state.update { it.copy(resetSuccessToast = false) }
            SettingEvents.ShowPermissionsSheet -> _state.update { it.copy(showPermissionsSheet = true) }
            SettingEvents.HidePermissionsSheet -> {
                settingsRepository.setFirstLaunchPermissionsShown(true)
                _state.update { it.copy(showPermissionsSheet = false) }
            }
            SettingEvents.LoadTrash -> loadTrash()
            is SettingEvents.ToggleTrashSelection -> toggleTrashSelection(event.id)
            SettingEvents.SelectAllTrash -> selectAllTrash()
            SettingEvents.ClearTrashSelection -> clearTrashSelection()
            is SettingEvents.RestoreTrashItem -> restoreTrashItem(event.id)
            is SettingEvents.DeleteTrashItem -> deleteTrashItem(event.id)
            SettingEvents.RestoreSelectedTrash -> restoreSelectedTrash()
            SettingEvents.ShowDeletePermanentlyDialog -> _state.update { it.copy(showDeletePermanentlyDialog = true) }
            SettingEvents.HideDeletePermanentlyDialog -> _state.update { it.copy(showDeletePermanentlyDialog = false) }
            SettingEvents.ConfirmDeletePermanently -> deletePermanentlySelected()
            SettingEvents.ShowEmptyTrashDialog -> _state.update { it.copy(showEmptyTrashDialog = true) }
            SettingEvents.HideEmptyTrashDialog -> _state.update { it.copy(showEmptyTrashDialog = false) }
            SettingEvents.ConfirmEmptyTrash -> emptyTrash()
            SettingEvents.ClearTrashToast -> _state.update { it.copy(trashToastMessage = null) }
        }
    }
}
