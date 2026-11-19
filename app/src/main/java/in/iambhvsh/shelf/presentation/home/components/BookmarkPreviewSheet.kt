@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.presentation.home.components

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import `in`.iambhvsh.shelf.R
import `in`.iambhvsh.shelf.domain.model.Bookmark
import `in`.iambhvsh.shelf.ui.components.expressiveBounce
import `in`.iambhvsh.shelf.ui.theme.ShelfShapes

private data class PreviewSheetAction(
    val title: String,
    val icon: Any,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BookmarkPreviewSheet(
    bookmark: Bookmark? = null,
    showBottomSheet: Boolean,
    isPinned: Boolean = false,
    onDismissRequest: () -> Unit,
    openInBrowser: () -> Unit,
    copyLinkButtonClick: () -> Unit,
    onShareButtonClick: () -> Unit,
    onPinButtonClick: (() -> Unit)? = null,
    onTagsButtonClick: (() -> Unit)? = null,
    onNoteButtonClick: (() -> Unit)? = null,
    onReminderButtonClick: (() -> Unit)? = null,
    onRenameButtonClick: (() -> Unit)? = null,
    onDeleteButtonClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    if (!showBottomSheet) return

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val actions = remember(
        bookmark, isPinned, onPinButtonClick, onTagsButtonClick,
        onNoteButtonClick, onReminderButtonClick, onRenameButtonClick, onDeleteButtonClick
    ) {
        buildList {
            add(
                PreviewSheetAction(
                    title = "Open Link",
                    icon = R.drawable.open_in_browser,
                    onClick = {
                        openInBrowser()
                        onDismissRequest()
                    }
                )
            )
            add(
                PreviewSheetAction(
                    title = "Copy Link",
                    icon = R.drawable.copy_icon,
                    onClick = {
                        copyLinkButtonClick()
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                            Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
                        }
                        onDismissRequest()
                    }
                )
            )
            add(
                PreviewSheetAction(
                    title = "Share Link",
                    icon = Icons.Outlined.Share,
                    onClick = {
                        onShareButtonClick()
                        onDismissRequest()
                    }
                )
            )
            if (onRenameButtonClick != null) {
                add(
                    PreviewSheetAction(
                        title = "Edit Bookmark",
                        icon = Icons.Outlined.Edit,
                        onClick = {
                            onRenameButtonClick()
                            onDismissRequest()
                        }
                    )
                )
            }
            if (onTagsButtonClick != null) {
                add(
                    PreviewSheetAction(
                        title = "Edit Tags",
                        icon = R.drawable.bookmark_add,
                        onClick = onTagsButtonClick
                    )
                )
            }
            if (onPinButtonClick != null) {
                add(
                    PreviewSheetAction(
                        title = if (isPinned) "Unpin from top" else "Pin to top",
                        icon = if (isPinned) Icons.Default.Star else Icons.Outlined.StarBorder,
                        onClick = {
                            onPinButtonClick()
                            onDismissRequest()
                        }
                    )
                )
            }
            if (onReminderButtonClick != null) {
                add(
                    PreviewSheetAction(
                        title = "Remind Me",
                        icon = Icons.Outlined.Notifications,
                        onClick = onReminderButtonClick
                    )
                )
            }
            if (onNoteButtonClick != null) {
                add(
                    PreviewSheetAction(
                        title = "Add Note",
                        icon = Icons.Outlined.EditNote,
                        onClick = onNoteButtonClick
                    )
                )
            }
            if (onDeleteButtonClick != null) {
                add(
                    PreviewSheetAction(
                        title = "Delete",
                        icon = Icons.Outlined.Delete,
                        isDestructive = true,
                        onClick = {
                            onDeleteButtonClick()
                            onDismissRequest()
                        }
                    )
                )
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        shape = MaterialTheme.shapes.extraLargeIncreased,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = screenHeight * 0.9f)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
        ) {
            if (bookmark != null) {
                ListItem(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.largeIncreased),
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    headlineContent = {
                        Text(
                            text = bookmark.title ?: bookmark.url,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    supportingContent = {
                        Text(
                            text = bookmark.url,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingContent = {
                        if (bookmark.imageUrl != null) {
                            AsyncImage(
                                model = bookmark.imageUrl,
                                contentDescription = "Bookmark Image",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (bookmark.title ?: bookmark.url).take(1).uppercase(),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(ShelfShapes.Gap)
            ) {
                actions.forEachIndexed { index, action ->
                    SegmentedListItem(
                        onClick = action.onClick,
                        shapes = ShelfShapes.segmented(index, actions.size),
                        modifier = Modifier
                            .fillMaxWidth()
                            .expressiveBounce(scaleDown = 0.98f),
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        content = {
                            Text(
                                text = action.title,
                                color = if (action.isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingContent = {
                            val tint = if (action.isDestructive) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                            if (action.icon is androidx.compose.ui.graphics.vector.ImageVector) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = action.title,
                                    modifier = Modifier.size(24.dp),
                                    tint = tint
                                )
                            } else if (action.icon is Int) {
                                Icon(
                                    painter = painterResource(action.icon),
                                    contentDescription = action.title,
                                    modifier = Modifier.size(24.dp),
                                    tint = tint
                                )
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}
