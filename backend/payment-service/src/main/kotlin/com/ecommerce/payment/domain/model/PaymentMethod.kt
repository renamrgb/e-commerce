package com.ecommerce.payment.domain.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "payment_methods")
data class PaymentMethod(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val userId: String,
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val type: PaymentMethodType,
    
    @Column(nullable = false)
    val providerTokenId: String,
    
    @Column(nullable = false)
    val last4Digits: String,
    
    @Column(nullable = true)
    val expiryMonth: Int? = null,
    
    @Column(nullable = true)
    val expiryYear: Int? = null,
    
    @Column(nullable = true)
    val cardBrand: String? = null,
    
    @Column(nullable = false)
    val isDefault: Boolean = false,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = true)
    val updatedAt: LocalDateTime? = null
)

enum class PaymentMethodType {
    CREDIT_CARD,
    DEBIT_CARD,
    BANK_TRANSFER,
    PIX,
    BOLETO
} 