package com.ecommerce.order.domain.entity

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Type
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.*

@Entity
@Table(name = "orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Type(type = "uuid-char")
    var id: UUID? = null,
    
    @Type(type = "uuid-char")
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    
    @Column(name = "order_number", nullable = false, unique = true)
    val orderNumber: String,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: OrderStatus = OrderStatus.PENDING,
    
    @Column(name = "subtotal", nullable = false)
    val subtotal: BigDecimal,
    
    @Column(name = "shipping_cost", nullable = false)
    val shippingCost: BigDecimal,
    
    @Column(name = "discount", nullable = false)
    val discount: BigDecimal,
    
    @Column(name = "tax", nullable = false)
    val tax: BigDecimal,
    
    @Column(name = "total", nullable = false)
    val total: BigDecimal,
    
    @Column(name = "coupon_code")
    val couponCode: String? = null,
    
    @Column(name = "notes")
    var notes: String? = null,
    
    @Column(name = "tracking_code")
    var trackingCode: String? = null,
    
    @Column(name = "shipping_address", nullable = false)
    val shippingAddress: String,
    
    @Column(name = "billing_address", nullable = false)
    val billingAddress: String,
    
    @Column(name = "payment_method", nullable = false)
    val paymentMethod: String,
    
    @Column(name = "shipping_method", nullable = false)
    val shippingMethod: String,
    
    @Column(name = "payment_id")
    var paymentId: String? = null,
    
    @Column(name = "paid_at")
    var paidAt: LocalDateTime? = null,
    
    @Column(name = "shipped_at")
    var shippedAt: LocalDateTime? = null,
    
    @Column(name = "delivered_at")
    var deliveredAt: LocalDateTime? = null,
    
    @Column(name = "canceled_at")
    var canceledAt: LocalDateTime? = null,
    
    @Column(name = "refunded_at")
    var refundedAt: LocalDateTime? = null,
    
    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<OrderItem> = mutableListOf(),
    
    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    val statusHistory: MutableList<OrderStatusHistory> = mutableListOf(),
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    
    /**
     * Adiciona um item ao pedido
     */
    fun addItem(item: OrderItem) {
        items.add(item)
        item.order = this
    }
    
    /**
     * Atualiza o status do pedido e adiciona ao histórico
     */
    fun updateStatus(newStatus: OrderStatus, comment: String? = null) {
        // Não permitir voltar a um status anterior (exceto em casos específicos)
        if (status.ordinal > newStatus.ordinal && newStatus != OrderStatus.CANCELED && newStatus != OrderStatus.REFUNDED) {
            throw IllegalStateException("Não é possível voltar de ${status} para ${newStatus}")
        }
        
        val oldStatus = status
        status = newStatus
        updatedAt = LocalDateTime.now()
        
        // Registrar a mudança no histórico
        val statusChange = OrderStatusHistory(
            order = this,
            fromStatus = oldStatus,
            toStatus = newStatus,
            comment = comment
        )
        statusHistory.add(statusChange)
        
        // Atualizar timestamps específicos de status
        when (newStatus) {
            OrderStatus.PAID -> paidAt = LocalDateTime.now()
            OrderStatus.SHIPPED -> shippedAt = LocalDateTime.now()
            OrderStatus.DELIVERED -> deliveredAt = LocalDateTime.now()
            OrderStatus.CANCELED -> canceledAt = LocalDateTime.now()
            OrderStatus.REFUNDED -> refundedAt = LocalDateTime.now()
            else -> {} // Não fazer nada para outros status
        }
    }
    
    /**
     * Verifica se o pedido pode ser cancelado
     */
    fun canBeCanceled(): Boolean {
        return status == OrderStatus.PENDING || status == OrderStatus.PROCESSING
    }
    
    /**
     * Verifica se o pedido pode ser reembolsado
     */
    fun canBeRefunded(): Boolean {
        return status == OrderStatus.PAID || status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED
    }
    
    enum class OrderStatus {
        PENDING,        // Aguardando pagamento
        PROCESSING,     // Pagamento em processamento
        PAID,           // Pagamento confirmado
        PREPARING,      // Preparando para envio
        SHIPPED,        // Enviado
        DELIVERED,      // Entregue
        CANCELED,       // Cancelado
        REFUNDED        // Reembolsado
    }
} 