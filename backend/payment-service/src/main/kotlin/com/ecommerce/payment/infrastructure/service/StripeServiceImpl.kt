package com.ecommerce.payment.infrastructure.service

import com.ecommerce.payment.infrastructure.metrics.PaymentMetrics
import com.stripe.Stripe
import com.stripe.exception.StripeException
import com.stripe.model.Charge
import com.stripe.model.PaymentIntent
import com.stripe.model.Refund
import com.stripe.param.PaymentIntentConfirmParams
import com.stripe.param.PaymentIntentCreateParams
import com.stripe.param.RefundCreateParams
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.ratelimiter.annotation.RateLimiter
import io.github.resilience4j.retry.annotation.Retry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant

/**
 * Implementação do serviço Stripe para ambiente de produção
 */
@Service
@Profile("!test")
class StripeServiceImpl(
    @Value("\${stripe.api.key}") private val apiKey: String,
    private val paymentMetrics: PaymentMetrics
) : StripeService {
    
    private val logger = LoggerFactory.getLogger(StripeServiceImpl::class.java)
    
    init {
        Stripe.apiKey = apiKey
        logger.info("Inicializado serviço Stripe")
    }
    
    /**
     * Cria um PaymentIntent no Stripe
     */
    @CircuitBreaker(name = "stripeService", fallbackMethod = "createPaymentIntentFallback")
    @Retry(name = "stripeService")
    @RateLimiter(name = "stripeApi")
    override fun createPaymentIntent(
        amount: BigDecimal,
        currency: String,
        paymentMethodId: String,
        description: String,
        orderId: String
    ): PaymentIntent {
        logger.info("Criando PaymentIntent no Stripe: amount={}, currency={}, orderId={}",
            amount, currency, orderId)
        
        val startTime = Instant.now()
        
        try {
            // Converte valor para centavos (padrão do Stripe)
            val amountInCents = amount.multiply(BigDecimal("100")).toLong()
            
            val params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(currency.lowercase())
                .setPaymentMethod(paymentMethodId)
                .setDescription(description)
                .putMetadata("order_id", orderId)
                .setConfirm(true)
                .build()
            
            val paymentIntent = PaymentIntent.create(params)
            
            val duration = Duration.between(startTime, Instant.now())
            paymentMetrics.recordPaymentIntentCreationTime(duration)
            paymentMetrics.incrementPaymentIntentCreation("success")
            
            logger.info("PaymentIntent criado com sucesso: id={}, status={}",
                paymentIntent.id, paymentIntent.status)
            
            return paymentIntent
        } catch (e: StripeException) {
            logger.error("Erro ao criar PaymentIntent: {}", e.message, e)
            paymentMetrics.incrementPaymentIntentCreation("error")
            throw e
        }
    }
    
    /**
     * Método de fallback para criação de PaymentIntent (chamado pelo circuit breaker)
     */
    private fun createPaymentIntentFallback(
        amount: BigDecimal,
        currency: String,
        paymentMethodId: String,
        description: String,
        orderId: String,
        e: Exception
    ): PaymentIntent {
        logger.error("Fallback para criação de PaymentIntent: {}", e.message)
        paymentMetrics.incrementCircuitBreakerFallback("createPaymentIntent")
        throw RuntimeException("Serviço Stripe temporariamente indisponível. Por favor, tente novamente mais tarde.", e)
    }
    
    /**
     * Confirma um PaymentIntent existente
     */
    @CircuitBreaker(name = "stripeService", fallbackMethod = "confirmPaymentIntentFallback")
    @Retry(name = "stripeService")
    @RateLimiter(name = "stripeApi")
    override fun confirmPaymentIntent(paymentIntentId: String): PaymentIntent {
        logger.info("Confirmando PaymentIntent: id={}", paymentIntentId)
        
        val startTime = Instant.now()
        
        try {
            val paymentIntent = PaymentIntent.retrieve(paymentIntentId)
            
            val params = PaymentIntentConfirmParams.builder().build()
            val confirmedIntent = paymentIntent.confirm(params)
            
            val duration = Duration.between(startTime, Instant.now())
            paymentMetrics.recordPaymentIntentConfirmationTime(duration)
            paymentMetrics.incrementPaymentIntentConfirmation("success")
            
            logger.info("PaymentIntent confirmado com sucesso: id={}, status={}",
                confirmedIntent.id, confirmedIntent.status)
            
            return confirmedIntent
        } catch (e: StripeException) {
            logger.error("Erro ao confirmar PaymentIntent: {}", e.message, e)
            paymentMetrics.incrementPaymentIntentConfirmation("error")
            throw e
        }
    }
    
    /**
     * Método de fallback para confirmação de PaymentIntent
     */
    private fun confirmPaymentIntentFallback(paymentIntentId: String, e: Exception): PaymentIntent {
        logger.error("Fallback para confirmação de PaymentIntent: {}", e.message)
        paymentMetrics.incrementCircuitBreakerFallback("confirmPaymentIntent")
        throw RuntimeException("Serviço Stripe temporariamente indisponível. Por favor, tente novamente mais tarde.", e)
    }
    
    /**
     * Cancela um PaymentIntent
     */
    @CircuitBreaker(name = "stripeService", fallbackMethod = "cancelPaymentIntentFallback")
    @Retry(name = "stripeService")
    @RateLimiter(name = "stripeApi")
    override fun cancelPayment(paymentIntentId: String): PaymentIntent {
        logger.info("Cancelando PaymentIntent: id={}", paymentIntentId)
        
        val startTime = Instant.now()
        
        try {
            val paymentIntent = PaymentIntent.retrieve(paymentIntentId)
            val canceledIntent = paymentIntent.cancel()
            
            val duration = Duration.between(startTime, Instant.now())
            paymentMetrics.recordPaymentCancellationTime(duration)
            paymentMetrics.incrementPaymentCancellation("success")
            
            logger.info("PaymentIntent cancelado com sucesso: id={}, status={}",
                canceledIntent.id, canceledIntent.status)
            
            return canceledIntent
        } catch (e: StripeException) {
            logger.error("Erro ao cancelar PaymentIntent: {}", e.message, e)
            paymentMetrics.incrementPaymentCancellation("error")
            throw e
        }
    }
    
    /**
     * Método de fallback para cancelamento de PaymentIntent
     */
    private fun cancelPaymentIntentFallback(paymentIntentId: String, e: Exception): PaymentIntent {
        logger.error("Fallback para cancelamento de PaymentIntent: {}", e.message)
        paymentMetrics.incrementCircuitBreakerFallback("cancelPaymentIntent")
        throw RuntimeException("Serviço Stripe temporariamente indisponível. Por favor, tente novamente mais tarde.", e)
    }
    
    /**
     * Reembolsa um pagamento
     */
    @CircuitBreaker(name = "stripeService", fallbackMethod = "refundPaymentFallback")
    @Retry(name = "stripeService")
    @RateLimiter(name = "stripeApi")
    override fun refundPayment(paymentIntentId: String, amount: BigDecimal?): Refund {
        logger.info("Reembolsando pagamento: paymentIntentId={}, amount={}", paymentIntentId, amount)
        
        val startTime = Instant.now()
        
        try {
            val paymentIntent = PaymentIntent.retrieve(paymentIntentId)
            val chargeId = paymentIntent.latestCharge
            
            if (chargeId == null) {
                throw IllegalStateException("Pagamento sem cobrança associada")
            }
            
            val paramsBuilder = RefundCreateParams.builder()
                .setCharge(chargeId)
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
            
            // Se o valor é especificado, reembolsa parcialmente
            if (amount != null) {
                val amountInCents = amount.multiply(BigDecimal("100")).toLong()
                paramsBuilder.setAmount(amountInCents)
            }
            
            val refund = Refund.create(paramsBuilder.build())
            
            val duration = Duration.between(startTime, Instant.now())
            paymentMetrics.recordPaymentRefundTime(duration)
            paymentMetrics.incrementPaymentRefund("success")
            
            logger.info("Pagamento reembolsado com sucesso: refundId={}, status={}",
                refund.id, refund.status)
            
            return refund
        } catch (e: StripeException) {
            logger.error("Erro ao reembolsar pagamento: {}", e.message, e)
            paymentMetrics.incrementPaymentRefund("error")
            throw e
        }
    }
    
    /**
     * Método de fallback para reembolso de pagamento
     */
    private fun refundPaymentFallback(paymentIntentId: String, amount: BigDecimal?, e: Exception): Refund {
        logger.error("Fallback para reembolso de pagamento: {}", e.message)
        paymentMetrics.incrementCircuitBreakerFallback("refundPayment")
        throw RuntimeException("Serviço Stripe temporariamente indisponível. Por favor, tente novamente mais tarde.", e)
    }
    
    /**
     * Obtém um PaymentIntent pelo ID
     */
    @CircuitBreaker(name = "stripeService")
    @Retry(name = "stripeService")
    @RateLimiter(name = "stripeApi")
    override fun getPaymentIntent(paymentIntentId: String): PaymentIntent {
        logger.info("Recuperando PaymentIntent: id={}", paymentIntentId)
        
        try {
            return PaymentIntent.retrieve(paymentIntentId)
        } catch (e: StripeException) {
            logger.error("Erro ao recuperar PaymentIntent: {}", e.message, e)
            throw e
        }
    }
    
    /**
     * Obtém uma cobrança pelo ID
     */
    @CircuitBreaker(name = "stripeService")
    @Retry(name = "stripeService")
    @RateLimiter(name = "stripeApi")
    override fun getCharge(chargeId: String): Charge {
        logger.info("Recuperando Charge: id={}", chargeId)
        
        try {
            return Charge.retrieve(chargeId)
        } catch (e: StripeException) {
            logger.error("Erro ao recuperar Charge: {}", e.message, e)
            throw e
        }
    }
} 