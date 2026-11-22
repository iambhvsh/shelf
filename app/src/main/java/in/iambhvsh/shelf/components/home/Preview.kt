@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.components.home

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import `in`.iambhvsh.shelf.components.common.rememberShelfSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.R
import `in`.iambhvsh.shelf.data.Bookmark

@OptIn(ExperimentalMaterial3Api::class)
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
    onDeleteButtonClick: (() -> Unit)? = null,
    onPhotoClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    if (!showBottomSheet) return

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val actions = remember(
        bookmark, isPinned, onPinButtonClick, onTagsButtonClick,
        onNoteButtonClick, onReminderButtonClick, onRenameButtonClick, onDeleteButtonClick
    ) {
        buildList {
            add(
                PreviewAction(
                    title = "Open Link",
                    icon = R.drawable.open_in_browser,
                    onClick = {
                        openInBrowser()
                        onDismissRequest()
                    }
                )
            )
            add(
                PreviewAction(
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
                PreviewAction(
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
                    PreviewAction(
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
                    PreviewAction(
                        title = "Edit Tags",
                        icon = R.drawable.bookmark_add,
                        onClick = onTagsButtonClick
                    )
                )
            }
            if (onPinButtonClick != null) {
                add(
                    PreviewAction(
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
                    PreviewAction(
                        title = "Remind Me",
                        icon = Icons.Outlined.Notifications,
                        onClick = onReminderButtonClick
                    )
                )
            }
            if (onNoteButtonClick != null) {
                add(
                    PreviewAction(
                        title = "Add Note",
                        icon = Icons.Outlined.EditNote,
                        onClick = onNoteButtonClick
                    )
                )
            }
            if (onDeleteButtonClick != null) {
                add(
                    PreviewAction(
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

    val sheetState = rememberShelfSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLargeIncreased,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = screenHeight * 0.70f)
                .verticalScroll(rememberScrollState())
        ) {
            if (bookmark != null) {
                PreviewDetails(
                    bookmark = bookmark,
                    onPhotoClick = onPhotoClick
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            PreviewActions(actions = actions)
            Spacer(Modifier.height(28.dp))
        }
    }
}
