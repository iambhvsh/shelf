package `in`.iambhvsh.shelf.features.setting

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun SettingViewModel.exportData() {
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

internal fun SettingViewModel.importData(jsonString: String) {
    viewModelScope.launch {
        _state.update { it.copy(importState = ImportState.Loading) }
        try {
            val result = backupManager.importFromJson(jsonString)
            _state.update {
                it.copy(
                    importState = ImportState.Success(
                        imported = result.imported,
                        updated = result.updated,
                        skipped = result.skipped,
                        collections = result.collections,
                        unresolvable = result.skippedUrls.size
                    )
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(importState = ImportState.Error(e.message ?: "Import failed")) }
        }
    }
}

internal fun SettingViewModel.exportBrowserData() {
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

internal fun SettingViewModel.importBrowserBookmarks(html: String) {
    viewModelScope.launch {
        _state.update { it.copy(browserImportState = BrowserImportState.Loading) }
        try {
            val result = backupManager.importFromBrowserBookmarks(html)
            backupManager.fetchMissingImages()
            _state.update {
                it.copy(
                    browserImportState = BrowserImportState.Success(
                        result.imported,
                        result.skipped,
                        result.collections
                    )
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(browserImportState = BrowserImportState.Error(e.message ?: "Import failed")) }
        }
    }
}

internal fun SettingViewModel.checkForUpdates() {
    _state.update { it.copy(isCheckingForUpdates = true) }
    viewModelScope.launch {
        val hasUpdate = updateManager.checkForUpdates(force = true)
        _state.update {
            it.copy(
                isCheckingForUpdates = false,
                showUpdateSheet = hasUpdate,
                showNoUpdateToast = !hasUpdate
            )
        }
    }
}

internal fun formatLastBackupTime(millis: Long): String {
    if (millis <= 0L) return ""
    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return "Last backup: ${sdf.format(Date(millis))}"
}

internal fun SettingViewModel.resetEverything() {
    viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        database.clearAllTables()
        settingsRepository.resetToDefaults()
        backupManager.stopAutoBackup()
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
            _state.update {
                SettingState(
                    themeMode = `in`.iambhvsh.shelf.theme.ThemeMode.SYSTEM,
                    dynamicColor = true,
                    accentColor = `in`.iambhvsh.shelf.theme.AccentColor.PERIWINKLE,
                    viewMode = ViewMode.GRID,
                    tapAction = TapAction.SHOW_PREVIEW,
                    autoBackupEnabled = false,
                    appLockEnabled = false,
                    resetSuccessToast = true
                )
            }
        }
    }
}
