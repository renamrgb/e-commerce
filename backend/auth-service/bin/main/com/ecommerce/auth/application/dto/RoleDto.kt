package com.ecommerce.auth.application.dto

import java.util.UUID

data class RoleDto(
    val id: UUID,
    val name: String,
    val description: String?
) 