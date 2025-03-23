package com.ecommerce.order.application.dto

import com.ecommerce.order.domain.entity.Order
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class OrderDto(
    val id: UUID?,
    val userId: UUID,
    val orderNumber: String,
    val status: String,
    val subtotal: BigDecimal,
    val shippingCost: BigDecimal,
    val discount: BigDecimal,
    val tax: BigDecimal,
    val total: BigDecimal,
    val couponCode: String?,
    val notes: String?,
    val trackingCode: String?,
    val shippingAddress: String,
    val billingAddress: String,
    val paymentMethod: String,
    val shippingMethod: String,
    val paymentId: String?,
    val paidAt: LocalDateTime?,
    val shippedAt: LocalDateTime?,
    val deliveredAt: LocalDateTime?,
    val canceledAt: LocalDateTime?,
    val refundedAt: LocalDateTime?,
    val items: List<OrderItemDto>,
    val statusHistory: List<OrderStatusHistoryDto>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class OrderSummaryDto(
    val id: UUID?,
    val orderNumber: String,
    val status: String,
    val total: BigDecimal,
    val itemCount: Int,
    val createdAt: LocalDateTime
)

data class OrderStatisticsDto(
    val totalOrders: Long,
    val totalSpent: BigDecimal,
    val averageOrderValue: BigDecimal,
    val mostOrderedProducts: List<ProductOrderCountDto>
)

data class ProductOrderCountDto(
    val productId: UUID,
    val productName: String,
    val count: Long
) 