package com.ecommerce.catalog.domain.model

import jakarta.persistence.*
import java.io.Serializable
import java.util.*

@Entity
@Table(name = "product_attribute_values")
data class ProductAttributeValue(
    @EmbeddedId
    val id: ProductAttributeValueId = ProductAttributeValueId(),

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    var product: Product? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("attributeId")
    @JoinColumn(name = "attribute_id")
    var attribute: ProductAttribute? = null,

    @Column(name = "value", nullable = false)
    var value: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProductAttributeValue

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "ProductAttributeValue(product=${product?.id}, attribute=${attribute?.name}, value='$value')"
    }
}

@Embeddable
data class ProductAttributeValueId(
    @Column(name = "product_id")
    var productId: UUID? = null,

    @Column(name = "attribute_id")
    var attributeId: UUID? = null
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProductAttributeValueId

        if (productId != other.productId) return false
        if (attributeId != other.attributeId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = productId?.hashCode() ?: 0
        result = 31 * result + (attributeId?.hashCode() ?: 0)
        return result
    }
} 