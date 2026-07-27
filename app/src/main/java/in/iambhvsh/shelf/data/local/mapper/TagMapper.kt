package `in`.iambhvsh.shelf.data.local.mapper

import `in`.iambhvsh.shelf.data.local.entity.TagEntity
import `in`.iambhvsh.shelf.domain.model.Tag

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
