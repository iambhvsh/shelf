@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.components.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Spring physics bounce modifier. Compresses slightly on press and springs back
 * with fluid, physical motion.
 */
@Composable
fun Modifier.expressiveBounce(
    scaleDown: Float = 0.96f,
    interactionSource: MutableInteractionSource? = null,
    enabled: Boolean = true
): Modifier {
    if (!enabled) return this
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "expressiveBounceScale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
