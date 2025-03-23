package com.ecommerce.catalog.application.mapper

import com.ecommerce.catalog.application.dto.ProductImageCreateRequest
import com.ecommerce.catalog.application.dto.ProductImageDto
import com.ecommerce.catalog.application.dto.ProductImageUpdateRequest
import com.ecommerce.catalog.domain.entity.Product
import com.ecommerce.catalog.domain.entity.ProductImage
import org.springframework.stereotype.Component
import java.util.*

@Component
class ProductImageMapper {

    fun toDto(entity: ProductImage): ProductImageDto {
        return ProductImageDto(
            id = entity.id,
            url = entity.url,
            altText = entity.altText,
            position = entity.position,
            isPrimary = entity.isPrimary,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toEntity(dto: ProductImageCreateRequest, product: Product): ProductImage {
        return ProductImage(
            id = UUID.randomUUID(),
            product = product,
            url = dto.url,
            altText = dto.altText,
            position = dto.position,
            isPrimary = dto.isPrimary
        )
    }

    fun updateEntity(entity: ProductImage, dto: ProductImageUpdateRequest): ProductImage {
        dto.url?.let { entity.url = it }
        entity.altText = dto.altText ?: entity.altText
        dto.position?.let { entity.position = it }
        dto.isPrimary?.let { entity.isPrimary = it }
        return entity
    }
} 