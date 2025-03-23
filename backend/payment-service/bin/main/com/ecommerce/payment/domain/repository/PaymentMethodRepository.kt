package com.ecommerce.payment.domain.repository

import com.ecommerce.payment.domain.model.PaymentMethod
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PaymentMethodRepository : JpaRepository<PaymentMethod, Long> {
    fun findByUserId(userId: String): List<PaymentMethod>
    fun findByUserIdAndIsDefault(userId: String, isDefault: Boolean): Optional<PaymentMethod>
    fun findByProviderTokenId(providerTokenId: String): Optional<PaymentMethod>
} 