package com.ecommerce.order.domain.model

import java.time.LocalDateTime
import java.util.UUID

/**
 * Modelo de domínio para histórico de status de pedido
 */
data class OrderStatusHistory(
    val id: UUID,
    val order: Order,
    val status: String,
    val observation: String?,
    val createdAt: LocalDateTime
) 