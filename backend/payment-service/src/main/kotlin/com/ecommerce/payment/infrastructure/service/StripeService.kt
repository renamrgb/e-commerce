package com.ecommerce.payment.infrastructure.service

import com.ecommerce.payment.domain.model.PaymentMethodType
import com.stripe.exception.StripeException
import com.stripe.model.PaymentIntent
import com.stripe.model.PaymentMethod
import com.stripe.param.PaymentIntentCreateParams
import com.stripe.param.PaymentMethodCreateParams
import org.springframework.stereotype.Service
import java.math.BigDecimal
import org.slf4j.LoggerFactory

@Service
class StripeService {
    
    private val logger = LoggerFactory.getLogger(StripeService::class.java)
    
    /**
     * Cria um token de método de pagamento no Stripe
     */
    fun createPaymentMethod(
        type: PaymentMethodType,
        cardNumber: String,
        expMonth: Int,
        expYear: Int,
        cvc: String,
        name: String
    ): PaymentMethod {
        try {
            val cardParams = PaymentMethodCreateParams.CardDetails.builder()
                .setNumber(cardNumber)
                .setExpMonth(expMonth.toLong())
                .setExpYear(expYear.toLong())
                .setCvc(cvc)
                .build()
            
            val params = PaymentMethodCreateParams.builder()
                .setType(PaymentMethodCreateParams.Type.CARD)
                .setCard(cardParams)
                .build()
            
            return PaymentMethod.create(params)
        } catch (e: StripeException) {
            logger.error("Erro ao criar método de pagamento no Stripe", e)
            throw RuntimeException("Falha ao processar cartão de crédito: ${e.message}")
        }
    }
    
    /**
     * Cria uma intenção de pagamento no Stripe
     */
    fun createPaymentIntent(
        amount: BigDecimal,
        currency: String,
        paymentMethodId: String,
        description: String,
        orderId: String
    ): PaymentIntent {
        try {
            // Convertendo para centavos (Stripe trabalha com a menor unidade monetária)
            val amountInCents = (amount.multiply(BigDecimal(100))).toLong()
            
            val params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(currency.toLowerCase())
                .setPaymentMethod(paymentMethodId)
                .setDescription(description)
                .putMetadata("orderId", orderId)
                .setConfirm(true)
                .build()
            
            return PaymentIntent.create(params)
        } catch (e: StripeException) {
            logger.error("Erro ao criar intenção de pagamento no Stripe", e)
            throw RuntimeException("Falha ao processar pagamento: ${e.message}")
        }
    }
    
    /**
     * Confirma um pagamento no Stripe
     */
    fun confirmPayment(paymentIntentId: String): PaymentIntent {
        try {
            val paymentIntent = PaymentIntent.retrieve(paymentIntentId)
            return paymentIntent.confirm()
        } catch (e: StripeException) {
            logger.error("Erro ao confirmar pagamento no Stripe", e)
            throw RuntimeException("Falha ao confirmar pagamento: ${e.message}")
        }
    }
    
    /**
     * Cancela um pagamento no Stripe
     */
    fun cancelPayment(paymentIntentId: String): PaymentIntent {
        try {
            val paymentIntent = PaymentIntent.retrieve(paymentIntentId)
            return paymentIntent.cancel()
        } catch (e: StripeException) {
            logger.error("Erro ao cancelar pagamento no Stripe", e)
            throw RuntimeException("Falha ao cancelar pagamento: ${e.message}")
        }
    }
} 