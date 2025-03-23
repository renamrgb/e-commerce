package com.ecommerce.order.domain.model

/**
 * Modelo de domínio para informações de pagamento
 */
data class PaymentInfo(
    val method: String,
    val cardLastDigits: String?,
    val installments: Int,
    val paymentId: String?
) 