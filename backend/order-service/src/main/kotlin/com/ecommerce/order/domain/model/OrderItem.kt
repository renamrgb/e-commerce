package com.ecommerce.order.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Modelo de domínio para itens de pedido
 */
data class OrderItem(
    val id: UUID,
    val order: Order,
    val productId: UUID,
    val productName: String,
    val productSlug: String,
    val productImage: String?,
    val variantId: UUID?,
    val variantName: String?,
    val price: BigDecimal,
    val quantity: Int,
    val total: BigDecimal,
    val createdAt: LocalDateTime
) 