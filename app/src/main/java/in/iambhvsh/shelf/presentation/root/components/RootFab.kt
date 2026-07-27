package in.iambhvsh.shelf.presentation.root.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import in.iambhvsh.shelf.R
import in.iambhvsh.shelf.presentation.collection.CollectionEvents
import in.iambhvsh.shelf.presentation.collection.CollectionState
import in.iambhvsh.shelf.presentation.collection.CollectionViewModel
import in.iambhvsh.shelf.presentation.home.HomeEvents
import in.iambhvsh.shelf.presentation.home.HomeState

@Composable
fun RootFab(
    currentTab: Int,
    homeState: HomeState,
    collectionState: CollectionState,
    isSearching: Boolean = false,
    onHomeFabClick: () -> Unit,
    onCollectionFabClick: () -> Unit
) {
    if (isSearching || homeState.isSelectionMode || collectionState.isSelectionMode ||
        collectionState.isDetailSelectionMode
    ) return

    when (currentTab) {
        0 -> FloatingActionButton(onClick = onHomeFabClick) {
            Icon(
                painterResource(R.drawable.add_icons),
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
        }

        1 -> if (collectionState.selectedCollection == null) {
            FloatingActionButton(onClick = onCollectionFabClick) {
                Icon(
                    painterResource(R.drawable.add_icons),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
