package com.iambhvsh.shelf.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.iambhvsh.shelf.data.local.dao.BookmarkDao
import com.iambhvsh.shelf.data.local.dao.CollectionDao
import com.iambhvsh.shelf.data.local.entity.BookmarkCollectionCrossRef
import com.iambhvsh.shelf.data.local.entity.BookmarkEntity
import com.iambhvsh.shelf.data.local.entity.CollectionEntity

@Database(
    entities = [BookmarkEntity::class, CollectionEntity::class, BookmarkCollectionCrossRef::class],
    version = 4
)
abstract class BookmarkDatabase : RoomDatabase() {

    abstract fun bookmarkDao(): BookmarkDao
    abstract fun collectionDao(): CollectionDao
}