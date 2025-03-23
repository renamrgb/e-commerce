package com.ecommerce.catalog.infrastructure.repository

import com.ecommerce.catalog.domain.model.Category
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CategoryRepository : JpaRepository<Category, UUID> {
    
    fun findBySlug(slug: String): Optional<Category>
    
    fun findByParentIsNull(pageable: Pageable): Page<Category>
    
    fun findByParentId(parentId: UUID, pageable: Pageable): Page<Category>
    
    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Category>
    
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.active = true")
    fun findRootCategories(pageable: Pageable): Page<Category>
    
    @Query("SELECT c FROM Category c WHERE c.name LIKE %:keyword% OR c.description LIKE %:keyword%")
    fun searchCategories(@Param("keyword") keyword: String, pageable: Pageable): Page<Category>
    
    @Query("SELECT COUNT(p) FROM Category c JOIN c.products p WHERE c.id = :categoryId")
    fun countProductsInCategory(@Param("categoryId") categoryId: UUID): Long
    
    @Query("""
        WITH RECURSIVE CategoryTree AS (
            SELECT id, name, parent_id, 1 as level
            FROM categories
            WHERE id = :categoryId
            UNION ALL
            SELECT c.id, c.name, c.parent_id, ct.level + 1
            FROM categories c
            JOIN CategoryTree ct ON c.parent_id = ct.id
        )
        SELECT id FROM CategoryTree
    """, nativeQuery = true)
    fun findAllSubcategoriesIds(@Param("categoryId") categoryId: UUID): List<UUID>
} 