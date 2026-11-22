package `in`.iambhvsh.shelf.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
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
import java.util.concurrent.ConcurrentHashMap

val aspectRatioCache = ConcurrentHashMap<String, Float>()

@Composable
fun Media(
    imageUrl: String?,
    host: String,
    cardAspectRatio: Float,
    isSelectionMode: Boolean,
    onLongClick: () -> Unit,
    photoClickUrl: (String) -> Unit,
    onRatioDetected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var imageFailed by remember { mutableStateOf(false) }

    val high = MaterialTheme.colorScheme.surfaceContainerHigh
    val low = MaterialTheme.colorScheme.surfaceContainerLow
    val gradientBrush = remember(high, low) {
        Brush.verticalGradient(listOf(high, low))
    }
    val overlayBrush = remember {
        Brush.verticalGradient(
            listOf(
                Color.Transparent,
                Color.Black.copy(alpha = 0.3f)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(cardAspectRatio)
            .clip(MaterialTheme.shapes.largeIncreased)
            .background(gradientBrush),
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
                onSuccess = { state ->
                    val size = state.painter.intrinsicSize
                    if (size.width.isFinite() && size.height.isFinite() && size.width > 0f && size.height > 0f) {
                        val ratio = (size.width / size.height).coerceIn(9f / 16f, 1.91f)
                        aspectRatioCache[imageUrl] = ratio
                        onRatioDetected(ratio)
                    }
                },
                onError = { imageFailed = true }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.BottomCenter)
                    .background(overlayBrush)
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
}
