package com.ecommerce.payment.infrastructure.kafka.event

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Evento de mudança de status de pagamento
 */
data class PaymentEvent(
    val paymentId: Long,
    val orderId: String,
    val userId: String,
    val amount: BigDecimal,
    val currency: String,
    val status: String,
    val eventType: String,
    val timestamp: LocalDateTime
)