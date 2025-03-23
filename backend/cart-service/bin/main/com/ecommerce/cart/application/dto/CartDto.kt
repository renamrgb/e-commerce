package com.ecommerce.cart.application.dto

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class CartDto(
    val id: UUID?,
    val userId: UUID?,
    val items: List<CartItemDto>,
    val couponCode: String?,
    val discount: BigDecimal,
    val subtotal: BigDecimal,
    val total: BigDecimal,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CartItemDto(
    val id: UUID,
    val productId: UUID,
    val productName: String,
    val productSlug: String,
    val productImage: String?,
    val variantId: UUID?,
    val variantName: String?,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val attributes: String?,
    val total: BigDecimal
)

data class AddItemRequest(
    val productId: UUID,
    val variantId: UUID? = null,
    val quantity: Int,
    val attributes: Map<String, String>? = null
)

data class UpdateItemRequest(
    val quantity: Int
)

data class ApplyCouponRequest(
    val code: String
) 