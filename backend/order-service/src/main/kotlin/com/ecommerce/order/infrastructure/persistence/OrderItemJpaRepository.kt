package com.ecommerce.order.infrastructure.persistence

import com.ecommerce.order.domain.entity.OrderItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OrderItemJpaRepository : JpaRepository<OrderItem, UUID> {

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
    fun findByOrderId(@Param("orderId") orderId: UUID): List<OrderItem>
} 