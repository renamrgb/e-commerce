package com.ecommerce.payment.domain.repository

import com.ecommerce.payment.domain.model.OutboxEvent
import com.ecommerce.payment.domain.model.OutboxStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface OutboxEventRepository : JpaRepository<OutboxEvent, Long> {

    /**
     * Busca eventos pelo status
     */
    fun findByStatus(status: OutboxStatus): List<OutboxEvent>

    /**
     * Busca eventos pelo status com limite
     */
    fun findByStatusOrderByCreatedAtAsc(status: OutboxStatus, limit: Int): List<OutboxEvent>

    /**
     * Busca eventos com status de falha que ainda podem ser reprocessados
     */
    @Query("SELECT e FROM OutboxEvent e WHERE e.status = 'FAILED' AND e.retries < :maxRetries AND e.updatedAt < :cutoffTime ORDER BY e.updatedAt ASC")
    fun findFailedEventsForRetry(maxRetries: Int, cutoffTime: LocalDateTime): List<OutboxEvent>

    /**
     * Busca eventos pelo tipo da agregação e ID da agregação
     */
    fun findByAggregateTypeAndAggregateId(aggregateType: String, aggregateId: String): List<OutboxEvent>

    /**
     * Conta eventos pelo status
     */
    fun countByStatus(status: OutboxStatus): Long
} 