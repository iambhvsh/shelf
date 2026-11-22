package `in`.iambhvsh.shelf.features.setting

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.font.FontWeight
import `in`.iambhvsh.shelf.components.common.ExpressiveTextButton
import `in`.iambhvsh.shelf.components.setting.AutoBackupInfoDialog
import `in`.iambhvsh.shelf.components.setting.InfoDialog

@Composable
fun SettingDialogs(
    state: SettingState,
    viewModel: SettingViewModel,
    context: Context
) {
    if (state.resetSuccessToast) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "All data reset successfully", Toast.LENGTH_SHORT).show()
            viewModel.onEvent(SettingEvents.ResetSuccessToastShown)
        }
    }

    if (state.showResetDialog) {
        ResetConfirmationDialog(
            onDismiss = { viewModel.onEvent(SettingEvents.HideResetDialog) },
            onConfirm = { viewModel.onEvent(SettingEvents.ConfirmReset) }
        )
    }

    if (state.showNoUpdateToast) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "You are already on the latest version", Toast.LENGTH_SHORT).show()
            viewModel.onEvent(SettingEvents.ResetNoUpdateToast)
        }
    }

    val exportError = state.exportState as? ExportState.Error
    if (exportError != null) {
        InfoDialog(
            title = "Export failed",
            text = exportError.message,
            onDismiss = { viewModel.onEvent(SettingEvents.DismissExport) }
        )
    }

    val importSuccess = state.importState as? ImportState.Success
    if (importSuccess != null) {
        val text = buildString {
            append("Restored ${importSuccess.imported} bookmarks")
            if (importSuccess.updated > 0) {
                append(" (${importSuccess.updated} unhidden/updated)")
            }
            append(" in ${importSuccess.collections} collections.")
            if (importSuccess.unresolvable > 0) {
                append("\n\n${importSuccess.unresolvable} links were skipped because they could not be found.")
            } else if (importSuccess.skipped > 0) {
                append("\n\n${importSuccess.skipped} existing bookmarks were already up to date.")
            }
        }
        InfoDialog(
            title = "Import successful",
            text = text,
            onDismiss = { viewModel.onEvent(SettingEvents.DismissImportResult) }
        )
    }

    val importError = state.importState as? ImportState.Error
    if (importError != null) {
        InfoDialog(
            title = "Import failed",
            text = importError.message,
            onDismiss = { viewModel.onEvent(SettingEvents.DismissImportResult) }
        )
    }

    val browserImportSuccess = state.browserImportState as? BrowserImportState.Success
    if (browserImportSuccess != null) {
        InfoDialog(
            title = "Browser import complete",
            text = "Imported ${browserImportSuccess.imported} bookmarks into ${browserImportSuccess.collections} folders (${browserImportSuccess.skipped} duplicates skipped).",
            onDismiss = { viewModel.onEvent(SettingEvents.DismissBrowserImportResult) }
        )
    }

    val browserImportError = state.browserImportState as? BrowserImportState.Error
    if (browserImportError != null) {
        InfoDialog(
            title = "Browser import failed",
            text = browserImportError.message,
            onDismiss = { viewModel.onEvent(SettingEvents.DismissBrowserImportResult) }
        )
    }

    val browserExportError = state.browserExportState as? ExportState.Error
    if (browserExportError != null) {
        InfoDialog(
            title = "Export as HTML failed",
            text = browserExportError.message,
            onDismiss = { viewModel.onEvent(SettingEvents.DismissBrowserExport) }
        )
    }

    if (state.showAutoBackupInfoDialog) {
        AutoBackupInfoDialog(
            onEnable = { viewModel.onEvent(SettingEvents.ConfirmAutoBackupEnable) },
            onDismiss = { viewModel.onEvent(SettingEvents.DismissAutoBackupInfoDialog) }
        )
    }
}

@Composable
fun ResetConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLargeIncreased,
        title = {
            Text(
                text = "Reset all data?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "This will permanently delete all your bookmarks, collections, tags, notes, reminders, and reset all settings to default. This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            ExpressiveTextButton(onClick = onConfirm) {
                Text(
                    text = "Reset",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            ExpressiveTextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
