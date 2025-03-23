package com.ecommerce.auth.application.dto

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "Nome é obrigatório")
    @field:Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    val firstName: String,

    @field:Size(max = 100, message = "Sobrenome deve ter no máximo 100 caracteres")
    val lastName: String? = null,

    @field:NotBlank(message = "Email é obrigatório")
    @field:Email(message = "Email deve ser válido")
    val email: String,

    @field:NotBlank(message = "Senha é obrigatória")
    @field:Size(min = 8, message = "Senha deve ter pelo menos 8 caracteres")
    val password: String
) 