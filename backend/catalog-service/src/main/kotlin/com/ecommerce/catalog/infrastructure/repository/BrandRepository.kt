package com.ecommerce.catalog.infrastructure.repository

import com.ecommerce.catalog.domain.model.Brand
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BrandRepository : JpaRepository<Brand, UUID> {
    
    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Brand>
    
    fun findByActiveTrue(pageable: Pageable): Page<Brand>
    
    @Query("SELECT COUNT(p) FROM Brand b JOIN b.products p WHERE b.id = :brandId")
    fun countProductsByBrand(@Param("brandId") brandId: UUID): Long
    
    @Query("""
        SELECT b FROM Brand b 
        JOIN b.products p 
        GROUP BY b 
        ORDER BY COUNT(p) DESC
    """)
    fun findTopBrandsByProductCount(pageable: Pageable): Page<Brand>
} 