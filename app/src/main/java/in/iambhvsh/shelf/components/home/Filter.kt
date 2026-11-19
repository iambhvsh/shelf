package `in`.iambhvsh.shelf.components.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.data.Tag
import `in`.iambhvsh.shelf.components.common.expressiveBounce

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagFilterRow(
    tags: List<Tag>,
    activeTagIds: Set<Long>,
    onToggleTag: (Long) -> Unit
) {
    if (tags.isEmpty()) return

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(tags, key = { it.id }) { tag ->
            val isSelected = activeTagIds.contains(tag.id)
            FilterChip(
                selected = isSelected,
                onClick = { onToggleTag(tag.id) },
                shape = CircleShape,
                modifier = Modifier.expressiveBounce(scaleDown = 0.94f),
                label = {
                    Text(
                        text = tag.name,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            )
        }
    }
}
