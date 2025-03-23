package com.ecommerce.payment.application.service

import com.ecommerce.payment.application.dto.*
import com.ecommerce.payment.application.mapper.PaymentMapper
import com.ecommerce.payment.domain.model.Payment
import com.ecommerce.payment.domain.model.PaymentStatus
import com.ecommerce.payment.domain.repository.PaymentMethodRepository
import com.ecommerce.payment.domain.repository.PaymentRepository
import com.ecommerce.payment.infrastructure.service.StripeService
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val paymentMapper: PaymentMapper,
    private val stripeService: StripeService
) {
    
    private val logger = LoggerFactory.getLogger(PaymentService::class.java)
    
    /**
     * Cria um novo pagamento
     */
    @Transactional
    fun createPayment(request: CreatePaymentRequest): PaymentResponse {
        logger.info("Criando pagamento para pedido ${request.orderId}")
        
        // Verifica se o método de pagamento existe
        val paymentMethodId = request.paymentMethodId
        if (paymentMethodId != null) {
            val paymentMethod = paymentMethodRepository.findById(paymentMethodId.toLong())
                .orElseThrow { EntityNotFoundException("Método de pagamento não encontrado: $paymentMethodId") }
            
            // Verifica se o método de pagamento pertence ao usuário
            if (paymentMethod.userId != request.userId) {
                throw IllegalArgumentException("Método de pagamento não pertence ao usuário")
            }
        }
        
        // Cria o pagamento no banco de dados
        val payment = paymentMapper.toEntity(request)
        val savedPayment = paymentRepository.save(payment)
        
        return paymentMapper.toResponse(savedPayment)
    }
    
    /**
     * Processa um pagamento
     */
    @Transactional
    fun processPayment(request: ProcessPaymentRequest): PaymentResponse {
        logger.info("Processando pagamento ${request.paymentId}")
        
        val payment = paymentRepository.findById(request.paymentId)
            .orElseThrow { EntityNotFoundException("Pagamento não encontrado: ${request.paymentId}") }
        
        if (payment.status != PaymentStatus.PENDING) {
            throw IllegalStateException("Pagamento não está pendente: ${payment.status}")
        }
        
        // Verifica se tem método de pagamento associado
        if (payment.paymentMethodId == null) {
            throw IllegalStateException("Pagamento não tem método de pagamento associado")
        }
        
        try {
            // Atualiza o status para em processamento
            val processingPayment = payment.copy(
                status = PaymentStatus.PROCESSING,
                updatedAt = LocalDateTime.now()
            )
            paymentRepository.save(processingPayment)
            
            // Recupera o método de pagamento
            val paymentMethod = paymentMethodRepository.findById(payment.paymentMethodId.toLong())
                .orElseThrow { EntityNotFoundException("Método de pagamento não encontrado") }
            
            // Processa o pagamento no Stripe
            val paymentIntent = stripeService.createPaymentIntent(
                amount = payment.amount,
                currency = payment.currency,
                paymentMethodId = paymentMethod.providerTokenId,
                description = "Pagamento para pedido ${payment.orderId}",
                orderId = payment.orderId
            )
            
            // Atualiza o pagamento de acordo com o resultado do Stripe
            val status = when (paymentIntent.status) {
                "succeeded" -> PaymentStatus.COMPLETED
                "requires_action" -> PaymentStatus.PROCESSING
                "requires_confirmation" -> PaymentStatus.PROCESSING
                "canceled" -> PaymentStatus.CANCELLED
                else -> PaymentStatus.FAILED
            }
            
            val updatedPayment = processingPayment.copy(
                status = status,
                paymentIntentId = paymentIntent.id,
                updatedAt = LocalDateTime.now(),
                completedAt = if (status == PaymentStatus.COMPLETED) LocalDateTime.now() else null,
                errorMessage = if (status == PaymentStatus.FAILED) "Falha ao processar o pagamento" else null
            )
            
            val result = paymentRepository.save(updatedPayment)
            return paymentMapper.toResponse(result)
            
        } catch (e: Exception) {
            logger.error("Erro ao processar pagamento", e)
            
            // Atualiza o pagamento para falha
            val failedPayment = payment.copy(
                status = PaymentStatus.FAILED,
                updatedAt = LocalDateTime.now(),
                errorMessage = e.message
            )
            
            val result = paymentRepository.save(failedPayment)
            return paymentMapper.toResponse(result)
        }
    }
    
    /**
     * Cancela um pagamento
     */
    @Transactional
    fun cancelPayment(request: CancelPaymentRequest): PaymentResponse {
        logger.info("Cancelando pagamento ${request.paymentId}")
        
        val payment = paymentRepository.findById(request.paymentId)
            .orElseThrow { EntityNotFoundException("Pagamento não encontrado: ${request.paymentId}") }
        
        if (payment.status == PaymentStatus.COMPLETED) {
            throw IllegalStateException("Não é possível cancelar um pagamento já concluído")
        }
        
        if (payment.status == PaymentStatus.CANCELLED) {
            return paymentMapper.toResponse(payment)
        }
        
        // Se o pagamento já possui um ID do Stripe, cancela no gateway
        if (payment.paymentIntentId != null) {
            try {
                stripeService.cancelPayment(payment.paymentIntentId)
            } catch (e: Exception) {
                logger.error("Erro ao cancelar pagamento no Stripe", e)
                // Continua com o cancelamento local mesmo se falhar no Stripe
            }
        }
        
        // Atualiza o pagamento para cancelado
        val cancelledPayment = payment.copy(
            status = PaymentStatus.CANCELLED,
            updatedAt = LocalDateTime.now(),
            errorMessage = request.reason ?: "Pagamento cancelado pelo usuário"
        )
        
        val result = paymentRepository.save(cancelledPayment)
        return paymentMapper.toResponse(result)
    }
    
    /**
     * Reembolsa um pagamento
     */
    @Transactional
    fun refundPayment(request: RefundPaymentRequest): PaymentResponse {
        logger.info("Reembolsando pagamento ${request.paymentId}")
        
        val payment = paymentRepository.findById(request.paymentId)
            .orElseThrow { EntityNotFoundException("Pagamento não encontrado: ${request.paymentId}") }
        
        if (payment.status != PaymentStatus.COMPLETED) {
            throw IllegalStateException("Apenas pagamentos concluídos podem ser reembolsados")
        }
        
        // TODO: Implementar reembolso no Stripe
        
        // Atualiza o pagamento para reembolsado
        val refundedPayment = payment.copy(
            status = PaymentStatus.REFUNDED,
            updatedAt = LocalDateTime.now()
        )
        
        val result = paymentRepository.save(refundedPayment)
        return paymentMapper.toResponse(result)
    }
    
    /**
     * Obtém um pagamento pelo ID
     */
    fun getPaymentById(id: Long): PaymentDto {
        return paymentRepository.findById(id)
            .map { paymentMapper.toDto(it) }
            .orElseThrow { EntityNotFoundException("Pagamento não encontrado: $id") }
    }
    
    /**
     * Obtém um pagamento pelo ID do pedido
     */
    fun getPaymentByOrderId(orderId: String): PaymentDto {
        return paymentRepository.findByOrderId(orderId)
            .map { paymentMapper.toDto(it) }
            .orElseThrow { EntityNotFoundException("Pagamento não encontrado para pedido: $orderId") }
    }
    
    /**
     * Obtém todos os pagamentos de um usuário
     */
    fun getPaymentsByUserId(userId: String): List<PaymentDto> {
        return paymentRepository.findByUserId(userId)
            .map { paymentMapper.toDto(it) }
    }
} 