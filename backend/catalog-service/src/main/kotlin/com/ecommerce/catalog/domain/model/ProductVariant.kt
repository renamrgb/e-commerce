package com.ecommerce.catalog.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "product_variants")
data class ProductVariant(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    var product: Product? = null,

    @Column(name = "sku", unique = true, nullable = false)
    var sku: String,

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "price", nullable = false)
    var price: BigDecimal,

    @Column(name = "sale_price")
    var salePrice: BigDecimal? = null,

    @Column(name = "cost_price")
    var costPrice: BigDecimal? = null,

    @Column(name = "stock_quantity")
    var stockQuantity: Int = 0,

    @Column(name = "weight")
    var weight: BigDecimal? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),

    @OneToMany(mappedBy = "variant", cascade = [CascadeType.ALL], orphanRemoval = true)
    val optionValues: MutableSet<VariantOptionValue> = mutableSetOf()
) {
    // Métodos para manipulação de relações
    fun addOptionValue(optionValue: VariantOptionValue) {
        optionValues.add(optionValue)
        optionValue.variant = this
    }

    fun removeOptionValue(optionValue: VariantOptionValue) {
        optionValues.remove(optionValue)
        optionValue.variant = null
    }

    // Método para calcular se a variante está com desconto
    fun hasDiscount(): Boolean {
        return salePrice != null && salePrice!! < price
    }

    // Método para calcular preço atual (com ou sem desconto)
    fun getCurrentPrice(): BigDecimal {
        return salePrice ?: price
    }

    // Método para atualizar a data de modificação
    @PreUpdate
    fun preUpdate() {
        updatedAt = OffsetDateTime.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProductVariant

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "ProductVariant(id=$id, sku='$sku', name='$name')"
    }
} 