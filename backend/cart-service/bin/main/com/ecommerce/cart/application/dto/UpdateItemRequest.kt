package com.ecommerce.cart.application.dto

import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

data class UpdateItemRequest(
    @field:NotNull(message = "Quantidade é obrigatória")
    @field:Min(value = 1, message = "Quantidade mínima é 1")
    val quantity: Int
) 