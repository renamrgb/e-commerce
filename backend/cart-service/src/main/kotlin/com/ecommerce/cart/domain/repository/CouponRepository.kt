package com.ecommerce.cart.domain.repository

import com.ecommerce.cart.domain.entity.Coupon
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

@Repository
interface CouponRepository : JpaRepository<Coupon, UUID> {
    
    /**
     * Busca um cupom ativo pelo código
     */
    fun findByCodeAndActiveTrue(code: String): Optional<Coupon>

    /**
     * Busca um cupom válido pelo código (ativo, dentro do período de validade e com usos disponíveis)
     */
    @Query("""
        SELECT c FROM Coupon c 
        WHERE c.code = :code 
        AND c.active = true 
        AND c.validFrom <= :now 
        AND (c.validUntil IS NULL OR c.validUntil >= :now) 
        AND (c.maxUses IS NULL OR c.currentUses < c.maxUses)
    """)
    fun findValidCouponByCode(@Param("code") code: String, @Param("now") now: LocalDateTime = LocalDateTime.now()): Optional<Coupon>

    /**
     * Verifica se existe um cupom com o código especificado
     */
    fun existsByCode(code: String): Boolean

    fun findByActiveTrue(pageable: Pageable): Page<Coupon>
} 