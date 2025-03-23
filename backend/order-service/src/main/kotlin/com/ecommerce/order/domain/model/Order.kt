package com.ecommerce.order.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Modelo de domínio para pedidos
 */
data class Order(
    val id: UUID,
    val userId: UUID,
    val orderNumber: String,
    var status: String,
    val subtotal: BigDecimal,
    val shippingCost: BigDecimal,
    val discount: BigDecimal,
    val tax: BigDecimal,
    val total: BigDecimal,
    val shippingAddress: ShippingAddress,
    val paymentInfo: PaymentInfo,
    val items: MutableList<OrderItem>,
    val statusHistory: MutableList<OrderStatusHistory>,
    val createdAt: LocalDateTime,
    var updatedAt: LocalDateTime
) 