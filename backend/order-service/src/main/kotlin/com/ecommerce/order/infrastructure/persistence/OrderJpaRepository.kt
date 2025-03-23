package com.ecommerce.order.infrastructure.persistence

import com.ecommerce.order.domain.entity.Order
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID

@Repository
interface OrderJpaRepository : JpaRepository<Order, UUID> {

    @EntityGraph(attributePaths = ["items"])
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    fun findByIdWithItems(@Param("id") id: UUID): Optional<Order>
    
    @EntityGraph(attributePaths = ["items", "statusHistory"])
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    fun findByIdWithItemsAndHistory(@Param("id") id: UUID): Optional<Order>
    
    @EntityGraph(attributePaths = ["items"])
    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    fun findByUserIdOrderByCreatedAtDesc(@Param("userId") userId: UUID, pageable: Pageable): Page<Order>
    
    @EntityGraph(attributePaths = ["items"])
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status = :status ORDER BY o.createdAt DESC")
    fun findByUserIdAndStatusOrderByCreatedAtDesc(
        @Param("userId") userId: UUID, 
        @Param("status") status: Order.OrderStatus, 
        pageable: Pageable
    ): Page<Order>
    
    @EntityGraph(attributePaths = ["items"])
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status IN :statuses ORDER BY o.createdAt DESC")
    fun findByUserIdAndStatusInOrderByCreatedAtDesc(
        @Param("userId") userId: UUID, 
        @Param("statuses") statuses: List<Order.OrderStatus>, 
        pageable: Pageable
    ): Page<Order>
    
    @Query("SELECT o FROM Order o WHERE o.orderNumber = :orderNumber")
    fun findByOrderNumber(@Param("orderNumber") orderNumber: String): Optional<Order>
    
    @EntityGraph(attributePaths = ["items"])
    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt DESC")
    fun findByStatusOrderByCreatedAtDesc(@Param("status") status: Order.OrderStatus, pageable: Pageable): Page<Order>
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId")
    fun countByUserId(@Param("userId") userId: UUID): Long
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId AND o.status IN :statuses")
    fun countByUserIdAndStatusIn(
        @Param("userId") userId: UUID, 
        @Param("statuses") statuses: List<Order.OrderStatus>
    ): Long
    
    @Query("SELECT SUM(o.total) FROM Order o WHERE o.userId = :userId AND o.status IN :statuses")
    fun sumTotalByUserIdAndStatusIn(
        @Param("userId") userId: UUID, 
        @Param("statuses") statuses: List<Order.OrderStatus>
    ): BigDecimal?
} 