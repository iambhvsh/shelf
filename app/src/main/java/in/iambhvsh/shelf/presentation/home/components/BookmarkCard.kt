package `in`.iambhvsh.shelf.presentation.home.components

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.core.net.toUri


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BookmarkCard(
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
    isPinned: Boolean = false,
    url: String,
    note: String? = null,
    reminderTime: Long? = null
) {

    val host = remember(url) { url.toUri().host.orEmpty() }
    
    val isVertical = remember(url) {
        listOf(
            "instagram.com/reel", "instagram.com/p/", "ig.me",
            "youtube.com/shorts", 
            "tiktok.com", 
            "facebook.com/reel", "fb.watch",
            "pinterest.com/pin", "pin.it"
        ).any { url.contains(it, ignoreCase = true) }
    }
                     
    val cardAspectRatio = if (isVertical) 9f / 16f else 1.2f

    val cleanHost = remember(host) {
        if (host.startsWith("www.")) {
            host.removePrefix("www.")
        } else {
            host
        }
    }

    var imageFailed by remember { mutableStateOf(false) }
    var faviconFailed by remember { mutableStateOf(false) }

    val cardModifier = if (isSelected) {
        modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(MaterialTheme.shapes.extraLarge)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.extraLarge
            )
            .combinedClickable(
                onClick = onLongClick,
                onLongClick = onLongClick
            )
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(6.dp)
    } else {
        modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(MaterialTheme.shapes.extraLarge)
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) onLongClick() else bodyClick()
                },
                onLongClick = onLongClick
            )
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(6.dp)
    }

    Column(
        modifier = cardModifier
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (faviconFailed) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cleanHost.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            } else {
                AsyncImage(
                    model = "https://t0.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&url=http://${host}&size=128",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(MaterialTheme.shapes.extraSmall),
                    onError = {
                        faviconFailed = true
                    }
                )
            }

            Spacer(Modifier.width(10.dp))

            Text(
                text = cleanHost,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )



            if (reminderTime != null) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Has Reminder",
                    modifier = Modifier.size(16.dp).padding(end = if (isPinned) 4.dp else 0.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isPinned) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Pinned",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(cardAspectRatio)
                .clip(MaterialTheme.shapes.extraLarge)
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

            if (!imageUrl.isNullOrBlank() && !imageFailed) {

                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            if (isSelectionMode) onLongClick() else photoClickUrl(imageUrl)
                        },
                    onError = {
                        imageFailed = true
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                )
                            )
                        )
                )

            } else {


                Text(
                    text = host,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }


        if (!title.isNullOrBlank() || !description.isNullOrBlank()) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {

                if (!title.isNullOrBlank()) {
                    Text(
                        text = title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                (description.takeUnless { it.isNullOrBlank() } ?: title)?.let {
                    Text(
                        text = it,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (note != null) {
                    androidx.compose.material3.Surface(
                        shape = MaterialTheme.shapes.small,
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
        }
    }
}

