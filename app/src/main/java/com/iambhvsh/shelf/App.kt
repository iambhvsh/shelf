package com.iambhvsh.shelf

import android.app.Application
import com.iambhvsh.shelf.data.backup.BackupManager
import com.iambhvsh.shelf.di.shelfModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.GlobalContext

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(shelfModule)
        }
        GlobalContext.getOrNull()?.get<BackupManager>()
    }
}