package `in`.iambhvsh.shelf.components.home

import android.content.Context
import androidx.compose.ui.platform.Clipboard
import `in`.iambhvsh.shelf.components.common.copyToClipboard
import `in`.iambhvsh.shelf.data.Bookmark
import `in`.iambhvsh.shelf.openChromeTab
import `in`.iambhvsh.shelf.features.home.HomeEvents
import `in`.iambhvsh.shelf.features.home.HomeViewModel
import `in`.iambhvsh.shelf.features.setting.TapAction

fun handleTap(
    item: Bookmark,
    tapAction: TapAction,
    context: Context,
    clipboard: Clipboard,
    viewModel: HomeViewModel
) {
    when (tapAction) {
        TapAction.OPEN_BROWSER -> openChromeTab(item.url, context)
        TapAction.COPY_LINK -> context.copyToClipboard(item.url)
        TapAction.SHOW_PREVIEW -> viewModel.homeEvents(HomeEvents.BookmarkPreviewClick(item))
    }
}
