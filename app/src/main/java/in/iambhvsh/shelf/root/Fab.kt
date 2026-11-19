package `in`.iambhvsh.shelf.root

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.R
import `in`.iambhvsh.shelf.collection.CollectionState
import `in`.iambhvsh.shelf.home.HomeState

@Composable
fun Fab(
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

        1 -> FloatingActionButton(onClick = onCollectionFabClick) {
            Icon(
                painterResource(R.drawable.add_icons),
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
