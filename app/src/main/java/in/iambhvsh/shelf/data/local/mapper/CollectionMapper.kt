package `in`.iambhvsh.shelf.data.local.mapper

import `in`.iambhvsh.shelf.data.local.dao.CollectionWithCount
import `in`.iambhvsh.shelf.data.local.entity.CollectionEntity
import `in`.iambhvsh.shelf.data.local.entity.CollectionWithBookmarks
import `in`.iambhvsh.shelf.domain.model.Collection

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
        previewUrls = previewUrls?.split("|||")?.filter { it.isNotBlank() } ?: emptyList()
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
        bookmarkCount = bookmarks.size
    )
}
