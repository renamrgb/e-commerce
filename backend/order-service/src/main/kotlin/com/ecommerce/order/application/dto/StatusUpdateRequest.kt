package com.ecommerce.order.application.dto

import javax.validation.constraints.NotBlank

/**
 * DTO para solicitação de atualização de status de pedido
 */
data class StatusUpdateRequest(
    @field:NotBlank(message = "Status é obrigatório")
    val status: String,
    
    val comment: String? = null
) 