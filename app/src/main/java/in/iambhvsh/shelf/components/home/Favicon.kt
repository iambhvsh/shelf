package `in`.iambhvsh.shelf.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import `in`.iambhvsh.shelf.theme.ShelfShapes

@Composable
fun Favicon(
    host: String,
    cleanHost: String,
    modifier: Modifier = Modifier
) {
    var faviconFailed by remember { mutableStateOf(false) }
    val faviconShape = ShelfShapes.expressiveShapeFor(cleanHost)

    if (faviconFailed) {
        Box(
            modifier = modifier
                .size(24.dp)
                .clip(faviconShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = cleanHost.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    } else {
        AsyncImage(
            model = "https://t0.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&url=http://${host}&size=128",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(24.dp)
                .clip(faviconShape),
            onError = { faviconFailed = true }
        )
    }
}
