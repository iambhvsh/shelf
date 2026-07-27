package `in`.iambhvsh.shelf.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import `in`.iambhvsh.shelf.data.local.dao.BookmarkDao
import `in`.iambhvsh.shelf.data.local.dao.CollectionDao
import `in`.iambhvsh.shelf.data.local.entity.BookmarkCollectionCrossRef
import `in`.iambhvsh.shelf.data.local.entity.BookmarkEntity
import `in`.iambhvsh.shelf.data.local.entity.CollectionEntity

@Database(
    entities = [BookmarkEntity::class, CollectionEntity::class, BookmarkCollectionCrossRef::class],
    version = 4,
    exportSchema = false
)
abstract class BookmarkDatabase : RoomDatabase() {

    abstract fun bookmarkDao(): BookmarkDao
    abstract fun collectionDao(): CollectionDao
}