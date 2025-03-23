package com.ecommerce.payment.domain.repository

import com.ecommerce.payment.domain.model.Payment
import com.ecommerce.payment.domain.model.PaymentStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface PaymentRepository : JpaRepository<Payment, String> {

    /**
     * Busca um pagamento pelo ID do pedido
     */
    fun findByOrderId(orderId: String): Payment?
    
    /**
     * Busca um pagamento pelo ID do pagamento no Stripe
     */
    fun findByPaymentIntentId(paymentIntentId: String): Payment?
    
    /**
     * Busca pagamentos pelo ID do usuário
     */
    fun findByUserId(userId: String, pageable: Pageable): Page<Payment>
    
    /**
     * Busca pagamentos por status
     */
    fun findByStatus(status: PaymentStatus, pageable: Pageable): Page<Payment>
    
    /**
     * Busca pagamentos por período de criação
     */
    fun findByCreatedAtBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<Payment>
    
    /**
     * Busca pagamentos por período e status
     */
    fun findByCreatedAtBetweenAndStatus(
        startDate: LocalDateTime, 
        endDate: LocalDateTime, 
        status: PaymentStatus
    ): List<Payment>
    
    /**
     * Conta pagamentos por status em um período
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status AND p.createdAt BETWEEN :startDate AND :endDate")
    fun countByStatusAndPeriod(
        @Param("status") status: PaymentStatus,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Long
    
    /**
     * Soma valores de pagamentos por status em um período
     */
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status AND p.createdAt BETWEEN :startDate AND :endDate")
    fun sumAmountByStatusAndPeriod(
        @Param("status") status: PaymentStatus,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): java.math.BigDecimal?
    
    /**
     * Busca pagamentos mais recentes
     */
    fun findTop10ByOrderByCreatedAtDesc(): List<Payment>
} 