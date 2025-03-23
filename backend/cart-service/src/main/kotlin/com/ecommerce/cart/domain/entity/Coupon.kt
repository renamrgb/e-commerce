package com.ecommerce.cart.domain.entity

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Type
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.*

@Entity
@Table(name = "coupons")
class Coupon(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Type(type = "uuid-char")
    var id: UUID? = null,
    
    @Column(nullable = false, unique = true, length = 50)
    val code: String,
    
    @Column(nullable = false, length = 100)
    val description: String,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    val discountType: DiscountType,
    
    @Column(name = "discount_value", nullable = false)
    val discountValue: BigDecimal,
    
    @Column(name = "min_purchase_amount")
    val minPurchaseAmount: BigDecimal? = null,
    
    @Column(name = "max_discount_amount")
    val maxDiscountAmount: BigDecimal? = null,
    
    @Column(name = "max_uses")
    val maxUses: Int? = null,
    
    @Column(name = "current_uses")
    var currentUses: Int = 0,
    
    @Column(name = "valid_from")
    val validFrom: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "valid_until")
    val validUntil: LocalDateTime? = null,
    
    @Column(nullable = false)
    var active: Boolean = true,
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    enum class DiscountType {
        PERCENTAGE,
        FIXED_AMOUNT
    }
    
    /**
     * Verifica se o cupom é válido considerando data de validade e número máximo de usos
     */
    fun isValid(): Boolean {
        val now = LocalDateTime.now()
        
        // Verifica se está ativo
        if (!active) return false
        
        // Verifica a data de início
        if (now.isBefore(validFrom)) return false
        
        // Verifica a data de validade, se definida
        if (validUntil != null && now.isAfter(validUntil)) return false
        
        // Verifica número máximo de usos, se definido
        if (maxUses != null && currentUses >= maxUses) return false
        
        return true
    }
    
    /**
     * Calcula o valor do desconto com base no subtotal do carrinho
     */
    fun calculateDiscount(subtotal: BigDecimal): BigDecimal {
        val discount = when (discountType) {
            DiscountType.PERCENTAGE -> {
                // Divide por 100 para converter percentual em decimal
                val percentageValue = discountValue.divide(BigDecimal("100"))
                subtotal.multiply(percentageValue)
            }
            DiscountType.FIXED_AMOUNT -> {
                discountValue
            }
        }
        
        // Limita o desconto ao valor máximo, se definido
        return if (maxDiscountAmount != null && discount > maxDiscountAmount) {
            maxDiscountAmount
        } else {
            discount
        }
    }
    
    /**
     * Incrementa o contador de uso do cupom
     */
    fun incrementUsage() {
        currentUses++
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Coupon

        if (id != null && other.id != null) {
            return id == other.id
        }

        return code == other.code
    }

    override fun hashCode(): Int {
        return if (id != null) id.hashCode() else code.hashCode()
    }
} 