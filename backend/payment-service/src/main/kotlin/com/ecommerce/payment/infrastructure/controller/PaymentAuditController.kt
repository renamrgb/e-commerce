package com.ecommerce.payment.infrastructure.controller

import com.ecommerce.payment.application.dto.PaymentAuditDto
import com.ecommerce.payment.application.service.PaymentAuditService
import com.ecommerce.payment.domain.model.AuditSource
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Controlador para acesso aos dados de auditoria de pagamentos
 */
@RestController
@RequestMapping("/api/v1/admin/audits")
@Tag(name = "Auditoria", description = "Endpoints para gerenciamento e consulta de auditorias de pagamentos")
@PreAuthorize("hasRole('ADMIN')")
class PaymentAuditController(
    private val paymentAuditService: PaymentAuditService
) {

    private val logger = LoggerFactory.getLogger(PaymentAuditController::class.java)
    
    /**
     * Busca o histórico de auditoria de um pagamento específico
     */
    @GetMapping("/payment/{paymentId}")
    @Operation(
        summary = "Busca histórico de auditoria de um pagamento",
        description = "Retorna todas as mudanças de status registradas para um pagamento específico"
    )
    fun getPaymentAuditHistory(
        @PathVariable paymentId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PaymentAuditDto>> {
        logger.info("Buscando histórico de auditoria para pagamento: {}", paymentId)
        
        val pageable = PageRequest.of(page, size)
        val audits = paymentAuditService.getPaymentAuditHistory(paymentId, pageable)
        val auditDtos = audits.map { PaymentAuditDto.fromEntity(it) }
        
        return ResponseEntity.ok(auditDtos)
    }
    
    /**
     * Busca auditorias por período
     */
    @GetMapping("/period")
    @Operation(
        summary = "Busca auditorias por período",
        description = "Retorna todas as auditorias registradas no período especificado"
    )
    fun getAuditsByPeriod(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<List<PaymentAuditDto>> {
        logger.info("Buscando auditorias de {} até {}", startDate, endDate)
        
        val start = LocalDateTime.of(startDate, LocalTime.MIN)
        val end = LocalDateTime.of(endDate, LocalTime.MAX)
        
        val audits = paymentAuditService.getAuditsByDateRange(start, end)
        val auditDtos = audits.map { PaymentAuditDto.fromEntity(it) }
        
        return ResponseEntity.ok(auditDtos)
    }
    
    /**
     * Busca auditorias por origem
     */
    @GetMapping("/source/{source}")
    @Operation(
        summary = "Busca auditorias por origem",
        description = "Retorna auditorias filtradas pela origem da mudança (SYSTEM, WEBHOOK, USER, API ou SCHEDULED)"
    )
    fun getAuditsBySource(
        @PathVariable source: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PaymentAuditDto>> {
        logger.info("Buscando auditorias por origem: {}", source)
        
        try {
            val auditSource = AuditSource.valueOf(source.uppercase())
            val pageable = PageRequest.of(page, size)
            
            val audits = paymentAuditService.getAuditsBySource(auditSource, pageable)
            val auditDtos = audits.map { PaymentAuditDto.fromEntity(it) }
            
            return ResponseEntity.ok(auditDtos)
        } catch (ex: IllegalArgumentException) {
            logger.error("Origem de auditoria inválida: {}", source)
            return ResponseEntity.badRequest().build()
        }
    }
    
    /**
     * Busca auditorias por ator
     */
    @GetMapping("/actor/{actorId}")
    @Operation(
        summary = "Busca auditorias por ator",
        description = "Retorna auditorias filtradas pelo ID do usuário que realizou a ação"
    )
    fun getAuditsByActor(
        @PathVariable actorId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<PaymentAuditDto>> {
        logger.info("Buscando auditorias por ator: {}", actorId)
        
        val pageable = PageRequest.of(page, size)
        val audits = paymentAuditService.getAuditsByActor(actorId, pageable)
        val auditDtos = audits.map { PaymentAuditDto.fromEntity(it) }
        
        return ResponseEntity.ok(auditDtos)
    }
} 