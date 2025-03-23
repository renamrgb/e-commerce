package com.ecommerce.order.domain.repository

import com.ecommerce.order.domain.entity.Order
import com.ecommerce.order.domain.model.Order
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

@Repository
interface OrderJpaRepositoryOld : JpaRepository<Order, UUID> {

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
    
    @Query("SELECT o FROM Order o WHERE o.orderNumber = :orderNumber")
    fun findByOrderNumber(@Param("orderNumber") orderNumber: String): Optional<Order>
    
    @EntityGraph(attributePaths = ["items"])
    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt DESC")
    fun findByStatusOrderByCreatedAtDesc(@Param("status") status: Order.OrderStatus, pageable: Pageable): Page<Order>
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId AND o.createdAt >= :since")
    fun countByUserIdAndCreatedAtGreaterThanEqual(
        @Param("userId") userId: UUID, 
        @Param("since") since: LocalDateTime
    ): Long
    
    @Query("SELECT SUM(o.total) FROM Order o WHERE o.userId = :userId AND o.status IN :statuses AND o.createdAt >= :since")
    fun sumTotalByUserIdAndStatusInAndCreatedAtGreaterThanEqual(
        @Param("userId") userId: UUID, 
        @Param("statuses") statuses: List<Order.OrderStatus>, 
        @Param("since") since: LocalDateTime
    ): BigDecimal?
}

/**
 * Repositório para operações com pedidos no modelo de domínio
 */
interface OrderRepository {
    
    fun save(order: com.ecommerce.order.domain.model.Order): com.ecommerce.order.domain.model.Order
    
    fun findById(id: UUID): Optional<com.ecommerce.order.domain.model.Order>
    
    fun findByIdWithItemsAndHistory(id: UUID): Optional<com.ecommerce.order.domain.model.Order>
    
    fun findByOrderNumber(orderNumber: String): Optional<com.ecommerce.order.domain.model.Order>
    
    fun findByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): Page<com.ecommerce.order.domain.model.Order>
    
    fun findByUserIdAndStatusInOrderByCreatedAtDesc(userId: UUID, statuses: List<String>, pageable: Pageable): Page<com.ecommerce.order.domain.model.Order>
    
    fun findByStatusOrderByCreatedAtDesc(status: String, pageable: Pageable): Page<com.ecommerce.order.domain.model.Order>
    
    fun countByUserId(userId: UUID): Long
    
    fun countByUserIdAndStatusIn(userId: UUID, statuses: List<String>): Long
    
    fun sumTotalByUserIdAndStatusIn(userId: UUID, statuses: List<String>): BigDecimal?
    
    fun countItemsByUserIdAndStatusIn(userId: UUID, statuses: List<String>): Long
} 