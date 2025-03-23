package com.ecommerce.payment.infrastructure.mock

import com.ecommerce.payment.infrastructure.service.StripeService
import com.stripe.model.Charge
import com.stripe.model.Event
import com.stripe.model.PaymentIntent
import com.stripe.model.Refund
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementação mock do serviço Stripe para testes
 * Simula o comportamento do Stripe sem fazer chamadas HTTP reais
 */
@Service
@Profile("test")
class StripeMockService : StripeService {
    
    private val logger = LoggerFactory.getLogger(StripeMockService::class.java)
    
    private val paymentIntents = ConcurrentHashMap<String, PaymentIntent>()
    private val charges = ConcurrentHashMap<String, Charge>()
    private val events = mutableListOf<Event>()
    private val refunds = ConcurrentHashMap<String, Refund>()
    
    /**
     * Cria um PaymentIntent simulado
     */
    override fun createPaymentIntent(
        amount: BigDecimal,
        currency: String,
        paymentMethodId: String,
        description: String,
        orderId: String
    ): PaymentIntent {
        logger.info("[MOCK] Criando PaymentIntent: amount={}, currency={}, orderId={}",
            amount, currency, orderId)
        
        val paymentIntentId = "pi_" + UUID.randomUUID().toString().replace("-", "")
        val amountInCents = amount.multiply(BigDecimal("100")).toLong()
        
        val paymentIntent = MockPaymentIntent(
            id = paymentIntentId,
            amount = amountInCents,
            currency = currency.lowercase(),
            paymentMethodId = paymentMethodId,
            description = description,
            metadata = mapOf("order_id" to orderId),
            status = "requires_confirmation"
        )
        
        paymentIntents[paymentIntentId] = paymentIntent
        
        logger.info("[MOCK] PaymentIntent criado: id={}, status={}", 
            paymentIntentId, paymentIntent.status)
        
        return paymentIntent
    }
    
    /**
     * Confirma um PaymentIntent simulado
     */
    override fun confirmPaymentIntent(paymentIntentId: String): PaymentIntent {
        logger.info("[MOCK] Confirmando PaymentIntent: id={}", paymentIntentId)
        
        val paymentIntent = paymentIntents[paymentIntentId] 
            ?: throw RuntimeException("PaymentIntent não encontrado: $paymentIntentId")
        
        // Atualiza o status para "succeeded"
        (paymentIntent as MockPaymentIntent).apply {
            status = "succeeded"
        }
        
        // Cria uma cobrança associada ao pagamento
        val chargeId = "ch_" + UUID.randomUUID().toString().replace("-", "")
        val charge = MockCharge(
            id = chargeId,
            amount = paymentIntent.amount,
            currency = paymentIntent.currency,
            paymentIntentId = paymentIntentId,
            status = "succeeded",
            refunded = false
        )
        
        charges[chargeId] = charge
        
        // Atualiza o payment intent com a referência da cobrança
        paymentIntent.latestCharge = chargeId
        
        logger.info("[MOCK] PaymentIntent confirmado: id={}, status={}, chargeId={}", 
            paymentIntentId, paymentIntent.status, chargeId)
        
        return paymentIntent
    }
    
    /**
     * Cancela um PaymentIntent simulado
     */
    override fun cancelPayment(paymentIntentId: String): PaymentIntent {
        logger.info("[MOCK] Cancelando PaymentIntent: id={}", paymentIntentId)
        
        val paymentIntent = paymentIntents[paymentIntentId]
            ?: throw RuntimeException("PaymentIntent não encontrado: $paymentIntentId")
        
        // Atualiza o status para "canceled"
        (paymentIntent as MockPaymentIntent).apply {
            status = "canceled"
        }
        
        logger.info("[MOCK] PaymentIntent cancelado: id={}, status={}", 
            paymentIntentId, paymentIntent.status)
        
        return paymentIntent
    }
    
    /**
     * Simula um pagamento falha
     */
    fun failPaymentIntent(paymentIntentId: String, errorMessage: String = "Pagamento recusado pela operadora"): PaymentIntent {
        logger.info("[MOCK] Fazendo PaymentIntent falhar: id={}", paymentIntentId)
        
        val paymentIntent = paymentIntents[paymentIntentId]
            ?: throw RuntimeException("PaymentIntent não encontrado: $paymentIntentId")
        
        // Atualiza o status para "failed"
        (paymentIntent as MockPaymentIntent).apply {
            status = "failed"
            lastPaymentError = errorMessage
        }
        
        logger.info("[MOCK] PaymentIntent falhou: id={}, status={}, erro={}",
            paymentIntentId, paymentIntent.status, errorMessage)
        
        return paymentIntent
    }
    
