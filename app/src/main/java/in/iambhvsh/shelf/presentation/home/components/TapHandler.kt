package `in`.iambhvsh.shelf.presentation.home.components

import android.content.Context
import androidx.compose.ui.platform.Clipboard
import `in`.iambhvsh.shelf.domain.model.Bookmark
import `in`.iambhvsh.shelf.openChromeTab
import `in`.iambhvsh.shelf.presentation.home.HomeEvents
import `in`.iambhvsh.shelf.presentation.home.HomeViewModel
import `in`.iambhvsh.shelf.presentation.setting.TapAction

fun handleTap(
    item: Bookmark,
    tapAction: TapAction,
    context: Context,
    clipboard: Clipboard,
    viewModel: HomeViewModel
) {
    when (tapAction) {
        TapAction.OPEN_BROWSER -> openChromeTab(item.url, context)
        TapAction.COPY_LINK -> item.url.let { clipboard.nativeClipboard.text = it }
        TapAction.SHOW_PREVIEW -> viewModel.homeEvents(HomeEvents.BookmarkPreviewClick(item))
    }
}
