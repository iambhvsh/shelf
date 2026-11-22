package `in`.iambhvsh.shelf.components.home

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
fun HomeInputSheet(
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

        val animateDismiss: (action: (() -> Unit)?) -> Unit = { action ->
            kotlinx.coroutines.CoroutineScope(scope.coroutineContext).run {
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
                    text = "New Bookmark",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                val hasScheme = value.startsWith("http://") || value.startsWith("https://")
                val looksLikeDomain = value.contains(".") && !value.contains(" ") && value.isNotBlank()
                val isValidUrl = hasScheme || looksLikeDomain
                val showError = value.isNotBlank() && !isValidUrl

                OutlinedTextField(
                    value = value,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.largeIncreased,
                    singleLine = true,
                    isError = showError,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.link_three),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.surfaceTint,
                        )
                    },
                    placeholder = {
                        Text("https://example.com")
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    supportingText = if (showError) {
                        {
                            Text(
                                "Enter a valid URL",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        null
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                ExpressiveButton(
                    onClick = { animateDismiss(onSaveClick) },
                    enabled = isValidUrl,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Bookmark")
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExpressiveTextButton(
                    onClick = { animateDismiss(null) }
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
