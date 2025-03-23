package com.ecommerce.auth.application.dto

import java.util.UUID

data class JwtResponse(
    val token: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val userId: UUID,
    val email: String,
    val firstName: String,
    val lastName: String?,
    val roles: List<String>
) 