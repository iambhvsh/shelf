@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object ShelfShapes {

    val Gap = ListItemDefaults.SegmentedGap

    val topItem: RoundedCornerShape
        @Composable get() = RoundedCornerShape(
            topStart = shapes.largeIncreased.topStart,
            topEnd = shapes.largeIncreased.topEnd,
            bottomStart = shapes.extraSmall.bottomStart,
            bottomEnd = shapes.extraSmall.bottomEnd
        )

    val middleItem: RoundedCornerShape
        @Composable get() = RoundedCornerShape(shapes.extraSmall.topStart)

    val bottomItem: RoundedCornerShape
        @Composable get() = RoundedCornerShape(
            topStart = shapes.extraSmall.topStart,
            topEnd = shapes.extraSmall.topEnd,
            bottomStart = shapes.largeIncreased.bottomStart,
            bottomEnd = shapes.largeIncreased.bottomEnd
        )

    val card: CornerBasedShape
        @Composable get() = shapes.largeIncreased

    @Composable
    fun segmented(index: Int, count: Int): ListItemShapes =
        ListItemDefaults.segmentedShapes(
            index,
            count,
            ListItemDefaults.shapes(
                shape = if (count == 1) shapes.largeIncreased else shapes.extraSmall,
                selectedShape = shapes.extraLargeIncreased,
                pressedShape = shapes.extraLargeIncreased,
                focusedShape = shapes.largeIncreased,
                hoveredShape = shapes.extraLarge,
                draggedShape = shapes.extraLargeIncreased
            )
        )

    @Composable
    fun item(index: Int, count: Int): Shape = when {
        count == 1 -> shapes.largeIncreased
        index == 0 -> topItem
        index == count - 1 -> bottomItem
        else -> middleItem
    }

    /**
     * Maps any string key (such as domain host or tag) to a distinct, deterministic
     * Material 3 Expressive organic shape.
     */
    @Composable
    fun expressiveShapeFor(key: String): Shape {
        val hash = (key.hashCode() and 0x7FFFFFFF) % 12
        return when (hash) {
            0 -> MaterialShapes.Cookie4Sided.toShape()
            1 -> MaterialShapes.Clover4Leaf.toShape()
            2 -> MaterialShapes.Pill.toShape()
            3 -> MaterialShapes.PuffyDiamond.toShape()
            4 -> MaterialShapes.Pentagon.toShape()
            5 -> MaterialShapes.Arch.toShape()
            6 -> MaterialShapes.Flower.toShape()
            7 -> MaterialShapes.Gem.toShape()
            8 -> MaterialShapes.Sunny.toShape()
            9 -> MaterialShapes.SoftBurst.toShape()
            10 -> MaterialShapes.Cookie12Sided.toShape()
            else -> MaterialShapes.Bun.toShape()
        }
    }
}
