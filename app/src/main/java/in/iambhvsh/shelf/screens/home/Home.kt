package `in`.iambhvsh.shelf.screens.home

import `in`.iambhvsh.shelf.features.home.*
import `in`.iambhvsh.shelf.components.home.*

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.components.home.BookmarkGrid
import `in`.iambhvsh.shelf.components.home.BookmarkList
import `in`.iambhvsh.shelf.components.home.EmptyBookmarkState
import `in`.iambhvsh.shelf.components.home.LoadingProgress
import `in`.iambhvsh.shelf.components.home.handleTap
import `in`.iambhvsh.shelf.screens.search.SearchResults
import `in`.iambhvsh.shelf.features.setting.TapAction
import `in`.iambhvsh.shelf.features.setting.ViewMode
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    sharedUrl: String? = null,
    tapAction: TapAction = TapAction.SHOW_PREVIEW,
    viewMode: ViewMode = ViewMode.GRID,
    searchResults: List<Bookmark>? = null,
    searchQuery: String = "",
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    var bookmarkToDelete by remember { mutableStateOf<Bookmark?>(null) }

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { }
        )
        LaunchedEffect(Unit) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

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

    if (searchResults != null) {
        SearchResults(
            results = searchResults,
            query = searchQuery,
            viewMode = viewMode,
            selectedIds = state.selectedIds,
            isSelectionMode = state.isSelectionMode,
            isLoading = state.isLoading,
            onBodyClick = { item -> handleTap(item, tapAction, context, clipboard, viewModel) },
            onPhotoClick = { viewModel.homeEvents(HomeEvents.PreviewImageClick(url = it)) },
            onNoteClick = { item ->
                viewModel.homeEvents(HomeEvents.BookmarkPreviewClick(item))
                viewModel.homeEvents(HomeEvents.ShowNoteEditor(item.note))
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (state.bookmarkData.isEmpty() && !state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyBookmarkState()
                    }
                } else if (viewMode == ViewMode.GRID) {
                    BookmarkGrid(
                        items = state.bookmarkData,
                        gridState = gridState,
                        selectedIds = state.selectedIds,
                        isSelectionMode = state.isSelectionMode,
                        tapAction = tapAction,
                        context = context,
                        clipboard = clipboard,
                        viewModel = viewModel,
                        hasTags = state.tags.isNotEmpty() && !state.isSelectionMode
                    )
                } else {
                    BookmarkList(
                        items = state.bookmarkData,
                        listState = listState,
                        selectedIds = state.selectedIds,
                        isSelectionMode = state.isSelectionMode,
                        tapAction = tapAction,
                        context = context,
                        clipboard = clipboard,
                        viewModel = viewModel,
                        hasTags = state.tags.isNotEmpty() && !state.isSelectionMode
                    )
                }
                LoadingProgress(state.isLoading)
            }
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
