package com.iambhvsh.shelf.di

import androidx.room.Room
import com.iambhvsh.shelf.data.backup.BackupManager
import com.iambhvsh.shelf.data.local.BookmarkDatabase
import com.iambhvsh.shelf.data.local.repository.BookmarkRepositoryImpl
import com.iambhvsh.shelf.data.local.repository.SettingsRepositoryImpl
import com.iambhvsh.shelf.domain.repository.BookmarkRepository
import com.iambhvsh.shelf.domain.repository.SettingsRepository
import com.iambhvsh.shelf.presentation.collection.CollectionViewModel
import com.iambhvsh.shelf.presentation.home.HomeViewModel
import com.iambhvsh.shelf.presentation.search.SearchViewModel
import com.iambhvsh.shelf.presentation.setting.SettingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val shelfModule = module {

    // Database
    single {
        Room.databaseBuilder(
                get(),
                BookmarkDatabase::class.java,
                "bookmark_db"
            )            .fallbackToDestructiveMigration(true)
            .build()
    }

    single {
        get<BookmarkDatabase>().bookmarkDao()
    }

    single {
        get<BookmarkDatabase>().collectionDao()
    }

    // Repository
    single<SettingsRepository> {
        SettingsRepositoryImpl(get())
    }

    single<BookmarkRepository> {
        BookmarkRepositoryImpl(get(), get())
    }

    single {
        BackupManager(get(), get(), get(), get())
    }

    viewModel {
        HomeViewModel(get())
    }

    viewModel {
        SearchViewModel(get())
    }

    viewModel {
        CollectionViewModel(get())
    }

    viewModel {
        SettingViewModel(get(), get())
    }
}
