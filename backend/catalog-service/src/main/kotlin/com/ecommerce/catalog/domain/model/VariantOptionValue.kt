package com.ecommerce.catalog.domain.model

import jakarta.persistence.*
import java.io.Serializable
import java.util.*

@Entity
@Table(name = "variant_option_values")
data class VariantOptionValue(
    @EmbeddedId
    val id: VariantOptionValueId = VariantOptionValueId(),

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("variantId")
    @JoinColumn(name = "variant_id")
    var variant: ProductVariant? = null,

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

        other as VariantOptionValue

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "VariantOptionValue(variant=${variant?.id}, attribute=${attribute?.name}, value='$value')"
    }
}

@Embeddable
data class VariantOptionValueId(
    @Column(name = "variant_id")
    var variantId: UUID? = null,

    @Column(name = "attribute_id")
    var attributeId: UUID? = null
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VariantOptionValueId

        if (variantId != other.variantId) return false
        if (attributeId != other.attributeId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = variantId?.hashCode() ?: 0
        result = 31 * result + (attributeId?.hashCode() ?: 0)
        return result
    }
} 