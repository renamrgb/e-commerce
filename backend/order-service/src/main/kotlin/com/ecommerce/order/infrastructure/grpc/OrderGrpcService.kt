package com.ecommerce.order.infrastructure.grpc

import com.ecommerce.order.*
import com.ecommerce.order.application.service.OrderService
import com.ecommerce.order.domain.exception.OrderNotFoundException
import com.ecommerce.order.domain.model.Order
import com.ecommerce.order.domain.model.OrderItem
import com.ecommerce.order.domain.model.OrderStatusHistory
import com.ecommerce.order.domain.model.PaymentInfo
import com.ecommerce.order.domain.model.ShippingAddress
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.lognet.springboot.grpc.GRpcService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@GRpcService
class OrderGrpcService(private val orderService: OrderService) : OrderServiceGrpc.OrderServiceImplBase() {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    private val dateFormatter = DateTimeFormatter.ISO_DATE_TIME
    
    /**
     * Obtém um pedido pelo ID
     */
    override fun getOrder(
        request: OrderRequest,
        responseObserver: StreamObserver<OrderResponse>
    ) {
        try {
            logger.info("gRPC - Obtendo pedido por ID: ${request.orderId}")
            
            val orderId = UUID.fromString(request.orderId)
            val order = orderService.findById(orderId)
            
            val response = convertToGrpcResponse(order)
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: IllegalArgumentException) {
            logger.error("gRPC - ID de pedido inválido: ${request.orderId}")
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("ID de pedido inválido: ${request.orderId}")
                    .asRuntimeException()
            )
        } catch (e: OrderNotFoundException) {
            logger.error("gRPC - Pedido não encontrado: ${request.orderId}")
            responseObserver.onError(
                Status.NOT_FOUND
                    .withDescription(e.message)
                    .asRuntimeException()
            )
        } catch (e: Exception) {
            logger.error("gRPC - Erro ao obter pedido: ${e.message}", e)
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Erro ao processar a requisição: ${e.message}")
                    .asRuntimeException()
            )
        }
    }
    
    /**
     * Obtém um pedido pelo número
     */
    override fun getOrderByNumber(
        request: OrderNumberRequest,
        responseObserver: StreamObserver<OrderResponse>
    ) {
        try {
            logger.info("gRPC - Obtendo pedido por número: ${request.orderNumber}")
            
            val order = orderService.findByOrderNumber(request.orderNumber)
            
            val response = convertToGrpcResponse(order)
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: OrderNotFoundException) {
            logger.error("gRPC - Pedido não encontrado com número: ${request.orderNumber}")
            responseObserver.onError(
                Status.NOT_FOUND
                    .withDescription(e.message)
                    .asRuntimeException()
            )
        } catch (e: Exception) {
            logger.error("gRPC - Erro ao obter pedido por número: ${e.message}", e)
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Erro ao processar a requisição: ${e.message}")
                    .asRuntimeException()
            )
        }
    }
    
    /**
     * Obtém pedidos de um usuário
     */
    override fun getUserOrders(
        request: UserOrdersRequest,
        responseObserver: StreamObserver<UserOrdersResponse>
    ) {
        try {
            logger.info("gRPC - Obtendo pedidos do usuário: ${request.userId}")
            
            val userId = UUID.fromString(request.userId)
            val pageable = PageRequest.of(request.page, request.size)
            val ordersPage = orderService.findAllByUserId(userId, pageable)
            
            val response = createUserOrdersResponse(ordersPage)
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: IllegalArgumentException) {
            logger.error("gRPC - ID de usuário inválido: ${request.userId}")
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("ID de usuário inválido: ${request.userId}")
                    .asRuntimeException()
            )
        } catch (e: Exception) {
            logger.error("gRPC - Erro ao obter pedidos do usuário: ${e.message}", e)
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Erro ao processar a requisição: ${e.message}")
                    .asRuntimeException()
            )
        }
    }
    
    /**
     * Cria um pedido a partir de um carrinho
     */
    override fun createOrder(
        request: CreateOrderRequest,
        responseObserver: StreamObserver<OrderResponse>
    ) {
        try {
            logger.info("gRPC - Criando pedido para usuário: ${request.userId}, carrinho: ${request.cartId}")
            
            val userId = UUID.fromString(request.userId)
            val cartId = UUID.fromString(request.cartId)
            
            // Converter endereço de entrega
            val shippingAddress = ShippingAddress(
                recipientName = request.shippingAddress.recipientName,
                street = request.shippingAddress.street,
                number = request.shippingAddress.number,
                complement = if (request.shippingAddress.complement.isEmpty()) null else request.shippingAddress.complement,
                neighborhood = request.shippingAddress.neighborhood,
                city = request.shippingAddress.city,
                state = request.shippingAddress.state,
                zipCode = request.shippingAddress.zipCode,
                country = request.shippingAddress.country,
                phone = request.shippingAddress.phone
            )
            
            // Converter informações de pagamento
            val paymentInfo = PaymentInfo(
                method = request.paymentInfo.method,
                cardLastDigits = if (request.paymentInfo.cardLastDigits.isEmpty()) null else request.paymentInfo.cardLastDigits,
                installments = request.paymentInfo.installments,
                paymentId = if (request.paymentInfo.paymentId.isEmpty()) null else request.paymentInfo.paymentId
            )
            
            // Criar o pedido
            val order = orderService.createFromCart(userId, cartId, shippingAddress, paymentInfo)
            
            val response = convertToGrpcResponse(order)
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: IllegalArgumentException) {
            logger.error("gRPC - Erro de argumento ao criar pedido: ${e.message}")
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription(e.message)
                    .asRuntimeException()
            )
        } catch (e: Exception) {
            logger.error("gRPC - Erro ao criar pedido: ${e.message}", e)
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Erro ao processar a requisição: ${e.message}")
                    .asRuntimeException()
            )
        }
    }
    
    /**
     * Atualiza o status de um pedido
     */
    override fun updateOrderStatus(
        request: UpdateStatusRequest,
        responseObserver: StreamObserver<OrderResponse>
    ) {
        try {
            logger.info("gRPC - Atualizando status do pedido: ${request.orderId} para: ${request.status}")
            
            val orderId = UUID.fromString(request.orderId)
            val comment = if (request.comment.isEmpty()) null else request.comment
            
            val order = orderService.updateStatus(orderId, request.status, comment)
            
            val response = convertToGrpcResponse(order)
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: IllegalArgumentException) {
            logger.error("gRPC - Argumento inválido ao atualizar status: ${e.message}")
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription(e.message)
                    .asRuntimeException()
            )
        } catch (e: OrderNotFoundException) {
            logger.error("gRPC - Pedido não encontrado: ${request.orderId}")
            responseObserver.onError(
                Status.NOT_FOUND
                    .withDescription(e.message)
                    .asRuntimeException()
            )
        } catch (e: IllegalStateException) {
            logger.error("gRPC - Estado inválido: ${e.message}")
            responseObserver.onError(
                Status.FAILED_PRECONDITION
                    .withDescription(e.message)
                    .asRuntimeException()
            )
        } catch (e: Exception) {
            logger.error("gRPC - Erro ao atualizar status do pedido: ${e.message}", e)
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Erro ao processar a requisição: ${e.message}")
                    .asRuntimeException()
            )
        }
    }
    
    /**
     * Cancela um pedido
     */
    override fun cancelOrder(
        request: OrderRequest,
        responseObserver: StreamObserver<OrderResponse>
    ) {
        try {
            logger.info("gRPC - Cancelando pedido: ${request.orderId}")
            
            val orderId = UUID.fromString(request.orderId)
            val order = orderService.cancelOrder(orderId)
            
            val response = convertToGrpcResponse(order)
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: IllegalArgumentException) {
            logger.error("gRPC - ID de pedido inválido: ${request.orderId}")
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("ID de pedido inválido: ${request.orderId}")
                    .asRuntimeException()
            )
        } catch (e: OrderNotFoundException) {
            logger.error("gRPC - Pedido não encontrado: ${request.orderId}")
            responseObserver.onError(
                Status.NOT_FOUND
                    .withDescription(e.message)
                    .asRuntimeException()
            )
        } catch (e: IllegalStateException) {
            logger.error("gRPC - Estado inválido para cancelamento: ${e.message}")
            responseObserver.onError(
                Status.FAILED_PRECONDITION
                    .withDescription(e.message)
                    .asRuntimeException()
            )
        } catch (e: Exception) {
            logger.error("gRPC - Erro ao cancelar pedido: ${e.message}", e)
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Erro ao processar a requisição: ${e.message}")
                    .asRuntimeException()
            )
        }
    }
    
    /**
     * Obtém estatísticas dos pedidos de um usuário
     */
    override fun getUserOrderStats(
        request: UserRequest,
        responseObserver: StreamObserver<UserOrderStatsResponse>
    ) {
        try {
            logger.info("gRPC - Obtendo estatísticas de pedidos para usuário: ${request.userId}")
            
            val userId = UUID.fromString(request.userId)
            val stats = orderService.getUserOrderStats(userId)
            
            val response = UserOrderStatsResponse.newBuilder()
                .setTotalOrders(stats.totalOrders)
                .setCompletedOrders(stats.completedOrders)
                .setCancelledOrders(stats.cancelledOrders)
                .setTotalSpent(stats.totalSpent.toString())
                .setItemsPurchased(stats.itemsPurchased)
                .build()
            
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: IllegalArgumentException) {
            logger.error("gRPC - ID de usuário inválido: ${request.userId}")
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("ID de usuário inválido: ${request.userId}")
                    .asRuntimeException()
            )
        } catch (e: Exception) {
            logger.error("gRPC - Erro ao obter estatísticas de pedidos: ${e.message}", e)
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Erro ao processar a requisição: ${e.message}")
                    .asRuntimeException()
            )
        }
    }
    
    /**
     * Converte um pedido para a resposta gRPC
     */
    private fun convertToGrpcResponse(order: Order): OrderResponse {
        // Formatando o endereço em uma string
        val shippingAddressStr = "${order.shippingAddress.recipientName}, ${order.shippingAddress.street}, ${order.shippingAddress.number}, ${order.shippingAddress.city}, ${order.shippingAddress.state}, ${order.shippingAddress.zipCode}"
        
        val responseBuilder = OrderResponse.newBuilder()
            .setId(order.id.toString())
            .setUserId(order.userId.toString())
            .setOrderNumber(order.orderNumber)
            .setStatus(order.status)
            .setSubtotal(order.subtotal.toString())
            .setShippingCost(order.shippingCost.toString())
            .setDiscount(order.discount.toString())
            .setTax(order.tax.toString())
            .setTotal(order.total.toString())
            .setShippingAddress(shippingAddressStr)
            .setPaymentMethod(order.paymentInfo.method)
            .setCreatedAt(order.createdAt.format(dateFormatter))
            .setUpdatedAt(order.updatedAt.format(dateFormatter))
        
        // Adicionar ID de pagamento se existir
        if (order.paymentInfo.paymentId != null) {
            responseBuilder.setPaymentId(order.paymentInfo.paymentId)
        }
        
        // Adicionar itens do pedido
        order.items.forEach { item ->
            responseBuilder.addItems(convertToGrpcItemResponse(item))
        }
        
        // Adicionar histórico de status
        order.statusHistory.forEach { history ->
            responseBuilder.addStatusHistory(convertToGrpcStatusHistoryResponse(history))
        }
        
        return responseBuilder.build()
    }
    
    /**
     * Converte um item de pedido para a resposta gRPC
     */
    private fun convertToGrpcItemResponse(item: OrderItem): OrderItemProto {
        val responseBuilder = OrderItemProto.newBuilder()
            .setId(item.id.toString())
            .setProductId(item.productId.toString())
            .setProductName(item.productName)
            .setProductSlug(item.productSlug)
            .setPrice(item.price.toString())
            .setQuantity(item.quantity)
            .setTotal(item.total.toString())
            .setCreatedAt(item.createdAt.format(dateFormatter))
        
        // Adicionar informações opcionais se existirem
        if (item.productImage != null) {
            responseBuilder.setProductImage(item.productImage)
        }
        
        if (item.variantId != null) {
            responseBuilder.setVariantId(item.variantId.toString())
        }
        
        if (item.variantName != null) {
            responseBuilder.setVariantName(item.variantName)
        }
        
        return responseBuilder.build()
    }
    
    /**
     * Converte um histórico de status para a resposta gRPC
     */
    private fun convertToGrpcStatusHistoryResponse(history: OrderStatusHistory): StatusHistoryProto {
        val responseBuilder = StatusHistoryProto.newBuilder()
            .setId(history.id.toString())
            .setStatus(history.status)
            .setCreatedAt(history.createdAt.format(dateFormatter))
        
        // Adicionar observação se existir
        if (history.observation != null) {
            responseBuilder.setObservation(history.observation)
        }
        
        return responseBuilder.build()
    }
    
    /**
     * Cria uma resposta com a lista de pedidos paginada
     */
    private fun createUserOrdersResponse(ordersPage: Page<Order>): UserOrdersResponse {
        val responseBuilder = UserOrdersResponse.newBuilder()
            .setTotalPages(ordersPage.totalPages)
            .setTotalElements(ordersPage.totalElements)
            .setPage(ordersPage.number)
            .setSize(ordersPage.size)
        
        // Adicionar resumos de pedidos
        ordersPage.content.forEach { order ->
            val orderSummary = OrderSummary.newBuilder()
                .setId(order.id.toString())
                .setOrderNumber(order.orderNumber)
                .setStatus(order.status)
                .setTotal(order.total.toString())
                .setItemCount(order.items.sumOf { it.quantity })
                .setCreatedAt(order.createdAt.format(dateFormatter))
                .build()
            
            responseBuilder.addOrders(orderSummary)
        }
        
        return responseBuilder.build()
    }
} 