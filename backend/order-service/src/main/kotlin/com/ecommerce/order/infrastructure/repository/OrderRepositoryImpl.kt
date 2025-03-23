package com.ecommerce.order.infrastructure.repository

import com.ecommerce.order.domain.model.Order
import com.ecommerce.order.domain.model.OrderItem
import com.ecommerce.order.domain.model.OrderStatusHistory
import com.ecommerce.order.domain.model.PaymentInfo
import com.ecommerce.order.domain.model.ShippingAddress
import com.ecommerce.order.domain.repository.OrderRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID
import com.ecommerce.order.infrastructure.persistence.OrderJpaRepository
import com.ecommerce.order.infrastructure.persistence.OrderItemJpaRepository
import com.ecommerce.order.infrastructure.persistence.OrderStatusHistoryJpaRepository
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.LocalDateTime

@Repository
class OrderRepositoryImpl(
    private val orderJpaRepository: OrderJpaRepository,
    private val orderItemJpaRepository: OrderItemJpaRepository,
    private val orderStatusHistoryJpaRepository: OrderStatusHistoryJpaRepository,
    private val objectMapper: ObjectMapper
) : OrderRepository {

    override fun save(order: Order): Order {
        // Converter o endereço e informações de pagamento para JSON
        val shippingAddressJson = objectMapper.writeValueAsString(order.shippingAddress)
        val paymentInfoJson = objectMapper.writeValueAsString(order.paymentInfo)

        // Criar a entidade JPA
        val orderEntity = com.ecommerce.order.domain.entity.Order(
            id = order.id,
            userId = order.userId,
            orderNumber = order.orderNumber,
            status = mapStatusToDomain(order.status),
            subtotal = order.subtotal,
            shippingCost = order.shippingCost,
            discount = order.discount,
            tax = order.tax,
            total = order.total,
            shippingAddress = shippingAddressJson,
            billingAddress = shippingAddressJson, // Usando o mesmo endereço para entrega e faturamento
            paymentMethod = order.paymentInfo.method,
            shippingMethod = "standard", // Valor padrão
            paymentId = order.paymentInfo.paymentId,
            createdAt = order.createdAt,
            updatedAt = order.updatedAt
        )

        // Salvar a entidade do pedido
        val savedOrderEntity = orderJpaRepository.save(orderEntity)

        // Salvar os itens do pedido
        val savedItems = order.items.map { item ->
            val itemEntity = com.ecommerce.order.domain.entity.OrderItem(
                id = item.id,
                order = savedOrderEntity,
                productId = item.productId,
                productName = item.productName,
                productSlug = item.productSlug,
                productImage = item.productImage,
                variantId = item.variantId,
                variantName = item.variantName,
                price = item.price,
                quantity = item.quantity,
                discount = BigDecimal.ZERO,
                total = item.total
            )
            orderItemJpaRepository.save(itemEntity)
        }

        // Salvar o histórico de status
        val savedHistory = order.statusHistory.map { history ->
            val historyEntity = com.ecommerce.order.domain.entity.OrderStatusHistory(
                id = history.id,
                order = savedOrderEntity,
                fromStatus = mapStatusToDomain(history.status),
                toStatus = mapStatusToDomain(history.status),
                comment = history.observation
            )
            orderStatusHistoryJpaRepository.save(historyEntity)
        }

        // Converter de volta para o modelo de domínio
        return mapToDomainModel(savedOrderEntity, savedItems, savedHistory)
    }

    override fun findById(id: UUID): Optional<Order> {
        return orderJpaRepository.findById(id).map { entity ->
            val items = orderItemJpaRepository.findByOrderId(id)
            val history = orderStatusHistoryJpaRepository.findByOrderIdOrderByCreatedAtDesc(id)
            mapToDomainModel(entity, items, history)
        }
    }

    override fun findByIdWithItemsAndHistory(id: UUID): Optional<Order> {
        return orderJpaRepository.findByIdWithItemsAndHistory(id).map { entity ->
            val items = entity.items.toList()
            val history = entity.statusHistory.toList()
            mapToDomainModel(entity, items, history)
        }
    }

    override fun findByOrderNumber(orderNumber: String): Optional<Order> {
        return orderJpaRepository.findByOrderNumber(orderNumber).map { entity ->
            val items = orderItemJpaRepository.findByOrderId(entity.id!!)
            val history = orderStatusHistoryJpaRepository.findByOrderIdOrderByCreatedAtDesc(entity.id!!)
            mapToDomainModel(entity, items, history)
        }
    }

    override fun findByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): Page<Order> {
        return orderJpaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).map { entity ->
            val items = entity.items.toList()
            val history = entity.statusHistory.toList()
            mapToDomainModel(entity, items, history)
        }
    }

    override fun findByUserIdAndStatusInOrderByCreatedAtDesc(userId: UUID, statuses: List<String>, pageable: Pageable): Page<Order> {
        val statusesEnum = statuses.map { mapStatusToDomain(it) }
        return orderJpaRepository.findByUserIdAndStatusInOrderByCreatedAtDesc(userId, statusesEnum, pageable).map { entity ->
            val items = entity.items.toList()
            val history = entity.statusHistory.toList()
            mapToDomainModel(entity, items, history)
        }
    }

    override fun findByStatusOrderByCreatedAtDesc(status: String, pageable: Pageable): Page<Order> {
        val statusEnum = mapStatusToDomain(status)
        return orderJpaRepository.findByStatusOrderByCreatedAtDesc(statusEnum, pageable).map { entity ->
            val items = entity.items.toList()
            val history = entity.statusHistory.toList()
            mapToDomainModel(entity, items, history)
        }
    }

    override fun countByUserId(userId: UUID): Long {
        return orderJpaRepository.countByUserId(userId)
    }

    override fun countByUserIdAndStatusIn(userId: UUID, statuses: List<String>): Long {
        val statusesEnum = statuses.map { mapStatusToDomain(it) }
        return orderJpaRepository.countByUserIdAndStatusIn(userId, statusesEnum)
    }

    override fun sumTotalByUserIdAndStatusIn(userId: UUID, statuses: List<String>): BigDecimal? {
        val statusesEnum = statuses.map { mapStatusToDomain(it) }
        return orderJpaRepository.sumTotalByUserIdAndStatusIn(userId, statusesEnum)
    }

    override fun countItemsByUserIdAndStatusIn(userId: UUID, statuses: List<String>): Long {
        val statusesEnum = statuses.map { mapStatusToDomain(it) }
        val orders = orderJpaRepository.findByUserIdAndStatusInOrderByCreatedAtDesc(userId, statusesEnum, Pageable.unpaged())
        var count = 0L
        orders.forEach { order ->
            count += order.items.sumOf { it.quantity }
        }
        return count
    }

    private fun mapToDomainModel(
        entity: com.ecommerce.order.domain.entity.Order,
        itemEntities: List<com.ecommerce.order.domain.entity.OrderItem>,
        historyEntities: List<com.ecommerce.order.domain.entity.OrderStatusHistory>
    ): Order {
        // Deserializar o endereço de entrega e informações de pagamento
        val shippingAddress = objectMapper.readValue(entity.shippingAddress, ShippingAddress::class.java)
        val paymentInfo = PaymentInfo(
            method = entity.paymentMethod,
            cardLastDigits = null, // Não temos essa informação na entidade
            installments = 1, // Valor padrão
            paymentId = entity.paymentId
        )

        // Criar o pedido de domínio
        val order = Order(
            id = entity.id!!,
            userId = entity.userId,
            orderNumber = entity.orderNumber,
            status = mapStatusToString(entity.status),
            subtotal = entity.subtotal,
            shippingCost = entity.shippingCost,
            discount = entity.discount,
            tax = entity.tax,
            total = entity.total,
            shippingAddress = shippingAddress,
            paymentInfo = paymentInfo,
            items = mutableListOf(),
            statusHistory = mutableListOf(),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )

        // Adicionar os itens do pedido
        val domainItems = itemEntities.map { itemEntity ->
            OrderItem(
                id = itemEntity.id!!,
                order = order,
                productId = itemEntity.productId,
                productName = itemEntity.productName,
                productSlug = itemEntity.productSlug,
                productImage = itemEntity.productImage,
                variantId = itemEntity.variantId,
                variantName = itemEntity.variantName,
                price = itemEntity.price,
                quantity = itemEntity.quantity,
                total = itemEntity.total,
                createdAt = itemEntity.createdAt
            )
        }
        order.items.addAll(domainItems)

        // Adicionar o histórico de status
        val domainHistory = historyEntities.map { historyEntity ->
            OrderStatusHistory(
                id = historyEntity.id!!,
                order = order,
                status = mapStatusToString(historyEntity.toStatus),
                observation = historyEntity.comment,
                createdAt = historyEntity.createdAt
            )
        }
        order.statusHistory.addAll(domainHistory)

        return order
    }

    private fun mapStatusToDomain(status: String): com.ecommerce.order.domain.entity.Order.OrderStatus {
        return try {
            com.ecommerce.order.domain.entity.Order.OrderStatus.valueOf(status)
        } catch (e: IllegalArgumentException) {
            // Status padrão caso não seja possível mapear
            com.ecommerce.order.domain.entity.Order.OrderStatus.PENDING
        }
    }

    private fun mapStatusToString(status: com.ecommerce.order.domain.entity.Order.OrderStatus): String {
        return status.name
    }
} 