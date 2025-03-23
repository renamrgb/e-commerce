package com.ecommerce.catalog.application.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductVariantDto(
    val id: UUID? = null,
    val sku: String,
    val name: String? = null,
    val price: BigDecimal? = null,
    val salePrice: BigDecimal? = null,
    val costPrice: BigDecimal? = null,
    val stockQuantity: Int = 0,
    val weight: BigDecimal? = null,
    val height: BigDecimal? = null,
    val width: BigDecimal? = null,
    val length: BigDecimal? = null,
    val active: Boolean = true,
    val attributeValues: Map<String, String>,
    val images: List<ProductImageDto>? = null,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null
)

data class ProductVariantCreateRequest(
    @field:NotBlank(message = "O SKU é obrigatório")
    @field:Pattern(regexp = "^[A-Za-z0-9-_]+$", message = "O SKU deve conter apenas letras, números, hífens e underscores")
    @field:Size(min = 3, max = 50, message = "O SKU deve ter entre 3 e 50 caracteres")
    val sku: String,

    @field:Size(min = 2, max = 255, message = "O nome deve ter entre 2 e 255 caracteres")
    val name: String? = null,

    @field:DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
    val price: BigDecimal? = null,

    @field:DecimalMin(value = "0.01", message = "O preço de promoção deve ser maior que zero")
    val salePrice: BigDecimal? = null,

    @field:DecimalMin(value = "0.01", message = "O preço de custo deve ser maior que zero")
    val costPrice: BigDecimal? = null,

    @field:Min(value = 0, message = "A quantidade em estoque não pode ser negativa")
    val stockQuantity: Int = 0,

    @field:DecimalMin(value = "0.01", message = "O peso deve ser maior que zero")
    val weight: BigDecimal? = null,

    @field:DecimalMin(value = "0.01", message = "A altura deve ser maior que zero")
    val height: BigDecimal? = null,

    @field:DecimalMin(value = "0.01", message = "A largura deve ser maior que zero")
    val width: BigDecimal? = null,

    @field:DecimalMin(value = "0.01", message = "O comprimento deve ser maior que zero")
    val length: BigDecimal? = null,

    val active: Boolean = true,

    @field:NotEmpty(message = "Uma variante deve ter pelo menos um valor de atributo")
    val attributeValues: Map<UUID, String>,

    @field:Valid
    val images: List<ProductImageCreateRequest>? = null
)

data class ProductVariantUpdateRequest(
    @field:Size(min = 2, max = 255, message = "O nome deve ter entre 2 e 255 caracteres")
    val name: String? = null,

    @field:DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
    val price: BigDecimal? = null,

    @field:DecimalMin(value = "0.01", message = "O preço de promoção deve ser maior que zero")
    val salePrice: BigDecimal? = null,

    @field:DecimalMin(value = "0.01", message = "O preço de custo deve ser maior que zero")
    val costPrice: BigDecimal? = null,

    @field:Min(value = 0, message = "A quantidade em estoque não pode ser negativa")
    val stockQuantity: Int? = null,

    @field:DecimalMin(value = "0.01", message = "O peso deve ser maior que zero")
    val weight: BigDecimal? = null,

    @field:DecimalMin(value = "0.01", message = "A altura deve ser maior que zero")
    val height: BigDecimal? = null,

    @field:DecimalMin(value = "0.01", message = "A largura deve ser maior que zero")
    val width: BigDecimal? = null,

    @field:DecimalMin(value = "0.01", message = "O comprimento deve ser maior que zero")
    val length: BigDecimal? = null,

    val active: Boolean? = null,

    val attributeValues: Map<UUID, String>? = null
) 