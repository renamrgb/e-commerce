package com.ecommerce.order.domain.repository

import com.ecommerce.order.domain.entity.OrderItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface OrderItemRepository : JpaRepository<OrderItem, UUID> {

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
    fun findByOrderId(@Param("orderId") orderId: UUID): List<OrderItem>
    
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.productId = :productId AND oi.order.createdAt >= :since")
    fun countByProductIdAndOrderCreatedAtGreaterThanEqual(
        @Param("productId") productId: UUID, 
        @Param("since") since: LocalDateTime
    ): Long
    
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.productId = :productId AND oi.order.createdAt >= :since")
    fun sumQuantityByProductIdAndOrderCreatedAtGreaterThanEqual(
        @Param("productId") productId: UUID, 
        @Param("since") since: LocalDateTime
    ): Int?
} 