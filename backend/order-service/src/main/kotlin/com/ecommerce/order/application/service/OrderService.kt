package com.ecommerce.order.application.service

import com.ecommerce.order.domain.exception.OrderNotFoundException
import com.ecommerce.order.domain.model.Order
import com.ecommerce.order.domain.model.OrderItem
import com.ecommerce.order.domain.model.OrderStatusHistory
import com.ecommerce.order.domain.model.PaymentInfo
import com.ecommerce.order.domain.model.ShippingAddress
import com.ecommerce.order.domain.repository.OrderRepository
import com.ecommerce.order.infrastructure.grpc.CartGrpcClient
import com.ecommerce.order.infrastructure.grpc.CatalogGrpcClient
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

/**
 * Serviço para operações relacionadas a pedidos
 */
@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val cartGrpcClient: CartGrpcClient,
    private val catalogGrpcClient: CatalogGrpcClient
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val orderNumberCounter = AtomicInteger(1000)

    /**
     * Obtém um pedido pelo ID
     */
    @Transactional(readOnly = true)
    @Cacheable(value = ["orders"], key = "#id")
    fun findById(id: UUID): Order {
        logger.info("Buscando pedido por ID: $id")
        return orderRepository.findByIdWithItemsAndHistory(id)
            .orElseThrow { OrderNotFoundException("Pedido não encontrado com ID: $id") }
    }
    
    /**
     * Obtém um pedido pelo número
     */
    @Transactional(readOnly = true)
    fun findByOrderNumber(orderNumber: String): Order {
        logger.info("Buscando pedido por número: $orderNumber")
        return orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow { OrderNotFoundException("Pedido não encontrado com número: $orderNumber") }
    }
    
    /**
     * Obtém os pedidos de um usuário (paginados)
     */
    @Transactional(readOnly = true)
    fun findAllByUserId(userId: UUID, pageable: Pageable): Page<Order> {
        logger.info("Buscando pedidos do usuário: $userId")
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
    }
    
    /**
     * Cria um pedido a partir de um carrinho
     */
    @Transactional
    fun createFromCart(
        userId: UUID,
        cartId: UUID,
        shippingAddress: ShippingAddress,
        paymentInfo: PaymentInfo
    ): Order {
        logger.info("Criando pedido para o usuário: $userId, carrinho: $cartId")
        
        // Obter o carrinho do usuário
        val cart = cartGrpcClient.getCart(cartId)
            ?: throw IllegalArgumentException("Carrinho não encontrado: $cartId")
        
        // Verificar se o carrinho pertence ao usuário
        if (cart.userId != userId) {
            throw IllegalArgumentException("O carrinho não pertence ao usuário")
        }
        
        // Verificar se o carrinho tem itens
        if (cart.items.isEmpty()) {
            throw IllegalArgumentException("O carrinho está vazio")
        }
        
        // Gerar número do pedido
        val orderNumber = generateOrderNumber()
        
        // Calcular valores do pedido
        val subtotal = cart.subtotal
        val discount = cart.discount
        val shippingCost = calculateShippingCost(subtotal)
        val tax = calculateTax(subtotal.subtract(discount))
        val total = subtotal.subtract(discount).add(shippingCost).add(tax)
        
        // Criar o pedido
        val order = Order(
            id = UUID.randomUUID(),
            userId = userId,
            orderNumber = orderNumber,
            status = "PENDING",
            subtotal = subtotal,
            discount = discount,
            shippingCost = shippingCost,
            tax = tax,
            total = total,
            shippingAddress = shippingAddress,
            paymentInfo = paymentInfo,
            items = mutableListOf(),
            statusHistory = mutableListOf(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        // Adicionar itens ao pedido
        cart.items.forEach { cartItem ->
            val orderItem = OrderItem(
                id = UUID.randomUUID(),
                order = order,
                productId = cartItem.productId,
                productName = cartItem.productName,
                productSlug = cartItem.productSlug,
                productImage = cartItem.productImage,
                variantId = cartItem.variantId,
                variantName = cartItem.variantName,
                price = cartItem.price,
                quantity = cartItem.quantity,
                total = cartItem.total,
                createdAt = LocalDateTime.now()
            )
            order.items.add(orderItem)
        }
        
        // Adicionar entrada no histórico de status
        val statusHistory = OrderStatusHistory(
            id = UUID.randomUUID(),
            order = order,
            status = "PENDING",
            observation = "Pedido criado",
            createdAt = LocalDateTime.now()
        )
        order.statusHistory.add(statusHistory)
        
        // Salvar o pedido
        val savedOrder = orderRepository.save(order)
        
        // Limpar o carrinho após criar o pedido
        cartGrpcClient.clearCart(cartId)
        
        return savedOrder
    }

    /**
     * Atualiza o status de um pedido
     */
    @Transactional
    @CacheEvict(value = ["orders"], key = "#orderId")
    fun updateStatus(orderId: UUID, newStatus: String, observation: String? = null): Order {
        logger.info("Atualizando status do pedido: $orderId para: $newStatus")
        
        val order = orderRepository.findByIdWithItemsAndHistory(orderId)
            .orElseThrow { OrderNotFoundException("Pedido não encontrado com ID: $orderId") }
        
        // Validar o status
        if (!isValidStatus(newStatus)) {
            throw IllegalArgumentException("Status inválido: $newStatus")
        }
        
        // Atualizar o status
        order.status = newStatus
        order.updatedAt = LocalDateTime.now()
        
        // Adicionar entrada no histórico de status
        val statusHistory = OrderStatusHistory(
            id = UUID.randomUUID(),
            order = order,
            status = newStatus,
            observation = observation,
            createdAt = LocalDateTime.now()
        )
        order.statusHistory.add(statusHistory)
        
        return orderRepository.save(order)
    }
    
    /**
     * Cancela um pedido
     */
    @Transactional
    @CacheEvict(value = ["orders"], key = "#orderId")
    fun cancelOrder(orderId: UUID): Order {
        logger.info("Cancelando pedido: $orderId")
        
        val order = orderRepository.findByIdWithItemsAndHistory(orderId)
            .orElseThrow { OrderNotFoundException("Pedido não encontrado com ID: $orderId") }
        
        if (!canBeCanceled(order.status)) {
            throw IllegalStateException("Não é possível cancelar um pedido com status: ${order.status}")
        }
        
        return updateStatus(orderId, "CANCELED", "Cancelado pelo usuário")
    }
    
    /**
     * Obtém estatísticas dos pedidos de um usuário
     */
    @Transactional(readOnly = true)
    fun getUserOrderStats(userId: UUID): UserOrderStatsDto {
        logger.info("Obtendo estatísticas de pedidos para o usuário: $userId")
        
        val completedStatuses = listOf("DELIVERED")
        val cancelledStatuses = listOf("CANCELED")
        
        val totalOrders = orderRepository.countByUserId(userId)
        val completedOrders = orderRepository.countByUserIdAndStatusIn(userId, completedStatuses)
        val cancelledOrders = orderRepository.countByUserIdAndStatusIn(userId, cancelledStatuses)
        val totalSpent = orderRepository.sumTotalByUserIdAndStatusIn(userId, completedStatuses) ?: BigDecimal.ZERO
        val itemsPurchased = orderRepository.countItemsByUserIdAndStatusIn(userId, completedStatuses)
        
        return UserOrderStatsDto(
            totalOrders = totalOrders,
            completedOrders = completedOrders,
            cancelledOrders = cancelledOrders,
            totalSpent = totalSpent,
            itemsPurchased = itemsPurchased
        )
    }
    
    /**
     * Gera um número de pedido único
     */
    private fun generateOrderNumber(): String {
        val timestamp = System.currentTimeMillis().toString().takeLast(6)
        val sequence = orderNumberCounter.incrementAndGet()
        return "ORD-$timestamp-$sequence"
    }
    
    /**
     * Calcula o custo de envio baseado no valor do pedido
     */
    private fun calculateShippingCost(subtotal: BigDecimal): BigDecimal {
        return when {
            subtotal >= BigDecimal(200) -> BigDecimal.ZERO // Frete grátis para compras acima de R$ 200
            subtotal >= BigDecimal(100) -> BigDecimal("10.00") // R$ 10,00 para compras entre R$ 100 e R$ 200
            else -> BigDecimal("20.00") // R$ 20,00 para compras abaixo de R$ 100
        }
    }
    
    /**
     * Calcula o imposto baseado no valor do pedido
     */
    private fun calculateTax(baseAmount: BigDecimal): BigDecimal {
        // Lógica para calcular impostos (pode variar conforme a região)
        return baseAmount.multiply(BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP) // 10% de imposto
    }
    
    /**
     * Verifica se um status é válido
     */
    private fun isValidStatus(status: String): Boolean {
        return listOf(
            "PENDING", "PAID", "PREPARING", "SHIPPED", 
            "DELIVERED", "CANCELED", "REFUNDED"
        ).contains(status)
    }
    
    /**
     * Verifica se um pedido pode ser cancelado
     */
    private fun canBeCanceled(status: String): Boolean {
        return listOf("PENDING", "PAID", "PREPARING").contains(status)
    }
    
    /**
     * DTO para estatísticas de pedidos de um usuário
     */
    data class UserOrderStatsDto(
        val totalOrders: Long,
        val completedOrders: Long,
        val cancelledOrders: Long,
        val totalSpent: BigDecimal,
        val itemsPurchased: Long
    )
} 