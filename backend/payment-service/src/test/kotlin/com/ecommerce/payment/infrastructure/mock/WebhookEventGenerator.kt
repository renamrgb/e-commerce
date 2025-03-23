package com.ecommerce.payment.infrastructure.mock

import com.stripe.model.Event
import com.stripe.model.EventData
import com.stripe.model.EventDataObjectDeserializer
import com.stripe.model.StripeObject
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

/**
 * Componente responsável por gerar eventos de webhook simulados do Stripe
 * para serem usados em testes de integração
 */
@Component
class WebhookEventGenerator {
    
    private val logger = LoggerFactory.getLogger(WebhookEventGenerator::class.java)
    
    /**
     * Gera um evento de pagamento bem-sucedido
     */
    fun generatePaymentIntentSucceededEvent(
        paymentIntentId: String,
        amount: Long,
        currency: String,
        paymentMethodId: String,
        chargeId: String,
        orderId: String
    ): Event {
        logger.info("[MOCK] Gerando evento payment_intent.succeeded: paymentIntentId={}", paymentIntentId)
        
        // Cria um objeto JSON simulando a estrutura de dados do Stripe
        val jsonData = JSONObject()
            .put("id", paymentIntentId)
            .put("object", "payment_intent")
            .put("amount", amount)
            .put("currency", currency)
            .put("status", "succeeded")
            .put("payment_method", paymentMethodId)
            .put("latest_charge", chargeId)
            .put("metadata", JSONObject().put("order_id", orderId))
        
        return createEvent("payment_intent.succeeded", jsonData)
    }
    
    /**
     * Gera um evento de pagamento falho
     */
    fun generatePaymentIntentFailedEvent(
        paymentIntentId: String, 
        amount: Long,
        currency: String,
        paymentMethodId: String,
        errorMessage: String = "Your card was declined"
    ): Event {
        logger.info("[MOCK] Gerando evento payment_intent.payment_failed: paymentIntentId={}", paymentIntentId)
        
        // Cria o objeto de erro
        val errorJson = JSONObject()
            .put("type", "card_error")
            .put("code", "card_declined")
            .put("message", errorMessage)
            .put("decline_code", "generic_decline")
        
        // Cria o objeto JSON do payment intent
        val jsonData = JSONObject()
            .put("id", paymentIntentId)
            .put("object", "payment_intent")
            .put("amount", amount)
            .put("currency", currency)
            .put("status", "requires_payment_method")
            .put("payment_method", paymentMethodId)
            .put("last_payment_error", errorJson)
        
        return createEvent("payment_intent.payment_failed", jsonData)
    }
    
    /**
     * Gera um evento de pagamento cancelado
     */
    fun generatePaymentIntentCanceledEvent(
        paymentIntentId: String,
        amount: Long,
        currency: String
    ): Event {
        logger.info("[MOCK] Gerando evento payment_intent.canceled: paymentIntentId={}", paymentIntentId)
        
        val jsonData = JSONObject()
            .put("id", paymentIntentId)
            .put("object", "payment_intent")
            .put("amount", amount)
            .put("currency", currency)
            .put("status", "canceled")
        
        return createEvent("payment_intent.canceled", jsonData)
    }
    
    /**
     * Gera um evento de cobrança reembolsada
     */
    fun generateChargeRefundedEvent(
        chargeId: String,
        paymentIntentId: String,
        amount: Long,
        amountRefunded: Long,
        currency: String,
        refundId: String
    ): Event {
        logger.info("[MOCK] Gerando evento charge.refunded: chargeId={}", chargeId)
        
        // Cria o objeto refund
        val refundJson = JSONObject()
            .put("id", refundId)
            .put("object", "refund")
            .put("amount", amountRefunded)
            .put("charge", chargeId)
            .put("currency", currency)
            .put("status", "succeeded")
        
        // Cria a lista de reembolsos
        val refundsList = listOf(refundJson)
        
        // Cria o JSON da cobrança
        val jsonData = JSONObject()
            .put("id", chargeId)
            .put("object", "charge")
            .put("amount", amount)
            .put("amount_refunded", amountRefunded)
            .put("currency", currency)
            .put("payment_intent", paymentIntentId)
            .put("refunded", amount == amountRefunded)
            .put("refunds", JSONObject().put("data", refundsList))
        
        return createEvent("charge.refunded", jsonData)
    }
    
    /**
     * Cria um evento de webhook genérico
     */
    private fun createEvent(eventType: String, dataObject: JSONObject): Event {
        val eventId = "evt_" + UUID.randomUUID().toString().replace("-", "")
        val timestamp = Instant.now().epochSecond
        
        // Cria um evento simulado
        val event = Event()
        event.id = eventId
        event.type = eventType
        event.created = timestamp
        event.apiVersion = "2022-11-15"
        event.data = createEventData(dataObject)
        
        logger.info("[MOCK] Evento criado: id={}, type={}", eventId, eventType)
        
        return event
    }
    
    /**
     * Cria um objeto EventData simulado
     */
    private fun createEventData(dataObject: JSONObject): EventData {
        val mockEventData = MockEventData(dataObject)
        return mockEventData
    }
    
    /**
     * Implementação mock de EventData
     */
    class MockEventData(private val dataObject: JSONObject) : EventData() {
        
        override fun getObject(): EventDataObjectDeserializer {
            return MockEventDataObjectDeserializer(dataObject)
        }
    }
    
    /**
     * Implementação mock de EventDataObjectDeserializer
     */
    class MockEventDataObjectDeserializer(private val dataObject: JSONObject) : EventDataObjectDeserializer() {
        
        override fun getObject(): StripeObject? {
            return MockStripeObject(dataObject)
        }
        
        override fun deserialize(): StripeObject? {
            return getObject()
        }
    }
    
    /**
     * Objeto Stripe genérico para testes
     */
    class MockStripeObject(private val jsonObject: JSONObject) : StripeObject() {
        
        fun getJsonObject(): JSONObject = jsonObject
        
        override fun toString(): String {
            return jsonObject.toString()
        }
    }
} 