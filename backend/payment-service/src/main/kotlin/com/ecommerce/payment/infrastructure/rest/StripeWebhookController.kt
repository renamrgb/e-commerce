package com.ecommerce.payment.infrastructure.rest

import com.ecommerce.payment.application.service.WebhookService
import com.ecommerce.payment.infrastructure.config.StripeConfig
import com.stripe.exception.SignatureVerificationException
import com.stripe.model.Event
import com.stripe.net.Webhook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/webhooks/stripe")
class StripeWebhookController(
    private val webhookService: WebhookService,
    private val stripeConfig: StripeConfig
) {
    
    private val logger = LoggerFactory.getLogger(StripeWebhookController::class.java)
    
    @PostMapping
    fun handleStripeWebhook(
        @RequestBody payload: String,
        @RequestHeader("Stripe-Signature") sigHeader: String
    ): ResponseEntity<String> {
        logger.info("Recebido webhook do Stripe")
        
        try {
            // Verifica a assinatura do webhook para garantir que veio do Stripe
            val event = Webhook.constructEvent(
                payload, sigHeader, stripeConfig.webhookSecret
            )
            
            logger.info("Evento de webhook verificado: {}", event.type)
            
            // Delega o processamento para o serviço
            webhookService.processStripeEvent(event)
            
            return ResponseEntity.ok().body("Webhook recebido e processado com sucesso")
        } catch (e: SignatureVerificationException) {
            logger.error("Falha na verificação de assinatura do webhook: {}", e.message)
            return ResponseEntity.badRequest().body("Assinatura inválida")
        } catch (e: Exception) {
            logger.error("Erro ao processar webhook: {}", e.message)
            return ResponseEntity.status(500).body("Erro ao processar webhook")
        }
    }
} 