package `in`.iambhvsh.shelf.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import `in`.iambhvsh.shelf.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkPreviewSheet(
    showBottomSheet: Boolean,
    isPinned: Boolean = false,
    onDismissRequest: () -> Unit,
    openInBrowser: () -> Unit,
    copyLinkButtonClick: () -> Unit,
    onPinButtonClick: (() -> Unit)? = null,
    onTagsButtonClick: (() -> Unit)? = null,
    onNoteButtonClick: (() -> Unit)? = null,
    onReminderButtonClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    if (!showBottomSheet) return

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        if (onPinButtonClick != null) {
            ListItem(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable {
                        onPinButtonClick()
                        onDismissRequest()
                    },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                headlineContent = {
                    Text(if (isPinned) "Unpin" else "Pin")
                },
                leadingContent = {
                    Icon(
                        imageVector = if (isPinned) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Pin Bookmark",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
        
        if (onTagsButtonClick != null) {
            ListItem(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable {
                        onTagsButtonClick()
                    },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                headlineContent = {
                    Text("Manage Tags")
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.bookmark_add),
                        contentDescription = "Manage Tags"
                    )
                }
            )
        }
        
        if (onNoteButtonClick != null) {
            ListItem(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable {
                        onNoteButtonClick()
                    },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                headlineContent = {
                    Text("Personal Note")
                },
                leadingContent = {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Outlined.Edit,
                        contentDescription = "Personal Note"
                    )
                }
            )
        }
        
        if (onReminderButtonClick != null) {
            ListItem(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable {
                        onReminderButtonClick()
                    },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                headlineContent = {
                    Text("Remind Me")
                },
                leadingContent = {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Outlined.Notifications,
                        contentDescription = "Remind Me"
                    )
                }
            )
        }
        ListItem(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .clickable {
                    openInBrowser()
                    onDismissRequest()
                },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            headlineContent = {
                Text("Open In Browser")
            },
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.open_in_browser),
                    contentDescription = "Copy Link"
                )
            }
        )
        ListItem(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .clickable {
                    copyLinkButtonClick()
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
                    }
                    onDismissRequest()
                },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            headlineContent = {
                Text("Copy Link")
            },
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.copy_icon),
                    contentDescription = "Copy Link"
                )
            }
        )


        Spacer(Modifier.height(24.dp))
    }
}