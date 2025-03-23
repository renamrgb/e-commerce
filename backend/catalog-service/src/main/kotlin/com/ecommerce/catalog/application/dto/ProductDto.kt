package com.ecommerce.catalog.application.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductDto(
    val id: UUID? = null,
    val sku: String,
    val name: String,
    val description: String? = null,
    val shortDescription: String? = null,
    val price: BigDecimal,
    val salePrice: BigDecimal? = null,
    val costPrice: BigDecimal? = null,
    val brandId: UUID? = null,
    val brandName: String? = null,
    val weight: BigDecimal? = null,
    val height: BigDecimal? = null,
    val width: BigDecimal? = null,
    val length: BigDecimal? = null,
    val active: Boolean = true,
    val featured: Boolean = false,
    val quantitySold: Int? = null,
    val avgRating: BigDecimal? = null,
    val categories: List<CategoryDto>? = null,
    val images: List<ProductImageDto>? = null,
    val variants: List<ProductVariantDto>? = null,
    val attributes: Map<String, String>? = null,
    val discountPercentage: Int? = null,
    val currentPrice: BigDecimal? = null,
    val hasDiscount: Boolean? = null,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null
)

data class ProductCreateRequest(
    @field:NotBlank(message = "O SKU é obrigatório")
    @field:Pattern(regexp = "^[A-Za-z0-9-_]+$", message = "O SKU deve conter apenas letras, números, hífens e underscores")
    @field:Size(min = 3, max = 50, message = "O SKU deve ter entre 3 e 50 caracteres")
    val sku: String,

    @field:NotBlank(message = "O nome é obrigatório")
    @field:Size(min = 3, max = 255, message = "O nome deve ter entre 3 e 255 caracteres")
    val name: String,

    @field:Size(max = 10000, message = "A descrição deve ter no máximo 10000 caracteres")
    val description: String? = null,

    @field:Size(max = 500, message = "A descrição curta deve ter no máximo 500 caracteres")
    val shortDescription: String? = null,

    @field:NotNull(message = "O preço é obrigatório")
    @field:DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
    val price: BigDecimal,

    @field:DecimalMin(value = "0.01", message = "O preço de promoção deve ser maior que zero")
    val salePrice: BigDecimal? = null,

    @field:DecimalMin(value = "0.01", message = "O preço de custo deve ser maior que zero")
    val costPrice: BigDecimal? = null,

    val brandId: UUID? = null,

    @field:DecimalMin(value = "0.01", message = "O peso deve ser maior que zero")
    val weight: BigDecimal? = null,

    @field:DecimalMin(value = "0.01", message = "A altura deve ser maior que zero")
    val height: BigDecimal? = null,

    @field:DecimalMin(value = "0.01", message = "A largura deve ser maior que zero")
    val width: BigDecimal? = null,

    @field:DecimalMin(value = "0.01", message = "O comprimento deve ser maior que zero")
    val length: BigDecimal? = null,

    val active: Boolean = true,
    val featured: Boolean = false,

    val categoryIds: List<UUID>? = null,

    @field:Valid
    val images: List<ProductImageCreateRequest>? = null,

    val attributes: Map<UUID, String>? = null
)

data class ProductUpdateRequest(
    @field:Size(min = 3, max = 255, message = "O nome deve ter entre 3 e 255 caracteres")
    val name: String? = null,

    @field:Size(max = 10000, message = "A descrição deve ter no máximo 10000 caracteres")
    val description: String? = null,

    @field:Size(max = 500, message = "A descrição curta deve ter no máximo 500 caracteres")
    val shortDescription: String? = null,

    @field:DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
    val price: BigDecimal? = null,

    @field:DecimalMin(value = "0.01", message = "O preço de promoção deve ser maior que zero")
    val salePrice: BigDecimal? = null,

    @field:DecimalMin(value = "0.01", message = "O preço de custo deve ser maior que zero")
    val costPrice: BigDecimal? = null,

    val brandId: UUID? = null,

    @field:DecimalMin(value = "0.01", message = "O peso deve ser maior que zero")
    val weight: BigDecimal? = null,

    @field:DecimalMin(value = "0.01", message = "A altura deve ser maior que zero")
    val height: BigDecimal? = null,

    @field:DecimalMin(value = "0.01", message = "A largura deve ser maior que zero")
    val width: BigDecimal? = null,

    @field:DecimalMin(value = "0.01", message = "O comprimento deve ser maior que zero")
    val length: BigDecimal? = null,

    val active: Boolean? = null,
    val featured: Boolean? = null,
    val categoryIds: List<UUID>? = null,
    val attributesToAdd: Map<UUID, String>? = null,
    val attributesToRemove: List<UUID>? = null
)

data class ProductResponse(
    val id: UUID,
    val sku: String,
    val name: String,
    val description: String? = null,
    val shortDescription: String? = null,
    val price: BigDecimal,
    val salePrice: BigDecimal? = null,
    val currentPrice: BigDecimal,
    val discountPercentage: Int? = null,
    val brandId: UUID? = null,
    val brandName: String? = null,
    val weight: BigDecimal? = null,
    val dimensions: DimensionsDto? = null,
    val active: Boolean,
    val featured: Boolean,
    val quantitySold: Int,
    val avgRating: BigDecimal,
    val hasDiscount: Boolean,
    val categories: List<CategoryDto>,
    val images: List<ProductImageDto>,
    val attributes: Map<String, String>,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

data class ProductDetailResponse(
    val id: UUID,
    val sku: String,
    val name: String,
    val description: String? = null,
    val shortDescription: String? = null,
    val price: BigDecimal,
    val salePrice: BigDecimal? = null,
    val currentPrice: BigDecimal,
    val discountPercentage: Int? = null,
    val brandId: UUID? = null,
    val brandName: String? = null,
    val weight: BigDecimal? = null,
    val dimensions: DimensionsDto? = null,
    val active: Boolean,
    val featured: Boolean,
    val quantitySold: Int,
    val avgRating: BigDecimal,
    val hasDiscount: Boolean,
    val categories: List<CategoryDto>,
    val images: List<ProductImageDto>,
    val attributes: Map<String, String>,
    val variants: List<ProductVariantDto>,
    val relatedProducts: List<ProductDto>? = null,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

data class ProductSearchRequest(
    val keyword: String? = null,
    val categoryId: UUID? = null,
    val categorySlug: String? = null,
    val brandId: UUID? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val onSale: Boolean? = null,
    val inStock: Boolean? = null,
    val featured: Boolean? = null,
    val attributes: Map<UUID, String>? = null,
    val sortBy: String? = null,
    val sortDirection: String? = null,
    val page: Int = 0,
    val size: Int = 20
)

data class ProductPage(
    val content: List<ProductResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean
)

data class DimensionsDto(
    val height: BigDecimal? = null,
    val width: BigDecimal? = null,
    val length: BigDecimal? = null
) 