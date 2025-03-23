package com.ecommerce.catalog.domain.model

import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "product_images")
data class ProductImage(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    var product: Product? = null,

    @Column(name = "url", nullable = false)
    var url: String,

    @Column(name = "alt_text")
    var altText: String? = null,

    @Column(name = "position")
    var position: Int = 0,

    @Column(name = "is_primary")
    var isPrimary: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
) {
    // Método para atualizar a data de modificação
    @PreUpdate
    fun preUpdate() {
        updatedAt = OffsetDateTime.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProductImage

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "ProductImage(id=$id, url='$url')"
    }
} 