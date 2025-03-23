package com.ecommerce.payment.infrastructure.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.DistributionSummary
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Componente responsável por registrar métricas relacionadas a pagamentos
 * Estas métricas serão expostas para o Prometheus para monitoramento
 */
@Component
class PaymentMetrics(private val meterRegistry: MeterRegistry) {

    // Timers para medir a duração de operações
    private val paymentIntentCreationTimer = Timer.builder("payment.intent.creation")
        .description("Tempo para criar um PaymentIntent no Stripe")
        .register(meterRegistry)
    
    private val paymentIntentConfirmationTimer = Timer.builder("payment.intent.confirmation")
        .description("Tempo para confirmar um PaymentIntent no Stripe")
        .register(meterRegistry)
    
    private val paymentCancellationTimer = Timer.builder("payment.cancellation")
        .description("Tempo para cancelar um pagamento no Stripe")
        .register(meterRegistry)
    
    private val paymentRefundTimer = Timer.builder("payment.refund")
        .description("Tempo para reembolsar um pagamento no Stripe")
        .register(meterRegistry)
    
    private val webhookProcessingTimer = Timer.builder("payment.webhook.processing")
        .description("Tempo para processar eventos de webhook do Stripe")
        .register(meterRegistry)
    
    // Contadores para eventos de pagamento
    fun incrementPaymentIntentCreation(status: String) {
        Counter.builder("payment.intent.created")
            .description("Número de PaymentIntents criados")
            .tag("status", status)
            .register(meterRegistry)
            .increment()
    }
    
    fun incrementPaymentIntentConfirmation(status: String) {
        Counter.builder("payment.intent.confirmed")
            .description("Número de PaymentIntents confirmados")
            .tag("status", status)
            .register(meterRegistry)
            .increment()
    }
    
    fun incrementPaymentCancellation(status: String) {
        Counter.builder("payment.cancelled")
            .description("Número de pagamentos cancelados")
            .tag("status", status)
            .register(meterRegistry)
            .increment()
    }
    
    fun incrementPaymentRefund(status: String) {
        Counter.builder("payment.refunded")
            .description("Número de pagamentos reembolsados")
            .tag("status", status)
            .register(meterRegistry)
            .increment()
    }
    
    fun incrementWebhookEvent(eventType: String, status: String) {
        Counter.builder("payment.webhook.event")
            .description("Número de eventos de webhook recebidos")
            .tag("event_type", eventType)
            .tag("status", status)
            .register(meterRegistry)
            .increment()
    }
    
    fun incrementCircuitBreakerFallback(operation: String) {
        Counter.builder("payment.circuit_breaker.fallback")
            .description("Número de vezes que fallbacks de circuit breaker foram acionados")
            .tag("operation", operation)
            .register(meterRegistry)
            .increment()
    }
    
    fun incrementOutboxEventProcessed(status: String) {
        Counter.builder("payment.outbox.event.processed")
            .description("Número de eventos outbox processados")
            .tag("status", status)
            .register(meterRegistry)
            .increment()
    }
    
    // Timers para registro de duração
    fun recordPaymentIntentCreationTime(duration: Duration) {
        paymentIntentCreationTimer.record(duration)
    }
    
    fun recordPaymentIntentConfirmationTime(duration: Duration) {
        paymentIntentConfirmationTimer.record(duration)
    }
    
    fun recordPaymentCancellationTime(duration: Duration) {
        paymentCancellationTimer.record(duration)
    }
    
    fun recordPaymentRefundTime(duration: Duration) {
        paymentRefundTimer.record(duration)
    }
    
    fun recordWebhookProcessingTime(duration: Duration) {
        webhookProcessingTimer.record(duration)
    }
    
    // Métricas para valores de pagamentos
    fun recordPaymentAmount(amount: Double, currency: String, status: String) {
        DistributionSummary.builder("payment.amount")
            .description("Distribuição de valores de pagamentos")
            .tag("currency", currency)
            .tag("status", status)
            .register(meterRegistry)
            .record(amount)
    }
    
    // Métricas para taxas de eventos do outbox
    fun gaugeOutboxQueueSize(size: Long) {
        meterRegistry.gauge("payment.outbox.queue.size", size)
    }
    
    // Métricas para erros de pagamento
    fun incrementPaymentErrors(errorType: String) {
        Counter.builder("payment.errors")
            .description("Contagem de erros de pagamento por tipo")
            .tag("error_type", errorType)
            .register(meterRegistry)
            .increment()
    }
} 