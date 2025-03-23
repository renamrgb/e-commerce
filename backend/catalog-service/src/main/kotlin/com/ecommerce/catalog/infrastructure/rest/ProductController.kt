package com.ecommerce.catalog.infrastructure.rest

import com.ecommerce.catalog.application.dto.*
import com.ecommerce.catalog.application.service.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Produtos", description = "API para gerenciamento de produtos")
class ProductController(private val productService: ProductService) {

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID")
    fun findById(@PathVariable id: UUID): ResponseEntity<ProductDetailResponse> {
        val product = productService.findById(id)
        return ResponseEntity.ok(product)
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Buscar produto por SKU")
    fun findBySku(@PathVariable sku: String): ResponseEntity<ProductDetailResponse> {
        val product = productService.findBySku(sku)
        return ResponseEntity.ok(product)
    }

    @GetMapping
    @Operation(summary = "Listar todos os produtos com paginação")
    fun findAll(@PageableDefault pageable: Pageable): ResponseEntity<ProductPage> {
        val products = productService.findAll(pageable)
        return ResponseEntity.ok(products)
    }

    @GetMapping("/active")
    @Operation(summary = "Listar produtos ativos com paginação")
    fun findAllActive(@PageableDefault pageable: Pageable): ResponseEntity<ProductPage> {
        val products = productService.findAllActive(pageable)
        return ResponseEntity.ok(products)
    }

    @GetMapping("/featured")
    @Operation(summary = "Listar produtos em destaque com paginação")
    fun findAllFeatured(@PageableDefault pageable: Pageable): ResponseEntity<ProductPage> {
        val products = productService.findAllFeatured(pageable)
        return ResponseEntity.ok(products)
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Listar produtos por categoria")
    fun findByCategory(
        @PathVariable categoryId: UUID,
        @PageableDefault pageable: Pageable
    ): ResponseEntity<ProductPage> {
        val products = productService.findByCategory(categoryId, pageable)
        return ResponseEntity.ok(products)
    }

    @GetMapping("/category/slug/{categorySlug}")
    @Operation(summary = "Listar produtos por slug da categoria")
    fun findByCategorySlug(
        @PathVariable categorySlug: String,
        @PageableDefault pageable: Pageable
    ): ResponseEntity<ProductPage> {
        val products = productService.findByCategorySlug(categorySlug, pageable)
        return ResponseEntity.ok(products)
    }

    @GetMapping("/brand/{brandId}")
    @Operation(summary = "Listar produtos por marca")
    fun findByBrand(
        @PathVariable brandId: UUID,
        @PageableDefault pageable: Pageable
    ): ResponseEntity<ProductPage> {
        val products = productService.findByBrand(brandId, pageable)
        return ResponseEntity.ok(products)
    }

    @GetMapping("/brand/slug/{brandSlug}")
    @Operation(summary = "Listar produtos por slug da marca")
    fun findByBrandSlug(
        @PathVariable brandSlug: String,
        @PageableDefault pageable: Pageable
    ): ResponseEntity<ProductPage> {
        val products = productService.findByBrandSlug(brandSlug, pageable)
        return ResponseEntity.ok(products)
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar produtos com filtros avançados")
    fun search(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) categoryId: UUID?,
        @RequestParam(required = false) categorySlug: String?,
        @RequestParam(required = false) brandId: UUID?,
        @RequestParam(required = false) minPrice: BigDecimal?,
        @RequestParam(required = false) maxPrice: BigDecimal?,
        @RequestParam(required = false) onSale: Boolean?,
        @RequestParam(required = false) featured: Boolean?,
        @RequestParam(required = false) sortBy: String?,
        @RequestParam(required = false) sortDirection: String?,
        @PageableDefault pageable: Pageable
    ): ResponseEntity<ProductPage> {
        val searchRequest = ProductSearchRequest(
            keyword = keyword,
            categoryId = categoryId,
            categorySlug = categorySlug,
            brandId = brandId,
            minPrice = minPrice,
            maxPrice = maxPrice,
            onSale = onSale,
            featured = featured,
            sortBy = sortBy,
            sortDirection = sortDirection,
            page = pageable.pageNumber,
            size = pageable.pageSize
        )
        val products = productService.search(searchRequest, pageable)
        return ResponseEntity.ok(products)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar um novo produto")
    fun create(@RequestBody @Valid request: ProductCreateRequest): ResponseEntity<ProductResponse> {
        val product = productService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(product)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar um produto existente")
    fun update(
        @PathVariable id: UUID,
        @RequestBody @Valid request: ProductUpdateRequest
    ): ResponseEntity<ProductResponse> {
        val product = productService.update(id, request)
        return ResponseEntity.ok(product)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir um produto")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        productService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/toggle-active")
    @Operation(summary = "Alternar o status de ativação de um produto")
    fun toggleActive(@PathVariable id: UUID): ResponseEntity<ProductResponse> {
        val product = productService.toggleActive(id)
        return ResponseEntity.ok(product)
    }

    @PatchMapping("/{id}/toggle-featured")
    @Operation(summary = "Alternar o status de destaque de um produto")
    fun toggleFeatured(@PathVariable id: UUID): ResponseEntity<ProductResponse> {
        val product = productService.toggleFeatured(id)
        return ResponseEntity.ok(product)
    }
} 