    /**
     * Reembolsa um pagamento simulado
     */
    override fun refundPayment(paymentIntentId: String, amount: BigDecimal?): Refund {
        logger.info("[MOCK] Reembolsando pagamento: paymentIntentId={}, amount={}", 
            paymentIntentId, amount)
        
        val paymentIntent = paymentIntents[paymentIntentId]
            ?: throw RuntimeException("PaymentIntent não encontrado: $paymentIntentId")
        
        val chargeId = paymentIntent.latestCharge
            ?: throw RuntimeException("Pagamento sem cobrança associada")
        
        val charge = charges[chargeId]
            ?: throw RuntimeException("Cobrança não encontrada: $chargeId")
        
        // Determina o valor do reembolso
        val refundAmount = amount?.multiply(BigDecimal("100"))?.toLong() ?: charge.amount
        
        // Cria o reembolso
        val refundId = "re_" + UUID.randomUUID().toString().replace("-", "")
        val refund = MockRefund(
            id = refundId,
            amount = refundAmount,
            chargeId = chargeId,
            currency = charge.currency,
            status = "succeeded"
        )
        
        refunds[refundId] = refund
        
        // Atualiza a cobrança como reembolsada
        (charge as MockCharge).apply {
            refunded = refundAmount >= this.amount
            amountRefunded = refundAmount
        }
        
        logger.info("[MOCK] Pagamento reembolsado: refundId={}, status={}", 
            refundId, refund.status)
        
        return refund
    }
    
    /**
     * Obtém um PaymentIntent simulado pelo ID
     */
    override fun getPaymentIntent(paymentIntentId: String): PaymentIntent {
        return paymentIntents[paymentIntentId]
            ?: throw RuntimeException("PaymentIntent não encontrado: $paymentIntentId")
    }
    
    /**
     * Obtém uma cobrança simulada pelo ID
     */
    override fun getCharge(chargeId: String): Charge {
        return charges[chargeId]
            ?: throw RuntimeException("Charge não encontrado: $chargeId")
    }
    
    /**
     * Limpa todos os dados mock
     */
    fun clearAllMockData() {
        paymentIntents.clear()
        charges.clear()
        refunds.clear()
        logger.info("[MOCK] Todos os dados mock foram limpos")
    }
    
    /**
     * Implementação mock da classe PaymentIntent do Stripe
     */
    class MockPaymentIntent(
        override val id: String,
        override val amount: Long,
        override val currency: String,
        val paymentMethodId: String,
        val description: String?,
        val metadata: Map<String, String>,
        var status: String,
        var lastPaymentError: String? = null,
        var latestCharge: String? = null
    ) : PaymentIntent() {
        
        override fun getStatus(): String = status
        
        override fun getLastPaymentError(): Any? = lastPaymentError?.let {
            object {
                val message = it
            }
        }
        
        override fun getLatestCharge(): String? = latestCharge
        
        override fun confirm(): PaymentIntent {
            status = "succeeded"
            return this
        }
        
        override fun cancel(): PaymentIntent {
            status = "canceled"
            return this
        }
    }
    
    /**
     * Implementação mock da classe Charge do Stripe
     */
    class MockCharge(
        override val id: String,
        override val amount: Long,
        override val currency: String,
        val paymentIntentId: String,
        var status: String,
        var refunded: Boolean,
        var amountRefunded: Long = 0
    ) : Charge() {
        
        override fun getStatus(): String = status
        
        override fun getRefunded(): Boolean = refunded
        
        override fun getAmountRefunded(): Long = amountRefunded
        
        override fun getPaymentIntent(): String = paymentIntentId
    }
    
    /**
     * Implementação mock da classe Refund do Stripe
     */
    class MockRefund(
        override val id: String,
        override val amount: Long,
        val chargeId: String,
        override val currency: String,
        var status: String
    ) : Refund() {
        
        override fun getStatus(): String = status
        
        override fun getCharge(): String = chargeId
    }
} 