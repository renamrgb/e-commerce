package com.ecommerce.payment.application.service

import com.ecommerce.payment.application.dto.CreatePaymentMethodRequest
import com.ecommerce.payment.application.dto.PaymentMethodDto
import com.ecommerce.payment.application.dto.PaymentMethodResponse
import com.ecommerce.payment.application.dto.UpdatePaymentMethodRequest
import com.ecommerce.payment.application.mapper.PaymentMethodMapper
import com.ecommerce.payment.domain.model.PaymentMethod
import com.ecommerce.payment.domain.repository.PaymentMethodRepository
import com.ecommerce.payment.infrastructure.service.StripeService
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PaymentMethodService(
    private val paymentMethodRepository: PaymentMethodRepository,
    private val paymentMethodMapper: PaymentMethodMapper,
    private val stripeService: StripeService
) {
    
    private val logger = LoggerFactory.getLogger(PaymentMethodService::class.java)
    
    /**
     * Cria um novo método de pagamento
     */
    @Transactional
    fun createPaymentMethod(request: CreatePaymentMethodRequest): PaymentMethodResponse {
        logger.info("Criando método de pagamento para usuário ${request.userId}")
        
        // Aqui nós deveríamos processar os dados do cartão e obter um token do Stripe
        // Para fins de demonstração, estamos usando um token fake
        val providerTokenId = "tok_${System.currentTimeMillis()}"
        
        // Se for definido como padrão, desativa os outros métodos padrão do usuário
        if (request.setAsDefault) {
            val defaultMethod = paymentMethodRepository.findByUserIdAndIsDefault(request.userId, true)
            if (defaultMethod.isPresent) {
                val oldDefaultMethod = defaultMethod.get()
                val updatedMethod = oldDefaultMethod.copy(
                    isDefault = false,
                    updatedAt = LocalDateTime.now()
                )
                paymentMethodRepository.save(updatedMethod)
            }
        }
        
        // Cria o método de pagamento no banco de dados
        val paymentMethod = paymentMethodMapper.toEntity(request, providerTokenId)
        val savedMethod = paymentMethodRepository.save(paymentMethod)
        
        return paymentMethodMapper.toResponse(savedMethod)
    }
    
    /**
     * Atualiza um método de pagamento
     */
    @Transactional
    fun updatePaymentMethod(request: UpdatePaymentMethodRequest): PaymentMethodResponse {
        logger.info("Atualizando método de pagamento ${request.id}")
        
        val paymentMethod = paymentMethodRepository.findById(request.id)
            .orElseThrow { EntityNotFoundException("Método de pagamento não encontrado: ${request.id}") }
        
        var updated = false
        var updatedMethod = paymentMethod
        
        // Se for para definir como padrão
        if (request.setAsDefault == true && !paymentMethod.isDefault) {
            // Desativa os outros métodos padrão do usuário
            val defaultMethod = paymentMethodRepository.findByUserIdAndIsDefault(paymentMethod.userId, true)
            if (defaultMethod.isPresent) {
                val oldDefaultMethod = defaultMethod.get()
                val updatedOldMethod = oldDefaultMethod.copy(
                    isDefault = false,
                    updatedAt = LocalDateTime.now()
                )
                paymentMethodRepository.save(updatedOldMethod)
            }
            
            // Atualiza este método para padrão
            updatedMethod = updatedMethod.copy(
                isDefault = true,
                updatedAt = LocalDateTime.now()
            )
            updated = true
        }
        
        // Atualiza data de expiração se fornecida
        if (request.expiryMonth != null || request.expiryYear != null) {
            updatedMethod = updatedMethod.copy(
                expiryMonth = request.expiryMonth ?: updatedMethod.expiryMonth,
                expiryYear = request.expiryYear ?: updatedMethod.expiryYear,
                updatedAt = LocalDateTime.now()
            )
            updated = true
        }
        
        // Salva apenas se houve alterações
        return if (updated) {
            val result = paymentMethodRepository.save(updatedMethod)
            paymentMethodMapper.toResponse(result)
        } else {
            paymentMethodMapper.toResponse(paymentMethod)
        }
    }
    
    /**
     * Remove um método de pagamento
     */
    @Transactional
    fun deletePaymentMethod(id: Long, userId: String) {
        logger.info("Removendo método de pagamento $id para usuário $userId")
        
        val paymentMethod = paymentMethodRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Método de pagamento não encontrado: $id") }
        
        // Verifica se o método pertence ao usuário
        if (paymentMethod.userId != userId) {
            throw IllegalArgumentException("Método de pagamento não pertence ao usuário")
        }
        
        // Se for o método padrão, escolhe outro método como padrão
        if (paymentMethod.isDefault) {
            val otherMethods = paymentMethodRepository.findByUserId(userId)
                .filter { it.id != id }
            
            if (otherMethods.isNotEmpty()) {
                val newDefault = otherMethods.first()
                val updatedMethod = newDefault.copy(
                    isDefault = true,
                    updatedAt = LocalDateTime.now()
                )
                paymentMethodRepository.save(updatedMethod)
            }
        }
        
        paymentMethodRepository.deleteById(id)
    }
    
    /**
     * Obtém um método de pagamento pelo ID
     */
    fun getPaymentMethodById(id: Long, userId: String): PaymentMethodDto {
        val paymentMethod = paymentMethodRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Método de pagamento não encontrado: $id") }
        
        // Verifica se o método pertence ao usuário
        if (paymentMethod.userId != userId) {
            throw IllegalArgumentException("Método de pagamento não pertence ao usuário")
        }
        
        return paymentMethodMapper.toDto(paymentMethod)
    }
    
    /**
     * Obtém todos os métodos de pagamento de um usuário
     */
    fun getPaymentMethodsByUserId(userId: String): List<PaymentMethodDto> {
        return paymentMethodRepository.findByUserId(userId)
            .map { paymentMethodMapper.toDto(it) }
    }
} 