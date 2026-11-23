package `in`.iambhvsh.shelf.screens.save

import `in`.iambhvsh.shelf.features.save.*
import `in`.iambhvsh.shelf.components.save.*

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import `in`.iambhvsh.shelf.components.common.rememberShelfSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.components.collection.CollectionPickerSheet
import `in`.iambhvsh.shelf.components.home.TagManagerSheet
import `in`.iambhvsh.shelf.components.common.ExpressiveButton
import `in`.iambhvsh.shelf.components.common.ExpressiveOutlinedButton
import `in`.iambhvsh.shelf.components.common.expressiveBounce
import `in`.iambhvsh.shelf.theme.HeroFont
import `in`.iambhvsh.shelf.theme.ShelfShapes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QuickSaveScreen(
    state: QuickSaveState,
    onEvent: (QuickSaveEvents) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberShelfSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLargeIncreased,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Add to Shelf",
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = HeroFont),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            QuickSavePreview(
                imageUrl = state.imageUrl,
                isSaving = state.isSaving,
                title = state.title,
                description = state.description,
                url = state.url
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Collection",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShelfShapes.card)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .expressiveBounce(scaleDown = 0.98f)
                    .clickable { onEvent(QuickSaveEvents.ShowCollectionPicker) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val collectionName = state.selectedCollection?.name ?: "No Collection"
                Text(
                    text = collectionName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Select Collection",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tags",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShelfShapes.card)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .expressiveBounce(scaleDown = 0.98f)
                    .clickable { onEvent(QuickSaveEvents.ShowTagPicker) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tagsText = if (state.selectedTags.isEmpty()) {
                    "+ Add tags"
                } else {
                    state.selectedTags.joinToString(", ") { it.name }
                }
                Text(
                    text = tagsText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (state.selectedTags.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ExpressiveOutlinedButton(
                    onClick = {
                        onEvent(QuickSaveEvents.CancelSave)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Cancel",
                        modifier = Modifier.padding(vertical = 4.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                ExpressiveButton(
                    onClick = {
                        state.message?.let {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            onEvent(QuickSaveEvents.MessageShown)
                        }
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Done",
                        modifier = Modifier.padding(vertical = 4.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }

    if (state.showCollectionPicker) {
        CollectionPickerSheet(
            collections = state.collections.filter { !it.isVirtual },
            onSelectCollection = { onEvent(QuickSaveEvents.SelectCollection(it)) },
            onDismiss = { onEvent(QuickSaveEvents.HideCollectionPicker) }
        )
    }

    if (state.showTagPicker) {
        TagManagerSheet(
            tags = state.allTags,
            selectedTags = state.selectedTags,
            onToggleTag = { tag, _ -> onEvent(QuickSaveEvents.ToggleTag(tag.id)) },
            onCreateTag = { onEvent(QuickSaveEvents.CreateTag(it)) },
            onDeleteTag = { onEvent(QuickSaveEvents.DeleteTag(it.id)) },
            onDismiss = { onEvent(QuickSaveEvents.HideTagPicker) }
        )
    }
}
