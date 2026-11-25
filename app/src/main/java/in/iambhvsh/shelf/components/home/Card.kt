package `in`.iambhvsh.shelf.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Notifications
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import `in`.iambhvsh.shelf.components.common.expressiveBounce

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
    reminderTime: Long? = null,
    onImageFailed: (() -> Unit)? = null
) {
    val host = remember(url) { url.toUri().host.orEmpty() }
    val cleanHost = remember(host) {
        if (host.startsWith("www.")) host.removePrefix("www.") else host
    }

    val cachedRatio = remember(imageUrl) {
        if (!imageUrl.isNullOrBlank()) aspectRatioCache[imageUrl] else null
    }
    val initialAspectRatio = remember(url) {
        val isVertical = listOf(
            "instagram.com/reel", "ig.me", "youtube.com/shorts",
            "tiktok.com", "facebook.com/reel", "fb.watch", "pinterest.com/pin", "pin.it"
        ).any { url.contains(it, ignoreCase = true) }
        if (isVertical) 9f / 16f else 16f / 9f
    }
    var detectedAspectRatio by remember(imageUrl) { mutableStateOf(cachedRatio) }
    val cardAspectRatio = detectedAspectRatio ?: initialAspectRatio

    val cardShape = if (isSelected) MaterialTheme.shapes.extraLargeIncreased else MaterialTheme.shapes.largeIncreased
    val cardModifier = modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .expressiveBounce(scaleDown = 0.96f)
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
        .padding(6.dp)

    Column(modifier = cardModifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Favicon(host = host, cleanHost = cleanHost)
            Spacer(Modifier.width(10.dp))
            Text(
                text = cleanHost,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
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
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Media(
            imageUrl = imageUrl,
            host = host,
            cardAspectRatio = cardAspectRatio,
            isSelectionMode = isSelectionMode,
            onLongClick = onLongClick,
            photoClickUrl = photoClickUrl,
            onRatioDetected = { detectedAspectRatio = it },
            onImageFailed = onImageFailed
        )

        if (!title.isNullOrBlank() || !description.isNullOrBlank() || note != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                if (!title.isNullOrBlank()) {
                    Text(
                        text = title,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        maxLines = 3,
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
        }
    }
}
