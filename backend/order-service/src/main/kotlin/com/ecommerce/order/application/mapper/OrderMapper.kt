package com.ecommerce.order.application.mapper

import com.ecommerce.order.application.dto.OrderDto
import com.ecommerce.order.application.dto.OrderItemDto
import com.ecommerce.order.application.dto.OrderStatusHistoryDto
import com.ecommerce.order.application.dto.OrderSummaryDto
import com.ecommerce.order.domain.model.Order
import com.ecommerce.order.domain.model.OrderItem
import com.ecommerce.order.domain.model.OrderStatusHistory
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class OrderMapper(private val objectMapper: ObjectMapper) {
    
    /**
     * Converte uma entidade Order para um DTO
     */
    fun toDto(order: Order): OrderDto {
        // Converter endereço de entrega para representação de string (simplificada)
        val shippingAddressStr = "${order.shippingAddress.recipientName}, ${order.shippingAddress.street}, ${order.shippingAddress.number}, ${order.shippingAddress.city}, ${order.shippingAddress.state}, ${order.shippingAddress.zipCode}"
        
        // Usando o mesmo endereço para billing por enquanto
        val billingAddressStr = shippingAddressStr
        
        return OrderDto(
            id = order.id,
            userId = order.userId,
            orderNumber = order.orderNumber,
            status = order.status,
            subtotal = order.subtotal,
            shippingCost = order.shippingCost,
            discount = order.discount,
            tax = order.tax,
            total = order.total,
            couponCode = null, // Não temos esse campo no modelo de domínio
            notes = null, // Não temos esse campo no modelo de domínio
            trackingCode = null, // Não temos esse campo no modelo de domínio
            shippingAddress = shippingAddressStr,
            billingAddress = billingAddressStr,
            paymentMethod = order.paymentInfo.method,
            shippingMethod = "Standard", // Valor fixo por enquanto
            paymentId = order.paymentInfo.paymentId,
            paidAt = null, // Não temos esse campo no modelo de domínio
            shippedAt = null, // Não temos esse campo no modelo de domínio
            deliveredAt = null, // Não temos esse campo no modelo de domínio
            canceledAt = null, // Não temos esse campo no modelo de domínio
            refundedAt = null, // Não temos esse campo no modelo de domínio
            items = order.items.map { toDto(it) },
            statusHistory = order.statusHistory.map { toDto(it) },
            createdAt = order.createdAt,
            updatedAt = order.updatedAt
        )
    }
    
    /**
     * Converte um OrderItem para DTO
     */
    fun toDto(item: OrderItem): OrderItemDto {
        return OrderItemDto(
            id = item.id,
            productId = item.productId,
            productName = item.productName,
            productSlug = item.productSlug,
            productImage = item.productImage,
            variantId = item.variantId,
            variantName = item.variantName,
            price = item.price,
            quantity = item.quantity,
            discount = BigDecimal.ZERO, // Não temos esse campo no modelo de domínio
            total = item.total,
            options = null, // Não temos esse campo no modelo de domínio
            createdAt = item.createdAt
        )
    }
    
    /**
     * Converte um OrderStatusHistory para DTO
     */
    fun toDto(history: OrderStatusHistory): OrderStatusHistoryDto {
        return OrderStatusHistoryDto(
            id = history.id,
            fromStatus = history.status, // No modelo de domínio atual, não temos o from/to claramente definidos
            toStatus = history.status,
            changedBy = null, // Não temos esse campo no modelo de domínio
            comment = history.observation,
            createdAt = history.createdAt
        )
    }
    
    /**
     * Converte um Order para OrderSummaryDto (visão resumida)
     */
    fun toSummaryDto(order: Order): OrderSummaryDto {
        return OrderSummaryDto(
            id = order.id,
            orderNumber = order.orderNumber,
            status = order.status,
            total = order.total,
            itemCount = order.items.sumOf { it.quantity },
            createdAt = order.createdAt
        )
    }
} 