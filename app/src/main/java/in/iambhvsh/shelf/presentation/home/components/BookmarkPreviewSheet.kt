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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Share
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
    bookmark: `in`.iambhvsh.shelf.domain.model.Bookmark? = null,
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

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
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
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge),
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                headlineContent = {
                    Text(bookmark.title ?: bookmark.url, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                supportingContent = {
                    Text(bookmark.url, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
            Spacer(modifier = Modifier.height(8.dp))
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
                Text("Open Link")
            },
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.open_in_browser),
                    contentDescription = "Open Link"
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

        ListItem(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .clickable {
                    onShareButtonClick()
                    onDismissRequest()
                },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            headlineContent = {
                Text("Share Link")
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "Share Link"
                )
            }
        )

        if (onRenameButtonClick != null) {
            ListItem(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable {
                        onRenameButtonClick()
                        onDismissRequest()
                    },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                headlineContent = {
                    Text("Edit Bookmark")
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit Bookmark"
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
                    Text("Edit Tags")
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.bookmark_add),
                        contentDescription = "Edit Tags"
                    )
                }
            )
        }
        
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
                    Text(if (isPinned) "Unpin from top" else "Pin to top")
                },
                leadingContent = {
                    Icon(
                        imageVector = if (isPinned) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Pin to top",
                        tint = MaterialTheme.colorScheme.primary
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
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Remind Me"
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
                    Text("Add Note")
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.EditNote,
                        contentDescription = "Add Note"
                    )
                }
            )
        }
        
        if (onDeleteButtonClick != null) {
            ListItem(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable {
                        onDeleteButtonClick()
                        onDismissRequest()
                    },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                headlineContent = {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }


        Spacer(Modifier.height(24.dp))
        }
    }
}