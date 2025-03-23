package com.ecommerce.cart.infrastructure.rest

import com.ecommerce.cart.application.dto.CouponCreateRequest
import com.ecommerce.cart.application.dto.CouponDto
import com.ecommerce.cart.application.dto.CouponUpdateRequest
import com.ecommerce.cart.application.service.CouponService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/coupons")
@Tag(name = "Cupons", description = "API para gerenciamento de cupons de desconto")
@SecurityRequirement(name = "Bearer Authentication")
class CouponController(private val couponService: CouponService) {

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cupom por ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    fun findById(@PathVariable id: UUID): ResponseEntity<CouponDto> {
        val coupon = couponService.findById(id)
        return ResponseEntity.ok(coupon)
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Buscar cupom por código")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    fun findByCode(@PathVariable code: String): ResponseEntity<CouponDto> {
        val coupon = couponService.findByCode(code)
        return ResponseEntity.ok(coupon)
    }

    @GetMapping
    @Operation(summary = "Listar todos os cupons")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    fun findAll(@PageableDefault pageable: Pageable): ResponseEntity<Page<CouponDto>> {
        val coupons = couponService.findAll(pageable)
        return ResponseEntity.ok(coupons)
    }

    @GetMapping("/active")
    @Operation(summary = "Listar cupons ativos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    fun findAllActive(@PageableDefault pageable: Pageable): ResponseEntity<Page<CouponDto>> {
        val coupons = couponService.findAllActive(pageable)
        return ResponseEntity.ok(coupons)
    }

    @PostMapping
    @Operation(summary = "Criar um novo cupom")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    fun create(@RequestBody @Valid request: CouponCreateRequest): ResponseEntity<CouponDto> {
        val coupon = couponService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(coupon)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar um cupom existente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    fun update(
        @PathVariable id: UUID,
        @RequestBody @Valid request: CouponUpdateRequest
    ): ResponseEntity<CouponDto> {
        val coupon = couponService.update(id, request)
        return ResponseEntity.ok(coupon)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir um cupom")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        couponService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/toggle-active")
    @Operation(summary = "Alternar o status de ativação de um cupom")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    fun toggleActive(@PathVariable id: UUID): ResponseEntity<CouponDto> {
        val coupon = couponService.toggleActive(id)
        return ResponseEntity.ok(coupon)
    }

    @GetMapping("/validate/{code}")
    @Operation(summary = "Validar um cupom")
    fun validateCoupon(@PathVariable code: String): ResponseEntity<Map<String, Boolean>> {
        val isValid = couponService.validateCoupon(code)
        return ResponseEntity.ok(mapOf("valid" to isValid))
    }
} 