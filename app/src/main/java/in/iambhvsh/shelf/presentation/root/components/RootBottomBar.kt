@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.presentation.root.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.presentation.root.bottomAppBarItems

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RootBottomBar(
    currentTab: Int,
    onTabChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier,
        shape = RoundedCornerShape(100),
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
            toolbarContainerColor = MaterialTheme.colorScheme.primaryContainer,
            toolbarContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        bottomAppBarItems.forEachIndexed { index, item ->
            val selected = currentTab == index
            ToggleButton(
                checked = selected,
                onCheckedChange = {
                    onTabChange(index)
                },
                colors = ToggleButtonDefaults.toggleButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    checkedContainerColor = MaterialTheme.colorScheme.primary,
                    checkedContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shapes = ToggleButtonShapes(
                    shape = CircleShape,
                    pressedShape = CircleShape,
                    checkedShape = CircleShape
                ),
                modifier = Modifier.height(56.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(if (selected) item.iconFilled else item.icon),
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                    AnimatedVisibility(
                        visible = selected,
                        enter = expandHorizontally(tween(200)),
                        exit = shrinkHorizontally(tween(150))
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier.padding(start = 8.dp, end = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
