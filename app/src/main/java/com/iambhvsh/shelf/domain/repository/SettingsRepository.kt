package com.iambhvsh.shelf.domain.repository

import com.iambhvsh.shelf.presentation.setting.TapAction
import com.iambhvsh.shelf.presentation.setting.ViewMode
import com.iambhvsh.shelf.ui.theme.AccentColor
import com.iambhvsh.shelf.ui.theme.ThemeMode

interface SettingsRepository {
    fun getThemeMode(): ThemeMode
    fun setThemeMode(mode: ThemeMode)
    fun getTapAction(): TapAction
    fun setTapAction(action: TapAction)
    fun getDynamicColor(): Boolean
    fun setDynamicColor(enabled: Boolean)
    fun getAccentColor(): AccentColor
    fun setAccentColor(color: AccentColor)
    fun getViewMode(): ViewMode
    fun setViewMode(mode: ViewMode)
    fun getAutoBackupEnabled(): Boolean
    fun setAutoBackupEnabled(enabled: Boolean)
}
