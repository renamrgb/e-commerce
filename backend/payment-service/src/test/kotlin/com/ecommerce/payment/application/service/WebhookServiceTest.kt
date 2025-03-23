package com.ecommerce.payment.application.service

import com.ecommerce.payment.domain.model.Payment
import com.ecommerce.payment.domain.model.PaymentStatus
import com.ecommerce.payment.domain.repository.PaymentRepository
import com.ecommerce.payment.infrastructure.kafka.PaymentEventProducer
import com.stripe.model.Charge
import com.stripe.model.Event
import com.stripe.model.EventDataObjectDeserializer
import com.stripe.model.PaymentIntent
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@ExtendWith(SpringExtension::class)
class WebhookServiceTest {

    @MockK
    lateinit var paymentRepository: PaymentRepository

    @MockK
    lateinit var paymentEventProducer: PaymentEventProducer

    @InjectMockKs
    lateinit var webhookService: WebhookService

    private val paymentIntentId = "pi_123456789"
    private val paymentId = 1L
    private val orderId = "order_123"
    private val userId = "user_123"
    private val amount = BigDecimal("100.00")
    private val currency = "BRL"

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `quando receber evento payment_intent_succeeded deve atualizar status para COMPLETED`() {
        // Arrange
        val payment = createPayment(PaymentStatus.PROCESSING)
        val paymentIntent = mockPaymentIntent()
        val event = mockEvent("payment_intent.succeeded", paymentIntent)

        every { paymentRepository.findByPaymentIntentId(paymentIntentId) } returns Optional.of(payment)
        every { paymentRepository.save(any()) } returns payment.copy(status = PaymentStatus.COMPLETED)
        every { paymentEventProducer.sendPaymentCompletedEvent(any()) } just runs

        // Act
        webhookService.processStripeEvent(event)

        // Assert
        verify {
            paymentRepository.findByPaymentIntentId(paymentIntentId)
            paymentRepository.save(match { it.status == PaymentStatus.COMPLETED })
            paymentEventProducer.sendPaymentCompletedEvent(any())
        }
    }

    @Test
    fun `quando receber evento payment_intent_payment_failed deve atualizar status para FAILED`() {
        // Arrange
        val payment = createPayment(PaymentStatus.PROCESSING)
        val paymentIntent = mockPaymentIntent().apply {
            lastPaymentError = PaymentIntent.LastPaymentError()
            lastPaymentError.message = "Cartão negado"
        }
        val event = mockEvent("payment_intent.payment_failed", paymentIntent)

        every { paymentRepository.findByPaymentIntentId(paymentIntentId) } returns Optional.of(payment)
        every { paymentRepository.save(any()) } returns payment.copy(status = PaymentStatus.FAILED)
        every { paymentEventProducer.sendPaymentFailedEvent(any()) } just runs

        // Act
        webhookService.processStripeEvent(event)

        // Assert
        verify {
            paymentRepository.findByPaymentIntentId(paymentIntentId)
            paymentRepository.save(match { 
                it.status == PaymentStatus.FAILED && it.errorMessage == "Cartão negado" 
            })
            paymentEventProducer.sendPaymentFailedEvent(any())
        }
    }

    @Test
    fun `quando receber evento payment_intent_canceled deve atualizar status para CANCELLED`() {
        // Arrange
        val payment = createPayment(PaymentStatus.PROCESSING)
        val paymentIntent = mockPaymentIntent()
        val event = mockEvent("payment_intent.canceled", paymentIntent)

        every { paymentRepository.findByPaymentIntentId(paymentIntentId) } returns Optional.of(payment)
        every { paymentRepository.save(any()) } returns payment.copy(status = PaymentStatus.CANCELLED)
        every { paymentEventProducer.sendPaymentCanceledEvent(any()) } just runs

        // Act
        webhookService.processStripeEvent(event)

        // Assert
        verify {
            paymentRepository.findByPaymentIntentId(paymentIntentId)
            paymentRepository.save(match { it.status == PaymentStatus.CANCELLED })
            paymentEventProducer.sendPaymentCanceledEvent(any())
        }
    }

    @Test
    fun `quando receber evento charge_refunded deve atualizar status para REFUNDED`() {
        // Arrange
        val payment = createPayment(PaymentStatus.COMPLETED)
        val charge = mockCharge()
        val event = mockEvent("charge.refunded", charge)

        every { paymentRepository.findByPaymentIntentId(paymentIntentId) } returns Optional.of(payment)
        every { paymentRepository.save(any()) } returns payment.copy(status = PaymentStatus.REFUNDED)
        every { paymentEventProducer.sendPaymentRefundedEvent(any()) } just runs

        // Act
        webhookService.processStripeEvent(event)

        // Assert
        verify {
            paymentRepository.findByPaymentIntentId(paymentIntentId)
            paymentRepository.save(match { it.status == PaymentStatus.REFUNDED })
            paymentEventProducer.sendPaymentRefundedEvent(any())
        }
    }

    @Test
    fun `quando receber evento não tratado não deve fazer alterações`() {
        // Arrange
        val paymentIntent = mockPaymentIntent()
        val event = mockEvent("payment_intent.created", paymentIntent)

        // Act
        webhookService.processStripeEvent(event)

        // Assert
        verify(exactly = 0) {
            paymentRepository.findByPaymentIntentId(any())
            paymentRepository.save(any())
            paymentEventProducer.sendPaymentCompletedEvent(any())
            paymentEventProducer.sendPaymentFailedEvent(any())
            paymentEventProducer.sendPaymentCanceledEvent(any())
            paymentEventProducer.sendPaymentRefundedEvent(any())
        }
    }

    @Test
    fun `quando não encontrar pagamento para o paymentIntentId não deve fazer alterações`() {
        // Arrange
        val paymentIntent = mockPaymentIntent()
        val event = mockEvent("payment_intent.succeeded", paymentIntent)

        every { paymentRepository.findByPaymentIntentId(paymentIntentId) } returns Optional.empty()

        // Act
        webhookService.processStripeEvent(event)

        // Assert
        verify {
            paymentRepository.findByPaymentIntentId(paymentIntentId)
        }
        verify(exactly = 0) {
            paymentRepository.save(any())
            paymentEventProducer.sendPaymentCompletedEvent(any())
        }
    }

    private fun createPayment(status: PaymentStatus): Payment {
        return Payment(
            id = paymentId,
            orderId = orderId,
            userId = userId,
            amount = amount,
            currency = currency,
            status = status,
            paymentIntentId = paymentIntentId,
            paymentMethodId = null,
            errorMessage = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            completedAt = null
        )
    }

    private fun mockPaymentIntent(): PaymentIntent {
        val paymentIntent = mockk<PaymentIntent>()
        every { paymentIntent.id } returns paymentIntentId
        every { paymentIntent.status } returns "succeeded"
        every { paymentIntent.lastPaymentError } returns null
        return paymentIntent
    }

    private fun mockCharge(): Charge {
        val charge = mockk<Charge>()
        every { charge.paymentIntent } returns paymentIntentId
        return charge
    }

    private fun <T> mockEvent(eventType: String, objectToDeserialize: T): Event {
        val event = mockk<Event>()
        val deserializer = mockk<EventDataObjectDeserializer>()

        every { event.type } returns eventType
        every { event.dataObjectDeserializer } returns deserializer
        every { deserializer.deserializeObject<T>() } returns Optional.of(objectToDeserialize)

        return event
    }
} 