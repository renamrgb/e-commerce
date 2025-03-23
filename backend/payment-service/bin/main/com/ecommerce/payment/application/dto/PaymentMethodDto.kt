package com.ecommerce.payment.application.dto

import com.ecommerce.payment.domain.model.PaymentMethodType
import java.time.LocalDateTime

data class PaymentMethodDto(
    val id: Long?,
    val userId: String,
    val type: PaymentMethodType,
    val last4Digits: String,
    val expiryMonth: Int?,
    val expiryYear: Int?,
    val cardBrand: String?,
    val isDefault: Boolean,
    val createdAt: LocalDateTime
)

data class CreatePaymentMethodRequest(
    val userId: String,
    val type: PaymentMethodType,
    val token: String,
    val setAsDefault: Boolean = false,
    // Dados adicionais específicos do tipo de pagamento
    val cardholderName: String? = null,
    val last4Digits: String? = null,
    val expiryMonth: Int? = null,
    val expiryYear: Int? = null
)

data class PaymentMethodResponse(
    val id: Long?,
    val type: PaymentMethodType,
    val last4Digits: String,
    val expiryMonth: Int?,
    val expiryYear: Int?,
    val cardBrand: String?,
    val isDefault: Boolean
)

data class UpdatePaymentMethodRequest(
    val id: Long,
    val setAsDefault: Boolean? = null,
    val expiryMonth: Int? = null,
    val expiryYear: Int? = null
) 