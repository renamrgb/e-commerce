package com.ecommerce.payment.domain.repository

import com.ecommerce.payment.domain.model.PaymentAudit
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Repositório para a entidade de auditoria de pagamentos
 */
@Repository
interface PaymentAuditRepository : JpaRepository<PaymentAudit, Long> {
    
    /**
     * Busca auditorias por ID do pagamento
     */
    fun findByPaymentIdOrderByCreatedAtDesc(paymentId: String): List<PaymentAudit>
    
    /**
     * Busca auditorias paginadas por ID do pagamento
     */
    fun findByPaymentIdOrderByCreatedAtDesc(paymentId: String, pageable: Pageable): Page<PaymentAudit>
    
    /**
     * Busca auditorias de pagamentos por período
     */
    fun findByCreatedAtBetweenOrderByCreatedAtDesc(start: LocalDateTime, end: LocalDateTime): List<PaymentAudit>
    
    /**
     * Busca auditorias por origem
     */
    fun findBySourceOrderByCreatedAtDesc(source: String, pageable: Pageable): Page<PaymentAudit>
    
    /**
     * Busca auditorias por ator
     */
    fun findByActorIdOrderByCreatedAtDesc(actorId: String, pageable: Pageable): Page<PaymentAudit>
    
    /**
     * Conta o número de mudanças de status para um pagamento
     */
    @Query("SELECT COUNT(a) FROM PaymentAudit a WHERE a.paymentId = :paymentId")
    fun countStatusChangesByPaymentId(@Param("paymentId") paymentId: String): Long
    
    /**
     * Busca a última auditoria de um pagamento
     */
    fun findTopByPaymentIdOrderByCreatedAtDesc(paymentId: String): PaymentAudit?
} 