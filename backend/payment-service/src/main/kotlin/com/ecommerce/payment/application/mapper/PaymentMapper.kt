package com.ecommerce.payment.application.mapper

import com.ecommerce.payment.application.dto.CreatePaymentRequest
import com.ecommerce.payment.application.dto.PaymentDto
import com.ecommerce.payment.application.dto.PaymentResponse
import com.ecommerce.payment.domain.model.Payment
import com.ecommerce.payment.domain.model.PaymentStatus
import java.time.LocalDateTime
import org.springframework.stereotype.Component

@Component
class PaymentMapper {
    
    fun toDto(payment: Payment): PaymentDto {
        return PaymentDto(
            id = payment.id,
            orderId = payment.orderId,
            userId = payment.userId,
            amount = payment.amount,
            currency = payment.currency,
            status = payment.status,
            paymentMethodId = payment.paymentMethodId,
            createdAt = payment.createdAt,
            updatedAt = payment.updatedAt,
            completedAt = payment.completedAt
        )
    }
    
    fun toResponse(payment: Payment): PaymentResponse {
        return PaymentResponse(
            id = payment.id,
            orderId = payment.orderId,
            status = payment.status,
            amount = payment.amount,
            createdAt = payment.createdAt,
            completedAt = payment.completedAt
        )
    }
    
    fun toEntity(request: CreatePaymentRequest): Payment {
        return Payment(
            id = null,
            orderId = request.orderId,
            userId = request.userId,
            amount = request.amount,
            currency = request.currency,
            status = PaymentStatus.PENDING,
            paymentMethodId = request.paymentMethodId,
            createdAt = LocalDateTime.now(),
            updatedAt = null,
            completedAt = null
        )
    }
} 