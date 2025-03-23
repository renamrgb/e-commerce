package com.ecommerce.catalog.application.mapper

import com.ecommerce.catalog.application.dto.*
import com.ecommerce.catalog.domain.entity.Product
import com.ecommerce.catalog.domain.entity.ProductAttribute
import com.ecommerce.catalog.domain.entity.ProductAttributeValue
import org.springframework.stereotype.Component
import java.util.*

@Component
class ProductAttributeMapper {

    fun toDto(entity: ProductAttribute): ProductAttributeDto {
        return ProductAttributeDto(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            type = entity.type,
            displayOrder = entity.displayOrder,
            isRequired = entity.isRequired,
            isFilterable = entity.isFilterable,
            isComparable = entity.isComparable,
            isVisibleOnProductPage = entity.isVisibleOnProductPage,
            allowedValues = entity.allowedValues,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toEntity(dto: ProductAttributeCreateRequest): ProductAttribute {
        return ProductAttribute(
            id = UUID.randomUUID(),
            name = dto.name,
            description = dto.description,
            type = dto.type,
            displayOrder = dto.displayOrder,
            isRequired = dto.isRequired,
            isFilterable = dto.isFilterable,
            isComparable = dto.isComparable,
            isVisibleOnProductPage = dto.isVisibleOnProductPage,
            allowedValues = dto.allowedValues
        )
    }

    fun updateEntity(entity: ProductAttribute, dto: ProductAttributeUpdateRequest): ProductAttribute {
        dto.name?.let { entity.name = it }
        entity.description = dto.description ?: entity.description
        dto.type?.let { entity.type = it }
        dto.displayOrder?.let { entity.displayOrder = it }
        dto.isRequired?.let { entity.isRequired = it }
        dto.isFilterable?.let { entity.isFilterable = it }
        dto.isComparable?.let { entity.isComparable = it }
        dto.isVisibleOnProductPage?.let { entity.isVisibleOnProductPage = it }
        dto.allowedValues?.let { entity.allowedValues = it }
        return entity
    }

    fun toResponse(entity: ProductAttribute): ProductAttributeResponse {
        return ProductAttributeResponse(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            type = entity.type,
            displayOrder = entity.displayOrder,
            isRequired = entity.isRequired,
            isFilterable = entity.isFilterable,
            isComparable = entity.isComparable,
            isVisibleOnProductPage = entity.isVisibleOnProductPage,
            allowedValues = entity.allowedValues,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toAttributeValueDto(entity: ProductAttributeValue): ProductAttributeValueDto {
        return ProductAttributeValueDto(
            id = entity.id,
            productId = entity.product.id,
            attributeId = entity.attribute.id,
            attributeName = entity.attribute.name,
            value = entity.value,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toAttributeValueEntity(
        product: Product,
        attribute: ProductAttribute,
        value: String
    ): ProductAttributeValue {
        return ProductAttributeValue(
            id = UUID.randomUUID(),
            product = product,
            attribute = attribute,
            value = value
        )
    }

    fun updateAttributeValueEntity(
        entity: ProductAttributeValue,
        value: String
    ): ProductAttributeValue {
        entity.value = value
        return entity
    }
} 