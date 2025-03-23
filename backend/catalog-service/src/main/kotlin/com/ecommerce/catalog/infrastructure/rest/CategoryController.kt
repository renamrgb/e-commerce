package com.ecommerce.catalog.infrastructure.rest

import com.ecommerce.catalog.application.dto.CategoryCreateRequest
import com.ecommerce.catalog.application.dto.CategoryDto
import com.ecommerce.catalog.application.dto.CategoryTreeResponse
import com.ecommerce.catalog.application.dto.CategoryUpdateRequest
import com.ecommerce.catalog.application.service.CategoryService
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
@RequestMapping("/api/v1/categories")
@Tag(name = "Categorias", description = "API para gerenciamento de categorias")
class CategoryController(private val categoryService: CategoryService) {

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria por ID")
    fun findById(@PathVariable id: UUID): ResponseEntity<CategoryDto> {
        val category = categoryService.findById(id)
        return ResponseEntity.ok(category)
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Buscar categoria por slug")
    fun findBySlug(@PathVariable slug: String): ResponseEntity<CategoryDto> {
        val category = categoryService.findBySlug(slug)
        return ResponseEntity.ok(category)
    }

    @GetMapping
    @Operation(summary = "Listar todas as categorias com paginação")
    fun findAll(@PageableDefault pageable: Pageable): ResponseEntity<Page<CategoryDto>> {
        val categories = categoryService.findAll(pageable)
        return ResponseEntity.ok(categories)
    }

    @GetMapping("/tree")
    @Operation(summary = "Listar todas as categorias em formato de árvore")
    fun findAllAsTree(): ResponseEntity<List<CategoryTreeResponse>> {
        val categoriesTree = categoryService.findAllAsTree()
        return ResponseEntity.ok(categoriesTree)
    }

    @GetMapping("/active/tree")
    @Operation(summary = "Listar categorias ativas em formato de árvore")
    fun findAllActiveAsTree(): ResponseEntity<List<CategoryTreeResponse>> {
        val categoriesTree = categoryService.findAllActiveAsTree()
        return ResponseEntity.ok(categoriesTree)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar uma nova categoria")
    fun create(@RequestBody @Valid request: CategoryCreateRequest): ResponseEntity<CategoryDto> {
        val category = categoryService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(category)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar uma categoria existente")
    fun update(
        @PathVariable id: UUID,
        @RequestBody @Valid request: CategoryUpdateRequest
    ): ResponseEntity<CategoryDto> {
        val category = categoryService.update(id, request)
        return ResponseEntity.ok(category)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir uma categoria")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        categoryService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/toggle-active")
    @Operation(summary = "Alternar o status de ativação de uma categoria")
    fun toggleActive(@PathVariable id: UUID): ResponseEntity<CategoryDto> {
        val category = categoryService.toggleActive(id)
        return ResponseEntity.ok(category)
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar categorias por nome")
    fun search(
        @RequestParam query: String,
        @PageableDefault pageable: Pageable
    ): ResponseEntity<Page<CategoryDto>> {
        val categories = categoryService.search(query, pageable)
        return ResponseEntity.ok(categories)
    }
} 