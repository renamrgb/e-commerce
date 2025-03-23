package com.ecommerce.cart.application.dto

import java.util.UUID
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

data class AddItemRequest(
    @field:NotNull(message = "ID do produto é obrigatório")
    val productId: UUID,
    
    val variantId: UUID? = null,
    
    @field:NotNull(message = "Quantidade é obrigatória")
    @field:Min(value = 1, message = "Quantidade mínima é 1")
    val quantity: Int,
    
    val attributes: Map<String, String>? = null
) 