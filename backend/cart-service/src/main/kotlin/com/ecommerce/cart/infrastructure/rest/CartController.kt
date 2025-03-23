package com.ecommerce.cart.infrastructure.rest

import com.ecommerce.cart.application.dto.AddItemRequest
import com.ecommerce.cart.application.dto.ApplyCouponRequest
import com.ecommerce.cart.application.dto.CartDto
import com.ecommerce.cart.application.dto.UpdateItemRequest
import com.ecommerce.cart.application.service.CartService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/carts")
@Tag(name = "Carrinho", description = "API para gerenciamento de carrinhos de compras")
@SecurityRequirement(name = "Bearer Authentication")
class CartController(private val cartService: CartService) {

    @GetMapping("/my")
    @Operation(summary = "Obter o carrinho do usuário autenticado")
    @PreAuthorize("isAuthenticated()")
    fun getMyCart(@AuthenticationPrincipal user: UserDetails): ResponseEntity<CartDto> {
        val userId = UUID.fromString(user.username)
        val cart = cartService.findOrCreateCartByUserId(userId)
        return ResponseEntity.ok(cart)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter um carrinho por ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    fun getCartById(@PathVariable id: UUID): ResponseEntity<CartDto> {
        val cart = cartService.findCartDtoById(id)
        return ResponseEntity.ok(cart)
    }

    @PostMapping
    @Operation(summary = "Criar um novo carrinho para o usuário autenticado")
    @PreAuthorize("isAuthenticated()")
    fun createCart(@AuthenticationPrincipal user: UserDetails): ResponseEntity<CartDto> {
        val userId = UUID.fromString(user.username)
        val cart = cartService.findOrCreateCartByUserId(userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(cart)
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Adicionar um item ao carrinho")
    fun addItemToCart(
        @PathVariable id: UUID,
        @RequestBody @Valid request: AddItemRequest
    ): ResponseEntity<CartDto> {
        val updatedCart = cartService.addItemToCart(id, request)
        return ResponseEntity.ok(updatedCart)
    }

    @PutMapping("/{cartId}/items/{itemId}")
    @Operation(summary = "Atualizar um item no carrinho")
    fun updateCartItem(
        @PathVariable cartId: UUID,
        @PathVariable itemId: UUID,
        @RequestBody @Valid request: UpdateItemRequest
    ): ResponseEntity<CartDto> {
        val updatedCart = cartService.updateCartItem(cartId, itemId, request)
        return ResponseEntity.ok(updatedCart)
    }

    @DeleteMapping("/{cartId}/items/{itemId}")
    @Operation(summary = "Remover um item do carrinho")
    fun removeCartItem(
        @PathVariable cartId: UUID,
        @PathVariable itemId: UUID
    ): ResponseEntity<CartDto> {
        val updatedCart = cartService.removeItemFromCart(cartId, itemId)
        return ResponseEntity.ok(updatedCart)
    }

    @DeleteMapping("/{id}/items")
    @Operation(summary = "Limpar todos os itens do carrinho")
    fun clearCart(@PathVariable id: UUID): ResponseEntity<CartDto> {
        val updatedCart = cartService.clearCart(id)
        return ResponseEntity.ok(updatedCart)
    }

    @PostMapping("/{id}/coupon")
    @Operation(summary = "Aplicar um cupom de desconto ao carrinho")
    fun applyCoupon(
        @PathVariable id: UUID,
        @RequestBody @Valid request: ApplyCouponRequest
    ): ResponseEntity<CartDto> {
        val updatedCart = cartService.applyCoupon(id, request)
        return ResponseEntity.ok(updatedCart)
    }

    @DeleteMapping("/{id}/coupon")
    @Operation(summary = "Remover um cupom de desconto do carrinho")
    fun removeCoupon(@PathVariable id: UUID): ResponseEntity<CartDto> {
        val updatedCart = cartService.removeCoupon(id)
        return ResponseEntity.ok(updatedCart)
    }
} 