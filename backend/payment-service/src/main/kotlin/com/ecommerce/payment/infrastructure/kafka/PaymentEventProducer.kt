package com.ecommerce.payment.infrastructure.kafka

import com.ecommerce.payment.domain.model.Payment
import com.ecommerce.payment.infrastructure.kafka.event.PaymentEvent
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class PaymentEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    
    private val logger = LoggerFactory.getLogger(PaymentEventProducer::class.java)
    
    companion object {
        const val PAYMENT_COMPLETED_TOPIC = "payment-service.payment.completed"
        const val PAYMENT_FAILED_TOPIC = "payment-service.payment.failed"
        const val PAYMENT_CANCELED_TOPIC = "payment-service.payment.canceled"
        const val PAYMENT_REFUNDED_TOPIC = "payment-service.payment.refunded"
    }
    
    /**
     * Envia evento de pagamento completado
     */
    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "fallbackSendEvent")
    @Retry(name = "kafkaProducer")
    fun sendPaymentCompletedEvent(payment: Payment) {
        val event = createPaymentEvent(payment, "COMPLETED")
        sendEvent(PAYMENT_COMPLETED_TOPIC, payment.id.toString(), event)
        logger.info("Evento de pagamento completado enviado: {}", payment.id)
    }
    
    /**
     * Envia evento de pagamento falho
     */
    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "fallbackSendEvent")
    @Retry(name = "kafkaProducer")
    fun sendPaymentFailedEvent(payment: Payment) {
        val event = createPaymentEvent(payment, "FAILED")
        sendEvent(PAYMENT_FAILED_TOPIC, payment.id.toString(), event)
        logger.info("Evento de pagamento falho enviado: {}", payment.id)
    }
    
    /**
     * Envia evento de pagamento cancelado
     */
    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "fallbackSendEvent")
    @Retry(name = "kafkaProducer")
    fun sendPaymentCanceledEvent(payment: Payment) {
        val event = createPaymentEvent(payment, "CANCELED")
        sendEvent(PAYMENT_CANCELED_TOPIC, payment.id.toString(), event)
        logger.info("Evento de pagamento cancelado enviado: {}", payment.id)
    }
    
    /**
     * Envia evento de pagamento reembolsado
     */
    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "fallbackSendEvent")
    @Retry(name = "kafkaProducer")
    fun sendPaymentRefundedEvent(payment: Payment) {
        val event = createPaymentEvent(payment, "REFUNDED")
        sendEvent(PAYMENT_REFUNDED_TOPIC, payment.id.toString(), event)
        logger.info("Evento de pagamento reembolsado enviado: {}", payment.id)
    }
    
    /**
     * Método de fallback para caso de falha no envio do evento
     */
    private fun fallbackSendEvent(payment: Payment, throwable: Throwable) {
        logger.error("Falha ao enviar evento de pagamento: {}", payment.id, throwable)
        // TODO: Implementar estratégia de fallback, como salvar em uma tabela de eventos pendentes
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