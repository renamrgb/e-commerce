package com.ecommerce.payment.infrastructure.outbox

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Agendador para processar eventos do outbox periodicamente
 */
@Component
class OutboxScheduler(private val outboxService: OutboxService) {
    
    private val logger = LoggerFactory.getLogger(OutboxScheduler::class.java)
    
    /**
     * Processa eventos pendentes a cada 5 segundos
     */
    @Scheduled(fixedDelayString = "\${outbox.scheduler.process-events-interval:5000}")
    fun processEvents() {
        logger.debug("Iniciando processamento de eventos pendentes no outbox")
        
        try {
            val batchSize = 50
            outboxService.processEvents(batchSize)
        } catch (e: Exception) {
            logger.error("Erro ao processar eventos do outbox", e)
        }
    }
    
    /**
     * Processa eventos falhos a cada 1 minuto
     */
    @Scheduled(fixedDelayString = "\${outbox.scheduler.retry-events-interval:60000}")
    fun processFailedEvents() {
        logger.debug("Iniciando reprocessamento de eventos falhos no outbox")
        
        try {
            val maxRetries = 5
            val retryDelayMinutes = 5
            outboxService.processFailedEvents(maxRetries, retryDelayMinutes)
        } catch (e: Exception) {
            logger.error("Erro ao reprocessar eventos falhos do outbox", e)
        }
    }
    
    /**
     * Reporta estatísticas do outbox a cada 5 minutos
     */
    @Scheduled(fixedDelayString = "\${outbox.scheduler.stats-interval:300000}")
    fun reportStats() {
        try {
            val stats = outboxService.getStats()
            
            logger.info("Estatísticas do Outbox: pendentes={}, processando={}, processados={}, falhos={}, total={}",
                stats.pendingCount, stats.processingCount, stats.processedCount, stats.failedCount, stats.totalCount)
            
        } catch (e: Exception) {
            logger.error("Erro ao obter estatísticas do outbox", e)
        }
    }
} 