package com.ecommerce.auth.application.dto

import javax.validation.constraints.Size

data class UserUpdateRequest(
    @field:Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    val firstName: String? = null,

    @field:Size(max = 100, message = "Sobrenome deve ter no máximo 100 caracteres")
    val lastName: String? = null,

    val active: Boolean? = null,

    val verified: Boolean? = null,

    val roles: Set<String>? = null
) 