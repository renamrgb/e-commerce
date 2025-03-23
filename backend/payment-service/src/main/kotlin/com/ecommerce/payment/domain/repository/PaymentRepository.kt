package com.ecommerce.payment.domain.repository

import com.ecommerce.payment.domain.model.Payment
import com.ecommerce.payment.domain.model.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findByOrderId(orderId: String): Optional<Payment>
    fun findByUserId(userId: String): List<Payment>
    fun findByUserIdAndStatus(userId: String, status: PaymentStatus): List<Payment>
    fun findByStatus(status: PaymentStatus): List<Payment>
    fun findByPaymentIntentId(paymentIntentId: String): Optional<Payment>
} 