package com.ecommerce.order.infrastructure.rest

import com.ecommerce.order.application.dto.OrderCreateRequest
import com.ecommerce.order.application.dto.OrderDto
import com.ecommerce.order.application.dto.OrderSummaryDto
import com.ecommerce.order.application.dto.StatusUpdateRequest
import com.ecommerce.order.application.mapper.OrderMapper
import com.ecommerce.order.application.service.OrderService
import com.ecommerce.order.domain.model.PaymentInfo
import com.ecommerce.order.domain.model.ShippingAddress
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Pedidos", description = "API para gerenciamento de pedidos")
@SecurityRequirement(name = "bearerAuth")
class OrderController(
    private val orderService: OrderService,
    private val orderMapper: OrderMapper
) {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    
    @GetMapping("/me")
    @Operation(
        summary = "Lista os pedidos do usuário autenticado",
        description = "Retorna uma lista paginada dos pedidos do usuário autenticado"
    )
    fun getMyOrders(
        @AuthenticationPrincipal userId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Page<OrderSummaryDto> {
        logger.info("Obtendo pedidos do usuário: $userId")
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val orders = orderService.findAllByUserId(UUID.fromString(userId), pageable)
        return orders.map { orderMapper.toSummaryDto(it) }
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtém um pedido por ID",
        description = "Retorna os detalhes de um pedido específico"
    )
    fun getOrderById(
        @PathVariable id: UUID,
        @AuthenticationPrincipal userId: String
    ): ResponseEntity<OrderDto> {
        logger.info("Obtendo pedido: $id para usuário: $userId")
        val order = orderService.findById(id)
        
        // Verificar se o pedido pertence ao usuário autenticado
        if (order.userId.toString() != userId && !hasAdminRole()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        
        return ResponseEntity.ok(orderMapper.toDto(order))
    }
    
    @PostMapping
    @Operation(
        summary = "Cria um novo pedido",
        description = "Cria um novo pedido a partir do carrinho do usuário"
    )
    fun createOrder(
        @Valid @RequestBody request: OrderCreateRequest,
        @AuthenticationPrincipal userId: String
    ): ResponseEntity<OrderDto> {
        logger.info("Criando pedido para usuário: $userId, carrinho: ${request.cartId}")
        
        // Preparar o endereço de entrega
        val shippingAddress = ShippingAddress(
            recipientName = request.shippingAddress.split(",")[0].trim(),
            street = request.shippingAddress.split(",")[1].trim(),
            number = request.shippingAddress.split(",")[2].trim(),
            complement = null,
            neighborhood = "",
            city = request.shippingAddress.split(",")[3].trim(),
            state = request.shippingAddress.split(",")[4].trim(),
            zipCode = request.shippingAddress.split(",")[5].trim(),
            country = "Brasil",
            phone = ""
        )
        
        // Preparar as informações de pagamento
        val paymentInfo = PaymentInfo(
            method = request.paymentMethod,
            cardLastDigits = null,
            installments = 1,
            paymentId = request.paymentIntentId
        )
        
        // Criar o pedido
        val order = orderService.createFromCart(
            UUID.fromString(userId),
            request.cartId,
            shippingAddress,
            paymentInfo
        )
        
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toDto(order))
    }
    
    @PutMapping("/{id}/status")
    @Operation(
        summary = "Atualiza o status de um pedido",
        description = "Atualiza o status de um pedido existente"
    )
    @PreAuthorize("hasRole('ADMIN')")
    fun updateOrderStatus(
        @PathVariable id: UUID,
        @Valid @RequestBody request: StatusUpdateRequest
    ): ResponseEntity<OrderDto> {
        logger.info("Atualizando status do pedido: $id para: ${request.status}")
        val order = orderService.updateStatus(id, request.status, request.comment)
        return ResponseEntity.ok(orderMapper.toDto(order))
    }
    
    @PostMapping("/{id}/cancel")
    @Operation(
        summary = "Cancela um pedido",
        description = "Cancela um pedido existente se estiver em um status que permite cancelamento"
    )
    fun cancelOrder(
        @PathVariable id: UUID,
        @AuthenticationPrincipal userId: String
    ): ResponseEntity<OrderDto> {
        logger.info("Cancelando pedido: $id para usuário: $userId")
        
        // Verificar se o pedido pertence ao usuário autenticado
        val order = orderService.findById(id)
        if (order.userId.toString() != userId && !hasAdminRole()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        
        // Cancelar o pedido
        val canceledOrder = orderService.cancelOrder(id)
        return ResponseEntity.ok(orderMapper.toDto(canceledOrder))
    }
    
    @GetMapping("/stats")
    @Operation(
        summary = "Obtém estatísticas dos pedidos do usuário",
        description = "Retorna estatísticas como total de pedidos, total gasto, etc."
    )
    fun getUserOrderStats(
        @AuthenticationPrincipal userId: String
    ): ResponseEntity<OrderService.UserOrderStatsDto> {
        logger.info("Obtendo estatísticas para usuário: $userId")
        val stats = orderService.getUserOrderStats(UUID.fromString(userId))
        return ResponseEntity.ok(stats)
    }
    
    /**
     * Verifica se o usuário autenticado tem o papel de ADMIN
     */
    private fun hasAdminRole(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication?.authorities?.any { it.authority == "ROLE_ADMIN" } ?: false
    }
} 