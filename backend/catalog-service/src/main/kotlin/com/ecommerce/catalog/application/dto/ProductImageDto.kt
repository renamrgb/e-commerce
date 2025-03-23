package com.ecommerce.catalog.application.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.OffsetDateTime
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductImageDto(
    val id: UUID? = null,
    val url: String,
    val altText: String? = null,
    val position: Int = 0,
    val isPrimary: Boolean = false,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null
)

data class ProductImageCreateRequest(
    @field:NotBlank(message = "A URL da imagem é obrigatória")
    @field:Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", message = "URL da imagem inválida")
    val url: String,

    @field:Size(max = 255, message = "O texto alternativo deve ter no máximo 255 caracteres")
    val altText: String? = null,

    val position: Int = 0,
    val isPrimary: Boolean = false
)

data class ProductImageUpdateRequest(
    @field:Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", message = "URL da imagem inválida")
    val url: String? = null,

    @field:Size(max = 255, message = "O texto alternativo deve ter no máximo 255 caracteres")
    val altText: String? = null,
    
    val position: Int? = null,
    val isPrimary: Boolean? = null
) 