package com.ecommerce.order.application.dto

data class OrderUpdateRequest(
    val status: String? = null,
    val trackingCode: String? = null,
    val notes: String? = null,
    val paymentId: String? = null
) 