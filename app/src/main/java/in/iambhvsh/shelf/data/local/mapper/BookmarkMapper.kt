package in.iambhvsh.shelf.data.local.mapper

import in.iambhvsh.shelf.data.local.entity.BookmarkEntity
import in.iambhvsh.shelf.domain.model.Bookmark


fun BookmarkEntity.toDomain(): Bookmark {
    return Bookmark(
        id = id,
        url = url,
        title = title,
        description = description,
        imageUrl = imageUrl,
        createdAt = createdAt
    )
}

fun Bookmark.toEntity(): BookmarkEntity {
    return BookmarkEntity(
        id = id,
        url = url,
        title = title,
        description = description,
        imageUrl = imageUrl,
        createdAt = createdAt
    )
}