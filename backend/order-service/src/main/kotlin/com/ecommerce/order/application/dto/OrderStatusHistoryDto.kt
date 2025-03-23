package com.ecommerce.order.application.dto

import java.time.LocalDateTime
import java.util.UUID

/**
 * DTO para histórico de status de pedido
 */
data class OrderStatusHistoryDto(
    val id: UUID?,
    val fromStatus: String,
    val toStatus: String,
    val changedBy: String?,
    val comment: String?,
    val createdAt: LocalDateTime
) 