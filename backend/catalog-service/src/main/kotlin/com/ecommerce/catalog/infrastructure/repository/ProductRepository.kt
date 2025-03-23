package com.ecommerce.catalog.infrastructure.repository

import com.ecommerce.catalog.domain.model.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.*

@Repository
interface ProductRepository : JpaRepository<Product, UUID> {
    
    fun findBySku(sku: String): Optional<Product>
    
    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Product>
    
    fun findByActiveTrue(pageable: Pageable): Page<Product>
    
    fun findByFeaturedTrueAndActiveTrue(pageable: Pageable): Page<Product>
    
    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.id = :categoryId AND p.active = true")
    fun findByCategoryId(@Param("categoryId") categoryId: UUID, pageable: Pageable): Page<Product>
    
    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.slug = :categorySlug AND p.active = true")
    fun findByCategorySlug(@Param("categorySlug") categorySlug: String, pageable: Pageable): Page<Product>
    
    @Query("SELECT p FROM Product p WHERE p.brand.id = :brandId AND p.active = true")
    fun findByBrandId(@Param("brandId") brandId: UUID, pageable: Pageable): Page<Product>
    
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.active = true")
    fun findByPriceRange(@Param("minPrice") minPrice: BigDecimal, @Param("maxPrice") maxPrice: BigDecimal, pageable: Pageable): Page<Product>
    
    @Query("""
        SELECT p FROM Product p
        WHERE (
            p.name LIKE %:keyword% OR 
            p.description LIKE %:keyword% OR 
            p.shortDescription LIKE %:keyword% OR
            p.sku LIKE %:keyword%
        ) AND p.active = true
    """)
    fun searchProducts(@Param("keyword") keyword: String, pageable: Pageable): Page<Product>
    
    @Query("""
        SELECT p FROM Product p
        JOIN p.categories c
        WHERE c.id IN :categoryIds AND p.active = true
    """)
    fun findByCategoryIds(@Param("categoryIds") categoryIds: List<UUID>, pageable: Pageable): Page<Product>
    
    @Query("""
        SELECT p FROM Product p
        LEFT JOIN p.variants v
        WHERE p.active = true
        GROUP BY p
        HAVING SUM(v.stockQuantity) > 0
    """)
    fun findInStock(pageable: Pageable): Page<Product>
    
    @Query("""
        SELECT p FROM Product p
        WHERE p.salePrice IS NOT NULL AND p.active = true
        ORDER BY (p.price - p.salePrice) / p.price DESC
    """)
    fun findOnSale(pageable: Pageable): Page<Product>
} 