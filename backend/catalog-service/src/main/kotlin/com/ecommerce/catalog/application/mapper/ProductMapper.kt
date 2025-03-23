package com.ecommerce.catalog.application.mapper

import com.ecommerce.catalog.application.dto.*
import com.ecommerce.catalog.domain.entity.*
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

@Component
class ProductMapper(
    private val categoryMapper: CategoryMapper,
    private val productImageMapper: ProductImageMapper,
    private val productVariantMapper: ProductVariantMapper,
    private val brandMapper: BrandMapper
) {

    fun toDto(entity: Product): ProductDto {
        val currentPrice = if (entity.salePrice != null && entity.salePrice > BigDecimal.ZERO) entity.salePrice else entity.price
        val hasDiscount = entity.salePrice != null && entity.salePrice < entity.price && entity.salePrice > BigDecimal.ZERO
        val discountPercentage = if (hasDiscount) {
            calculateDiscountPercentage(entity.price, entity.salePrice!!)
        } else null

        return ProductDto(
            id = entity.id,
            sku = entity.sku,
            name = entity.name,
            description = entity.description,
            shortDescription = entity.shortDescription,
            price = entity.price,
            salePrice = entity.salePrice,
            costPrice = entity.costPrice,
            brandId = entity.brand?.id,
            brandName = entity.brand?.name,
            weight = entity.weight,
            height = entity.height,
            width = entity.width,
            length = entity.length,
            active = entity.active,
            featured = entity.featured,
            quantitySold = entity.quantitySold,
            avgRating = entity.avgRating,
            categories = entity.categories?.map { categoryMapper.toDto(it) },
            images = entity.images?.map { productImageMapper.toDto(it) },
            variants = entity.variants?.map { productVariantMapper.toDto(it) },
            attributes = entity.attributeValues?.associate { it.attribute.name to it.value },
            currentPrice = currentPrice,
            hasDiscount = hasDiscount,
            discountPercentage = discountPercentage,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toEntity(dto: ProductCreateRequest, brand: Brand?, categories: List<Category>): Product {
        return Product(
            id = UUID.randomUUID(),
            sku = dto.sku,
            name = dto.name,
            description = dto.description,
            shortDescription = dto.shortDescription,
            price = dto.price,
            salePrice = dto.salePrice,
            costPrice = dto.costPrice,
            brand = brand,
            weight = dto.weight,
            height = dto.height,
            width = dto.width,
            length = dto.length,
            active = dto.active,
            featured = dto.featured,
            categories = categories.toMutableList()
        )
    }

    fun updateEntity(entity: Product, dto: ProductUpdateRequest, brand: Brand?, categoriesToAdd: List<Category>): Product {
        dto.name?.let { entity.name = it }
        entity.description = dto.description ?: entity.description
        entity.shortDescription = dto.shortDescription ?: entity.shortDescription
        dto.price?.let { entity.price = it }
        entity.salePrice = dto.salePrice ?: entity.salePrice
        entity.costPrice = dto.costPrice ?: entity.costPrice
        entity.brand = brand ?: entity.brand
        entity.weight = dto.weight ?: entity.weight
        entity.height = dto.height ?: entity.height
        entity.width = dto.width ?: entity.width
        entity.length = dto.length ?: entity.length
        dto.active?.let { entity.active = it }
        dto.featured?.let { entity.featured = it }
        
        // Add new categories while keeping existing ones that are not in the list
        if (categoriesToAdd.isNotEmpty()) {
            entity.categories = (entity.categories ?: mutableListOf()).apply {
                addAll(categoriesToAdd)
                // Remove duplicates
                distinctBy { it.id }.toMutableList()
            }
        }
        
        return entity
    }

    fun toResponse(entity: Product): ProductResponse {
        val currentPrice = if (entity.salePrice != null && entity.salePrice > BigDecimal.ZERO) entity.salePrice else entity.price
        val hasDiscount = entity.salePrice != null && entity.salePrice < entity.price && entity.salePrice > BigDecimal.ZERO
        val discountPercentage = if (hasDiscount) {
            calculateDiscountPercentage(entity.price, entity.salePrice!!)
        } else null

        val dimensions = if (entity.height != null || entity.width != null || entity.length != null) {
            DimensionsDto(
                height = entity.height,
                width = entity.width,
                length = entity.length
            )
        } else null

        return ProductResponse(
            id = entity.id,
            sku = entity.sku,
            name = entity.name,
            description = entity.description,
            shortDescription = entity.shortDescription,
            price = entity.price,
            salePrice = entity.salePrice,
            currentPrice = currentPrice,
            discountPercentage = discountPercentage,
            brandId = entity.brand?.id,
            brandName = entity.brand?.name,
            weight = entity.weight,
            dimensions = dimensions,
            active = entity.active,
            featured = entity.featured,
            quantitySold = entity.quantitySold ?: 0,
            avgRating = entity.avgRating ?: BigDecimal.ZERO,
            hasDiscount = hasDiscount,
            categories = entity.categories?.map { categoryMapper.toDto(it) } ?: listOf(),
            images = entity.images?.map { productImageMapper.toDto(it) } ?: listOf(),
            attributes = entity.attributeValues?.associate { it.attribute.name to it.value } ?: mapOf(),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toDetailResponse(entity: Product, relatedProducts: List<Product>? = null): ProductDetailResponse {
        val currentPrice = if (entity.salePrice != null && entity.salePrice > BigDecimal.ZERO) entity.salePrice else entity.price
        val hasDiscount = entity.salePrice != null && entity.salePrice < entity.price && entity.salePrice > BigDecimal.ZERO
        val discountPercentage = if (hasDiscount) {
            calculateDiscountPercentage(entity.price, entity.salePrice!!)
        } else null

        val dimensions = if (entity.height != null || entity.width != null || entity.length != null) {
            DimensionsDto(
                height = entity.height,
                width = entity.width,
                length = entity.length
            )
        } else null

        return ProductDetailResponse(
            id = entity.id,
            sku = entity.sku,
            name = entity.name,
            description = entity.description,
            shortDescription = entity.shortDescription,
            price = entity.price,
            salePrice = entity.salePrice,
            currentPrice = currentPrice,
            discountPercentage = discountPercentage,
            brandId = entity.brand?.id,
            brandName = entity.brand?.name,
            weight = entity.weight,
            dimensions = dimensions,
            active = entity.active,
            featured = entity.featured,
            quantitySold = entity.quantitySold ?: 0,
            avgRating = entity.avgRating ?: BigDecimal.ZERO,
            hasDiscount = hasDiscount,
            categories = entity.categories?.map { categoryMapper.toDto(it) } ?: listOf(),
            images = entity.images?.map { productImageMapper.toDto(it) } ?: listOf(),
            attributes = entity.attributeValues?.associate { it.attribute.name to it.value } ?: mapOf(),
            variants = entity.variants?.map { productVariantMapper.toDto(it) } ?: listOf(),
            relatedProducts = relatedProducts?.map { toDto(it) },
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toPage(page: Page<Product>): ProductPage {
        return ProductPage(
            content = page.content.map { toResponse(it) },
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            last = page.isLast
        )
    }

    private fun calculateDiscountPercentage(originalPrice: BigDecimal, salePrice: BigDecimal): Int {
        if (originalPrice <= BigDecimal.ZERO) return 0
        val percentage = (originalPrice.subtract(salePrice))
            .divide(originalPrice, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal(100))
            .setScale(0, RoundingMode.DOWN)
        return percentage.toInt()
    }
} 