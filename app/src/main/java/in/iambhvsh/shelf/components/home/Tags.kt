@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.components.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.data.Tag
import `in`.iambhvsh.shelf.components.common.ExpressiveIconButton
import `in`.iambhvsh.shelf.components.common.expressiveBounce
import `in`.iambhvsh.shelf.theme.ShelfShapes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TagManagerSheet(
    tags: List<Tag>,
    selectedTags: List<Tag>,
    onToggleTag: (Tag, Boolean) -> Unit,
    onCreateTag: (String) -> Unit,
    onDeleteTag: (Tag) -> Unit,
    onDismiss: () -> Unit
) {
    var newTagName by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()
    val configuration = LocalConfiguration.current
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
                .padding(bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp)
            ) {
                Text(
                    text = "Manage Tags",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Create New Tag Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    placeholder = { Text("New tag name\u2026") },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.largeIncreased,
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                ExpressiveIconButton(
                    onClick = {
                        if (newTagName.isNotBlank()) {
                            onCreateTag(newTagName.trim())
                            newTagName = ""
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Tag")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (tags.isEmpty()) {
                Text(
                    text = "No tags available. Create one above.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(ShelfShapes.Gap)
                ) {
                    itemsIndexed(tags, key = { _, tag -> tag.id }) { index, tag ->
                        val isSelected = selectedTags.any { it.id == tag.id }
                        SegmentedListItem(
                            onClick = { onToggleTag(tag, !isSelected) },
                            shapes = ShelfShapes.segmented(index, tags.size),
                            modifier = Modifier
                                .fillMaxWidth()
                                .expressiveBounce(scaleDown = 0.98f),
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            content = {
                                Text(
                                    text = tag.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ExpressiveIconButton(onClick = { onDeleteTag(tag) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Tag",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Switch(
                                        checked = isSelected,
                                        onCheckedChange = { onToggleTag(tag, it) }
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
