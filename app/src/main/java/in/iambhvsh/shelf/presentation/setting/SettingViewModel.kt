package `in`.iambhvsh.shelf.presentation.setting

import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.iambhvsh.shelf.data.backup.BackupManager
import `in`.iambhvsh.shelf.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingViewModel(
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingState(
            themeMode = settingsRepository.getThemeMode(),
            tapAction = settingsRepository.getTapAction(),
            dynamicColor = settingsRepository.getDynamicColor(),
            isDynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
            accentColor = settingsRepository.getAccentColor(),
            viewMode = settingsRepository.getViewMode(),
            autoBackupEnabled = settingsRepository.getAutoBackupEnabled(),
            appLockEnabled = settingsRepository.getAppLockEnabled()
        )
    )
    val state = _state.asStateFlow()

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
    }

    fun onEvent(event: SettingEvents) {
        when (event) {
            is SettingEvents.SelectTheme -> {
                settingsRepository.setThemeMode(event.themeMode)
                _state.update {
                    it.copy(
                        themeMode = event.themeMode,
                        showThemeSheet = false
                    )
                }
            }

            SettingEvents.ShowThemeSheet -> {
                _state.update { it.copy(showThemeSheet = true) }
            }

            SettingEvents.HideThemeSheet -> {
                _state.update { it.copy(showThemeSheet = false) }
            }

            is SettingEvents.SelectTapAction -> {
                settingsRepository.setTapAction(event.action)
                _state.update {
                    it.copy(
                        tapAction = event.action,
                        showTapActionSheet = false
                    )
                }
            }

            SettingEvents.ShowTapActionSheet -> {
                _state.update { it.copy(showTapActionSheet = true) }
            }

            SettingEvents.HideTapActionSheet -> {
                _state.update { it.copy(showTapActionSheet = false) }
            }

            SettingEvents.ExportData -> {
                exportData()
            }

            is SettingEvents.ToggleDynamicColor -> {
                settingsRepository.setDynamicColor(event.enabled)
                _state.update { it.copy(dynamicColor = event.enabled) }
            }

            is SettingEvents.SelectAccentColor -> {
                settingsRepository.setAccentColor(event.accentColor)
                _state.update {
                    it.copy(
                        accentColor = event.accentColor,
                        showAccentColorSheet = false
                    )
                }
            }

            SettingEvents.ShowAccentColorSheet -> {
                _state.update { it.copy(showAccentColorSheet = true) }
            }

            SettingEvents.HideAccentColorSheet -> {
                _state.update { it.copy(showAccentColorSheet = false) }
            }

            is SettingEvents.ToggleViewMode -> {
                settingsRepository.setViewMode(event.viewMode)
                _state.update {
                    it.copy(
                        viewMode = event.viewMode,
                        showViewModeSheet = false
                    )
                }
            }

            SettingEvents.ShowViewModeSheet -> {
                _state.update { it.copy(showViewModeSheet = true) }
            }

            SettingEvents.HideViewModeSheet -> {
                _state.update { it.copy(showViewModeSheet = false) }
            }

            SettingEvents.DismissExport -> {
                _state.update { it.copy(exportState = ExportState.Idle) }
            }

            is SettingEvents.ImportData -> {
                importData(event.json)
            }

            SettingEvents.DismissImportResult -> {
                _state.update { it.copy(importState = ImportState.Idle) }
            }

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
                backupManager.startAutoBackup()
                backupManager.refreshLastBackupTime()
                _state.update { it.copy(autoBackupEnabled = true, showAutoBackupInfoDialog = false) }
            }

            SettingEvents.DismissAutoBackupInfoDialog -> {
                _state.update { it.copy(showAutoBackupInfoDialog = false) }
            }
            
            is SettingEvents.ToggleAppLock -> {
                settingsRepository.setAppLockEnabled(event.enabled)
                _state.update { it.copy(appLockEnabled = event.enabled) }
            }

            is SettingEvents.ImportBrowserBookmarks -> {
                importBrowserBookmarks(event.html)
            }

            SettingEvents.DismissBrowserImportResult -> {
                _state.update { it.copy(browserImportState = BrowserImportState.Idle) }
            }

            SettingEvents.ShowAboutSheet -> {
                _state.update { it.copy(showAboutSheet = true) }
            }
            SettingEvents.HideAboutSheet -> {
                _state.update { it.copy(showAboutSheet = false) }
            }

            SettingEvents.ExportBrowserBookmarks -> {
                exportBrowserData()
            }

            SettingEvents.DismissBrowserExport -> {
                _state.update { it.copy(browserExportState = ExportState.Idle) }
            }

            SettingEvents.ShowExportSheet -> {
                _state.update { it.copy(showExportSheet = true) }
            }

            SettingEvents.HideExportSheet -> {
                _state.update { it.copy(showExportSheet = false) }
            }

            SettingEvents.ShowImportSheet -> {
                _state.update { it.copy(showImportSheet = true) }
            }

            SettingEvents.HideImportSheet -> {
                _state.update { it.copy(showImportSheet = false) }
            }

            SettingEvents.ShowAboutDialog -> {
                _state.update { it.copy(showAboutDialog = true) }
            }

            SettingEvents.HideAboutDialog -> {
                _state.update { it.copy(showAboutDialog = false) }
            }
        }
    }

    private fun exportData() {
        viewModelScope.launch {
            _state.update { it.copy(exportState = ExportState.Loading) }
            try {
                val jsonString = backupManager.generateBackupJson()
                _state.update { it.copy(exportState = ExportState.Ready(jsonString)) }
            } catch (e: Exception) {
                _state.update { it.copy(exportState = ExportState.Error(e.message ?: "Export failed")) }
            }
        }
    }

    private fun importData(jsonString: String) {
        viewModelScope.launch {
            _state.update { it.copy(importState = ImportState.Loading) }
            try {
                backupManager.importFromJson(jsonString)
                _state.update { it.copy(importState = ImportState.Success) }
            } catch (e: Exception) {
                _state.update { it.copy(importState = ImportState.Error(e.message ?: "Import failed")) }
            }
        }
    }

    private fun exportBrowserData() {
        viewModelScope.launch {
            _state.update { it.copy(browserExportState = ExportState.Loading) }
            try {
                val html = backupManager.generateExportHtml()
                _state.update { it.copy(browserExportState = ExportState.Ready(html)) }
            } catch (e: Exception) {
                _state.update { it.copy(browserExportState = ExportState.Error(e.message ?: "Export failed")) }
            }
        }
    }

    private fun importBrowserBookmarks(html: String) {
        viewModelScope.launch {
            _state.update { it.copy(browserImportState = BrowserImportState.Loading) }
            try {
                val result = backupManager.importFromBrowserBookmarks(html)
                backupManager.fetchMissingImages()
                _state.update { it.copy(browserImportState = BrowserImportState.Success(result.imported, result.skipped, result.collections)) }
            } catch (e: Exception) {
                _state.update { it.copy(browserImportState = BrowserImportState.Error(e.message ?: "Import failed")) }
            }
        }
    }

    private fun formatLastBackupTime(millis: Long): String {
        if (millis <= 0L) return ""
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        return "Last backup: ${sdf.format(Date(millis))}"
    }
}
