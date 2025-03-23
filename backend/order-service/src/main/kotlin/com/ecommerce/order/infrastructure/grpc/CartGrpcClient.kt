package com.ecommerce.order.infrastructure.grpc

import com.ecommerce.cart.CartRequest
import com.ecommerce.cart.CartResponse
import com.ecommerce.cart.CartServiceGrpc
import com.ecommerce.cart.UserCartRequest
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class CartGrpcClient(private val cartChannel: ManagedChannel) {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    private val cartStub = CartServiceGrpc.newBlockingStub(cartChannel)
    
    /**
     * Obtém um carrinho pelo ID
     */
    fun getCart(cartId: UUID): CartInfo? {
        try {
            val request = CartRequest.newBuilder()
                .setId(cartId.toString())
                .build()
            
            val response = cartStub.withDeadlineAfter(3, TimeUnit.SECONDS)
                .getCart(request)
            
            return if (response != null) mapToCartInfo(response) else null
        } catch (e: Exception) {
            logger.error("Erro ao obter carrinho via gRPC: ${e.message}")
            return null
        }
    }
    
    /**
     * Obtém o carrinho ativo de um usuário
     */
    fun getCartByUserId(userId: UUID): CartInfo? {
        try {
            val request = UserCartRequest.newBuilder()
                .setUserId(userId.toString())
                .build()
            
            val response = cartStub.withDeadlineAfter(3, TimeUnit.SECONDS)
                .getCartByUserId(request)
            
            return if (response != null) mapToCartInfo(response) else null
        } catch (e: Exception) {
            logger.error("Erro ao obter carrinho do usuário via gRPC: ${e.message}")
            return null
        }
    }
    
    /**
     * Limpa um carrinho (remove todos os itens)
     */
    fun clearCart(cartId: UUID): Boolean {
        try {
            val request = CartRequest.newBuilder()
                .setId(cartId.toString())
                .build()
            
            cartStub.withDeadlineAfter(3, TimeUnit.SECONDS)
                .clearCart(request)
            
            return true
        } catch (e: Exception) {
            logger.error("Erro ao limpar carrinho via gRPC: ${e.message}")
            return false
        }
    }
    
    /**
     * Converte a resposta gRPC para um objeto de domínio
     */
    private fun mapToCartInfo(response: CartResponse): CartInfo {
        return CartInfo(
            id = UUID.fromString(response.id),
            userId = if (response.hasUserId()) UUID.fromString(response.userId) else null,
            items = response.itemsList.map { cartItem ->
                CartItemInfo(
                    id = UUID.fromString(cartItem.id),
                    productId = UUID.fromString(cartItem.productId),
                    productName = cartItem.productName,
                    productSlug = cartItem.productSlug,
                    productImage = if (cartItem.hasProductImage()) cartItem.productImage else null,
                    variantId = if (cartItem.hasVariantId()) UUID.fromString(cartItem.variantId) else null,
                    variantName = if (cartItem.hasVariantName()) cartItem.variantName else null,
                    price = BigDecimal(cartItem.price),
                    quantity = cartItem.quantity,
                    total = BigDecimal(cartItem.total)
                )
            },
            couponCode = if (response.hasCouponCode()) response.couponCode else null,
            subtotal = BigDecimal(response.subtotal),
            discount = BigDecimal(response.discount),
            total = BigDecimal(response.total)
        )
    }
    
    /**
     * Classe de domínio para informações do carrinho
     */
    data class CartInfo(
        val id: UUID,
        val userId: UUID?,
        val items: List<CartItemInfo>,
        val couponCode: String?,
        val subtotal: BigDecimal,
        val discount: BigDecimal,
        val total: BigDecimal
    )
    
    /**
     * Classe de domínio para informações dos itens do carrinho
     */
    data class CartItemInfo(
        val id: UUID,
        val productId: UUID,
        val productName: String,
        val productSlug: String,
        val productImage: String?,
        val variantId: UUID?,
        val variantName: String?,
        val price: BigDecimal,
        val quantity: Int,
        val total: BigDecimal
    )
} 