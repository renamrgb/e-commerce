package com.ecommerce.payment.infrastructure.rest

import com.ecommerce.payment.application.dto.*
import com.ecommerce.payment.application.service.PaymentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/payments")
class PaymentController(private val paymentService: PaymentService) {

    @PostMapping
    fun createPayment(
        @Valid @RequestBody request: CreatePaymentRequest,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<PaymentResponse> {
        // Verifica se o usuário logado está criando um pagamento para si mesmo
        val userId = jwt.claims["sub"] as String
        if (userId != request.userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        
        val payment = paymentService.createPayment(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(payment)
    }
    
    @PostMapping("/{id}/process")
    fun processPayment(
        @PathVariable("id") paymentId: Long,
        @RequestBody request: ProcessPaymentRequest
    ): ResponseEntity<PaymentResponse> {
        val processRequest = ProcessPaymentRequest(
            paymentId = paymentId,
            confirmationToken = request.confirmationToken
        )
        
        val payment = paymentService.processPayment(processRequest)
        return ResponseEntity.ok(payment)
    }
    
    @PostMapping("/{id}/cancel")
    fun cancelPayment(
        @PathVariable("id") paymentId: Long,
        @RequestBody(required = false) request: CancelPaymentRequest?
    ): ResponseEntity<PaymentResponse> {
        val cancelRequest = CancelPaymentRequest(
            paymentId = paymentId,
            reason = request?.reason
        )
        
        val payment = paymentService.cancelPayment(cancelRequest)
        return ResponseEntity.ok(payment)
    }
    
    @PostMapping("/{id}/refund")
    fun refundPayment(
        @PathVariable("id") paymentId: Long,
        @Valid @RequestBody request: RefundPaymentRequest
    ): ResponseEntity<PaymentResponse> {
        val refundRequest = RefundPaymentRequest(
            paymentId = paymentId,
            amount = request.amount,
            reason = request.reason
        )
        
        val payment = paymentService.refundPayment(refundRequest)
        return ResponseEntity.ok(payment)
    }
    
    @GetMapping("/{id}")
    fun getPaymentById(@PathVariable("id") paymentId: Long): ResponseEntity<PaymentDto> {
        val payment = paymentService.getPaymentById(paymentId)
        return ResponseEntity.ok(payment)
    }
    
    @GetMapping("/order/{orderId}")
    fun getPaymentByOrderId(@PathVariable orderId: String): ResponseEntity<PaymentDto> {
        val payment = paymentService.getPaymentByOrderId(orderId)
        return ResponseEntity.ok(payment)
    }
    
    @GetMapping
    fun getPaymentsByUser(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<List<PaymentDto>> {
        val userId = jwt.claims["sub"] as String
        val payments = paymentService.getPaymentsByUserId(userId)
        return ResponseEntity.ok(payments)
    }
} 