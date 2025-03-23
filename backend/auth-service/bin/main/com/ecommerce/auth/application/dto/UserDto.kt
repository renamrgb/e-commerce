package com.ecommerce.auth.application.dto

import java.time.LocalDateTime
import java.util.UUID

data class UserDto(
    val id: UUID,
    val firstName: String,
    val lastName: String?,
    val email: String,
    val active: Boolean,
    val verified: Boolean,
    val roles: Set<RoleDto>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) 