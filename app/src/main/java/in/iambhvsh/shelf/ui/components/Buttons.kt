@file:Suppress("FunctionName", "OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Material 3 Expressive Button with shape morphing on press/hover/drag.
 */
@Composable
fun ExpressiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.expressiveBounce(scaleDown = 0.97f),
        enabled = enabled,
        shapes = ButtonDefaults.shapes(),
        colors = colors,
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * Material 3 Expressive FilledTonalButton with shape morphing.
 */
@Composable
fun ExpressiveFilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.expressiveBounce(scaleDown = 0.97f),
        enabled = enabled,
        shapes = ButtonDefaults.shapes(),
        colors = colors,
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * Material 3 Expressive OutlinedButton with shape morphing.
 */
@Composable
fun ExpressiveOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.expressiveBounce(scaleDown = 0.97f),
        enabled = enabled,
        shapes = ButtonDefaults.shapes(),
        colors = colors,
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * Material 3 Expressive TextButton with shape morphing.
 */
@Composable
fun ExpressiveTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.expressiveBounce(scaleDown = 0.97f),
        enabled = enabled,
        shapes = ButtonDefaults.shapes(),
        colors = colors,
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * Material 3 Expressive IconButton with shape morphing.
 */
@Composable
fun ExpressiveIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.expressiveBounce(scaleDown = 0.92f),
        enabled = enabled,
        shapes = IconButtonDefaults.shapes(),
        colors = colors,
        content = content
    )
}
