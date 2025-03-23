package com.ecommerce.catalog.application.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.OffsetDateTime
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BrandDto(
    val id: UUID? = null,
    val name: String,
    val description: String? = null,
    val slug: String,
    val logoUrl: String? = null,
    val websiteUrl: String? = null,
    val active: Boolean = true,
    val productCount: Int? = null,
    val featured: Boolean = false,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null
)

data class BrandCreateRequest(
    @field:NotBlank(message = "O nome é obrigatório")
    @field:Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    val name: String,

    @field:Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
    val description: String? = null,

    @field:Pattern(regexp = "^[a-z0-9-]+$", message = "O slug deve conter apenas letras minúsculas, números e hífens")
    @field:Size(min = 2, max = 100, message = "O slug deve ter entre 2 e 100 caracteres")
    val slug: String? = null,

    @field:Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", message = "URL do logo inválida")
    val logoUrl: String? = null,

    @field:Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", message = "URL do website inválida")
    val websiteUrl: String? = null,
    
    val active: Boolean = true,
    val featured: Boolean = false
)

data class BrandUpdateRequest(
    @field:Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    val name: String? = null,

    @field:Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
    val description: String? = null,

    @field:Pattern(regexp = "^[a-z0-9-]+$", message = "O slug deve conter apenas letras minúsculas, números e hífens")
    @field:Size(min = 2, max = 100, message = "O slug deve ter entre 2 e 100 caracteres")
    val slug: String? = null,

    @field:Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", message = "URL do logo inválida")
    val logoUrl: String? = null,

    @field:Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", message = "URL do website inválida")
    val websiteUrl: String? = null,
    
    val active: Boolean? = null,
    val featured: Boolean? = null
)

data class BrandResponse(
    val id: UUID,
    val name: String,
    val description: String? = null,
    val slug: String,
    val logoUrl: String? = null,
    val websiteUrl: String? = null,
    val active: Boolean,
    val productCount: Int,
    val featured: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
) 