package `in`.iambhvsh.shelf.components.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import `in`.iambhvsh.shelf.R

@Composable
fun CollectionCover(
    imageUrls: List<String>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when (imageUrls.size) {
            0 -> EmptyCoverPlaceholder()
            1 -> SinglePhoto(imageUrls[0])
            2 -> TwoPhotoCollage(imageUrls[0], imageUrls[1])
            3 -> ThreePhotoCollage(imageUrls[0], imageUrls[1], imageUrls[2])
            else -> FourPhotoCollage(imageUrls[0], imageUrls[1], imageUrls[2], imageUrls[3])
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmptyCoverPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
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
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(MaterialShapes.Cookie4Sided.toShape())
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.bookmark_one),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun SinglePhoto(url: String) {
    AsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun TwoPhotoCollage(url1: String, url2: String) {
    Row(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = url1,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.weight(1f).fillMaxSize()
        )
        Spacer(modifier = Modifier.width(2.dp))
        AsyncImage(
            model = url2,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.weight(1f).fillMaxSize()
        )
    }
}

@Composable
private fun ThreePhotoCollage(url1: String, url2: String, url3: String) {
    Row(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = url1,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.weight(1f).fillMaxSize()
        )
        Spacer(modifier = Modifier.width(2.dp))
        Column(modifier = Modifier.weight(1f)) {
            AsyncImage(
                model = url2,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(2.dp))
            AsyncImage(
                model = url3,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FourPhotoCollage(url1: String, url2: String, url3: String, url4: String) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(1f)) {
            AsyncImage(
                model = url1,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.weight(1f).fillMaxSize()
            )
            Spacer(modifier = Modifier.width(2.dp))
            AsyncImage(
                model = url2,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.weight(1f).fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(modifier = Modifier.weight(1f)) {
            AsyncImage(
                model = url3,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.weight(1f).fillMaxSize()
            )
            Spacer(modifier = Modifier.width(2.dp))
            AsyncImage(
                model = url4,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.weight(1f).fillMaxSize()
            )
        }
    }
}
