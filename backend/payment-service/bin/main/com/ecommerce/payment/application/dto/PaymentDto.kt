package com.ecommerce.payment.application.dto

import com.ecommerce.payment.domain.model.PaymentStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class PaymentDto(
    val id: Long?,
    val orderId: String,
    val userId: String,
    val amount: BigDecimal,
    val currency: String,
    val status: PaymentStatus,
    val paymentMethodId: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val completedAt: LocalDateTime?
)

data class CreatePaymentRequest(
    val orderId: String,
    val userId: String,
    val amount: BigDecimal,
    val currency: String = "BRL",
    val paymentMethodId: String?
)

data class PaymentResponse(
    val id: Long?,
    val orderId: String,
    val status: PaymentStatus,
    val amount: BigDecimal,
    val createdAt: LocalDateTime,
    val completedAt: LocalDateTime?
)

data class ProcessPaymentRequest(
    val paymentId: Long,
    val confirmationToken: String?
)

data class CancelPaymentRequest(
    val paymentId: Long,
    val reason: String?
)

data class RefundPaymentRequest(
    val paymentId: Long,
    val amount: BigDecimal?,
    val reason: String?
) 