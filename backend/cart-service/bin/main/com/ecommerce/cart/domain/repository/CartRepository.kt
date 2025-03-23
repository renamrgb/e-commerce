package com.ecommerce.cart.domain.repository

import com.ecommerce.cart.domain.entity.Cart
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface CartRepository : JpaRepository<Cart, UUID> {
    /**
     * Busca um carrinho por ID incluindo seus itens
     */
    @EntityGraph(attributePaths = ["items"])
    @Query("SELECT c FROM Cart c WHERE c.id = :id")
    fun findByIdWithItems(id: UUID): Optional<Cart>
    
    /**
     * Busca o carrinho ativo de um usuário
     */
    fun findByUserIdAndIsActiveTrue(userId: UUID): Optional<Cart>
    
    /**
     * Busca todos os carrinhos de um usuário ordenados pelo mais recente
     */
    @Query("SELECT c FROM Cart c WHERE c.userId = :userId ORDER BY c.createdAt DESC")
    fun findAllByUserId(@Param("userId") userId: UUID): List<Cart>

    @Query("SELECT COUNT(c) > 0 FROM Cart c WHERE c.id = :cartId AND c.userId = :userId")
    fun existsByIdAndUserId(cartId: UUID, userId: UUID): Boolean

    @EntityGraph(attributePaths = ["items"])
    @Query("SELECT c FROM Cart c WHERE c.userId = :userId AND c.isActive = true")
    fun findByUserIdAndIsActiveTrueWithItems(userId: UUID): Optional<Cart>

    @Query("SELECT COUNT(c) FROM Cart c WHERE c.userId = :userId AND c.isActive = true")
    fun countActiveCartsByUserId(userId: UUID): Long
} 