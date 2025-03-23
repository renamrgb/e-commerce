package com.ecommerce.catalog.application.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.OffsetDateTime
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductAttributeDto(
    val id: UUID? = null,
    val name: String,
    val description: String? = null,
    val type: String,
    val displayOrder: Int = 0,
    val isRequired: Boolean = false,
    val isFilterable: Boolean = false,
    val isComparable: Boolean = false,
    val isVisibleOnProductPage: Boolean = true,
    val allowedValues: List<String>? = null,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null
)

data class ProductAttributeCreateRequest(
    @field:NotBlank(message = "O nome é obrigatório")
    @field:Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    val name: String,

    @field:Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
    val description: String? = null,

    @field:NotBlank(message = "O tipo é obrigatório")
    @field:Size(min = 2, max = 50, message = "O tipo deve ter entre 2 e 50 caracteres")
    val type: String,

    val displayOrder: Int = 0,
    val isRequired: Boolean = false,
    val isFilterable: Boolean = false,
    val isComparable: Boolean = false,
    val isVisibleOnProductPage: Boolean = true,
    val allowedValues: List<String>? = null
)

data class ProductAttributeUpdateRequest(
    @field:Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    val name: String? = null,

    @field:Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
    val description: String? = null,

    @field:Size(min = 2, max = 50, message = "O tipo deve ter entre 2 e 50 caracteres")
    val type: String? = null,

    val displayOrder: Int? = null,
    val isRequired: Boolean? = null,
    val isFilterable: Boolean? = null,
    val isComparable: Boolean? = null,
    val isVisibleOnProductPage: Boolean? = null,
    val allowedValues: List<String>? = null
)

data class ProductAttributeResponse(
    val id: UUID,
    val name: String,
    val description: String? = null,
    val type: String,
    val displayOrder: Int,
    val isRequired: Boolean,
    val isFilterable: Boolean,
    val isComparable: Boolean,
    val isVisibleOnProductPage: Boolean,
    val allowedValues: List<String>? = null,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

data class ProductAttributeValueDto(
    val id: UUID? = null,
    val productId: UUID,
    val attributeId: UUID,
    val attributeName: String? = null,
    val value: String,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null
)

data class ProductAttributeValueCreateRequest(
    @field:NotNull(message = "O ID do produto é obrigatório")
    val productId: UUID,

    @field:NotNull(message = "O ID do atributo é obrigatório")
    val attributeId: UUID,

    @field:NotBlank(message = "O valor é obrigatório")
    @field:Size(min = 1, max = 500, message = "O valor deve ter entre 1 e 500 caracteres")
    val value: String
)

data class ProductAttributeValueUpdateRequest(
    @field:NotBlank(message = "O valor é obrigatório")
    @field:Size(min = 1, max = 500, message = "O valor deve ter entre 1 e 500 caracteres")
    val value: String? = null
) 