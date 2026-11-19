package `in`.iambhvsh.shelf.presentation.setting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import `in`.iambhvsh.shelf.navigation.AppRoute
import `in`.iambhvsh.shelf.presentation.setting.screens.SettingsMainScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingScreen(
    viewModel: SettingViewModel = koinViewModel(),
    onNavigate: (AppRoute) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()
    }

    SettingsMainScreen(
        state = state,
        versionName = versionName,
        onNavigate = onNavigate,
        modifier = modifier
    )
}
