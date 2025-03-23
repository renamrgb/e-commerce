package com.ecommerce.catalog.infrastructure.repository

import com.ecommerce.catalog.domain.model.ProductAttribute
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProductAttributeRepository : JpaRepository<ProductAttribute, UUID> {
    
    fun findByNameContainingIgnoreCase(name: String): List<ProductAttribute>
    
    @Query("""
        SELECT DISTINCT a FROM ProductAttribute a
        JOIN a.productAttributeValues v
        JOIN v.product p
        WHERE p.id = :productId
    """)
    fun findByProductId(@Param("productId") productId: UUID): List<ProductAttribute>
    
    @Query("""
        SELECT DISTINCT a FROM ProductAttribute a
        JOIN a.variantOptionValues v
        JOIN v.variant var
        JOIN var.product p
        WHERE p.id = :productId
    """)
    fun findVariantAttributesByProductId(@Param("productId") productId: UUID): List<ProductAttribute>
} 