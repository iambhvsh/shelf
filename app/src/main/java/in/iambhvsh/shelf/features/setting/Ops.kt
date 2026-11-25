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
            backupManager.refreshImportedThumbnailsAsync(checkExistingImages = true)
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
            backupManager.refreshImportedThumbnailsAsync(checkExistingImages = false)
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
        val existing = database.bookmarkDao().getAllBookmarksOnce()
        existing.forEach { reminderManager.cancelReminder(it.id) }
        database.clearAllTables()
        settingsRepository.resetToDefaults()
        backupManager.stopAutoBackup()
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
            _state.update { current ->
                SettingState(
                    themeMode = `in`.iambhvsh.shelf.theme.ThemeMode.SYSTEM,
                    dynamicColor = true,
                    isDynamicColorSupported = current.isDynamicColorSupported,
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

internal fun SettingViewModel.loadTrash() {
    trashJob?.cancel()
    trashJob = viewModelScope.launch {
        _state.update { it.copy(isTrashLoading = true) }
        bookmarkRepository.getTrashedBookmarks().collect { resource ->
            when (resource) {
                is `in`.iambhvsh.shelf.util.Resource.Loading -> _state.update { it.copy(isTrashLoading = true) }
                is `in`.iambhvsh.shelf.util.Resource.Error -> _state.update { it.copy(isTrashLoading = false) }
                is `in`.iambhvsh.shelf.util.Resource.Success -> {
                    val list = resource.data ?: emptyList()
                    _state.update {
                        it.copy(
                            isTrashLoading = false,
                            trashedBookmarks = list,
                            trashedCount = list.size,
                            selectedTrashIds = it.selectedTrashIds.filter { id -> list.any { bm -> bm.id == id } }.toSet()
                        )
                    }
                }
            }
        }
    }
}

internal fun SettingViewModel.toggleTrashSelection(id: Long) {
    _state.update {
        val current = it.selectedTrashIds
        val updated = if (current.contains(id)) current - id else current + id
        it.copy(selectedTrashIds = updated)
    }
}

internal fun SettingViewModel.selectAllTrash() {
    _state.update {
        it.copy(selectedTrashIds = it.trashedBookmarks.map { bm -> bm.id }.toSet())
    }
}

internal fun SettingViewModel.clearTrashSelection() {
    _state.update { it.copy(selectedTrashIds = emptySet()) }
}

internal fun SettingViewModel.restoreTrashItem(id: Long) {
    viewModelScope.launch {
        try {
            bookmarkRepository.unhideBookmarks(listOf(id))
            val bm = bookmarkRepository.getBookmarkById(id)
            val time = bm?.reminderTime
            if (time != null && time > System.currentTimeMillis()) {
                reminderManager.scheduleReminder(id, time)
            }
            _state.update { it.copy(trashToastMessage = "Bookmark restored") }
        } catch (e: Exception) {
            _state.update { it.copy(trashToastMessage = "Restore failed") }
        }
    }
}

internal fun SettingViewModel.deleteTrashItem(id: Long) {
    viewModelScope.launch {
        try {
            reminderManager.cancelReminder(id)
            bookmarkRepository.permanentlyDeleteBookmarks(listOf(id))
            _state.update {
                it.copy(
                    selectedTrashIds = it.selectedTrashIds - id,
                    trashToastMessage = "Bookmark deleted"
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(trashToastMessage = "Delete failed") }
        }
    }
}

internal fun SettingViewModel.restoreSelectedTrash() {
    val ids = _state.value.selectedTrashIds.toList()
    if (ids.isEmpty()) return
    viewModelScope.launch {
        try {
            bookmarkRepository.unhideBookmarks(ids)
            val now = System.currentTimeMillis()
            ids.forEach { id ->
                val bm = bookmarkRepository.getBookmarkById(id)
                val time = bm?.reminderTime
                if (time != null && time > now) {
                    reminderManager.scheduleReminder(id, time)
                }
            }
            _state.update {
                it.copy(
                    selectedTrashIds = emptySet(),
                    trashToastMessage = if (ids.size > 1) "${ids.size} bookmarks restored" else "Bookmark restored"
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(trashToastMessage = "Restore failed") }
        }
    }
}

internal fun SettingViewModel.deletePermanentlySelected() {
    val ids = _state.value.selectedTrashIds.toList()
    if (ids.isEmpty()) return
    viewModelScope.launch {
        try {
            ids.forEach { reminderManager.cancelReminder(it) }
            bookmarkRepository.permanentlyDeleteBookmarks(ids)
            _state.update {
                it.copy(
                    selectedTrashIds = emptySet(),
                    showDeletePermanentlyDialog = false,
                    trashToastMessage = if (ids.size > 1) "${ids.size} bookmarks deleted" else "Bookmark deleted"
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(showDeletePermanentlyDialog = false, trashToastMessage = "Delete failed") }
        }
    }
}

internal fun SettingViewModel.emptyTrash() {
    viewModelScope.launch {
        try {
            _state.value.trashedBookmarks.forEach { reminderManager.cancelReminder(it.id) }
            bookmarkRepository.emptyTrash()
            _state.update {
                it.copy(
                    selectedTrashIds = emptySet(),
                    showEmptyTrashDialog = false,
                    trashToastMessage = "Trash emptied"
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(showEmptyTrashDialog = false, trashToastMessage = "Failed to empty trash") }
        }
    }
}
