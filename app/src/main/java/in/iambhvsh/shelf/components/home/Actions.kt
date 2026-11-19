package `in`.iambhvsh.shelf.components.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.components.common.expressiveBounce
import `in`.iambhvsh.shelf.theme.ShelfShapes

data class PreviewAction(
    val title: String,
    val icon: Any,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PreviewActions(
    actions: List<PreviewAction>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(ShelfShapes.Gap)
    ) {
        actions.forEachIndexed { index, action ->
            SegmentedListItem(
                onClick = action.onClick,
                shapes = ShelfShapes.segmented(index, actions.size),
                modifier = Modifier
                    .fillMaxWidth()
                    .expressiveBounce(scaleDown = 0.98f),
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                content = {
                    Text(
                        text = action.title,
                        color = if (action.isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                },
                leadingContent = {
                    val tint = if (action.isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    if (action.icon is ImageVector) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = action.title,
                            modifier = Modifier.size(24.dp),
                            tint = tint
                        )
                    } else if (action.icon is Int) {
                        Icon(
                            painter = painterResource(action.icon),
                            contentDescription = action.title,
                            modifier = Modifier.size(24.dp),
                            tint = tint
                        )
                    }
                }
            )
        }
    }
}
