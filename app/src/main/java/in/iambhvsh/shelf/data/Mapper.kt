package `in`.iambhvsh.shelf.data

import `in`.iambhvsh.shelf.data.dao.CollectionWithCount

fun BookmarkEntity.toDomain(): Bookmark {
    return Bookmark(
        id = id,
        url = url,
        title = title,
        description = description,
        imageUrl = imageUrl,
        createdAt = createdAt,
        isPinned = isPinned,
        note = note,
        reminderTime = reminderTime
    )
}

fun Bookmark.toEntity(): BookmarkEntity {
    return BookmarkEntity(
        id = id,
        url = url,
        title = title,
        description = description,
        imageUrl = imageUrl,
        createdAt = createdAt,
        isPinned = isPinned,
        note = note,
        reminderTime = reminderTime
    )
}

fun CollectionEntity.toDomain(): Collection {
    return Collection(
        id = id,
        name = name
    )
}

fun CollectionWithCount.toDomain(): Collection {
    return Collection(
        id = id,
        name = name,
        bookmarkCount = bookmarkCount,
        previewUrls = previewUrls?.split("|||")?.filter { it.isNotBlank() } ?: emptyList(),
        isVirtual = id == Collection.UNCATEGORISED_ID
    )
}

fun Collection.toEntity(): CollectionEntity {
    return CollectionEntity(
        id = id,
        name = name
    )
}

fun CollectionWithBookmarks.toDomain(): Collection {
    return Collection(
        id = collection.id,
        name = collection.name,
        bookmarkCount = bookmarks.count { !it.isHidden }
    )
}

fun TagEntity.toDomain(): Tag {
    return Tag(
        id = id,
        name = name
    )
}

fun Tag.toEntity(): TagEntity {
    return TagEntity(
        id = id,
        name = name
    )
}
