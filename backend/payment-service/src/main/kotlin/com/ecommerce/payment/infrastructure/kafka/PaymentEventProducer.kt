package com.ecommerce.payment.infrastructure.kafka

import com.ecommerce.payment.domain.model.Payment
import com.ecommerce.payment.infrastructure.kafka.event.PaymentEvent
import com.ecommerce.payment.infrastructure.outbox.OutboxService
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

/**
 * Produtor de eventos relacionados a pagamentos
 */
@Component
class PaymentEventProducer(
    private val objectMapper: ObjectMapper,
    private val outboxService: OutboxService,
    @Value("\${app.kafka.topics.payment-events}") private val paymentEventsTopic: String,
    @Value("\${app.kafka.topics.notification-events:notification-events}") private val notificationEventsTopic: String
) {
    private val logger = LoggerFactory.getLogger(PaymentEventProducer::class.java)
    
    companion object {
        const val PAYMENT_COMPLETED_TOPIC = "payment-service.payment.completed"
        const val PAYMENT_FAILED_TOPIC = "payment-service.payment.failed"
        const val PAYMENT_CANCELED_TOPIC = "payment-service.payment.canceled"
        const val PAYMENT_REFUNDED_TOPIC = "payment-service.payment.refunded"
    }
    
    /**
     * Envia um evento de pagamento para processamento assíncrono
     */
    fun sendPaymentEvent(payment: Payment) {
        try {
            logger.info("Enviando evento de pagamento: id={}, status={}", payment.id, payment.status)
            
            val event = PaymentEvent(
                eventId = UUID.randomUUID().toString(),
                paymentId = payment.id,
                orderId = payment.orderId,
                userId = payment.userId,
                amount = payment.amount,
                status = payment.status,
                paymentIntentId = payment.paymentIntentId,
                errorMessage = payment.errorMessage,
                eventTimestamp = LocalDateTime.now()
            )
            
            val payload = objectMapper.writeValueAsString(event)
            
            // Usando o sistema outbox para garantir a entrega
            outboxService.createEvent(
                aggregateId = payment.id,
                aggregateType = "Payment", 
                eventType = "PaymentStatusChanged",
                topic = paymentEventsTopic,
                messageKey = payment.id,
                payload = payload
            )
            
            logger.debug("Evento de pagamento registrado no outbox: {}", event.eventId)
        } catch (e: Exception) {
            logger.error("Erro ao enviar evento de pagamento: {}", e.message, e)
            throw e
        }
    }
    
    /**
     * Envia um evento de notificação relacionado a pagamento
     */
    fun sendNotificationEvent(notificationData: Map<String, Any>) {
        try {
            val eventId = notificationData["eventId"] as String
            val eventType = notificationData["eventType"] as String
            logger.info("Enviando evento de notificação: id={}, tipo={}", eventId, eventType)
            
            val payload = objectMapper.writeValueAsString(notificationData)
            val aggregateId = eventId
            
            // Usando o sistema outbox para garantir a entrega
            outboxService.createEvent(
                aggregateId = aggregateId,
                aggregateType = "Notification", 
                eventType = eventType,
                topic = notificationEventsTopic,
                messageKey = aggregateId,
                payload = payload
            )
            
            logger.debug("Evento de notificação registrado no outbox: {}", eventId)
        } catch (e: Exception) {
            logger.error("Erro ao enviar evento de notificação: {}", e.message, e)
            throw e
        }
    }
    
    /**
     * Envia evento de pagamento completado através do outbox
     */
    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "fallbackSendEvent")
    @Retry(name = "kafkaProducer")
    fun sendPaymentCompletedEvent(payment: Payment) {
        val event = createPaymentEvent(payment, "COMPLETED")
        
        outboxService.createEvent(
            aggregateId = payment.id.toString(),
            aggregateType = "payment",
            eventType = "COMPLETED",
            topic = PAYMENT_COMPLETED_TOPIC,
            messageKey = payment.id.toString(),
            payload = event
        )
        
        logger.info("Evento de pagamento completado registrado no outbox: {}", payment.id)
    }
    
    /**
     * Envia evento de pagamento falho através do outbox
     */
    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "fallbackSendEvent")
    @Retry(name = "kafkaProducer")
    fun sendPaymentFailedEvent(payment: Payment) {
        val event = createPaymentEvent(payment, "FAILED")
        
        outboxService.createEvent(
            aggregateId = payment.id.toString(),
            aggregateType = "payment",
            eventType = "FAILED",
            topic = PAYMENT_FAILED_TOPIC,
            messageKey = payment.id.toString(),
            payload = event
        )
        
        logger.info("Evento de pagamento falho registrado no outbox: {}", payment.id)
    }
    
    /**
     * Envia evento de pagamento cancelado através do outbox
     */
    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "fallbackSendEvent")
    @Retry(name = "kafkaProducer")
    fun sendPaymentCanceledEvent(payment: Payment) {
        val event = createPaymentEvent(payment, "CANCELED")
        
        outboxService.createEvent(
            aggregateId = payment.id.toString(),
            aggregateType = "payment",
            eventType = "CANCELED",
            topic = PAYMENT_CANCELED_TOPIC,
            messageKey = payment.id.toString(),
            payload = event
        )
        
        logger.info("Evento de pagamento cancelado registrado no outbox: {}", payment.id)
    }
    
    /**
     * Envia evento de pagamento reembolsado através do outbox
     */
    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "fallbackSendEvent")
    @Retry(name = "kafkaProducer")
    fun sendPaymentRefundedEvent(payment: Payment) {
        val event = createPaymentEvent(payment, "REFUNDED")
        
        outboxService.createEvent(
            aggregateId = payment.id.toString(),
            aggregateType = "payment",
            eventType = "REFUNDED",
            topic = PAYMENT_REFUNDED_TOPIC,
            messageKey = payment.id.toString(),
            payload = event
        )
        
        logger.info("Evento de pagamento reembolsado registrado no outbox: {}", payment.id)
    }
    
    /**
     * Método de fallback para caso de falha no envio do evento
     */
    private fun fallbackSendEvent(payment: Payment, throwable: Throwable) {
        logger.error("Falha ao registrar evento de pagamento no outbox: {}", payment.id, throwable)
        // O padrão outbox já lida com a persistência e retry automático
    }
    
    /**
     * Cria o objeto de evento de pagamento
     */
    private fun createPaymentEvent(payment: Payment, eventType: String): PaymentEvent {
        return PaymentEvent(
            paymentId = payment.id,
            orderId = payment.orderId,
            userId = payment.userId,
            amount = payment.amount,
            currency = payment.currency,
            status = payment.status.name,
            eventType = eventType,
            timestamp = LocalDateTime.now()
        )
    }
    
    /**
     * Envia o evento para o Kafka
     */
    private fun sendEvent(topic: String, key: String, event: PaymentEvent) {
        try {
            val payload = objectMapper.writeValueAsString(event)
            kafkaTemplate.send(topic, key, payload).get()
        } catch (e: Exception) {
            logger.error("Erro ao serializar ou enviar evento: {}", e.message)
            throw e
        }
    }
} 