package com.ecommerce.payment.application.service

import com.ecommerce.payment.domain.model.PaymentStatus
import com.ecommerce.payment.domain.repository.PaymentRepository
import com.ecommerce.payment.infrastructure.kafka.PaymentEventProducer
import com.stripe.model.Event
import com.stripe.model.PaymentIntent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class WebhookService(
    private val paymentRepository: PaymentRepository,
    private val paymentEventProducer: PaymentEventProducer
) {
    
    private val logger = LoggerFactory.getLogger(WebhookService::class.java)
    
    /**
     * Processa eventos recebidos do Stripe
     */
    @Transactional
    fun processStripeEvent(event: Event) {
        logger.info("Processando evento do Stripe: {}", event.type)
        
        when (event.type) {
            "payment_intent.succeeded" -> handlePaymentIntentSucceeded(event)
            "payment_intent.payment_failed" -> handlePaymentIntentFailed(event)
            "payment_intent.canceled" -> handlePaymentIntentCanceled(event)
            "charge.refunded" -> handleChargeRefunded(event)
            else -> logger.info("Evento não processado: {}", event.type)
        }
    }
    
    /**
     * Processa um pagamento bem-sucedido
     */
    private fun handlePaymentIntentSucceeded(event: Event) {
        val paymentIntent = event.dataObjectDeserializer.deserializeObject<PaymentIntent>().get()
        val paymentIntentId = paymentIntent.id
        
        logger.info("Pagamento bem-sucedido: {}", paymentIntentId)
        
        // Busca o pagamento pelo ID do PaymentIntent
        val paymentOptional = paymentRepository.findByPaymentIntentId(paymentIntentId)
        
        if (paymentOptional.isPresent) {
            val payment = paymentOptional.get()
            
            // Atualiza o status do pagamento para COMPLETED
            val updatedPayment = payment.copy(
                status = PaymentStatus.COMPLETED,
                updatedAt = LocalDateTime.now(),
                completedAt = LocalDateTime.now()
            )
            
            paymentRepository.save(updatedPayment)
            
            // Envia evento para o Kafka
            paymentEventProducer.sendPaymentCompletedEvent(updatedPayment)
            
            logger.info("Pagamento atualizado para COMPLETED: {}", payment.id)
        } else {
            logger.warn("Pagamento não encontrado para PaymentIntent: {}", paymentIntentId)
        }
    }
    
    /**
     * Processa um pagamento falho
     */
    private fun handlePaymentIntentFailed(event: Event) {
        val paymentIntent = event.dataObjectDeserializer.deserializeObject<PaymentIntent>().get()
        val paymentIntentId = paymentIntent.id
        
        logger.info("Pagamento falhou: {}", paymentIntentId)
        
        // Busca o pagamento pelo ID do PaymentIntent
        val paymentOptional = paymentRepository.findByPaymentIntentId(paymentIntentId)
        
        if (paymentOptional.isPresent) {
            val payment = paymentOptional.get()
            
            // Obtém a mensagem de erro
            val errorMessage = paymentIntent.lastPaymentError?.message ?: "Falha no processamento do pagamento"
            
            // Atualiza o status do pagamento para FAILED
            val updatedPayment = payment.copy(
                status = PaymentStatus.FAILED,
                updatedAt = LocalDateTime.now(),
                errorMessage = errorMessage
            )
            
            paymentRepository.save(updatedPayment)
            
            // Envia evento para o Kafka
            paymentEventProducer.sendPaymentFailedEvent(updatedPayment)
            
            logger.info("Pagamento atualizado para FAILED: {}", payment.id)
        } else {
            logger.warn("Pagamento não encontrado para PaymentIntent: {}", paymentIntentId)
        }
    }
    
    /**
     * Processa um pagamento cancelado
     */
    private fun handlePaymentIntentCanceled(event: Event) {
        val paymentIntent = event.dataObjectDeserializer.deserializeObject<PaymentIntent>().get()
        val paymentIntentId = paymentIntent.id
        
        logger.info("Pagamento cancelado: {}", paymentIntentId)
        
        // Busca o pagamento pelo ID do PaymentIntent
        val paymentOptional = paymentRepository.findByPaymentIntentId(paymentIntentId)
        
        if (paymentOptional.isPresent) {
            val payment = paymentOptional.get()
            
            // Atualiza o status do pagamento para CANCELLED
            val updatedPayment = payment.copy(
                status = PaymentStatus.CANCELLED,
                updatedAt = LocalDateTime.now()
            )
            
            paymentRepository.save(updatedPayment)
            
            // Envia evento para o Kafka
            paymentEventProducer.sendPaymentCanceledEvent(updatedPayment)
            
            logger.info("Pagamento atualizado para CANCELLED: {}", payment.id)
        } else {
            logger.warn("Pagamento não encontrado para PaymentIntent: {}", paymentIntentId)
        }
    }
    
    /**
     * Processa um reembolso
     */
    private fun handleChargeRefunded(event: Event) {
        val charge = event.dataObjectDeserializer.deserializeObject<com.stripe.model.Charge>().get()
        val paymentIntentId = charge.paymentIntent
        
        logger.info("Pagamento reembolsado: {}", paymentIntentId)
        
        // Busca o pagamento pelo ID do PaymentIntent
        val paymentOptional = paymentRepository.findByPaymentIntentId(paymentIntentId)
        
        if (paymentOptional.isPresent) {
            val payment = paymentOptional.get()
            
            // Atualiza o status do pagamento para REFUNDED
            val updatedPayment = payment.copy(
                status = PaymentStatus.REFUNDED,
                updatedAt = LocalDateTime.now()
            )
            
            paymentRepository.save(updatedPayment)
            
            // Envia evento para o Kafka
            paymentEventProducer.sendPaymentRefundedEvent(updatedPayment)
            
            logger.info("Pagamento atualizado para REFUNDED: {}", payment.id)
        } else {
            logger.warn("Pagamento não encontrado para PaymentIntent: {}", paymentIntentId)
        }
    }
} 