package com.ecommerce.payment.application.service

import com.ecommerce.payment.domain.model.Payment
import com.ecommerce.payment.domain.model.PaymentStatus
import com.ecommerce.payment.infrastructure.kafka.PaymentEventProducer
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

/**
 * Serviço responsável por enviar notificações relacionadas a pagamentos
 */
@Service
class NotificationService(
    private val paymentEventProducer: PaymentEventProducer,
    private val objectMapper: ObjectMapper,
    @Value("\${app.notification.enabled:true}") private val notificationsEnabled: Boolean
) {
    private val logger = LoggerFactory.getLogger(NotificationService::class.java)
    
    /**
     * Envia uma notificação quando um pagamento é criado
     */
    @Async
    fun notifyPaymentCreated(payment: Payment) {
        if (!notificationsEnabled) return
        
        try {
            logger.info("Enviando notificação de pagamento criado: id={}, valor={}", 
                payment.id, payment.amount)
            
            val eventData = createNotificationEvent(
                payment = payment,
                eventType = "PAYMENT_CREATED",
                templateCode = "payment_created",
                subject = "Seu pagamento foi recebido"
            )
            
            paymentEventProducer.sendNotificationEvent(eventData)
        } catch (ex: Exception) {
            logger.error("Falha ao enviar notificação de pagamento criado: {}", ex.message)
        }
    }
    
    /**
     * Envia uma notificação quando um pagamento é completado com sucesso
     */
    @Async
    fun notifyPaymentCompleted(payment: Payment) {
        if (!notificationsEnabled) return
        
        try {
            logger.info("Enviando notificação de pagamento completado: id={}, valor={}", 
                payment.id, payment.amount)
            
            val eventData = createNotificationEvent(
                payment = payment,
                eventType = "PAYMENT_COMPLETED",
                templateCode = "payment_completed",
                subject = "Seu pagamento foi aprovado"
            )
            
            paymentEventProducer.sendNotificationEvent(eventData)
        } catch (ex: Exception) {
            logger.error("Falha ao enviar notificação de pagamento completado: {}", ex.message)
        }
    }
    
    /**
     * Envia uma notificação quando um pagamento falha
     */
    @Async
    fun notifyPaymentFailed(payment: Payment) {
        if (!notificationsEnabled) return
        
        try {
            logger.info("Enviando notificação de pagamento falhou: id={}, valor={}", 
                payment.id, payment.amount)
            
            val eventData = createNotificationEvent(
                payment = payment,
                eventType = "PAYMENT_FAILED",
                templateCode = "payment_failed",
                subject = "Problemas com seu pagamento"
            )
            
            paymentEventProducer.sendNotificationEvent(eventData)
        } catch (ex: Exception) {
            logger.error("Falha ao enviar notificação de pagamento falhou: {}", ex.message)
        }
    }
    
    /**
     * Envia uma notificação quando um pagamento é reembolsado
     */
    @Async
    fun notifyPaymentRefunded(payment: Payment) {
        if (!notificationsEnabled) return
        
        try {
            logger.info("Enviando notificação de pagamento reembolsado: id={}, valor={}", 
                payment.id, payment.amount)
            
            val eventData = createNotificationEvent(
                payment = payment,
                eventType = "PAYMENT_REFUNDED",
                templateCode = "payment_refunded",
                subject = "Seu reembolso foi processado"
            )
            
            paymentEventProducer.sendNotificationEvent(eventData)
        } catch (ex: Exception) {
            logger.error("Falha ao enviar notificação de pagamento reembolsado: {}", ex.message)
        }
    }
    
    /**
     * Envia uma notificação quando um pagamento é cancelado
     */
    @Async
    fun notifyPaymentCanceled(payment: Payment) {
        if (!notificationsEnabled) return
        
        try {
            logger.info("Enviando notificação de pagamento cancelado: id={}, valor={}", 
                payment.id, payment.amount)
            
            val eventData = createNotificationEvent(
                payment = payment,
                eventType = "PAYMENT_CANCELED",
                templateCode = "payment_canceled",
                subject = "Seu pagamento foi cancelado"
            )
            
            paymentEventProducer.sendNotificationEvent(eventData)
        } catch (ex: Exception) {
            logger.error("Falha ao enviar notificação de pagamento cancelado: {}", ex.message)
        }
    }
    
    /**
     * Cria o objeto de dados para o evento de notificação
     */
    private fun createNotificationEvent(
        payment: Payment,
        eventType: String,
        templateCode: String,
        subject: String
    ): Map<String, Any> {
        val templateData = mapOf(
            "paymentId" to payment.id,
            "orderId" to payment.orderId,
            "amount" to payment.amount.toString(),
            "status" to payment.status.name,
            "createdAt" to payment.createdAt.toString(),
            "userName" to payment.userName ?: "Cliente",
            "paymentMethod" to (payment.paymentMethod ?: "cartão de crédito")
        )
        
        return mapOf(
            "eventId" to UUID.randomUUID().toString(),
            "eventType" to eventType,
            "timestamp" to LocalDateTime.now().toString(),
            "recipientEmail" to payment.userEmail,
            "recipientPhone" to payment.userPhone,
            "templateCode" to templateCode,
            "subject" to subject,
            "templateData" to templateData
        )
    }
    
    /**
     * Envia uma notificação baseada no status do pagamento
     */
    @Async
    fun notifyByStatus(payment: Payment, previousStatus: PaymentStatus?) {
        when (payment.status) {
            PaymentStatus.PENDING -> if (previousStatus == null) notifyPaymentCreated(payment)
            PaymentStatus.COMPLETED -> notifyPaymentCompleted(payment)
            PaymentStatus.FAILED -> notifyPaymentFailed(payment)
            PaymentStatus.REFUNDED -> notifyPaymentRefunded(payment)
            PaymentStatus.CANCELED -> notifyPaymentCanceled(payment)
        }
    }
} 