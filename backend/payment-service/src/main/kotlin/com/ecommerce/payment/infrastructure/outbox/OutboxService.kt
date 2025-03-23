package com.ecommerce.payment.infrastructure.outbox

import com.ecommerce.payment.domain.model.OutboxEvent
import com.ecommerce.payment.domain.model.OutboxStatus
import com.ecommerce.payment.domain.repository.OutboxEventRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Serviço para gerenciar a tabela de outbox e enviar eventos de forma confiável
 */
@Service
class OutboxService(
    private val outboxEventRepository: OutboxEventRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(OutboxService::class.java)
    
    /**
     * Cria um novo evento no outbox
     */
    @Transactional
    fun createEvent(
        aggregateId: String,
        aggregateType: String,
        eventType: String,
        topic: String,
        messageKey: String,
        payload: Any
    ): OutboxEvent {
        val serializedPayload = objectMapper.writeValueAsString(payload)
        
        logger.info("Criando evento no outbox: tipo=$eventType, agregação=$aggregateType:$aggregateId")
        
        val outboxEvent = OutboxEvent(
            aggregateId = aggregateId,
            aggregateType = aggregateType,
            eventType = eventType,
            topic = topic,
            messageKey = messageKey,
            payload = serializedPayload,
            status = OutboxStatus.PENDING,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        return outboxEventRepository.save(outboxEvent)
    }
    
    /**
     * Processa eventos pendentes no outbox (chamado por um scheduler)
     */
    @Transactional
    fun processEvents(batchSize: Int = 50) {
        logger.info("Processando lote de eventos do outbox, tamanho=$batchSize")
        
        val pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING, batchSize)
        
        for (event in pendingEvents) {
            try {
                // Marca como em processamento
                val processingEvent = updateEventStatus(event, OutboxStatus.PROCESSING)
                
                // Tenta enviar para o Kafka
                kafkaTemplate.send(event.topic, event.messageKey, event.payload).get()
                
                // Marca como processado
                val processedEvent = processingEvent.copy(
                    status = OutboxStatus.PROCESSED,
                    updatedAt = LocalDateTime.now(),
                    processedAt = LocalDateTime.now()
                )
                outboxEventRepository.save(processedEvent)
                
                logger.info("Evento processado com sucesso: id=${event.id}")
            } catch (e: Exception) {
                logger.error("Erro ao processar evento do outbox: id=${event.id}", e)
                
                val failedEvent = event.copy(
                    status = OutboxStatus.FAILED,
                    retries = event.retries + 1,
                    errorMessage = e.message ?: "Erro desconhecido ao enviar evento",
                    updatedAt = LocalDateTime.now()
                )
                outboxEventRepository.save(failedEvent)
            }
        }
    }
    
    /**
     * Processa eventos que falharam anteriormente
     */
    @Transactional
    fun processFailedEvents(maxRetries: Int = 5, retryDelayMinutes: Int = 5) {
        logger.info("Processando eventos do outbox que falharam, maxRetries=$maxRetries, delay=$retryDelayMinutes min")
        
        val cutoffTime = LocalDateTime.now().minusMinutes(retryDelayMinutes.toLong())
        val failedEvents = outboxEventRepository.findFailedEventsForRetry(maxRetries, cutoffTime)
        
        for (event in failedEvents) {
            try {
                // Marca como em processamento novamente
                val processingEvent = updateEventStatus(event, OutboxStatus.PROCESSING)
                
                // Tenta enviar para o Kafka novamente
                kafkaTemplate.send(event.topic, event.messageKey, event.payload).get()
                
                // Marca como processado
                val processedEvent = processingEvent.copy(
                    status = OutboxStatus.PROCESSED,
                    updatedAt = LocalDateTime.now(),
                    processedAt = LocalDateTime.now()
                )
                outboxEventRepository.save(processedEvent)
                
                logger.info("Evento reprocessado com sucesso: id=${event.id}, tentativas=${event.retries}")
            } catch (e: Exception) {
                logger.error("Erro ao reprocessar evento do outbox: id=${event.id}, tentativas=${event.retries}", e)
                
                val failedEvent = event.copy(
                    retries = event.retries + 1,
                    errorMessage = e.message ?: "Erro desconhecido ao enviar evento",
                    updatedAt = LocalDateTime.now()
                )
                outboxEventRepository.save(failedEvent)
            }
        }
    }
    
    /**
     * Atualiza o status de um evento
     */
    private fun updateEventStatus(event: OutboxEvent, status: OutboxStatus): OutboxEvent {
        val updatedEvent = event.copy(
            status = status,
            updatedAt = LocalDateTime.now()
        )
        return outboxEventRepository.save(updatedEvent)
    }
    
    /**
     * Obtém estatísticas do outbox
     */
    fun getStats(): OutboxStats {
        val pendingCount = outboxEventRepository.countByStatus(OutboxStatus.PENDING)
        val processingCount = outboxEventRepository.countByStatus(OutboxStatus.PROCESSING)
        val processedCount = outboxEventRepository.countByStatus(OutboxStatus.PROCESSED)
        val failedCount = outboxEventRepository.countByStatus(OutboxStatus.FAILED)
        
        return OutboxStats(
            pendingCount = pendingCount,
            processingCount = processingCount,
            processedCount = processedCount,
            failedCount = failedCount,
            totalCount = pendingCount + processingCount + processedCount + failedCount
        )
    }
}

/**
 * Estatísticas dos eventos no outbox
 */
data class OutboxStats(
    val pendingCount: Long,
    val processingCount: Long,
    val processedCount: Long,
    val failedCount: Long,
    val totalCount: Long
) 