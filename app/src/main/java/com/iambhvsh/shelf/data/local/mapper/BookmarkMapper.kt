package com.iambhvsh.shelf.data.local.mapper

import com.iambhvsh.shelf.data.local.entity.BookmarkEntity
import com.iambhvsh.shelf.domain.model.Bookmark


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