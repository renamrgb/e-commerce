package com.ecommerce.payment.infrastructure.controller

import com.ecommerce.payment.application.service.WebhookService
import com.ecommerce.payment.domain.model.Payment
import com.ecommerce.payment.domain.model.PaymentStatus
import com.ecommerce.payment.domain.repository.PaymentRepository
import com.ecommerce.payment.infrastructure.mock.StripeMockService
import com.ecommerce.payment.infrastructure.mock.WebhookEventGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import java.math.RoundingMode

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebhookControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var webhookService: WebhookService

    @Autowired
    private lateinit var paymentRepository: PaymentRepository

    @Autowired
    private lateinit var stripeMockService: StripeMockService

    @Autowired
    private lateinit var webhookEventGenerator: WebhookEventGenerator

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val webhookEndpoint = "/api/v1/webhook/stripe"
    private val webhookSignatureHeader = "Stripe-Signature"
    private val mockSignature = "test_signature_123"

    @BeforeEach
    fun setup() {
        stripeMockService.clearAllMockData()
        paymentRepository.deleteAll()
    }

    @Test
    fun `deve processar evento de PaymentIntent bem-sucedido`() {
        // Arranjo
        val orderId = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()
        val amount = BigDecimal("100.00")
        val amountInCents = amount.multiply(BigDecimal("100")).toLong()
        val currency = "brl"
        val paymentMethodId = "pm_" + UUID.randomUUID().toString().replace("-", "")
        
        // Cria um pagamento pendente no banco
        val paymentIntent = stripeMockService.createPaymentIntent(
            amount = amount,
            currency = currency,
            paymentMethodId = paymentMethodId,
            description = "Pagamento do pedido #$orderId",
            orderId = orderId
        )
        
        val payment = Payment(
            id = UUID.randomUUID().toString(),
            orderId = orderId,
            userId = userId,
            amount = amount,
            currency = currency,
            status = PaymentStatus.PENDING,
            paymentIntentId = paymentIntent.id,
            paymentMethodId = paymentMethodId,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        paymentRepository.save(payment)
        
        // Confirma o pagamento para criar uma cobrança
        val confirmedIntent = stripeMockService.confirmPaymentIntent(paymentIntent.id)
        val chargeId = confirmedIntent.latestCharge
        
        // Gera o evento de pagamento bem-sucedido
        val event = webhookEventGenerator.generatePaymentIntentSucceededEvent(
            paymentIntentId = paymentIntent.id,
            amount = amountInCents,
            currency = currency,
            paymentMethodId = paymentMethodId,
            chargeId = chargeId!!,
            orderId = orderId
        )
        
        // Ação
        mockMvc.perform(
            MockMvcRequestBuilders.post(webhookEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(webhookSignatureHeader, mockSignature)
                .content(objectMapper.writeValueAsString(event))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
        
        // Verificação
        val updatedPayment = paymentRepository.findByPaymentIntentId(paymentIntent.id)
        assert(updatedPayment != null)
        assert(updatedPayment!!.status == PaymentStatus.COMPLETED)
        assert(updatedPayment.completedAt != null)
    }
    
    @Test
    fun `deve processar evento de PaymentIntent falho`() {
        // Arranjo
        val orderId = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()
        val amount = BigDecimal("100.00")
        val amountInCents = amount.multiply(BigDecimal("100")).toLong()
        val currency = "brl"
        val paymentMethodId = "pm_" + UUID.randomUUID().toString().replace("-", "")
        val errorMessage = "Cartão recusado pela operadora"
        
        // Cria um pagamento pendente no banco
        val paymentIntent = stripeMockService.createPaymentIntent(
            amount = amount,
            currency = currency,
            paymentMethodId = paymentMethodId,
            description = "Pagamento do pedido #$orderId",
            orderId = orderId
        )
        
        val payment = Payment(
            id = UUID.randomUUID().toString(),
            orderId = orderId,
            userId = userId,
            amount = amount,
            currency = currency,
            status = PaymentStatus.PENDING,
            paymentIntentId = paymentIntent.id,
            paymentMethodId = paymentMethodId,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        paymentRepository.save(payment)
        
        // Faz o pagamento falhar
        stripeMockService.failPaymentIntent(paymentIntent.id, errorMessage)
        
        // Gera o evento de pagamento falho
        val event = webhookEventGenerator.generatePaymentIntentFailedEvent(
            paymentIntentId = paymentIntent.id,
            amount = amountInCents,
            currency = currency,
            paymentMethodId = paymentMethodId,
            errorMessage = errorMessage
        )
        
        // Ação
        mockMvc.perform(
            MockMvcRequestBuilders.post(webhookEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(webhookSignatureHeader, mockSignature)
                .content(objectMapper.writeValueAsString(event))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
        
        // Verificação
        val updatedPayment = paymentRepository.findByPaymentIntentId(paymentIntent.id)
        assert(updatedPayment != null)
        assert(updatedPayment!!.status == PaymentStatus.FAILED)
        assert(updatedPayment.errorMessage == errorMessage)
    }
    
    @Test
    fun `deve processar evento de PaymentIntent cancelado`() {
        // Arranjo
        val orderId = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()
        val amount = BigDecimal("100.00")
        val amountInCents = amount.multiply(BigDecimal("100")).toLong()
        val currency = "brl"
        val paymentMethodId = "pm_" + UUID.randomUUID().toString().replace("-", "")
        
        // Cria um pagamento pendente no banco
        val paymentIntent = stripeMockService.createPaymentIntent(
            amount = amount,
            currency = currency,
            paymentMethodId = paymentMethodId,
            description = "Pagamento do pedido #$orderId",
            orderId = orderId
        )
        
        val payment = Payment(
            id = UUID.randomUUID().toString(),
            orderId = orderId,
            userId = userId,
            amount = amount,
            currency = currency,
            status = PaymentStatus.PENDING,
            paymentIntentId = paymentIntent.id,
            paymentMethodId = paymentMethodId,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        paymentRepository.save(payment)
        
        // Cancela o pagamento
        stripeMockService.cancelPayment(paymentIntent.id)
        
        // Gera o evento de pagamento cancelado
        val event = webhookEventGenerator.generatePaymentIntentCanceledEvent(
            paymentIntentId = paymentIntent.id,
            amount = amountInCents,
            currency = currency
        )
        
        // Ação
        mockMvc.perform(
            MockMvcRequestBuilders.post(webhookEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(webhookSignatureHeader, mockSignature)
                .content(objectMapper.writeValueAsString(event))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
        
        // Verificação
        val updatedPayment = paymentRepository.findByPaymentIntentId(paymentIntent.id)
        assert(updatedPayment != null)
        assert(updatedPayment!!.status == PaymentStatus.CANCELED)
    }
    
    @Test
    fun `deve processar evento de cobrança reembolsada`() {
        // Arranjo
        val orderId = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()
        val amount = BigDecimal("100.00")
        val amountInCents = amount.multiply(BigDecimal("100")).toLong()
        val currency = "brl"
        val paymentMethodId = "pm_" + UUID.randomUUID().toString().replace("-", "")
        
        // Cria um pagamento completado no banco
        val paymentIntent = stripeMockService.createPaymentIntent(
            amount = amount,
            currency = currency,
            paymentMethodId = paymentMethodId,
            description = "Pagamento do pedido #$orderId",
            orderId = orderId
        )
        
        val confirmedIntent = stripeMockService.confirmPaymentIntent(paymentIntent.id)
        val chargeId = confirmedIntent.latestCharge
        
        val payment = Payment(
            id = UUID.randomUUID().toString(),
            orderId = orderId,
            userId = userId,
            amount = amount,
            currency = currency,
            status = PaymentStatus.COMPLETED,
            paymentIntentId = paymentIntent.id,
            paymentMethodId = paymentMethodId,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            completedAt = LocalDateTime.now()
        )
        paymentRepository.save(payment)
        
        // Reembolsa o pagamento
        val refund = stripeMockService.refundPayment(paymentIntent.id)
        
        // Gera o evento de reembolso
        val event = webhookEventGenerator.generateChargeRefundedEvent(
            chargeId = chargeId!!,
            paymentIntentId = paymentIntent.id,
            amount = amountInCents,
            amountRefunded = amountInCents,
            currency = currency,
            refundId = refund.id
        )
        
        // Ação
        mockMvc.perform(
            MockMvcRequestBuilders.post(webhookEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header(webhookSignatureHeader, mockSignature)
                .content(objectMapper.writeValueAsString(event))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
        
        // Verificação
        val updatedPayment = paymentRepository.findByPaymentIntentId(paymentIntent.id)
        assert(updatedPayment != null)
        assert(updatedPayment!!.status == PaymentStatus.REFUNDED)
    }
    
    @Test
    fun `deve retornar 400 quando assinatura do webhook estiver ausente`() {
        // Arranjo
        val event = webhookEventGenerator.generatePaymentIntentSucceededEvent(
            paymentIntentId = "pi_" + UUID.randomUUID().toString().replace("-", ""),
            amount = 10000,
            currency = "brl",
            paymentMethodId = "pm_" + UUID.randomUUID().toString().replace("-", ""),
            chargeId = "ch_" + UUID.randomUUID().toString().replace("-", ""),
            orderId = UUID.randomUUID().toString()
        )
        
        // Ação e verificação
        mockMvc.perform(
            MockMvcRequestBuilders.post(webhookEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }
} 