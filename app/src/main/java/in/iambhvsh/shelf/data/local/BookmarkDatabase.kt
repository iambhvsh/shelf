package `in`.iambhvsh.shelf.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import `in`.iambhvsh.shelf.data.local.dao.BookmarkDao
import `in`.iambhvsh.shelf.data.local.dao.CollectionDao
import `in`.iambhvsh.shelf.data.local.dao.TagDao
import `in`.iambhvsh.shelf.data.local.entity.BookmarkCollectionCrossRef
import `in`.iambhvsh.shelf.data.local.entity.BookmarkEntity
import `in`.iambhvsh.shelf.data.local.entity.CollectionEntity
import `in`.iambhvsh.shelf.data.local.entity.TagEntity
import `in`.iambhvsh.shelf.data.local.entity.BookmarkTagCrossRef

@Database(
    entities = [
        BookmarkEntity::class, 
        CollectionEntity::class, 
        BookmarkCollectionCrossRef::class,
        TagEntity::class,
        BookmarkTagCrossRef::class
    ],
    version = 5,
    exportSchema = false
)
abstract class BookmarkDatabase : RoomDatabase() {

    abstract fun bookmarkDao(): BookmarkDao
    abstract fun collectionDao(): CollectionDao
    abstract fun tagDao(): TagDao
    
    companion object {
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bookmarks ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE TABLE IF NOT EXISTS `tags` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `bookmark_tag_cross_ref` (`bookmarkId` INTEGER NOT NULL, `tagId` INTEGER NOT NULL, PRIMARY KEY(`bookmarkId`, `tagId`), FOREIGN KEY(`bookmarkId`) REFERENCES `bookmarks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`tagId`) REFERENCES `tags`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmark_tag_cross_ref_bookmarkId` ON `bookmark_tag_cross_ref` (`bookmarkId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmark_tag_cross_ref_tagId` ON `bookmark_tag_cross_ref` (`tagId`)")
            }
        }
    }
}