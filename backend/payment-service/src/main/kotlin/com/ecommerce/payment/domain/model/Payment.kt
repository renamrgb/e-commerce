package com.ecommerce.payment.domain.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "payments")
data class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val orderId: String,
    
    @Column(nullable = false)
    val userId: String,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val amount: BigDecimal,
    
    @Column(nullable = false)
    val currency: String = "BRL",
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val status: PaymentStatus,
    
    @Column(nullable = true)
    val paymentIntentId: String? = null,
    
    @Column(nullable = true)
    val paymentMethodId: String? = null,
    
    @Column(nullable = true)
    val errorMessage: String? = null,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = true)
    val updatedAt: LocalDateTime? = null,
    
    @Column(nullable = true)
    val completedAt: LocalDateTime? = null
)

enum class PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED,
    CANCELLED
} 