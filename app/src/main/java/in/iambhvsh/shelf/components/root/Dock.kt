@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.components.root

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.zIndex
import `in`.iambhvsh.shelf.R

data class BottomAppBarItem(
    val title: String,
    val icon: Int,
    val iconFilled: Int
)

val bottomAppBarItems = listOf(
    BottomAppBarItem(
        title = "Home",
        icon = R.drawable.home_icon,
        iconFilled = R.drawable.home_icon_filled
    ),
    BottomAppBarItem(
        title = "Collections",
        icon = R.drawable.bookmark_one,
        iconFilled = R.drawable.bookmark_fill
    ),
    BottomAppBarItem(
        title = "Settings",
        icon = R.drawable.setting_icon,
        iconFilled = R.drawable.setting_icon_filled
    )
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Dock(
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
                        enter = fadeIn() + slideInHorizontally(
                            initialOffsetX = { -15 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) + expandHorizontally(expandFrom = Alignment.Start),
                        exit = fadeOut() + slideOutHorizontally(
                            targetOffsetX = { -15 }
                        ) + shrinkHorizontally(
                            shrinkTowards = Alignment.Start
                        )
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

@Composable
fun AnimatedDock(
    visible: Boolean,
    currentTab: Int,
    onTabChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 16.dp)
            .zIndex(10f),
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeOut()
    ) {
        Dock(
            currentTab = currentTab,
            onTabChange = onTabChange
        )
    }
}
