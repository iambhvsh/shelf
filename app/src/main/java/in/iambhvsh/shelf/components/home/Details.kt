package `in`.iambhvsh.shelf.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import `in`.iambhvsh.shelf.data.Bookmark

@Composable
fun PreviewDetails(
    bookmark: Bookmark,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
    onPhotoClick: ((String) -> Unit)? = null
) {
    val host = remember(bookmark.url) { bookmark.url.toUri().host.orEmpty() }
    val cleanHost = remember(host) {
        if (host.startsWith("www.")) host.removePrefix("www.") else host
    }

    val cachedRatio = remember(bookmark.imageUrl) {
        if (!bookmark.imageUrl.isNullOrBlank()) aspectRatioCache[bookmark.imageUrl] else null
    }
    val initialAspectRatio = remember(bookmark.url) {
        val isVertical = listOf(
            "instagram.com/reel", "ig.me", "youtube.com/shorts",
            "tiktok.com", "facebook.com/reel", "fb.watch", "pinterest.com/pin", "pin.it"
        ).any { bookmark.url.contains(it, ignoreCase = true) }
        if (isVertical) 9f / 16f else 16f / 9f
    }
    var detectedRatio by remember(bookmark.imageUrl) { mutableStateOf(cachedRatio) }
    val ratio = detectedRatio ?: initialAspectRatio
    val isPortrait = ratio < 0.95f

    Card(
        modifier = modifier
            .padding(contentPadding)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.largeIncreased),
        shape = MaterialTheme.shapes.largeIncreased,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (!bookmark.imageUrl.isNullOrBlank()) {
                PreviewMedia(
                    imageUrl = bookmark.imageUrl,
                    isPortrait = isPortrait,
                    onRatioDetected = { newRatio ->
                        detectedRatio = newRatio
                        aspectRatioCache[bookmark.imageUrl] = newRatio
                    },
                    onPhotoClick = onPhotoClick
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Favicon(host = host, cleanHost = cleanHost, modifier = Modifier.size(24.dp))
                    Text(
                        text = bookmark.title?.takeIf { it.isNotBlank() } ?: cleanHost.ifEmpty { "Bookmark" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                if (!bookmark.description.isNullOrBlank() && bookmark.description != bookmark.title) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = bookmark.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewMedia(
    imageUrl: String,
    isPortrait: Boolean,
    onRatioDetected: (Float) -> Unit,
    onPhotoClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var imageFailed by remember { mutableStateOf(false) }
    if (imageFailed) return

    val containerHeight = if (isPortrait) 240.dp else 200.dp
    val containerModifier = modifier
        .fillMaxWidth()
        .height(containerHeight)
        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        .then(
            if (onPhotoClick != null) Modifier.clickable { onPhotoClick(imageUrl) }
            else Modifier
        )

    Box(
        modifier = containerModifier,
        contentAlignment = Alignment.Center
    ) {
        if (isPortrait) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(20.dp)
                    .graphicsLayer { alpha = 0.45f }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.35f)
                            )
                        )
                    )
            )

            AsyncImage(
                model = imageUrl,
                contentDescription = "Bookmark Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 10.dp)
                    .clip(RoundedCornerShape(12.dp)),
                onSuccess = { state ->
                    val size = state.painter.intrinsicSize
                    if (size.width.isFinite() && size.height.isFinite() && size.width > 0f && size.height > 0f) {
                        val detected = size.width / size.height
                        onRatioDetected(detected)
                    }
                },
                onError = { imageFailed = true }
            )
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Bookmark Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onSuccess = { state ->
                    val size = state.painter.intrinsicSize
                    if (size.width.isFinite() && size.height.isFinite() && size.width > 0f && size.height > 0f) {
                        val detected = size.width / size.height
                        onRatioDetected(detected)
                    }
                },
                onError = { imageFailed = true }
            )
        }
    }
}
