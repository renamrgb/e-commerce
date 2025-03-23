package com.ecommerce.order.application.dto

import java.util.UUID
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

/**
 * DTO para solicitação de criação de pedido
 */
data class OrderCreateRequest(
    @field:NotNull(message = "ID do carrinho é obrigatório")
    val cartId: UUID,
    
    @field:NotBlank(message = "Endereço de entrega é obrigatório")
    val shippingAddress: String,
    
    @field:NotBlank(message = "Endereço de cobrança é obrigatório")
    val billingAddress: String,
    
    @field:NotBlank(message = "Método de pagamento é obrigatório")
    val paymentMethod: String,
    
    @field:NotBlank(message = "Método de envio é obrigatório")
    val shippingMethod: String,
    
    val paymentIntentId: String? = null,
    
    val notes: String? = null
) 