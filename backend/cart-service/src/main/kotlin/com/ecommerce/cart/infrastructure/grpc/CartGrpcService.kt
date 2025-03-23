package com.ecommerce.cart.infrastructure.grpc

import com.ecommerce.cart.application.service.CartService
import com.ecommerce.cart.application.dto.AddItemRequest as DtoAddItemRequest
import com.ecommerce.cart.application.dto.UpdateItemRequest as DtoUpdateItemRequest
import com.ecommerce.cart.application.dto.ApplyCouponRequest as DtoApplyCouponRequest
import com.ecommerce.cart.domain.entity.Cart
import com.ecommerce.cart.domain.entity.CartItem
import com.ecommerce.cart.grpc.*
import com.google.protobuf.Timestamp
import io.grpc.Status
import net.devh.boot.grpc.server.service.GrpcService
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneOffset
import java.util.*

@GrpcService
class CartGrpcService(private val cartService: CartService) : CartServiceGrpc.CartServiceImplBase() {

    private val logger = LoggerFactory.getLogger(CartGrpcService::class.java)

    override fun getCart(request: CartRequest, responseObserver: io.grpc.stub.StreamObserver<CartResponse>) {
        try {
            val cartId = UUID.fromString(request.cartId)
            val cart = cartService.findById(cartId)
            
            val response = mapCartToResponse(cart)
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            logger.error("Erro ao obter carrinho: ${e.message}", e)
            responseObserver.onError(
                Status.INTERNAL.withDescription("Erro ao obter carrinho: ${e.message}").asRuntimeException()
            )
        }
    }

    override fun getCartByUserId(request: UserCartRequest, responseObserver: io.grpc.stub.StreamObserver<CartResponse>) {
        try {
            val userId = UUID.fromString(request.userId)
            val cart = cartService.findOrCreateByUserId(userId)
            
            val response = mapCartToResponse(cart)
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            logger.error("Erro ao obter carrinho do usuário: ${e.message}", e)
            responseObserver.onError(
                Status.INTERNAL.withDescription("Erro ao obter carrinho do usuário: ${e.message}").asRuntimeException()
            )
        }
    }

    @Transactional
    override fun addItemToCart(request: AddItemRequest, responseObserver: io.grpc.stub.StreamObserver<CartResponse>) {
        try {
            val cartId = UUID.fromString(request.cartId)
            val productId = UUID.fromString(request.productId)
            val variantId = if (request.variantId.isNullOrEmpty()) null else UUID.fromString(request.variantId)
            
            val addItemRequest = DtoAddItemRequest(
                productId = productId,
                variantId = variantId,
                quantity = request.quantity
            )
            
            val cart = cartService.addItem(cartId, addItemRequest)
            
            val response = mapCartToResponse(cart)
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            logger.error("Erro ao adicionar item ao carrinho: ${e.message}", e)
            responseObserver.onError(
                Status.INTERNAL.withDescription("Erro ao adicionar item ao carrinho: ${e.message}").asRuntimeException()
            )
        }
    }

    @Transactional
    override fun updateCartItem(request: UpdateItemRequest, responseObserver: io.grpc.stub.StreamObserver<CartResponse>) {
        try {
            val cartId = UUID.fromString(request.cartId)
            val itemId = UUID.fromString(request.itemId)
            
            val updateItemRequest = DtoUpdateItemRequest(
                quantity = request.quantity
            )
            
            val cart = cartService.updateItem(cartId, itemId, updateItemRequest)
            
            val response = mapCartToResponse(cart)
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            logger.error("Erro ao atualizar item do carrinho: ${e.message}", e)
            responseObserver.onError(
                Status.INTERNAL.withDescription("Erro ao atualizar item do carrinho: ${e.message}").asRuntimeException()
            )
        }
    }

    @Transactional
    override fun removeCartItem(request: RemoveItemRequest, responseObserver: io.grpc.stub.StreamObserver<CartResponse>) {
        try {
            val cartId = UUID.fromString(request.cartId)
            val itemId = UUID.fromString(request.itemId)
            
            val cart = cartService.removeItem(cartId, itemId)
            
            val response = mapCartToResponse(cart)
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            logger.error("Erro ao remover item do carrinho: ${e.message}", e)
            responseObserver.onError(
                Status.INTERNAL.withDescription("Erro ao remover item do carrinho: ${e.message}").asRuntimeException()
            )
        }
    }

    @Transactional
    override fun clearCart(request: CartRequest, responseObserver: io.grpc.stub.StreamObserver<CartResponse>) {
        try {
            val cartId = UUID.fromString(request.cartId)
            val cart = cartService.clear(cartId)
            
            val response = mapCartToResponse(cart)
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            logger.error("Erro ao limpar carrinho: ${e.message}", e)
            responseObserver.onError(
                Status.INTERNAL.withDescription("Erro ao limpar carrinho: ${e.message}").asRuntimeException()
            )
        }
    }

    @Transactional
    override fun applyCoupon(request: ApplyCouponRequest, responseObserver: io.grpc.stub.StreamObserver<CartResponse>) {
        try {
            val cartId = UUID.fromString(request.cartId)
            
            val applyCouponRequest = DtoApplyCouponRequest(
                code = request.couponCode
            )
            
            val cart = cartService.applyCoupon(cartId, applyCouponRequest)
            
            val response = mapCartToResponse(cart)
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            logger.error("Erro ao aplicar cupom: ${e.message}", e)
            responseObserver.onError(
                Status.INTERNAL.withDescription("Erro ao aplicar cupom: ${e.message}").asRuntimeException()
            )
        }
    }

    @Transactional
    override fun removeCoupon(request: CartRequest, responseObserver: io.grpc.stub.StreamObserver<CartResponse>) {
        try {
            val cartId = UUID.fromString(request.cartId)
            val cart = cartService.removeCoupon(cartId)
            
            val response = mapCartToResponse(cart)
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            logger.error("Erro ao remover cupom: ${e.message}", e)
            responseObserver.onError(
                Status.INTERNAL.withDescription("Erro ao remover cupom: ${e.message}").asRuntimeException()
            )
        }
    }

    private fun mapCartToResponse(cart: com.ecommerce.cart.application.dto.CartDto): CartResponse {
        val responseBuilder = CartResponse.newBuilder()
            .setId(cart.id.toString())
            .setUserId(cart.userId.toString())
            .setCouponCode(cart.couponCode ?: "")
            .setDiscountPercentage(cart.discountPercentage ?: 0)
            .setDiscountAmount(cart.discountAmount?.toDouble() ?: 0.0)
            .setSubtotal(cart.subtotal.toDouble())
            .setTotal(cart.total.toDouble())
            .setItemCount(cart.itemCount)
            .setCreatedAt(cart.createdAt.toInstant(ZoneOffset.UTC).toString())
            .setUpdatedAt(cart.updatedAt.toInstant(ZoneOffset.UTC).toString())
        
        // Adicionar itens do carrinho
        cart.items.forEach { item ->
            val itemResponse = CartItemResponse.newBuilder()
                .setId(item.id.toString())
                .setProductId(item.productId.toString())
                .setProductName(item.productName)
                .setProductSlug(item.productSlug)
                .setProductImage(item.productImage ?: "")
                .setVariantId(item.variantId?.toString() ?: "")
                .setVariantName(item.variantName ?: "")
                .setUnitPrice(item.unitPrice.toDouble())
                .setQuantity(item.quantity)
                .setAttributes(item.attributes ?: "")
                .setTotal(item.total.toDouble())
                .build()
            
            responseBuilder.addItems(itemResponse)
        }
        
        return responseBuilder.build()
    }
} 