package `in`.iambhvsh.shelf

import android.app.Application
import `in`.iambhvsh.shelf.data.backup.BackupManager
import `in`.iambhvsh.shelf.di.shelfModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.GlobalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import `in`.iambhvsh.shelf.domain.repository.SettingsRepository
import org.koin.android.ext.android.inject

class App : Application(), DefaultLifecycleObserver {

    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate() {
        super<Application>.onCreate()
        startKoin {
            androidContext(this@App)
            modules(shelfModule)
        }
        GlobalContext.getOrNull()?.get<BackupManager>()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        val appLockEnabled = settingsRepository.getAppLockEnabled()
        val timeout = settingsRepository.getAppLockTimeout()
        SessionManager.onAppForegrounded(appLockEnabled, timeout)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        SessionManager.onAppBackgrounded()
    }
}