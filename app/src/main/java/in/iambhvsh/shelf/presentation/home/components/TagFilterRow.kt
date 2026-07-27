package `in`.iambhvsh.shelf.presentation.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.domain.model.Tag

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
                label = { Text(tag.name) }
            )
        }
    }
}
