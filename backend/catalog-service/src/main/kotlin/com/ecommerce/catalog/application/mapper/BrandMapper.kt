package com.ecommerce.catalog.application.mapper

import com.ecommerce.catalog.application.dto.*
import com.ecommerce.catalog.domain.entity.Brand
import org.springframework.stereotype.Component
import java.util.*

@Component
class BrandMapper {

    fun toDto(entity: Brand): BrandDto {
        return BrandDto(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            slug = entity.slug,
            logoUrl = entity.logoUrl,
            websiteUrl = entity.websiteUrl,
            active = entity.active,
            productCount = entity.productCount,
            featured = entity.featured,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toEntity(dto: BrandCreateRequest): Brand {
        val slug = dto.slug ?: slugify(dto.name)
        return Brand(
            id = UUID.randomUUID(),
            name = dto.name,
            description = dto.description,
            slug = slug,
            logoUrl = dto.logoUrl,
            websiteUrl = dto.websiteUrl,
            active = dto.active,
            featured = dto.featured
        )
    }

    fun updateEntity(entity: Brand, dto: BrandUpdateRequest): Brand {
        dto.name?.let { entity.name = it }
        entity.description = dto.description ?: entity.description
        dto.slug?.let { entity.slug = it }
        dto.logoUrl?.let { entity.logoUrl = it }
        dto.websiteUrl?.let { entity.websiteUrl = it }
        dto.active?.let { entity.active = it }
        dto.featured?.let { entity.featured = it }
        return entity
    }

    fun toResponse(entity: Brand): BrandResponse {
        return BrandResponse(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            slug = entity.slug,
            logoUrl = entity.logoUrl,
            websiteUrl = entity.websiteUrl,
            active = entity.active,
            productCount = entity.productCount ?: 0,
            featured = entity.featured,
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