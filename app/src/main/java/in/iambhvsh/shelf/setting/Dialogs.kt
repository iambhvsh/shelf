package `in`.iambhvsh.shelf.setting

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.setting.components.AutoBackupInfoDialog
import `in`.iambhvsh.shelf.setting.components.InfoDialog

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

    if (state.importState is ImportState.Success) {
        InfoDialog(
            title = "Import successful",
            text = "Your bookmarks and collections have been restored.",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Reset all data?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "This will permanently delete all your bookmarks, collections, tags, notes, reminders, and reset all settings to default. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = onConfirm,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Reset")
                    }
                }
            }
        }
    }
}
