package com.ecommerce.catalog.infrastructure.rest

import com.ecommerce.catalog.application.dto.BrandCreateRequest
import com.ecommerce.catalog.application.dto.BrandDto
import com.ecommerce.catalog.application.dto.BrandResponse
import com.ecommerce.catalog.application.dto.BrandUpdateRequest
import com.ecommerce.catalog.application.service.BrandService
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
@RequestMapping("/api/v1/brands")
@Tag(name = "Marcas", description = "API para gerenciamento de marcas")
class BrandController(private val brandService: BrandService) {

    @GetMapping("/{id}")
    @Operation(summary = "Buscar marca por ID")
    fun findById(@PathVariable id: UUID): ResponseEntity<BrandDto> {
        val brand = brandService.findById(id)
        return ResponseEntity.ok(brand)
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Buscar marca por slug")
    fun findBySlug(@PathVariable slug: String): ResponseEntity<BrandDto> {
        val brand = brandService.findBySlug(slug)
        return ResponseEntity.ok(brand)
    }

    @GetMapping
    @Operation(summary = "Listar todas as marcas com paginação")
    fun findAll(@PageableDefault pageable: Pageable): ResponseEntity<Page<BrandResponse>> {
        val brands = brandService.findAll(pageable)
        return ResponseEntity.ok(brands)
    }

    @GetMapping("/active")
    @Operation(summary = "Listar marcas ativas com paginação")
    fun findAllActive(@PageableDefault pageable: Pageable): ResponseEntity<Page<BrandResponse>> {
        val brands = brandService.findAllActive(pageable)
        return ResponseEntity.ok(brands)
    }

    @GetMapping("/featured")
    @Operation(summary = "Listar marcas em destaque")
    fun findAllFeatured(): ResponseEntity<List<BrandResponse>> {
        val brands = brandService.findAllFeatured()
        return ResponseEntity.ok(brands)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar uma nova marca")
    fun create(@RequestBody @Valid request: BrandCreateRequest): ResponseEntity<BrandResponse> {
        val brand = brandService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(brand)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar uma marca existente")
    fun update(
        @PathVariable id: UUID,
        @RequestBody @Valid request: BrandUpdateRequest
    ): ResponseEntity<BrandResponse> {
        val brand = brandService.update(id, request)
        return ResponseEntity.ok(brand)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir uma marca")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        brandService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/toggle-active")
    @Operation(summary = "Alternar o status de ativação de uma marca")
    fun toggleActive(@PathVariable id: UUID): ResponseEntity<BrandResponse> {
        val brand = brandService.toggleActive(id)
        return ResponseEntity.ok(brand)
    }

    @PatchMapping("/{id}/toggle-featured")
    @Operation(summary = "Alternar o status de destaque de uma marca")
    fun toggleFeatured(@PathVariable id: UUID): ResponseEntity<BrandResponse> {
        val brand = brandService.toggleFeatured(id)
        return ResponseEntity.ok(brand)
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar marcas por nome")
    fun search(
        @RequestParam query: String,
        @PageableDefault pageable: Pageable
    ): ResponseEntity<Page<BrandResponse>> {
        val brands = brandService.search(query, pageable)
        return ResponseEntity.ok(brands)
    }
} 