package `in`.iambhvsh.shelf.presentation.setting.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import `in`.iambhvsh.shelf.ui.components.ExpressiveTextButton

@Composable
fun AutoBackupInfoDialog(
    onEnable: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLargeIncreased,
        title = {
            Text(
                text = "Enable Auto Backup?",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        append("Auto backup mirrors your current bookmarks.\n\n")
                    }
                    append("If you delete bookmarks in the app, they are also removed from the auto-backup.\n\n")
                    append("Use ")
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        append("Backup Bookmarks")
                    }
                    append(" in Settings, Data section to save a permanent copy before deleting anything.")
                },
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            ExpressiveTextButton(onClick = onEnable) {
                Text("Enable")
            }
        },
        dismissButton = {
            ExpressiveTextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
