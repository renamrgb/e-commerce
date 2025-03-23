package com.ecommerce.cart.application.dto

import com.ecommerce.cart.domain.entity.Coupon
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class CouponDto(
    val id: UUID,
    val code: String,
    val description: String,
    val discountType: String,
    val discountValue: BigDecimal,
    val minPurchaseAmount: BigDecimal?,
    val maxDiscountAmount: BigDecimal?,
    val maxUses: Int?,
    val currentUses: Int,
    val validFrom: LocalDateTime,
    val validUntil: LocalDateTime?,
    val active: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CouponCreateRequest(
    val code: String,
    val description: String,
    val discountType: Coupon.DiscountType,
    val discountValue: BigDecimal,
    val minPurchaseAmount: BigDecimal? = null,
    val maxDiscountAmount: BigDecimal? = null,
    val maxUses: Int? = null,
    val validFrom: LocalDateTime = LocalDateTime.now(),
    val validUntil: LocalDateTime? = null,
    val active: Boolean = true
)

data class CouponUpdateRequest(
    val description: String? = null,
    val discountValue: BigDecimal? = null,
    val minPurchaseAmount: BigDecimal? = null,
    val maxDiscountAmount: BigDecimal? = null,
    val maxUses: Int? = null,
    val validUntil: LocalDateTime? = null,
    val active: Boolean? = null
) 