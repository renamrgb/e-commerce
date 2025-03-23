package com.ecommerce.catalog.application.service

import com.ecommerce.catalog.application.dto.*
import com.ecommerce.catalog.application.mapper.ProductMapper
import com.ecommerce.catalog.domain.entity.Product
import com.ecommerce.catalog.domain.entity.ProductAttributeValue
import com.ecommerce.catalog.domain.entity.ProductImage
import com.ecommerce.catalog.domain.entity.ProductVariant
import com.ecommerce.catalog.domain.exception.EntityNotFoundException
import com.ecommerce.catalog.domain.repository.*
import com.ecommerce.catalog.infrastructure.messaging.producer.ProductEventProducer
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*
import javax.persistence.criteria.JoinType
import javax.persistence.criteria.Predicate

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val brandRepository: BrandRepository,
    private val productAttributeRepository: ProductAttributeRepository,
    private val productAttributeValueRepository: ProductAttributeValueRepository,
    private val productImageRepository: ProductImageRepository,
    private val productVariantRepository: ProductVariantRepository,
    private val productMapper: ProductMapper,
    private val productEventProducer: ProductEventProducer
) {

    @Cacheable(value = ["products"], key = "#id")
    @Transactional(readOnly = true)
    fun findById(id: UUID): ProductDetailResponse {
        val product = productRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("Produto não encontrado com ID: $id")
        
        // Buscar produtos relacionados (mesma categoria)
        val relatedProducts = product.categories?.firstOrNull()?.let { category ->
            productRepository.findByCategoriesContainingAndIdNot(category, product.id, Pageable.ofSize(4))
                .content
        } ?: listOf()
        
        return productMapper.toDetailResponse(product, relatedProducts)
    }

    @Cacheable(value = ["products"], key = "#sku")
    @Transactional(readOnly = true)
    fun findBySku(sku: String): ProductDetailResponse {
        val product = productRepository.findBySku(sku)
            ?: throw EntityNotFoundException("Produto não encontrado com SKU: $sku")
        
        // Buscar produtos relacionados (mesma categoria)
        val relatedProducts = product.categories?.firstOrNull()?.let { category ->
            productRepository.findByCategoriesContainingAndIdNot(category, product.id, Pageable.ofSize(4))
                .content
        } ?: listOf()
        
        return productMapper.toDetailResponse(product, relatedProducts)
    }

    @Cacheable(value = ["products"], key = "'all-page:' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): ProductPage {
        val products = productRepository.findAll(pageable)
        return productMapper.toPage(products)
    }

    @Cacheable(value = ["products"], key = "'active-page:' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    fun findAllActive(pageable: Pageable): ProductPage {
        val products = productRepository.findAllByActiveTrue(pageable)
        return productMapper.toPage(products)
    }

    @Cacheable(value = ["products"], key = "'featured'")
    @Transactional(readOnly = true)
    fun findAllFeatured(pageable: Pageable): ProductPage {
        val products = productRepository.findAllByActiveTrueAndFeaturedTrue(pageable)
        return productMapper.toPage(products)
    }

    @Cacheable(value = ["products"], key = "'category:' + #categoryId + ':page:' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    fun findByCategory(categoryId: UUID, pageable: Pageable): ProductPage {
        val category = categoryRepository.findByIdOrNull(categoryId)
            ?: throw EntityNotFoundException("Categoria não encontrada com ID: $categoryId")
        
        val products = productRepository.findByCategoriesContaining(category, pageable)
        return productMapper.toPage(products)
    }

    @Cacheable(value = ["products"], key = "'category-slug:' + #categorySlug + ':page:' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    fun findByCategorySlug(categorySlug: String, pageable: Pageable): ProductPage {
        val category = categoryRepository.findBySlug(categorySlug)
            ?: throw EntityNotFoundException("Categoria não encontrada com slug: $categorySlug")
        
        val products = productRepository.findByCategoriesContaining(category, pageable)
        return productMapper.toPage(products)
    }

    @Cacheable(value = ["products"], key = "'brand:' + #brandId + ':page:' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    fun findByBrand(brandId: UUID, pageable: Pageable): ProductPage {
        val brand = brandRepository.findByIdOrNull(brandId)
            ?: throw EntityNotFoundException("Marca não encontrada com ID: $brandId")
        
        val products = productRepository.findByBrand(brand, pageable)
        return productMapper.toPage(products)
    }

    @Cacheable(value = ["products"], key = "'brand-slug:' + #brandSlug + ':page:' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    fun findByBrandSlug(brandSlug: String, pageable: Pageable): ProductPage {
        val brand = brandRepository.findBySlug(brandSlug)
            ?: throw EntityNotFoundException("Marca não encontrada com slug: $brandSlug")
        
        val products = productRepository.findByBrand(brand, pageable)
        return productMapper.toPage(products)
    }

    @Cacheable(value = ["products"], key = "'search:' + #request.toString() + ':page:' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    fun search(request: ProductSearchRequest, pageable: Pageable): ProductPage {
        val specification = createSpecification(request)
        val products = productRepository.findAll(specification, pageable)
        return productMapper.toPage(products)
    }

    @CacheEvict(value = ["products"], allEntries = true)
    @Transactional
    fun create(request: ProductCreateRequest): ProductResponse {
        // Verificar se já existe um produto com o mesmo SKU
        if (productRepository.existsBySku(request.sku)) {
            throw IllegalArgumentException("Já existe um produto com o SKU: ${request.sku}")
        }
        
        // Obter brand se fornecido
        val brand = request.brandId?.let {
            brandRepository.findByIdOrNull(it)
                ?: throw EntityNotFoundException("Marca não encontrada com ID: $it")
        }
        
        // Obter categorias
        val categories = request.categoryIds?.mapNotNull { categoryId ->
            categoryRepository.findByIdOrNull(categoryId)
                ?: throw EntityNotFoundException("Categoria não encontrada com ID: $categoryId")
        } ?: listOf()
        
        // Criar produto
        val product = productMapper.toEntity(request, brand, categories)
        val savedProduct = productRepository.save(product)
        
        // Adicionar imagens
        request.images?.forEach { imageRequest ->
            val image = ProductImage(
                id = UUID.randomUUID(),
                product = savedProduct,
                url = imageRequest.url,
                altText = imageRequest.altText,
                position = imageRequest.position,
                isPrimary = imageRequest.isPrimary
            )
            productImageRepository.save(image)
            
            if (savedProduct.images == null) {
                savedProduct.images = mutableListOf()
            }
            savedProduct.images!!.add(image)
        }
        
        // Adicionar atributos
        request.attributes?.forEach { (attributeId, value) ->
            val attribute = productAttributeRepository.findByIdOrNull(attributeId)
                ?: throw EntityNotFoundException("Atributo não encontrado com ID: $attributeId")
            
            val attributeValue = ProductAttributeValue(
                id = UUID.randomUUID(),
                product = savedProduct,
                attribute = attribute,
                value = value
            )
            productAttributeValueRepository.save(attributeValue)
            
            if (savedProduct.attributeValues == null) {
                savedProduct.attributeValues = mutableListOf()
            }
            savedProduct.attributeValues!!.add(attributeValue)
        }
        
        val productResponse = productMapper.toResponse(savedProduct)
        
        // Publicar evento de produto criado
        productEventProducer.publishProductCreatedEvent(productResponse)
        
        return productResponse
    }

    @CacheEvict(value = ["products"], allEntries = true)
    @Transactional
    fun update(id: UUID, request: ProductUpdateRequest): ProductResponse {
        val product = productRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("Produto não encontrado com ID: $id")
        
        // Obter marca se fornecida
        val brand = request.brandId?.let {
            brandRepository.findByIdOrNull(it)
                ?: throw EntityNotFoundException("Marca não encontrada com ID: $it")
        }
        
        // Obter categorias
        val categoriesToAdd = request.categoryIds?.mapNotNull { categoryId ->
            categoryRepository.findByIdOrNull(categoryId)
                ?: throw EntityNotFoundException("Categoria não encontrada com ID: $categoryId")
        } ?: listOf()
        
        // Atualizar produto
        val updatedProduct = productMapper.updateEntity(product, request, brand, categoriesToAdd)
        
        // Adicionar novos atributos
        request.attributesToAdd?.forEach { (attributeId, value) ->
            val attribute = productAttributeRepository.findByIdOrNull(attributeId)
                ?: throw EntityNotFoundException("Atributo não encontrado com ID: $attributeId")
            
            // Verificar se o produto já tem este atributo
            val existingAttribute = product.attributeValues?.find { it.attribute.id == attributeId }
            
            if (existingAttribute != null) {
                // Atualizar valor existente
                existingAttribute.value = value
                productAttributeValueRepository.save(existingAttribute)
            } else {
                // Adicionar novo valor de atributo
                val attributeValue = ProductAttributeValue(
                    id = UUID.randomUUID(),
                    product = product,
                    attribute = attribute,
                    value = value
                )
                productAttributeValueRepository.save(attributeValue)
                
                if (product.attributeValues == null) {
                    product.attributeValues = mutableListOf()
                }
                product.attributeValues!!.add(attributeValue)
            }
        }
        
        // Remover atributos
        request.attributesToRemove?.forEach { attributeId ->
            product.attributeValues?.find { it.attribute.id == attributeId }?.let { attributeValue ->
                product.attributeValues!!.remove(attributeValue)
                productAttributeValueRepository.delete(attributeValue)
            }
        }
        
        val savedProduct = productRepository.save(updatedProduct)
        val productResponse = productMapper.toResponse(savedProduct)
        
        // Publicar evento de produto atualizado
        productEventProducer.publishProductUpdatedEvent(productResponse)
        
        return productResponse
    }

    @CacheEvict(value = ["products"], allEntries = true)
    @Transactional
    fun delete(id: UUID) {
        val product = productRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("Produto não encontrado com ID: $id")
        
        // Remover relações com categorias
        product.categories?.clear()
        
        // Remover valores de atributos
        product.attributeValues?.forEach {
            productAttributeValueRepository.delete(it)
        }
        product.attributeValues?.clear()
        
        // Remover imagens
        product.images?.forEach {
            productImageRepository.delete(it)
        }
        product.images?.clear()
        
        // Remover variantes
        product.variants?.forEach {
            // Remover valores de atributos das variantes
            it.attributeValues?.forEach { attrValue ->
                productAttributeValueRepository.delete(attrValue)
            }
            it.attributeValues?.clear()
            
            // Remover imagens das variantes
            it.images?.forEach { image ->
                productImageRepository.delete(image)
            }
            it.images?.clear()
            
            productVariantRepository.delete(it)
        }
        product.variants?.clear()
        
        // Salvar produto com relações removidas
        productRepository.save(product)
        
        // Excluir produto
        productRepository.delete(product)
        
        // Publicar evento de produto excluído
        productEventProducer.publishProductDeletedEvent(id)
    }

    @CacheEvict(value = ["products"], key = "#id")
    @Transactional
    fun toggleActive(id: UUID): ProductResponse {
        val product = productRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("Produto não encontrado com ID: $id")
        
        product.active = !product.active
        val savedProduct = productRepository.save(product)
        val productResponse = productMapper.toResponse(savedProduct)
        
        // Publicar evento de produto atualizado
        productEventProducer.publishProductUpdatedEvent(productResponse)
        
        return productResponse
    }

    @CacheEvict(value = ["products"], key = "#id")
    @Transactional
    fun toggleFeatured(id: UUID): ProductResponse {
        val product = productRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("Produto não encontrado com ID: $id")
        
        product.featured = !product.featured
        val savedProduct = productRepository.save(product)
        val productResponse = productMapper.toResponse(savedProduct)
        
        // Publicar evento de produto atualizado
        productEventProducer.publishProductUpdatedEvent(productResponse)
        
        return productResponse
    }

    private fun createSpecification(request: ProductSearchRequest): Specification<Product> {
        return Specification { root, query, criteriaBuilder ->
            val predicates = mutableListOf<Predicate>()
            
            // Filtro por palavra-chave (nome ou descrição)
            request.keyword?.let { keyword ->
                val nameOrDescriptionPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + keyword.lowercase() + "%"
                    ),
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        "%" + keyword.lowercase() + "%"
                    ),
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("shortDescription")),
                        "%" + keyword.lowercase() + "%"
                    )
                )
                predicates.add(nameOrDescriptionPredicate)
            }
            
            // Filtro por categoria
            request.categoryId?.let { categoryId ->
                val categoriesJoin = root.join<Product, Any>("categories", JoinType.INNER)
                predicates.add(criteriaBuilder.equal(categoriesJoin.get<UUID>("id"), categoryId))
            }
            
            // Filtro por slug da categoria
            request.categorySlug?.let { categorySlug ->
                val categoriesJoin = root.join<Product, Any>("categories", JoinType.INNER)
                predicates.add(criteriaBuilder.equal(categoriesJoin.get<String>("slug"), categorySlug))
            }
            
            // Filtro por marca
            request.brandId?.let { brandId ->
                val brandJoin = root.join<Product, Any>("brand", JoinType.LEFT)
                predicates.add(criteriaBuilder.equal(brandJoin.get<UUID>("id"), brandId))
            }
            
            // Filtro por faixa de preço
            request.minPrice?.let { minPrice ->
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("price"),
                    minPrice
                ))
            }
            
            request.maxPrice?.let { maxPrice ->
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("price"),
                    maxPrice
                ))
            }
            
            // Filtro por produtos em promoção
            request.onSale?.let { onSale ->
                if (onSale) {
                    predicates.add(criteriaBuilder.and(
                        criteriaBuilder.isNotNull(root.get<BigDecimal>("salePrice")),
                        criteriaBuilder.greaterThan(root.get<BigDecimal>("salePrice"), BigDecimal.ZERO),
                        criteriaBuilder.lessThan(root.get<BigDecimal>("salePrice"), root.get<BigDecimal>("price"))
                    ))
                }
            }
            
            // Filtro por produtos em destaque
            request.featured?.let { featured ->
                predicates.add(criteriaBuilder.equal(root.get<Boolean>("featured"), featured))
            }
            
            // Filtro por produtos ativos
            predicates.add(criteriaBuilder.equal(root.get<Boolean>("active"), true))
            
            // Filtro por atributos
            request.attributes?.forEach { (attributeId, value) ->
                val attributeValuesJoin = root.join<Product, ProductAttributeValue>("attributeValues", JoinType.LEFT)
                val attributeJoin = attributeValuesJoin.join<ProductAttributeValue, Any>("attribute", JoinType.LEFT)
                
                val attributePredicate = criteriaBuilder.and(
                    criteriaBuilder.equal(attributeJoin.get<UUID>("id"), attributeId),
                    criteriaBuilder.equal(attributeValuesJoin.get<String>("value"), value)
                )
                predicates.add(attributePredicate)
            }
            
            // Aplicar ordenação
            if (request.sortBy != null && request.sortDirection != null) {
                if (request.sortDirection.equals("asc", ignoreCase = true)) {
                    query.orderBy(criteriaBuilder.asc(root.get<Any>(request.sortBy)))
                } else {
                    query.orderBy(criteriaBuilder.desc(root.get<Any>(request.sortBy)))
                }
            } else {
                // Ordenação padrão: produtos mais recentes primeiro
                query.orderBy(criteriaBuilder.desc(root.get<Date>("createdAt")))
            }
            
            criteriaBuilder.and(*predicates.toTypedArray())
        }
    }
} 