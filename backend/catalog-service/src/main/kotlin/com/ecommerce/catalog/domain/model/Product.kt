package com.ecommerce.catalog.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "products")
data class Product(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "sku", unique = true, nullable = false)
    var sku: String,

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "short_description")
    var shortDescription: String? = null,

    @Column(name = "price", nullable = false)
    var price: BigDecimal,

    @Column(name = "sale_price")
    var salePrice: BigDecimal? = null,

    @Column(name = "cost_price")
    var costPrice: BigDecimal? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    var brand: Brand? = null,

    @Column(name = "weight")
    var weight: BigDecimal? = null,

    @Column(name = "height")
    var height: BigDecimal? = null,

    @Column(name = "width")
    var width: BigDecimal? = null,

    @Column(name = "length")
    var length: BigDecimal? = null,

    @Column(name = "active")
    var active: Boolean = true,

    @Column(name = "featured")
    var featured: Boolean = false,

    @Column(name = "quantity_sold")
    var quantitySold: Int = 0,

    @Column(name = "avg_rating")
    var avgRating: BigDecimal = BigDecimal.ZERO,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),

    @ManyToMany(cascade = [CascadeType.MERGE])
    @JoinTable(
        name = "product_categories",
        joinColumns = [JoinColumn(name = "product_id")],
        inverseJoinColumns = [JoinColumn(name = "category_id")]
    )
    val categories: MutableSet<Category> = mutableSetOf(),

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    val images: MutableSet<ProductImage> = mutableSetOf(),

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    val variants: MutableSet<ProductVariant> = mutableSetOf(),

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    val attributeValues: MutableSet<ProductAttributeValue> = mutableSetOf()
) {
    // Métodos para manipulação de relações
    fun addCategory(category: Category) {
        categories.add(category)
        category.products.add(this)
    }

    fun removeCategory(category: Category) {
        categories.remove(category)
        category.products.remove(this)
    }

    fun addImage(image: ProductImage) {
        images.add(image)
        image.product = this
    }

    fun removeImage(image: ProductImage) {
        images.remove(image)
        image.product = null
    }

    fun addVariant(variant: ProductVariant) {
        variants.add(variant)
        variant.product = this
    }

    fun removeVariant(variant: ProductVariant) {
        variants.remove(variant)
        variant.product = null
    }

    fun addAttributeValue(attributeValue: ProductAttributeValue) {
        attributeValues.add(attributeValue)
        attributeValue.product = this
    }

    fun removeAttributeValue(attributeValue: ProductAttributeValue) {
        attributeValues.remove(attributeValue)
        attributeValue.product = null
    }

    // Método para calcular se o produto está com desconto
    fun hasDiscount(): Boolean {
        return salePrice != null && salePrice!! < price
    }

    // Método para calcular o percentual de desconto
    fun discountPercentage(): Int? {
        return if (hasDiscount() && price > BigDecimal.ZERO) {
            val discount = price.subtract(salePrice)
            (discount.multiply(BigDecimal(100)).divide(price, 0, BigDecimal.ROUND_HALF_UP)).toInt()
        } else null
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

        other as Product

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Product(id=$id, sku='$sku', name='$name')"
    }
} 