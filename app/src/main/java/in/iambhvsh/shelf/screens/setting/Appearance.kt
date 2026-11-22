@file:Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR")

package `in`.iambhvsh.shelf.screens.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.iambhvsh.shelf.features.setting.SettingEvents
import `in`.iambhvsh.shelf.features.setting.SettingState
import `in`.iambhvsh.shelf.features.setting.SettingViewModel
import `in`.iambhvsh.shelf.theme.ShelfShapes

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsAppearanceScreen(
    state: SettingState,
    viewModel: SettingViewModel,
    modifier: Modifier = Modifier
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val topPadding = statusBarTop + 68.dp
    val bottomPadding = navBarBottom + 24.dp

    val hasAccent = !state.isDynamicColorSupported || !state.dynamicColor

    val containerColor = MaterialTheme.colorScheme.surfaceContainer

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = topPadding + 8.dp, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(ShelfShapes.Gap)
    ) {
        ThemeSelectorCard(
            selectedTheme = state.themeMode,
            onThemeChange = { viewModel.onEvent(SettingEvents.SelectTheme(it)) },
            shape = ShelfShapes.topItem
        )

        if (state.isDynamicColorSupported) {
            DynamicColorCard(
                state = state,
                viewModel = viewModel,
                hasAccent = hasAccent,
                containerColor = containerColor
            )
        }

        AccentColorCard(
            state = state,
            viewModel = viewModel,
            hasAccent = hasAccent,
            containerColor = containerColor
        )
    }
}
