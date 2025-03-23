package com.ecommerce.cart.application.service

import com.ecommerce.cart.application.dto.AddItemRequest
import com.ecommerce.cart.application.dto.ApplyCouponRequest
import com.ecommerce.cart.application.dto.CartDto
import com.ecommerce.cart.application.dto.CartItemDto
import com.ecommerce.cart.application.dto.UpdateItemRequest
import com.ecommerce.cart.domain.entity.Cart
import com.ecommerce.cart.domain.entity.CartItem
import com.ecommerce.cart.domain.exception.EntityNotFoundException
import com.ecommerce.cart.domain.repository.CartItemRepository
import com.ecommerce.cart.domain.repository.CartRepository
import com.ecommerce.cart.domain.repository.CouponRepository
import com.ecommerce.cart.infrastructure.grpc.CatalogGrpcClient
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val couponRepository: CouponRepository,
    private val catalogClient: CatalogGrpcClient
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    @Cacheable(value = ["carts"], key = "#id")
    fun findById(id: UUID): Cart {
        logger.info("Buscando carrinho por ID: $id")
        return cartRepository.findByIdWithItems(id)
            .orElseThrow { EntityNotFoundException("Carrinho não encontrado com ID: $id") }
    }

    @Transactional(readOnly = true)
    fun findCartDtoById(id: UUID): CartDto {
        return toDto(findById(id))
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ["user-carts"], key = "#userId")
    fun findCartByUserId(userId: UUID): CartDto {
        logger.info("Buscando carrinho ativo para o usuário: $userId")
        val cart = cartRepository.findByUserIdAndIsActiveTrue(userId)
            .orElseThrow { EntityNotFoundException("Nenhum carrinho ativo encontrado para o usuário: $userId") }
        return toDto(cart)
    }

    @Transactional
    @CacheEvict(value = ["user-carts"], key = "#userId")
    fun findOrCreateCartByUserId(userId: UUID): CartDto {
        logger.info("Buscando ou criando carrinho para o usuário: $userId")
        
        val cartOptional = cartRepository.findByUserIdAndIsActiveTrue(userId)
        
        if (cartOptional.isPresent) {
            return toDto(cartOptional.get())
        }
        
        // Criar novo carrinho
        val newCart = Cart(
            userId = userId,
            isActive = true
        )
        
        val savedCart = cartRepository.save(newCart)
        return toDto(savedCart)
    }

    @Transactional
    @CacheEvict(value = ["carts", "user-carts"], allEntries = true)
    fun addItemToCart(cartId: UUID, request: AddItemRequest): CartDto {
        logger.info("Adicionando item ao carrinho $cartId: ${request.productId}, quantidade: ${request.quantity}")
        
        val cart = findById(cartId)
        
        // Verificar se o produto existe e está disponível
        val product = catalogClient.getProduct(request.productId)
            ?: throw IllegalArgumentException("Produto não encontrado: ${request.productId}")
        
        if (!product.active) {
            throw IllegalArgumentException("Produto não está disponível para compra: ${product.name}")
        }
        
        // Verificar se o item já existe no carrinho
        var existingItem = cart.items.find { 
            it.productId == request.productId && it.variantId == request.variantId 
        }
        
        if (existingItem != null) {
            // Atualizar quantidade do item existente
            existingItem.quantity += request.quantity
            existingItem.updatedAt = LocalDateTime.now()
            cart.updatedAt = LocalDateTime.now()
        } else {
            // Criar novo item
            val newItem = CartItem(
                productId = request.productId,
                productName = product.name,
                productSlug = product.slug,
                productImage = product.mainImage,
                variantId = request.variantId,
                variantName = request.variantName,
                price = request.price,
                quantity = request.quantity
            )
            
            cart.addItem(newItem)
        }
        
        val updatedCart = cartRepository.save(cart)
        return toDto(updatedCart)
    }

    @Transactional
    @CacheEvict(value = ["carts", "user-carts"], allEntries = true)
    fun updateCartItem(cartId: UUID, itemId: UUID, request: UpdateItemRequest): CartDto {
        logger.info("Atualizando item $itemId no carrinho $cartId para quantidade: ${request.quantity}")
        
        val cart = findById(cartId)
        
        val item = cart.items.find { it.id == itemId }
            ?: throw EntityNotFoundException("Item não encontrado no carrinho: $itemId")
        
        if (request.quantity <= 0) {
            // Se a quantidade for zero ou negativa, remove o item
            return removeItemFromCart(cartId, itemId)
        }
        
        // Atualizar quantidade
        item.quantity = request.quantity
        item.updatedAt = LocalDateTime.now()
        cart.updatedAt = LocalDateTime.now()
        
        val updatedCart = cartRepository.save(cart)
        return toDto(updatedCart)
    }

    @Transactional
    @CacheEvict(value = ["carts", "user-carts"], allEntries = true)
    fun removeItemFromCart(cartId: UUID, itemId: UUID): CartDto {
        logger.info("Removendo item $itemId do carrinho $cartId")
        
        val cart = findById(cartId)
        
        val item = cart.items.find { it.id == itemId }
            ?: throw EntityNotFoundException("Item não encontrado no carrinho: $itemId")
        
        cart.removeItem(item)
        cart.updatedAt = LocalDateTime.now()
        
        // Deletar o item do banco de dados
        cartItemRepository.deleteById(itemId)
        
        val updatedCart = cartRepository.save(cart)
        return toDto(updatedCart)
    }

    @Transactional
    @CacheEvict(value = ["carts", "user-carts"], allEntries = true)
    fun clearCart(cartId: UUID): CartDto {
        logger.info("Limpando todos os itens do carrinho $cartId")
        
        val cart = findById(cartId)
        
        // Deletar todos os itens do banco de dados
        cartItemRepository.deleteByCartId(cartId)
        
        // Limpar a lista de itens em memória
        cart.clear()
        cart.updatedAt = LocalDateTime.now()
        
        val updatedCart = cartRepository.save(cart)
        return toDto(updatedCart)
    }

    @Transactional
    @CacheEvict(value = ["carts", "user-carts"], allEntries = true)
    fun applyCoupon(cartId: UUID, request: ApplyCouponRequest): CartDto {
        logger.info("Aplicando cupom ${request.code} ao carrinho $cartId")
        
        val cart = findById(cartId)
        val couponOptional = couponRepository.findByCode(request.code)
        
        if (couponOptional.isEmpty) {
            throw IllegalArgumentException("Cupom não encontrado: ${request.code}")
        }
        
        val coupon = couponOptional.get()
        
        // Verificar se o cupom é válido
        if (!coupon.isValid()) {
            throw IllegalArgumentException("Cupom inválido ou expirado: ${request.code}")
        }
        
        // Verificar se o valor mínimo de compra foi atingido
        if (coupon.minPurchaseAmount != null && cart.subtotal < coupon.minPurchaseAmount) {
            throw IllegalArgumentException("Valor mínimo de compra não atingido para este cupom. Mínimo: ${coupon.minPurchaseAmount}")
        }
        
        // Calcular o desconto
        val discount = coupon.calculateDiscount(cart.subtotal)
        
        // Atualizar o carrinho
        cart.coupon = coupon
        cart.discount = discount
        cart.updatedAt = LocalDateTime.now()
        
        // Incrementar o uso do cupom
        coupon.incrementUsage()
        couponRepository.save(coupon)
        
        val updatedCart = cartRepository.save(cart)
        return toDto(updatedCart)
    }

    @Transactional
    @CacheEvict(value = ["carts", "user-carts"], allEntries = true)
    fun removeCoupon(cartId: UUID): CartDto {
        logger.info("Removendo cupom do carrinho $cartId")
        
        val cart = findById(cartId)
        
        // Remover cupom e desconto
        cart.coupon = null
        cart.discount = BigDecimal.ZERO
        cart.updatedAt = LocalDateTime.now()
        
        val updatedCart = cartRepository.save(cart)
        return toDto(updatedCart)
    }

    private fun toDto(cart: Cart): CartDto {
        return cart.toDto()
    }

    private fun toDto(item: CartItem): CartItemDto {
        return item.toDto()
    }
} 