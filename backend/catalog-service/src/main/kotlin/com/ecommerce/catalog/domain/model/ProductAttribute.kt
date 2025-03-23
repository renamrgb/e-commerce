package com.ecommerce.catalog.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "product_attributes")
data class ProductAttribute(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),

    @OneToMany(mappedBy = "attribute", cascade = [CascadeType.ALL], orphanRemoval = true)
    val productAttributeValues: MutableSet<ProductAttributeValue> = mutableSetOf(),

    @OneToMany(mappedBy = "attribute", cascade = [CascadeType.ALL], orphanRemoval = true)
    val variantOptionValues: MutableSet<VariantOptionValue> = mutableSetOf()
) {
    // Método para atualizar a data de modificação
    @PreUpdate
    fun preUpdate() {
        updatedAt = OffsetDateTime.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProductAttribute

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "ProductAttribute(id=$id, name='$name')"
    }
} 