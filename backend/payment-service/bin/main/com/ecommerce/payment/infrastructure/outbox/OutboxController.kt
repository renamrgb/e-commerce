package com.ecommerce.payment.infrastructure.outbox

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Controlador para monitoramento e gestão do outbox
 * Só deve ser acessível por administradores
 */
@RestController
@RequestMapping("/admin/outbox")
@PreAuthorize("hasRole('ADMIN')")
class OutboxController(private val outboxService: OutboxService) {
    
    /**
     * Obtém as estatísticas atuais do outbox
     */
    @GetMapping("/stats")
    fun getStats(): ResponseEntity<OutboxStats> {
        val stats = outboxService.getStats()
        return ResponseEntity.ok(stats)
    }
    
    /**
     * Força o processamento de eventos pendentes
     */
    @PostMapping("/process")
    fun processEvents(@RequestParam(defaultValue = "50") batchSize: Int): ResponseEntity<Map<String, Any>> {
        outboxService.processEvents(batchSize)
        
        val stats = outboxService.getStats()
        return ResponseEntity.ok(mapOf(
            "message" to "Processamento de eventos iniciado",
            "batchSize" to batchSize,
            "stats" to stats
        ))
    }
    
    /**
     * Força o reprocessamento de eventos falhos
     */
    @PostMapping("/retry")
    fun retryFailedEvents(
        @RequestParam(defaultValue = "5") maxRetries: Int,
        @RequestParam(defaultValue = "5") retryDelayMinutes: Int
    ): ResponseEntity<Map<String, Any>> {
        outboxService.processFailedEvents(maxRetries, retryDelayMinutes)
        
        val stats = outboxService.getStats()
        return ResponseEntity.ok(mapOf(
            "message" to "Reprocessamento de eventos falhos iniciado",
            "maxRetries" to maxRetries,
            "retryDelayMinutes" to retryDelayMinutes,
            "stats" to stats
        ))
    }
} 