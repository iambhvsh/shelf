@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.screens.setting

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.features.setting.SettingEvents
import `in`.iambhvsh.shelf.features.setting.SettingState
import `in`.iambhvsh.shelf.features.setting.SettingViewModel

@Composable
fun DynamicColorCard(
    state: SettingState,
    viewModel: SettingViewModel,
    hasAccent: Boolean,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    val dynamicInteractionSource = remember { MutableInteractionSource() }
    val isDynamicPressed by dynamicInteractionSource.collectIsPressedAsState()

    val smallRadius = 4.dp
    val largeRadius = 28.dp

    val dynamicTopRadius by animateDpAsState(
        targetValue = if (isDynamicPressed) largeRadius else smallRadius,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "dynamicTopRadius"
    )

    val dynamicBottomRadius by animateDpAsState(
        targetValue = if (isDynamicPressed || !hasAccent) largeRadius else smallRadius,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "dynamicBottomRadius"
    )

    val dynamicShape = RoundedCornerShape(
        topStart = dynamicTopRadius.coerceAtLeast(0.dp),
        topEnd = dynamicTopRadius.coerceAtLeast(0.dp),
        bottomStart = dynamicBottomRadius.coerceAtLeast(0.dp),
        bottomEnd = dynamicBottomRadius.coerceAtLeast(0.dp)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(dynamicShape)
            .clickable(
                interactionSource = dynamicInteractionSource,
                indication = ripple(),
                onClick = { viewModel.onEvent(SettingEvents.ToggleDynamicColor(!state.dynamicColor)) }
            ),
        shape = dynamicShape,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ColorLens,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Dynamic color",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (state.dynamicColor) "Match wallpaper colors" else "Use custom accent color",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = state.dynamicColor,
                onCheckedChange = { viewModel.onEvent(SettingEvents.ToggleDynamicColor(it)) }
            )
        }
    }
}

@Composable
fun AccentColorCard(
    state: SettingState,
    viewModel: SettingViewModel,
    hasAccent: Boolean,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = hasAccent,
        modifier = modifier,
        enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                expandVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)),
        exit = fadeOut(spring(stiffness = Spring.StiffnessLow)) +
                shrinkVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow))
    ) {
        val accentInteractionSource = remember { MutableInteractionSource() }
        val isAccentPressed by accentInteractionSource.collectIsPressedAsState()

        val smallRadius = 4.dp
        val largeRadius = 28.dp

        val accentTopRadius by animateDpAsState(
            targetValue = if (isAccentPressed) largeRadius else smallRadius,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "accentTopRadius"
        )

        val accentShape = RoundedCornerShape(
            topStart = accentTopRadius.coerceAtLeast(0.dp),
            topEnd = accentTopRadius.coerceAtLeast(0.dp),
            bottomStart = largeRadius,
            bottomEnd = largeRadius
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(accentShape)
                .clickable(
                    interactionSource = accentInteractionSource,
                    indication = ripple(),
                    onClick = { viewModel.onEvent(SettingEvents.ShowAccentColorSheet) }
                ),
            shape = accentShape,
            colors = CardDefaults.cardColors(containerColor = containerColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Accent color",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = state.accentColor.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(state.accentColor.seed)
                )
            }
        }
    }
}
