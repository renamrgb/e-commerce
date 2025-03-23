package com.ecommerce.catalog.application.mapper

import com.ecommerce.catalog.application.dto.*
import com.ecommerce.catalog.domain.entity.Category
import org.springframework.stereotype.Component
import java.util.*

@Component
class CategoryMapper {

    fun toDto(entity: Category): CategoryDto {
        return CategoryDto(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            slug = entity.slug,
            parentId = entity.parent?.id,
            imageUrl = entity.imageUrl,
            active = entity.active,
            children = entity.children?.map { toDto(it) },
            productCount = entity.productCount,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toEntity(dto: CategoryCreateRequest, parentEntity: Category? = null): Category {
        val slug = dto.slug ?: slugify(dto.name)
        return Category(
            id = UUID.randomUUID(),
            name = dto.name,
            description = dto.description,
            slug = slug,
            parent = parentEntity,
            imageUrl = dto.imageUrl,
            active = dto.active
        )
    }

    fun updateEntity(entity: Category, dto: CategoryUpdateRequest): Category {
        dto.name?.let { entity.name = it }
        entity.description = dto.description ?: entity.description
        dto.slug?.let { entity.slug = it }
        dto.imageUrl?.let { entity.imageUrl = it }
        dto.active?.let { entity.active = it }
        return entity
    }

    fun toResponse(entity: Category): CategoryResponse {
        return CategoryResponse(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            slug = entity.slug,
            parentId = entity.parent?.id,
            imageUrl = entity.imageUrl,
            active = entity.active,
            productCount = entity.productCount ?: 0,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toTreeResponse(entity: Category): CategoryTreeResponse {
        return CategoryTreeResponse(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            slug = entity.slug,
            parentId = entity.parent?.id,
            imageUrl = entity.imageUrl,
            active = entity.active,
            children = entity.children?.map { toTreeResponse(it) } ?: listOf(),
            productCount = entity.productCount ?: 0,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun slugify(input: String): String {
        return input.lowercase()
            .replace("[^a-z0-9\\s-]".toRegex(), "")
            .replace("\\s+".toRegex(), "-")
    }
} 