package com.ecommerce.payment.application.dto

import com.ecommerce.payment.domain.model.AuditSource
import com.ecommerce.payment.domain.model.PaymentAudit
import com.ecommerce.payment.domain.model.PaymentStatus
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.LocalDateTime

/**
 * DTO para a entidade de auditoria de pagamentos
 */
data class PaymentAuditDto(
    val id: Long?,
    val paymentId: String,
    val previousStatus: PaymentStatus?,
    val newStatus: PaymentStatus,
    val message: String?,
    val actorId: String?,
    val source: String,
    val metadata: Map<String, Any>?,
    val createdAt: LocalDateTime
) {
    companion object {
        private val objectMapper = ObjectMapper()
        
        /**
         * Converte uma entidade PaymentAudit para um DTO
         */
        fun fromEntity(entity: PaymentAudit): PaymentAuditDto {
            val metadata: Map<String, Any>? = entity.metadata?.let {
                try {
                    @Suppress("UNCHECKED_CAST")
                    objectMapper.readValue(it, Map::class.java) as Map<String, Any>
                } catch (e: Exception) {
                    null
                }
            }
            
            return PaymentAuditDto(
                id = entity.id,
                paymentId = entity.paymentId,
                previousStatus = entity.previousStatus,
                newStatus = entity.newStatus,
                message = entity.message,
                actorId = entity.actorId,
                source = entity.source.name,
                metadata = metadata,
                createdAt = entity.createdAt
            )
        }
    }
} 