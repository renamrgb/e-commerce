package com.ecommerce.payment.infrastructure.service

import com.stripe.model.Charge
import com.stripe.model.PaymentIntent
import com.stripe.model.Refund
import java.math.BigDecimal

/**
 * Interface para serviços de integração com Stripe
 * Permite diferentes implementações (produção e teste)
 */
interface StripeService {
    
    /**
     * Cria um PaymentIntent no Stripe
     *
     * @param amount Valor do pagamento
     * @param currency Moeda (ex: "brl", "usd")
     * @param paymentMethodId ID do método de pagamento no Stripe
     * @param description Descrição do pagamento
     * @param orderId ID do pedido relacionado
     * @return PaymentIntent criado
     */
    fun createPaymentIntent(
        amount: BigDecimal,
        currency: String,
        paymentMethodId: String,
        description: String,
        orderId: String
    ): PaymentIntent
    
    /**
     * Confirma um PaymentIntent existente
     *
     * @param paymentIntentId ID do PaymentIntent
     * @return PaymentIntent confirmado
     */
    fun confirmPaymentIntent(paymentIntentId: String): PaymentIntent
    
    /**
     * Cancela um PaymentIntent
     *
     * @param paymentIntentId ID do PaymentIntent
     * @return PaymentIntent cancelado
     */
    fun cancelPayment(paymentIntentId: String): PaymentIntent
    
    /**
     * Reembolsa um pagamento
     *
     * @param paymentIntentId ID do PaymentIntent
     * @param amount Valor a ser reembolsado (null para reembolso total)
     * @return Objeto Refund criado
     */
    fun refundPayment(paymentIntentId: String, amount: BigDecimal? = null): Refund
    
    /**
     * Obtém um PaymentIntent pelo ID
     *
     * @param paymentIntentId ID do PaymentIntent
     * @return PaymentIntent encontrado
     */
    fun getPaymentIntent(paymentIntentId: String): PaymentIntent
    
    /**
     * Obtém uma cobrança pelo ID
     *
     * @param chargeId ID da cobrança
     * @return Objeto Charge encontrado
     */
    fun getCharge(chargeId: String): Charge
} 