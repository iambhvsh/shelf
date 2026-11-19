package `in`.iambhvsh.shelf.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import `in`.iambhvsh.shelf.ui.components.expressiveBounce

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BookmarkListItem(
    imageUrl: String?,
    title: String?,
    description: String?,
    modifier: Modifier = Modifier,
    photoClickUrl: (String) -> Unit,
    bodyClick: () -> Unit,
    onLongClick: () -> Unit,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onNoteClick: () -> Unit = {},
    url: String,
    note: String? = null,
    reminderTime: Long? = null
) {
    val host = remember(url) { url.toUri().host.orEmpty() }
    val cleanHost = remember(host) { if (host.startsWith("www.")) host.removePrefix("www.") else host }

    var imageFailed by remember { mutableStateOf(false) }
    var imageAspectRatio by remember { mutableStateOf(1f) }
    val hasImage = !imageUrl.isNullOrBlank() && !imageFailed

    val cardShape = if (isSelected) MaterialTheme.shapes.extraLargeIncreased else MaterialTheme.shapes.largeIncreased
    val cardModifier = modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .expressiveBounce(scaleDown = 0.97f)
        .clip(cardShape)
        .then(
            if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, cardShape)
            else Modifier
        )
        .combinedClickable(
            onClick = {
                if (isSelectionMode) onLongClick() else bodyClick()
            },
            onLongClick = onLongClick
        )
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .padding(14.dp)

    Row(
        modifier = cardModifier,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Favicon(host = host, cleanHost = cleanHost)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = cleanHost,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (reminderTime != null) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Has Reminder",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(2.dp))

            if (!title.isNullOrBlank()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val desc = remember(description, title) { description.takeUnless { it.isNullOrBlank() } ?: title }
            if (desc != null) {
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
            } else if (title.isNullOrBlank()) {
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (note != null) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clickable { onNoteClick() }
                ) {
                    Text(
                        text = note,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(Modifier.width(14.dp))
        if (hasImage) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(140.dp)
                    .aspectRatio(imageAspectRatio)
                    .clip(MaterialTheme.shapes.largeIncreased)
                    .clickable {
                        if (isSelectionMode) onLongClick() else photoClickUrl(imageUrl)
                    },
                onError = { imageFailed = true },
                onSuccess = { state ->
                    val size = state.painter.intrinsicSize
                    if (size.width.isFinite() && size.width > 0 && size.height > 0) {
                        imageAspectRatio = (size.width / size.height).coerceIn(1f, 1.77f)
                    }
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .aspectRatio(1.2f)
                    .clip(MaterialTheme.shapes.largeIncreased)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cleanHost,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
