package com.ecommerce.payment.infrastructure.rest

import com.ecommerce.payment.application.dto.CreatePaymentMethodRequest
import com.ecommerce.payment.application.dto.PaymentMethodDto
import com.ecommerce.payment.application.dto.PaymentMethodResponse
import com.ecommerce.payment.application.dto.UpdatePaymentMethodRequest
import com.ecommerce.payment.application.service.PaymentMethodService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/payment-methods")
class PaymentMethodController(private val paymentMethodService: PaymentMethodService) {

    @PostMapping
    fun createPaymentMethod(
        @Valid @RequestBody request: CreatePaymentMethodRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<PaymentMethodResponse> {
        // Verifica se o usuário logado está criando um método de pagamento para si mesmo
        val userId = jwt.claims["sub"] as String
        if (userId != request.userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        
        val paymentMethod = paymentMethodService.createPaymentMethod(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentMethod)
    }
    
    @PutMapping("/{id}")
    fun updatePaymentMethod(
        @PathVariable("id") id: Long,
        @Valid @RequestBody request: UpdatePaymentMethodRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<PaymentMethodResponse> {
        val userId = jwt.claims["sub"] as String
        
        // Verifica se o método pertence ao usuário
        try {
            paymentMethodService.getPaymentMethodById(id, userId)
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
        
        val updateRequest = UpdatePaymentMethodRequest(
            id = id,
            setAsDefault = request.setAsDefault,
            expiryMonth = request.expiryMonth,
            expiryYear = request.expiryYear
        )
        
        val paymentMethod = paymentMethodService.updatePaymentMethod(updateRequest)
        return ResponseEntity.ok(paymentMethod)
    }
    
    @DeleteMapping("/{id}")
    fun deletePaymentMethod(
        @PathVariable("id") id: Long,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<Void> {
        val userId = jwt.claims["sub"] as String
        
        try {
            paymentMethodService.deletePaymentMethod(id, userId)
            return ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }
    
    @GetMapping("/{id}")
    fun getPaymentMethodById(
        @PathVariable("id") id: Long,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<PaymentMethodDto> {
        val userId = jwt.claims["sub"] as String
        
        try {
            val paymentMethod = paymentMethodService.getPaymentMethodById(id, userId)
            return ResponseEntity.ok(paymentMethod)
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }
    
    @GetMapping
    fun getPaymentMethodsByUser(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<List<PaymentMethodDto>> {
        val userId = jwt.claims["sub"] as String
        val paymentMethods = paymentMethodService.getPaymentMethodsByUserId(userId)
        return ResponseEntity.ok(paymentMethods)
    }
} 