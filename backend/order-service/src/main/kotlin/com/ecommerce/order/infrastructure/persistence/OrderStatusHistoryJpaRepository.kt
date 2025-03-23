package com.ecommerce.order.infrastructure.persistence

import com.ecommerce.order.domain.entity.OrderStatusHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OrderStatusHistoryJpaRepository : JpaRepository<OrderStatusHistory, UUID> {

    @Query("SELECT osh FROM OrderStatusHistory osh WHERE osh.order.id = :orderId ORDER BY osh.createdAt DESC")
    fun findByOrderIdOrderByCreatedAtDesc(@Param("orderId") orderId: UUID): List<OrderStatusHistory>
} 