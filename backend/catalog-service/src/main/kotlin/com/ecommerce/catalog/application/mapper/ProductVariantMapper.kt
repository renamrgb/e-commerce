package com.ecommerce.catalog.application.mapper

import com.ecommerce.catalog.application.dto.ProductVariantCreateRequest
import com.ecommerce.catalog.application.dto.ProductVariantDto
import com.ecommerce.catalog.application.dto.ProductVariantUpdateRequest
import com.ecommerce.catalog.domain.entity.Product
import com.ecommerce.catalog.domain.entity.ProductAttribute
import com.ecommerce.catalog.domain.entity.ProductVariant
import org.springframework.stereotype.Component
import java.util.*

@Component
class ProductVariantMapper(
    private val productImageMapper: ProductImageMapper
) {

    fun toDto(entity: ProductVariant): ProductVariantDto {
        val attributeValues = entity.attributeValues
            ?.associate { it.attribute.name to it.value } ?: mapOf()

        return ProductVariantDto(
            id = entity.id,
            sku = entity.sku,
            name = entity.name,
            price = entity.price,
            salePrice = entity.salePrice,
            costPrice = entity.costPrice,
            stockQuantity = entity.stockQuantity,
            weight = entity.weight,
            height = entity.height,
            width = entity.width,
            length = entity.length,
            active = entity.active,
            attributeValues = attributeValues,
            images = entity.images?.map { productImageMapper.toDto(it) },
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toEntity(
        dto: ProductVariantCreateRequest, 
        product: Product,
        attributeMap: Map<UUID, ProductAttribute>
    ): ProductVariant {
        return ProductVariant(
            id = UUID.randomUUID(),
            product = product,
            sku = dto.sku,
            name = dto.name,
            price = dto.price,
            salePrice = dto.salePrice,
            costPrice = dto.costPrice,
            stockQuantity = dto.stockQuantity,
            weight = dto.weight,
            height = dto.height,
            width = dto.width,
            length = dto.length,
            active = dto.active
        )
    }

    fun updateEntity(
        entity: ProductVariant, 
        dto: ProductVariantUpdateRequest
    ): ProductVariant {
        dto.name?.let { entity.name = it }
        dto.price?.let { entity.price = it }
        entity.salePrice = dto.salePrice ?: entity.salePrice
        entity.costPrice = dto.costPrice ?: entity.costPrice
        dto.stockQuantity?.let { entity.stockQuantity = it }
        entity.weight = dto.weight ?: entity.weight
        entity.height = dto.height ?: entity.height
        entity.width = dto.width ?: entity.width
        entity.length = dto.length ?: entity.length
        dto.active?.let { entity.active = it }
        
        return entity
    }
} 