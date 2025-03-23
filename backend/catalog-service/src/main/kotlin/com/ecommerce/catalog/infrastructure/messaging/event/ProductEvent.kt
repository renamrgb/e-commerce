package com.ecommerce.catalog.infrastructure.messaging.event

data class ProductEvent(
    val id: String,
    val productId: String,
    val type: String,
    val data: Any,
    val timestamp: Long
) 