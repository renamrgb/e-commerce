package com.ecommerce.payment.application.service

import com.ecommerce.payment.domain.model.AuditSource
import com.ecommerce.payment.domain.model.Payment
import com.ecommerce.payment.domain.model.PaymentAudit
import com.ecommerce.payment.domain.model.PaymentStatus
import com.ecommerce.payment.domain.repository.PaymentAuditRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Serviço para gerenciar a auditoria de pagamentos
 */
@Service
class PaymentAuditService(
    private val paymentAuditRepository: PaymentAuditRepository,
    private val objectMapper: ObjectMapper
) {
    
    private val logger = LoggerFactory.getLogger(PaymentAuditService::class.java)
    
    /**
     * Registra uma mudança de status em um pagamento
     */
    fun recordStatusChange(
        payment: Payment,
        previousStatus: PaymentStatus?,
        message: String? = null,
        actorId: String? = null,
        source: AuditSource = AuditSource.SYSTEM,
        metadata: Map<String, Any>? = null
    ): PaymentAudit {
        logger.info(
            "Registrando auditoria de pagamento: id={}, status: {} -> {}, source={}",
            payment.id, previousStatus, payment.status, source
        )
        
        val metadataJson = metadata?.let { objectMapper.writeValueAsString(it) }
        
        val audit = PaymentAudit(
            paymentId = payment.id,
            previousStatus = previousStatus,
            newStatus = payment.status,
            message = message,
            actorId = actorId,
            source = source,
            metadata = metadataJson,
            createdAt = LocalDateTime.now()
        )
        
        return paymentAuditRepository.save(audit)
    }
    
    /**
     * Registra uma auditoria de webhook
     */
    fun recordWebhookEvent(
        payment: Payment,
        previousStatus: PaymentStatus,
        eventType: String,
        eventId: String
    ): PaymentAudit {
        val metadata = mapOf(
            "eventType" to eventType,
            "eventId" to eventId,
            "timestamp" to LocalDateTime.now().toString()
        )
        
        return recordStatusChange(
            payment = payment,
            previousStatus = previousStatus,
            message = "Status atualizado por evento webhook: $eventType",
            source = AuditSource.WEBHOOK,
            metadata = metadata
        )
    }
    
    /**
     * Registra uma auditoria de ação administrativa
     */
    fun recordAdminAction(
        payment: Payment,
        previousStatus: PaymentStatus,
        action: String,
        adminId: String,
        reason: String? = null
    ): PaymentAudit {
        val metadata = mapOf(
            "action" to action,
            "adminId" to adminId,
            "reason" to (reason ?: "Não informado"),
            "timestamp" to LocalDateTime.now().toString()
        )
        
        return recordStatusChange(
            payment = payment,
            previousStatus = previousStatus,
            message = "Status alterado por ação administrativa: $action. Motivo: ${reason ?: 'Não informado'}",
            actorId = adminId,
            source = AuditSource.USER,
            metadata = metadata
        )
    }
    
    /**
     * Busca o histórico de auditoria de um pagamento
     */
    fun getPaymentAuditHistory(paymentId: String, pageable: Pageable): Page<PaymentAudit> {
        return paymentAuditRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId, pageable)
    }
    
    /**
     * Busca todas as auditorias de um período
     */
    fun getAuditsByDateRange(start: LocalDateTime, end: LocalDateTime): List<PaymentAudit> {
        return paymentAuditRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end)
    }
    
    /**
     * Busca auditorias por origem
     */
    fun getAuditsBySource(source: AuditSource, pageable: Pageable): Page<PaymentAudit> {
        return paymentAuditRepository.findBySourceOrderByCreatedAtDesc(source.name, pageable)
    }
    
    /**
     * Busca auditorias por ator
     */
    fun getAuditsByActor(actorId: String, pageable: Pageable): Page<PaymentAudit> {
        return paymentAuditRepository.findByActorIdOrderByCreatedAtDesc(actorId, pageable)
    }
} 