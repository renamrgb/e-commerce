package com.ecommerce.payment.infrastructure.controller

import com.ecommerce.payment.application.dto.PaymentDto
import com.ecommerce.payment.application.dto.PaymentStatisticsDto
import com.ecommerce.payment.application.service.PaymentService
import com.ecommerce.payment.domain.model.PaymentStatus
import com.ecommerce.payment.infrastructure.outbox.OutboxService
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Controlador para o dashboard administrativo de pagamentos
 * Fornece endpoints para consultar estatísticas e gerenciar pagamentos
 */
@RestController
@RequestMapping("/api/v1/admin/payments")
class AdminDashboardController(
    private val paymentService: PaymentService,
    private val outboxService: OutboxService
) {
    
    private val logger = LoggerFactory.getLogger(AdminDashboardController::class.java)
    
    /**
     * Retorna estatísticas de pagamentos por período
     */
    @GetMapping("/statistics")
    fun getPaymentStatistics(
        @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<PaymentStatisticsDto> {
        
        logger.info("Obtendo estatísticas de pagamentos de {} até {}", startDate, endDate)
        
        val start = LocalDateTime.of(startDate, LocalTime.MIN)
        val end = LocalDateTime.of(endDate, LocalTime.MAX)
        
        val statistics = PaymentStatisticsDto(
            totalTransactions = paymentService.countPaymentsByPeriod(start, end),
            totalAmount = paymentService.sumPaymentAmountByStatusAndPeriod(null, start, end),
            successfulTransactions = paymentService.countPaymentsByStatusAndPeriod(PaymentStatus.COMPLETED, start, end),
            successfulAmount = paymentService.sumPaymentAmountByStatusAndPeriod(PaymentStatus.COMPLETED, start, end),
            failedTransactions = paymentService.countPaymentsByStatusAndPeriod(PaymentStatus.FAILED, start, end),
            canceledTransactions = paymentService.countPaymentsByStatusAndPeriod(PaymentStatus.CANCELED, start, end),
            refundedTransactions = paymentService.countPaymentsByStatusAndPeriod(PaymentStatus.REFUNDED, start, end),
            refundedAmount = paymentService.sumPaymentAmountByStatusAndPeriod(PaymentStatus.REFUNDED, start, end),
            pendingTransactions = paymentService.countPaymentsByStatusAndPeriod(PaymentStatus.PENDING, start, end),
            pendingAmount = paymentService.sumPaymentAmountByStatusAndPeriod(PaymentStatus.PENDING, start, end),
            period = "${startDate.toString()} - ${endDate.toString()}"
        )
        
        return ResponseEntity.ok(statistics)
    }
    
    /**
     * Retorna estatísticas do sistema de mensageria outbox
     */
    @GetMapping("/outbox/statistics")
    fun getOutboxStatistics(): ResponseEntity<Map<String, Any>> {
        val pendingEvents = outboxService.countEventsByStatus("PENDING")
        val processingEvents = outboxService.countEventsByStatus("PROCESSING")
        val completedEvents = outboxService.countEventsByStatus("COMPLETED")
        val failedEvents = outboxService.countEventsByStatus("ERROR")
        
        val statistics = mapOf(
            "pendingEvents" to pendingEvents,
            "processingEvents" to processingEvents,
            "completedEvents" to completedEvents,
            "failedEvents" to failedEvents,
            "totalEvents" to (pendingEvents + processingEvents + completedEvents + failedEvents)
        )
        
        return ResponseEntity.ok(statistics)
    }
    
    /**
     * Busca pagamentos por critérios
     */
    @GetMapping
    fun findPayments(
        @RequestParam(required = false) orderId: String?,
        @RequestParam(required = false) userId: String?,
        @RequestParam(required = false) status: PaymentStatus?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<PaymentDto>> {
        
        val start = startDate?.let { LocalDateTime.of(it, LocalTime.MIN) }
        val end = endDate?.let { LocalDateTime.of(it, LocalTime.MAX) }
        
        val payments = paymentService.findPayments(orderId, userId, status, start, end, page, size)
        
        return ResponseEntity.ok(payments)
    }
    
    /**
     * Obtém detalhes de um pagamento específico
     */
    @GetMapping("/{paymentId}")
    fun getPaymentDetails(@PathVariable paymentId: String): ResponseEntity<PaymentDto> {
        val payment = paymentService.findPaymentById(paymentId)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(payment)
    }
    
    /**
     * Solicita o reembolso de um pagamento
     */
    @PostMapping("/{paymentId}/refund")
    fun refundPayment(
        @PathVariable paymentId: String,
        @RequestParam(required = false) amount: BigDecimal?
    ): ResponseEntity<PaymentDto> {
        logger.info("Solicitando reembolso para pagamento {}, valor: {}", paymentId, amount)
        
        val refundedPayment = paymentService.refundPayment(paymentId, amount)
        
        return ResponseEntity.ok(refundedPayment)
    }
    
    /**
     * Cancela um pagamento pendente
     */
    @PostMapping("/{paymentId}/cancel")
    fun cancelPayment(@PathVariable paymentId: String): ResponseEntity<PaymentDto> {
        logger.info("Cancelando pagamento {}", paymentId)
        
        val canceledPayment = paymentService.cancelPayment(paymentId)
        
        return ResponseEntity.ok(canceledPayment)
    }
    
    /**
     * Força o reprocessamento de um evento do outbox
     */
    @PostMapping("/outbox/{eventId}/process")
    fun processOutboxEvent(@PathVariable eventId: String): ResponseEntity<Map<String, Any>> {
        logger.info("Forçando processamento do evento outbox {}", eventId)
        
        val result = outboxService.processEvent(eventId)
        
        return ResponseEntity.ok(mapOf(
            "eventId" to eventId,
            "processed" to result,
            "timestamp" to LocalDateTime.now().toString()
        ))
    }
    
    /**
     * Força o reprocessamento de eventos do outbox em status de erro
     */
    @PostMapping("/outbox/retry-failed")
    fun retryFailedOutboxEvents(): ResponseEntity<Map<String, Any>> {
        logger.info("Forçando reprocessamento de eventos outbox com falha")
        
        val processedCount = outboxService.retryFailedEvents()
        
        return ResponseEntity.ok(mapOf(
            "processedEvents" to processedCount,
            "timestamp" to LocalDateTime.now().toString()
        ))
    }
} 