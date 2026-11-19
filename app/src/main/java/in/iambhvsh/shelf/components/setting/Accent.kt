@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.components.setting

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.components.common.expressiveBounce
import `in`.iambhvsh.shelf.theme.AccentColor

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun shapeForAccent(accent: AccentColor): Shape {
    return when (accent) {
        AccentColor.PERIWINKLE -> MaterialShapes.Clover4Leaf.toShape()
        AccentColor.RED -> MaterialShapes.Heart.toShape()
        AccentColor.ORANGE -> MaterialShapes.Sunny.toShape()
        AccentColor.YELLOW -> MaterialShapes.Flower.toShape()
        AccentColor.GREEN -> MaterialShapes.Cookie4Sided.toShape()
        AccentColor.TEAL -> MaterialShapes.Pill.toShape()
        AccentColor.BLUE -> MaterialShapes.Gem.toShape()
        AccentColor.PURPLE -> MaterialShapes.Burst.toShape()
        AccentColor.PINK -> MaterialShapes.PuffyDiamond.toShape()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccentColorSheet(
    current: AccentColor,
    onSelect: (AccentColor) -> Unit,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLargeIncreased,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = screenHeight * 0.9f)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
        ) {
            Text(
                text = "Accent color",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 16.dp)
            )

            val rows = AccentColor.entries.chunked(4)

            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                rows.forEach { rowColors ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        rowColors.forEach { accent ->
                            AccentSwatch(
                                accent = accent,
                                selected = accent == current,
                                onClick = { onSelect(accent) }
                            )
                        }
                        repeat(4 - rowColors.size) {
                            Spacer(modifier = Modifier.size(64.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.size(24.dp))
        }
    }
}

@Composable
private fun AccentSwatch(
    accent: AccentColor,
    selected: Boolean,
    onClick: () -> Unit
) {
    val checkTint = if (accent.seed.luminance() > 0.5f) Color.Black else Color.White
    val swatchShape = shapeForAccent(accent)

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "accentScale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .expressiveBounce(scaleDown = 0.92f)
                .selectable(
                    selected = selected,
                    onClick = onClick,
                    role = Role.RadioButton
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(swatchShape)
                    .background(accent.seed, swatchShape)
                    .border(
                        width = if (selected) 3.dp else 0.dp,
                        color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                        shape = swatchShape
                    )
            )

            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = checkTint,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(Modifier.size(6.dp))
        Text(
            text = accent.label,
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
