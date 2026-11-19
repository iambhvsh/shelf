@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.components.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.theme.ShelfShapes

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingItem(
    icon: Any,
    title: String,
    index: Int = 0,
    count: Int = 1,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    destructive: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val headline: @Composable () -> Unit = {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }

    val supporting: (@Composable () -> Unit)? = subtitle?.let {
        {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    val leading: @Composable () -> Unit = {
        if (icon is androidx.compose.ui.graphics.vector.ImageVector) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        } else if (icon is Int) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }

    ListItem(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(ShelfShapes.item(index, count))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        headlineContent = headline,
        supportingContent = supporting,
        leadingContent = leading,
        trailingContent = trailing
    )
}
