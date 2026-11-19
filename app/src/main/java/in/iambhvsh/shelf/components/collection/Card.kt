package `in`.iambhvsh.shelf.components.collection

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.data.Collection
import `in`.iambhvsh.shelf.components.common.expressiveBounce

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun CollectionCard(
    collection: Collection,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    modifier: Modifier = Modifier
) {
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
                if (isSelectionMode) onLongClick() else onClick()
            },
            onLongClick = onLongClick
        )
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .padding(6.dp)

    Column(modifier = cardModifier) {
        CollectionCover(
            imageUrls = collection.previewUrls,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f)
                .clip(MaterialTheme.shapes.largeIncreased)
        )

        val displayName = remember(collection.name) {
            if (collection.name.length > 13) {
                "${collection.name.take(13)}..."
            } else {
                collection.name
            }
        }

        Text(
            text = displayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 8.dp)
        )

        val countText = "${collection.bookmarkCount} bookmark${if (collection.bookmarkCount != 1) "s" else ""}"
        Text(
            text = countText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
        )
    }
}
