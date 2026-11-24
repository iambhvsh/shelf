package `in`.iambhvsh.shelf.components.home

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import `in`.iambhvsh.shelf.components.common.ExpressiveTextButton

@Composable
fun DeleteConfirmationDialog(
    title: String = "Delete",
    text: String = "Are you sure you want to delete this item? It will be moved to Recently Deleted.",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLargeIncreased,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = { Text(text, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            ExpressiveTextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            ExpressiveTextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
