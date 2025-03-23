package com.ecommerce.catalog.application.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.OffsetDateTime
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CategoryDto(
    val id: UUID? = null,
    val name: String,
    val description: String? = null,
    val slug: String,
    val parentId: UUID? = null,
    val imageUrl: String? = null,
    val active: Boolean = true,
    val children: List<CategoryDto>? = null,
    val productCount: Long? = null,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null
)

data class CategoryCreateRequest(
    @field:NotBlank(message = "O nome é obrigatório")
    @field:Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    val name: String,

    @field:Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
    val description: String? = null,

    @field:Pattern(regexp = "^[a-z0-9-]+$", message = "O slug deve conter apenas letras minúsculas, números e hífens")
    @field:Size(min = 2, max = 100, message = "O slug deve ter entre 2 e 100 caracteres")
    val slug: String? = null, // Será gerado automaticamente se não for fornecido

    val parentId: UUID? = null,

    @field:Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", message = "URL da imagem inválida")
    val imageUrl: String? = null,
    
    val active: Boolean = true
)

data class CategoryUpdateRequest(
    @field:Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    val name: String? = null,

    @field:Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
    val description: String? = null,

    @field:Pattern(regexp = "^[a-z0-9-]+$", message = "O slug deve conter apenas letras minúsculas, números e hífens")
    @field:Size(min = 2, max = 100, message = "O slug deve ter entre 2 e 100 caracteres")
    val slug: String? = null,

    val parentId: UUID? = null,

    @field:Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", message = "URL da imagem inválida")
    val imageUrl: String? = null,
    
    val active: Boolean? = null,
    
    val clearParent: Boolean? = null
)

data class CategoryResponse(
    val id: UUID,
    val name: String,
    val description: String? = null,
    val slug: String,
    val parentId: UUID? = null,
    val imageUrl: String? = null,
    val active: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

data class CategoryTreeResponse(
    val id: UUID,
    val name: String,
    val description: String? = null,
    val slug: String,
    val parentId: UUID? = null,
    val imageUrl: String? = null,
    val active: Boolean,
    val children: List<CategoryTreeResponse>? = null,
    val productCount: Long? = null,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null
) 