package com.ecommerce.order.application.dto

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class OrderItemDto(
    val id: UUID?,
    val productId: UUID,
    val productName: String,
    val productSlug: String,
    val productImage: String?,
    val variantId: UUID?,
    val variantName: String?,
    val price: BigDecimal,
    val quantity: Int,
    val discount: BigDecimal,
    val total: BigDecimal,
    val options: String?,
    val createdAt: LocalDateTime
) 