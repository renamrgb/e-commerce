package com.ecommerce.catalog.infrastructure.rest

import com.ecommerce.catalog.application.dto.ProductAttributeCreateRequest
import com.ecommerce.catalog.application.dto.ProductAttributeResponse
import com.ecommerce.catalog.application.dto.ProductAttributeUpdateRequest
import com.ecommerce.catalog.application.service.ProductAttributeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/product-attributes")
@Tag(name = "Atributos de Produto", description = "API para gerenciamento de atributos de produto")
class ProductAttributeController(private val productAttributeService: ProductAttributeService) {

    @GetMapping("/{id}")
    @Operation(summary = "Buscar atributo de produto por ID")
    fun findById(@PathVariable id: UUID): ResponseEntity<ProductAttributeResponse> {
        val attribute = productAttributeService.findById(id)
        return ResponseEntity.ok(attribute)
    }

    @GetMapping
    @Operation(summary = "Listar todos os atributos de produto com paginação")
    fun findAll(@PageableDefault pageable: Pageable): ResponseEntity<Page<ProductAttributeResponse>> {
        val attributes = productAttributeService.findAll(pageable)
        return ResponseEntity.ok(attributes)
    }

    @GetMapping("/filterable")
    @Operation(summary = "Listar atributos de produto filtráveis")
    fun findAllFilterable(): ResponseEntity<List<ProductAttributeResponse>> {
        val attributes = productAttributeService.findAllFilterable()
        return ResponseEntity.ok(attributes)
    }

    @GetMapping("/comparable")
    @Operation(summary = "Listar atributos de produto comparáveis")
    fun findAllComparable(): ResponseEntity<List<ProductAttributeResponse>> {
        val attributes = productAttributeService.findAllComparable()
        return ResponseEntity.ok(attributes)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar um novo atributo de produto")
    fun create(@RequestBody @Valid request: ProductAttributeCreateRequest): ResponseEntity<ProductAttributeResponse> {
        val attribute = productAttributeService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(attribute)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar um atributo de produto existente")
    fun update(
        @PathVariable id: UUID,
        @RequestBody @Valid request: ProductAttributeUpdateRequest
    ): ResponseEntity<ProductAttributeResponse> {
        val attribute = productAttributeService.update(id, request)
        return ResponseEntity.ok(attribute)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir um atributo de produto")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        productAttributeService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar atributos de produto por nome")
    fun search(
        @RequestParam query: String,
        @PageableDefault pageable: Pageable
    ): ResponseEntity<Page<ProductAttributeResponse>> {
        val attributes = productAttributeService.search(query, pageable)
        return ResponseEntity.ok(attributes)
    }
} 