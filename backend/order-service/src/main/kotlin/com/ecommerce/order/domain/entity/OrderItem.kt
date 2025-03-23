package com.ecommerce.order.domain.entity

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Type
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.*

@Entity
@Table(name = "order_items")
class OrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Type(type = "uuid-char")
    var id: UUID? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order? = null,
    
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
    val quantity: Int,
    
    @Column(name = "discount")
    val discount: BigDecimal = BigDecimal.ZERO,
    
    @Column(name = "total", nullable = false)
    val total: BigDecimal,
    
    @Column(name = "options")
    val options: String? = null,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OrderItem

        if (id != null && other.id != null) {
            return id == other.id
        }

        if (order?.id != other.order?.id) return false
        if (productId != other.productId) return false
        if (variantId != other.variantId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        if (result == 0) {
            result = 31 * result + (order?.id?.hashCode() ?: 0)
            result = 31 * result + productId.hashCode()
            result = 31 * result + (variantId?.hashCode() ?: 0)
        }
        return result
    }
} 