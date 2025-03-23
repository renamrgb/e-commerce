package com.ecommerce.payment.domain.model

import java.time.LocalDateTime
import javax.persistence.*

/**
 * Entidade para registrar auditoria de mudanças no status de pagamentos
 */
@Entity
@Table(name = "payment_audits")
data class PaymentAudit(
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    // Referência ao pagamento
    @Column(name = "payment_id", nullable = false)
    val paymentId: String,
    
    // Status anterior
    @Column(name = "previous_status", nullable = true)
    @Enumerated(EnumType.STRING)
    val previousStatus: PaymentStatus?,
    
    // Novo status
    @Column(name = "new_status", nullable = false)
    @Enumerated(EnumType.STRING)
    val newStatus: PaymentStatus,
    
    // Mensagem (opcional)
    @Column(name = "message", length = 500)
    val message: String? = null,
    
    // ID do usuário que realizou a ação (se aplicável)
    @Column(name = "actor_id")
    val actorId: String? = null,
    
    // Origem da mudança (SYSTEM, WEBHOOK, USER, etc)
    @Column(name = "source", nullable = false)
    @Enumerated(EnumType.STRING)
    val source: AuditSource,
    
    // Metadados adicionais (JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    val metadata: String? = null,
    
    // Data e hora da mudança
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Origem da mudança de status
 */
enum class AuditSource {
    SYSTEM,      // Alteração automática pelo sistema
    WEBHOOK,     // Alteração por webhook do provedor de pagamento
    USER,        // Alteração manual por usuário
    API,         // Alteração via API
    SCHEDULED    // Alteração por job agendado
} 