package `in`.iambhvsh.shelf.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.ui.components.ExpressiveButton
import `in`.iambhvsh.shelf.ui.components.ExpressiveTextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorSheet(
    initialNote: String?,
    onSave: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var noteText by remember { mutableStateOf(initialNote ?: "") }

    val maxChar = 500
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLargeIncreased,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = screenHeight * 0.9f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .imePadding()
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
        ) {
            Text(
                text = "Personal Note",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = noteText,
                onValueChange = { if (it.length <= maxChar) noteText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = MaterialTheme.shapes.largeIncreased,
                placeholder = { Text("Write your thoughts, ideas, or context here...") },
                textStyle = MaterialTheme.typography.bodyLarge,
                supportingText = {
                    Text(
                        text = "${noteText.length} / $maxChar",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ExpressiveTextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                ExpressiveButton(onClick = { 
                    val finalNote = noteText.trim().ifEmpty { null }
                    onSave(finalNote) 
                }) {
                    Text("Save Note")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
