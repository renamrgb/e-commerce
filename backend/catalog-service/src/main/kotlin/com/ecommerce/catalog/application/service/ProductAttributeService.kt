package com.ecommerce.catalog.application.service

import com.ecommerce.catalog.application.dto.*
import com.ecommerce.catalog.application.mapper.ProductAttributeMapper
import com.ecommerce.catalog.domain.exception.EntityNotFoundException
import com.ecommerce.catalog.domain.repository.ProductAttributeRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ProductAttributeService(
    private val productAttributeRepository: ProductAttributeRepository,
    private val productAttributeMapper: ProductAttributeMapper
) {

    @Cacheable(value = ["product_attributes"], key = "#id")
    @Transactional(readOnly = true)
    fun findById(id: UUID): ProductAttributeResponse {
        val attribute = productAttributeRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("Atributo de produto não encontrado com ID: $id")
        return productAttributeMapper.toResponse(attribute)
    }

    @Cacheable(value = ["product_attributes"], key = "'all-page:' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<ProductAttributeResponse> {
        return productAttributeRepository.findAll(pageable)
            .map { productAttributeMapper.toResponse(it) }
    }

    @Cacheable(value = ["product_attributes"], key = "'filterable'")
    @Transactional(readOnly = true)
    fun findAllFilterable(): List<ProductAttributeResponse> {
        return productAttributeRepository.findAllByIsFilterableTrue()
            .map { productAttributeMapper.toResponse(it) }
    }

    @Cacheable(value = ["product_attributes"], key = "'comparable'")
    @Transactional(readOnly = true)
    fun findAllComparable(): List<ProductAttributeResponse> {
        return productAttributeRepository.findAllByIsComparableTrue()
            .map { productAttributeMapper.toResponse(it) }
    }

    @CacheEvict(value = ["product_attributes"], allEntries = true)
    @Transactional
    fun create(request: ProductAttributeCreateRequest): ProductAttributeResponse {
        // Verificar se já existe um atributo com o mesmo nome
        if (productAttributeRepository.existsByName(request.name)) {
            throw IllegalArgumentException("Já existe um atributo com o nome: ${request.name}")
        }
        
        val attribute = productAttributeMapper.toEntity(request)
        val savedAttribute = productAttributeRepository.save(attribute)
        return productAttributeMapper.toResponse(savedAttribute)
    }

    @CacheEvict(value = ["product_attributes"], allEntries = true)
    @Transactional
    fun update(id: UUID, request: ProductAttributeUpdateRequest): ProductAttributeResponse {
        val attribute = productAttributeRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("Atributo de produto não encontrado com ID: $id")

        // Verificar se o nome atualizado já existe em outro atributo
        request.name?.let {
            if (productAttributeRepository.existsByNameAndIdNot(it, id)) {
                throw IllegalArgumentException("Já existe um atributo com o nome: $it")
            }
        }

        val updatedAttribute = productAttributeMapper.updateEntity(attribute, request)
        val savedAttribute = productAttributeRepository.save(updatedAttribute)
        return productAttributeMapper.toResponse(savedAttribute)
    }

    @CacheEvict(value = ["product_attributes"], allEntries = true)
    @Transactional
    fun delete(id: UUID) {
        val attribute = productAttributeRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("Atributo de produto não encontrado com ID: $id")

        // Verificar se o atributo está sendo usado em algum produto
        if (attribute.attributeValues?.isNotEmpty() == true) {
            throw IllegalStateException("Não é possível excluir um atributo que está sendo usado em produtos")
        }

        productAttributeRepository.delete(attribute)
    }

    @Transactional(readOnly = true)
    fun search(query: String, pageable: Pageable): Page<ProductAttributeResponse> {
        return productAttributeRepository.findByNameContainingIgnoreCase(query, pageable)
            .map { productAttributeMapper.toResponse(it) }
    }
} 