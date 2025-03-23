package com.ecommerce.payment.application.mapper

import com.ecommerce.payment.application.dto.CreatePaymentMethodRequest
import com.ecommerce.payment.application.dto.PaymentMethodDto
import com.ecommerce.payment.application.dto.PaymentMethodResponse
import com.ecommerce.payment.domain.model.PaymentMethod
import java.time.LocalDateTime
import org.springframework.stereotype.Component

@Component
class PaymentMethodMapper {
    
    fun toDto(paymentMethod: PaymentMethod): PaymentMethodDto {
        return PaymentMethodDto(
            id = paymentMethod.id,
            userId = paymentMethod.userId,
            type = paymentMethod.type,
            last4Digits = paymentMethod.last4Digits,
            expiryMonth = paymentMethod.expiryMonth,
            expiryYear = paymentMethod.expiryYear,
            cardBrand = paymentMethod.cardBrand,
            isDefault = paymentMethod.isDefault,
            createdAt = paymentMethod.createdAt
        )
    }
    
    fun toResponse(paymentMethod: PaymentMethod): PaymentMethodResponse {
        return PaymentMethodResponse(
            id = paymentMethod.id,
            type = paymentMethod.type,
            last4Digits = paymentMethod.last4Digits,
            expiryMonth = paymentMethod.expiryMonth,
            expiryYear = paymentMethod.expiryYear,
            cardBrand = paymentMethod.cardBrand,
            isDefault = paymentMethod.isDefault
        )
    }
    
    fun toEntity(request: CreatePaymentMethodRequest, providerTokenId: String): PaymentMethod {
        return PaymentMethod(
            id = null,
            userId = request.userId,
            type = request.type,
            providerTokenId = providerTokenId,
            last4Digits = request.last4Digits ?: "0000",
            expiryMonth = request.expiryMonth,
            expiryYear = request.expiryYear,
            cardBrand = null, // Será preenchido pelo provedor de pagamento
            isDefault = request.setAsDefault,
            createdAt = LocalDateTime.now(),
            updatedAt = null
        )
    }
} 