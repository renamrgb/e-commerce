package com.ecommerce.cart.application.dto

import com.ecommerce.cart.domain.entity.Coupon
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class CouponUpdateRequest(
    @field:NotBlank(message = "O código é obrigatório")
    @field:Size(min = 3, max = 50, message = "O código deve ter entre 3 e 50 caracteres")
    val code: String,
    
    @field:NotBlank(message = "A descrição é obrigatória")
    @field:Size(max = 100, message = "A descrição deve ter no máximo 100 caracteres")
    val description: String,
    
    @field:NotNull(message = "O tipo de desconto é obrigatório")
    val discountType: Coupon.DiscountType,
    
    @field:NotNull(message = "O valor do desconto é obrigatório")
    @field:Min(value = 0, message = "O valor do desconto deve ser maior ou igual a zero")
    val discountValue: BigDecimal,
    
    @field:Min(value = 0, message = "O valor mínimo de compra deve ser maior ou igual a zero")
    val minPurchaseAmount: BigDecimal? = null,
    
    @field:Min(value = 0, message = "O valor máximo de desconto deve ser maior ou igual a zero")
    val maxDiscountAmount: BigDecimal? = null,
    
    @field:Min(value = 1, message = "O número máximo de usos deve ser maior ou igual a 1")
    val maxUses: Int? = null,
    
    val currentUses: Int? = null,
    
    val validFrom: LocalDateTime,
    
    val validUntil: LocalDateTime? = null,
    
    val active: Boolean
) 