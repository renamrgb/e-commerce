package com.ecommerce.cart.domain.entity

import com.ecommerce.cart.application.dto.CartItemDto
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Type
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.*

@Entity
@Table(name = "cart_items")
class CartItem(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Type(type = "uuid-char")
    var id: UUID? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    var cart: Cart? = null,
    
    @Type(type = "uuid-char")
    @Column(name = "product_id", nullable = false)
    val productId: UUID,
    
    @Column(name = "product_name", nullable = false)
    val productName: String,
    
    @Column(name = "product_slug", nullable = false)
    val productSlug: String,
    
    @Column(name = "product_image")
    val productImage: String? = null,
    
    @Type(type = "uuid-char")
    @Column(name = "variant_id")
    val variantId: UUID? = null,
    
    @Column(name = "variant_name")
    val variantName: String? = null,
    
    @Column(name = "price", nullable = false)
    val price: BigDecimal,
    
    @Column(name = "quantity", nullable = false)
    var quantity: Int,
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Calcula o total do item (preço * quantidade)
     */
    @get:Transient
    val total: BigDecimal
        get() = price.multiply(BigDecimal(quantity))

    /**
     * Converte a entidade para DTO
     */
    fun toDto(): CartItemDto {
        return CartItemDto(
            id = id,
            productId = productId,
            productName = productName,
            productSlug = productSlug,
            productImage = productImage,
            variantId = variantId,
            variantName = variantName,
            price = price,
            quantity = quantity,
            total = total,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    /**
     * Atualiza a quantidade do item
     */
    fun updateQuantity(newQuantity: Int) {
        require(newQuantity > 0) { "A quantidade deve ser maior que zero" }
        this.quantity = newQuantity
    }
    
    /**
     * Calcula o valor total do item (preço unitário x quantidade)
     */
    fun getTotal(): BigDecimal {
        return price.multiply(BigDecimal(quantity))
    }
    
    /**
     * Verifica se o CartItem possui o produto e variante especificados
     */
    fun hasProductAndVariant(productId: UUID, variantId: UUID?): Boolean {
        return this.productId == productId && this.variantId == variantId
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CartItem

        if (id != null && other.id != null) {
            return id == other.id
        }

        if (productId != other.productId) return false
        if (variantId != other.variantId) return false

        return true
    }
    
    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        if (result == 0) {
            result = 31 * result + productId.hashCode()
            result = 31 * result + (variantId?.hashCode() ?: 0)
        }
        return result
    }
} 