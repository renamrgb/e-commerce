package com.ecommerce.cart.application.dto

import javax.validation.constraints.NotBlank

data class ApplyCouponRequest(
    @field:NotBlank(message = "Código do cupom é obrigatório")
    val code: String
) 