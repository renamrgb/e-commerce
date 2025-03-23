package com.ecommerce.cart.application.dto

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class CartItemDto(
    val id: UUID?,
    val productId: UUID,
    val productName: String,
    val productSlug: String,
    val productImage: String?,
    val variantId: UUID?,
    val variantName: String?,
    val price: BigDecimal,
    val quantity: Int,
    val total: BigDecimal,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) 