package com.ecommerce.auth.application.dto

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "Email é obrigatório")
    @field:Email(message = "Email deve ser válido")
    val email: String,

    @field:NotBlank(message = "Senha é obrigatória")
    val password: String
) 