package com.ecommerce.payment.domain.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

/**
 * Entidade para o padrão Outbox que garante a entrega de mensagens para o Kafka
 * mesmo em caso de falhas temporárias
 */
@Entity
@Table(name = "outbox_events")
data class OutboxEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /**
     * Identificador único da agregação relacionada a este evento
     */
    @Column(name = "aggregate_id", nullable = false)
    val aggregateId: String,

    /**
     * Tipo da agregação (ex: "payment", "payment_method")
     */
    @Column(name = "aggregate_type", nullable = false)
    val aggregateType: String,

    /**
     * Tipo do evento (ex: "completed", "failed", "canceled")
     */
    @Column(name = "event_type", nullable = false)
    val eventType: String,

    /**
     * Tópico Kafka para onde o evento deve ser enviado
     */
    @Column(name = "topic", nullable = false)
    val topic: String,

    /**
     * Chave para a mensagem Kafka
     */
    @Column(name = "message_key", nullable = false)
    val messageKey: String,

    /**
     * Conteúdo serializado da mensagem
     */
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    val payload: String,

    /**
     * Status do processamento do evento
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: OutboxStatus = OutboxStatus.PENDING,

    /**
     * Número de tentativas de envio
     */
    @Column(name = "retries", nullable = false)
    val retries: Int = 0,

    /**
     * Última mensagem de erro durante o processamento
     */
    @Column(name = "error_message")
    val errorMessage: String? = null,

    /**
     * Data e hora em que o evento foi criado
     */
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    /**
     * Data e hora da última atualização do evento
     */
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    /**
     * Data e hora do processamento do evento
     */
    @Column(name = "processed_at")
    val processedAt: LocalDateTime? = null
)

/**
 * Status de processamento do evento no outbox
 */
enum class OutboxStatus {
    PENDING,      // Evento pendente de processamento
    PROCESSING,   // Evento em processamento
    PROCESSED,    // Evento processado com sucesso
    FAILED        // Falha no processamento do evento
} 