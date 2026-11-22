package `in`.iambhvsh.shelf.screens.collection

import android.content.ClipData
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import `in`.iambhvsh.shelf.components.common.copyToClipboard
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.features.collection.CollectionDetailSheets
import `in`.iambhvsh.shelf.features.collection.CollectionEvents
import `in`.iambhvsh.shelf.features.collection.CollectionViewModel
import `in`.iambhvsh.shelf.features.setting.TapAction
import `in`.iambhvsh.shelf.features.setting.ViewMode
import `in`.iambhvsh.shelf.openChromeTab
import `in`.iambhvsh.shelf.screens.search.SearchResults

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionDetailScreen(
    collectionId: Long,
    tapAction: TapAction = TapAction.SHOW_PREVIEW,
    viewMode: ViewMode = ViewMode.GRID,
    viewModel: CollectionViewModel,
    searchResults: List<Bookmark>? = null,
    searchQuery: String = ""
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current
    val gridState = rememberLazyStaggeredGridState()
    val listState = rememberLazyListState()
    val itemCount = state.collectionBookmarks.size
    var prevCount by rememberSaveable { mutableIntStateOf(itemCount) }
    var bookmarkToDelete by remember { mutableStateOf<Bookmark?>(null) }

    LaunchedEffect(itemCount) {
        if (itemCount > prevCount && itemCount > 0) {
            if (viewMode == ViewMode.GRID) gridState.animateScrollToItem(0)
            else listState.animateScrollToItem(0)
        }
        prevCount = itemCount
    }

    LaunchedEffect(state.sortOrder) {
        if (itemCount > 0) {
            if (viewMode == ViewMode.GRID) gridState.scrollToItem(0)
            else listState.scrollToItem(0)
        }
    }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(CollectionEvents.ClearToast)
        }
    }

    BackHandler(enabled = state.isDetailSelectionMode) {
        viewModel.onEvent(CollectionEvents.ClearDetailSelection)
    }

    LaunchedEffect(collectionId) {
        viewModel.onEvent(CollectionEvents.SelectCollectionById(collectionId))
    }

    val isSearching = searchResults != null
    var cachedResults by remember { mutableStateOf(searchResults ?: emptyList()) }
    var cachedQuery by remember { mutableStateOf(searchQuery) }

    if (searchResults != null) {
        cachedResults = searchResults
        cachedQuery = searchQuery
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CollectionDetailContent(
            state = state,
            collectionId = collectionId,
            viewMode = viewMode,
            gridState = gridState,
            listState = listState,
            tapAction = tapAction,
            context = context,
            clipboardManager = clipboardManager,
            viewModel = viewModel
        )

        AnimatedVisibility(
            visible = isSearching,
            enter = fadeIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
            exit = fadeOut(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        ) {
            SearchResults(
                results = searchResults ?: cachedResults,
                query = if (isSearching) searchQuery else cachedQuery,
                viewMode = viewMode,
                selectedIds = state.detailSelectedIds,
                isSelectionMode = state.isDetailSelectionMode,
                isLoading = state.isDetailLoading,
                onBodyClick = { item ->
                    when (tapAction) {
                        TapAction.OPEN_BROWSER -> item.url.let { openChromeTab(it, context) }
                        TapAction.COPY_LINK -> context.copyToClipboard(item.url)
                        TapAction.SHOW_PREVIEW -> viewModel.onEvent(CollectionEvents.ShowDetailBodySheet(item))
                    }
                },
                onPhotoClick = { Log.d("CollectionDetail", "Photo click: $it") },
                onLongClick = { viewModel.onEvent(CollectionEvents.ToggleDetailSelection(it)) }
            )
        }
    }

    CollectionDetailSheets(
        state = state,
        viewModel = viewModel,
        context = context,
        clipboard = clipboardManager,
        bookmarkToDelete = bookmarkToDelete,
        onSetBookmarkToDelete = { bookmarkToDelete = it }
    )
}
