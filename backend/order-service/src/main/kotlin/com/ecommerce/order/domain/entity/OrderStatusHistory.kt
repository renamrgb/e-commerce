package com.ecommerce.order.domain.entity

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Type
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.*

@Entity
@Table(name = "order_status_history")
class OrderStatusHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Type(type = "uuid-char")
    var id: UUID? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", nullable = false)
    val fromStatus: Order.OrderStatus,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    val toStatus: Order.OrderStatus,
    
    @Type(type = "uuid-char")
    @Column(name = "changed_by")
    val changedBy: UUID? = null,
    
    @Column(name = "comment")
    val comment: String? = null,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OrderStatusHistory

        if (id != null && other.id != null) {
            return id == other.id
        }

        if (order?.id != other.order?.id) return false
        if (fromStatus != other.fromStatus) return false
        if (toStatus != other.toStatus) return false
        if (createdAt != other.createdAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        if (result == 0) {
            result = 31 * result + (order?.id?.hashCode() ?: 0)
            result = 31 * result + fromStatus.hashCode()
            result = 31 * result + toStatus.hashCode()
            result = 31 * result + createdAt.hashCode()
        }
        return result
    }
} 