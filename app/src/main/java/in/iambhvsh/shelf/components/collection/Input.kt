package `in`.iambhvsh.shelf.components.collection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import `in`.iambhvsh.shelf.components.common.rememberShelfSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.R
import `in`.iambhvsh.shelf.components.common.ExpressiveButton
import `in`.iambhvsh.shelf.components.common.ExpressiveTextButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionInputSheet(
    showBottomSheet: Boolean,
    onDismissRequest: () -> Unit,
    value: String,
    onTextChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    if (showBottomSheet) {
        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp.dp
        val sheetState = rememberShelfSheetState(skipPartiallyExpanded = false)
        val scope = androidx.compose.runtime.rememberCoroutineScope()
        var isSubmitting by remember { mutableStateOf(false) }

        val animateDismiss: (action: (() -> Unit)?) -> Unit = { action ->
            if (!isSubmitting) {
                isSubmitting = true
                scope.launch {
                    sheetState.hide()
                    action?.invoke()
                    onDismissRequest()
                }
            }
        }

        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            shape = MaterialTheme.shapes.extraLargeIncreased
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = screenHeight * 0.9f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .imePadding()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "New Collection",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = value,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.largeIncreased,
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.bookmark_one),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.surfaceTint,
                        )
                    },
                    placeholder = {
                        Text("Collection name")
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                ExpressiveButton(
                    onClick = { animateDismiss(onSaveClick) },
                    enabled = value.isNotBlank() && !isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Collection")
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExpressiveTextButton(
                    onClick = { animateDismiss(null) },
                    enabled = !isSubmitting
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
