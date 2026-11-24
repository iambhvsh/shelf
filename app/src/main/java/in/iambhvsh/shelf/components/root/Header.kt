@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.components.root

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelfHeader(
    currentTab: Int,
    isDeepScreen: Boolean,
    deepScreenTitle: String? = null,
    isCollectionDetail: Boolean = false,
    isSelectionMode: Boolean = false,
    selectedCount: Int = 0,
    isAllSelected: Boolean = false,
    isSearching: Boolean = false,
    searchQuery: String = "",
    searchPlaceholder: String = "Search bookmarks\u2026",
    onSearchQueryChange: (String) -> Unit = {},
    focusRequester: FocusRequester? = null,
    scrollBehavior: TopAppBarScrollBehavior,
    onBack: () -> Unit = {},
    onNewBookmarkClick: () -> Unit = {},
    onNewCollectionClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSortClick: () -> Unit = {},
    onCloseSelection: () -> Unit = {},
    onSelectAll: () -> Unit = {},
    onDeselectAll: () -> Unit = {},
    onDeleteSelected: (() -> Unit)? = null,
    onAddToCollection: (() -> Unit)? = null,
    onRenameSelected: (() -> Unit)? = null,
    onCloseSearch: () -> Unit = {},
    isTrashScreen: Boolean = false,
    onRestoreSelected: (() -> Unit)? = null,
    onEmptyTrash: (() -> Unit)? = null
) {
    val smoothed = rememberSmoothedHeaderValues(scrollBehavior.state)
    val smoothedAlpha = smoothed.alpha
    val smoothedOffset = smoothed.offset

    val sideSlotWidth = 72.dp
    val isShiftedToStart = isDeepScreen || isSelectionMode

    val titleBias by animateFloatAsState(
        targetValue = if (isShiftedToStart) -1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "TitleAlignment"
    )

    val titleXOffset by animateDpAsState(
        targetValue = if (isShiftedToStart) 80.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "TitleOffset"
    )

    val surface = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = smoothedAlpha.value }
            .background(surface)
    ) {
        CenterAlignedTopAppBar(
            scrollBehavior = scrollBehavior,
            windowInsets = WindowInsets.statusBars,
            navigationIcon = {},
            title = {},
            actions = {},
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            )
        )

        HeaderCenterSlot(
            isSearching = isSearching,
            titleBias = titleBias,
            titleXOffset = titleXOffset,
            isShiftedToStart = isShiftedToStart,
            isSelectionMode = isSelectionMode,
            selectedCount = selectedCount,
            isDeepScreen = isDeepScreen,
            deepScreenTitle = deepScreenTitle,
            currentTab = currentTab,
            searchQuery = searchQuery,
            searchPlaceholder = searchPlaceholder,
            onSearchQueryChange = onSearchQueryChange,
            focusRequester = focusRequester,
            smoothedAlpha = smoothedAlpha,
            smoothedOffset = smoothedOffset
        )

        val leftState = when {
            isSearching -> HeaderLeftType.CLOSE
            isSelectionMode -> HeaderLeftType.CLOSE
            isDeepScreen -> HeaderLeftType.BACK
            currentTab == 0 || currentTab == 1 -> HeaderLeftType.NEW
            else -> HeaderLeftType.NONE
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset { IntOffset(0, smoothedOffset.value.roundToInt()) }
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(64.dp)
                .widthIn(min = sideSlotWidth)
                .graphicsLayer {
                    val a = smoothedAlpha.value
                    alpha = a
                    val scale = 1f - ((1f - a) * 0.12f)
                    scaleX = scale
                    scaleY = scale
                }
                .zIndex(2f),
            contentAlignment = Alignment.CenterStart
        ) {
            LeftActionSlot(
                leftState = leftState,
                isSearching = isSearching,
                onBack = onBack,
                onCloseSearch = onCloseSearch,
                onCloseSelection = onCloseSelection,
                onNewBookmarkClick = onNewBookmarkClick,
                onNewCollectionClick = onNewCollectionClick
            )
        }

        AnimatedVisibility(
            visible = !isSearching,
            modifier = Modifier.align(Alignment.TopEnd),
            enter = fadeIn(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)) +
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
                    ),
            exit = fadeOut(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)) +
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
                    )
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(0, smoothedOffset.value.roundToInt()) }
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .height(64.dp)
                    .widthIn(min = sideSlotWidth)
                .graphicsLayer {
                    val a = smoothedAlpha.value
                    alpha = a
                    val scale = 1f - ((1f - a) * 0.12f)
                    scaleX = scale
                    scaleY = scale
                }
                .zIndex(3f),
            contentAlignment = Alignment.CenterEnd
        ) {
            RightActionSlot(
                isSelectionMode = isSelectionMode,
                currentTab = currentTab,
                isAllSelected = isAllSelected,
                onSelectAll = onSelectAll,
                onDeselectAll = onDeselectAll,
                onSearchClick = onSearchClick,
                onSortClick = onSortClick,
                onAddToCollection = onAddToCollection,
                onRenameSelected = onRenameSelected,
                onDeleteSelected = onDeleteSelected,
                isCollectionDetail = isCollectionDetail,
                onNewBookmarkClick = onNewBookmarkClick,
                isTrashScreen = isTrashScreen,
                onRestoreSelected = onRestoreSelected,
                onEmptyTrash = onEmptyTrash
            )
        }
    }
}
}
