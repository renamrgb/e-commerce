package com.ecommerce.cart.domain.entity

import com.ecommerce.cart.application.dto.CartDto
import com.ecommerce.cart.application.dto.CartItemDto
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Type
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.*

@Entity
@Table(name = "carts")
class Cart(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Type(type = "uuid-char")
    var id: UUID? = null,

    @Type(type = "uuid-char")
    @Column(name = "user_id")
    var userId: UUID? = null,

    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var items: MutableList<CartItem> = mutableListOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    var coupon: Coupon? = null,

    @Column(name = "discount")
    var discount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @Column(name = "expired_at")
    var expiredAt: LocalDateTime? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Adiciona um item ao carrinho
     */
    fun addItem(item: CartItem) {
        items.add(item)
        item.cart = this
    }

    /**
     * Remove um item do carrinho
     */
    fun removeItem(item: CartItem) {
        items.remove(item)
        item.cart = null
    }

    /**
     * Limpa todos os itens do carrinho
     */
    fun clear() {
        items.clear()
    }

    /**
     * Calcula o subtotal do carrinho (soma dos itens sem desconto)
     */
    @get:Transient
    val subtotal: BigDecimal
        get() = items.sumOf { it.total }

    /**
     * Calcula o total do carrinho (subtotal - desconto)
     */
    @get:Transient
    val total: BigDecimal
        get() {
            val subtotalValue = subtotal
            return if (subtotalValue > discount) subtotalValue.subtract(discount) else BigDecimal.ZERO
        }

    /**
     * Converte a entidade para DTO
     */
    fun toDto(): CartDto {
        return CartDto(
            id = id,
            userId = userId,
            items = items.map { it.toDto() },
            couponCode = coupon?.code,
            discount = discount,
            subtotal = subtotal,
            total = total,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Cart

        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
} 