package com.ecommerce.cart.domain.repository

import com.ecommerce.cart.domain.entity.CartItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.List
import java.util.Optional
import java.util.UUID

@Repository
interface CartItemRepository : JpaRepository<CartItem, UUID> {
    /**
     * Busca um item por carrinho e produto e variante
     */
    @Query("SELECT i FROM CartItem i WHERE i.cart.id = :cartId AND i.productId = :productId AND i.variantId = :variantId")
    fun findByCartIdAndProductIdAndVariantId(
        @Param("cartId") cartId: UUID,
        @Param("productId") productId: UUID,
        @Param("variantId") variantId: UUID?
    ): Optional<CartItem>
    
    /**
     * Busca todos os itens de um carrinho
     */
    fun findByCartId(cartId: UUID): List<CartItem>
    
    /**
     * Exclui todos os itens de um carrinho
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    fun deleteByCartId(cartId: UUID)

    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId")
    fun countByCartId(cartId: UUID): Long

    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.cart.id = :cartId")
    fun countItemQuantitiesByCartId(cartId: UUID): Long?
} 