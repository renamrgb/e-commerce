package com.ecommerce.order.domain.model

/**
 * Modelo de domínio para endereço de entrega
 */
data class ShippingAddress(
    val recipientName: String,
    val street: String,
    val number: String,
    val complement: String?,
    val neighborhood: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val phone: String
) 