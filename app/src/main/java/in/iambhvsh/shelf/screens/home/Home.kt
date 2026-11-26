package `in`.iambhvsh.shelf.screens.home

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.TopAppBarScrollBehavior
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
import `in`.iambhvsh.shelf.components.home.handleTap
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.features.home.HomeEvents
import `in`.iambhvsh.shelf.features.home.HomeSheets
import `in`.iambhvsh.shelf.features.home.HomeViewModel
import `in`.iambhvsh.shelf.features.setting.TapAction
import `in`.iambhvsh.shelf.features.setting.ViewMode
import `in`.iambhvsh.shelf.screens.search.SearchResults
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    sharedUrl: String? = null,
    tapAction: TapAction = TapAction.SHOW_PREVIEW,
    viewMode: ViewMode = ViewMode.GRID,
    isSearching: Boolean = false,
    searchResults: List<Bookmark> = emptyList(),
    searchQuery: String = "",
    isSearchLoading: Boolean = false,
    viewModel: HomeViewModel = koinViewModel(),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    var bookmarkToDelete by remember { mutableStateOf<Bookmark?>(null) }

    LaunchedEffect(sharedUrl) {
        if (sharedUrl != null) {
            viewModel.homeEvents(HomeEvents.OnTextFieldValueChange(sharedUrl))
            viewModel.homeEvents(HomeEvents.FabClick)
        }
    }

    val gridState = rememberLazyStaggeredGridState()
    val listState = rememberLazyListState()
    val itemCount = state.bookmarkData.size
    var prevCount by rememberSaveable { mutableIntStateOf(itemCount) }

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

    LaunchedEffect(state.duplicateToastKey) {
        if (state.duplicateToastKey > 0) {
            Toast.makeText(context, "URL already exists", Toast.LENGTH_SHORT).show()
            viewModel.homeEvents(HomeEvents.ClearDuplicateToast)
        }
    }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.homeEvents(HomeEvents.ClearToast)
        }
    }

    BackHandler(enabled = state.isSelectionMode) {
        viewModel.homeEvents(HomeEvents.ClearSelection)
    }

    var cachedResults by remember { mutableStateOf(searchResults) }
    var cachedQuery by remember { mutableStateOf(searchQuery) }

    if (isSearching) {
        cachedResults = searchResults
        cachedQuery = searchQuery
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HomeContent(
            state = state,
            viewMode = viewMode,
            gridState = gridState,
            listState = listState,
            tapAction = tapAction,
            context = context,
            clipboard = clipboard,
            viewModel = viewModel,
            scrollBehavior = scrollBehavior
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
                results = if (isSearching) searchResults else cachedResults,
                query = if (isSearching) searchQuery else cachedQuery,
                viewMode = viewMode,
                selectedIds = state.selectedIds,
                isSelectionMode = state.isSelectionMode,
                isLoading = isSearchLoading,
                onBodyClick = { item -> handleTap(item, tapAction, context, clipboard, viewModel) },
                onPhotoClick = { viewModel.homeEvents(HomeEvents.PreviewImageClick(url = it)) },
                onNoteClick = { item ->
                    viewModel.homeEvents(HomeEvents.BookmarkPreviewClick(item))
                    viewModel.homeEvents(HomeEvents.ShowNoteEditor(item.note))
                },
                onImageFailed = { item ->
                    viewModel.homeEvents(HomeEvents.RefreshBookmarkImage(item.id, item.url, item.imageUrl))
                }
            )
        }
    }

    HomeSheets(
        state = state,
        viewModel = viewModel,
        context = context,
        clipboard = clipboard,
        bookmarkToDelete = bookmarkToDelete,
        onSetBookmarkToDelete = { bookmarkToDelete = it }
    )
}
