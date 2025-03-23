package com.ecommerce.auth.application.dto

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class PasswordResetRequest(
    @field:NotBlank(message = "Token é obrigatório")
    val token: String,
    
    @field:NotBlank(message = "Nova senha é obrigatória")
    @field:Size(min = 8, message = "Nova senha deve ter pelo menos 8 caracteres")
    val newPassword: String
) 