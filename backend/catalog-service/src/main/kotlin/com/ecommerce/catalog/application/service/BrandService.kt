package com.ecommerce.catalog.application.service

import com.ecommerce.catalog.application.dto.*
import com.ecommerce.catalog.application.mapper.BrandMapper
import com.ecommerce.catalog.domain.exception.EntityNotFoundException
import com.ecommerce.catalog.domain.repository.BrandRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class BrandService(
    private val brandRepository: BrandRepository,
    private val brandMapper: BrandMapper
) {

    @Cacheable(value = ["brands"], key = "#id")
    @Transactional(readOnly = true)
    fun findById(id: UUID): BrandDto {
        val brand = brandRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("Marca não encontrada com ID: $id")
        return brandMapper.toDto(brand)
    }

    @Cacheable(value = ["brands"], key = "#slug")
    @Transactional(readOnly = true)
    fun findBySlug(slug: String): BrandDto {
        val brand = brandRepository.findBySlug(slug)
            ?: throw EntityNotFoundException("Marca não encontrada com slug: $slug")
        return brandMapper.toDto(brand)
    }

    @Cacheable(value = ["brands"], key = "'all-page:' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<BrandResponse> {
        return brandRepository.findAll(pageable)
            .map { brandMapper.toResponse(it) }
    }

    @Cacheable(value = ["brands"], key = "'active'")
    @Transactional(readOnly = true)
    fun findAllActive(pageable: Pageable): Page<BrandResponse> {
        return brandRepository.findAllByActiveTrue(pageable)
            .map { brandMapper.toResponse(it) }
    }

    @Cacheable(value = ["brands"], key = "'featured'")
    @Transactional(readOnly = true)
    fun findAllFeatured(): List<BrandResponse> {
        return brandRepository.findAllByActiveTrueAndFeaturedTrue()
            .map { brandMapper.toResponse(it) }
    }

    @CacheEvict(value = ["brands"], allEntries = true)
    @Transactional
    fun create(request: BrandCreateRequest): BrandResponse {
        // Verificar se já existe uma marca com o mesmo slug
        request.slug?.let {
            if (brandRepository.existsBySlug(it)) {
                throw IllegalArgumentException("Já existe uma marca com o slug: $it")
            }
        }
        
        val brand = brandMapper.toEntity(request)
        val savedBrand = brandRepository.save(brand)
        return brandMapper.toResponse(savedBrand)
    }

    @CacheEvict(value = ["brands"], allEntries = true)
    @Transactional
    fun update(id: UUID, request: BrandUpdateRequest): BrandResponse {
        val brand = brandRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("Marca não encontrada com ID: $id")

        // Verificar se o slug atualizado já existe em outra marca
        request.slug?.let {
            if (brandRepository.existsBySlugAndIdNot(it, id)) {
                throw IllegalArgumentException("Já existe uma marca com o slug: $it")
            }
        }

        val updatedBrand = brandMapper.updateEntity(brand, request)
        val savedBrand = brandRepository.save(updatedBrand)
        return brandMapper.toResponse(savedBrand)
    }

    @CacheEvict(value = ["brands"], allEntries = true)
    @Transactional
    fun delete(id: UUID) {
        val brand = brandRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("Marca não encontrada com ID: $id")

        // Verificar se a marca está associada a produtos
        if (brand.products?.isNotEmpty() == true) {
            throw IllegalStateException("Não é possível excluir uma marca que possui produtos associados")
        }

        brandRepository.delete(brand)
    }

    @CacheEvict(value = ["brands"], key = "#id")
    @Transactional
    fun toggleActive(id: UUID): BrandResponse {
        val brand = brandRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("Marca não encontrada com ID: $id")

        brand.active = !brand.active
        val savedBrand = brandRepository.save(brand)
        return brandMapper.toResponse(savedBrand)
    }

    @CacheEvict(value = ["brands"], key = "#id")
    @Transactional
    fun toggleFeatured(id: UUID): BrandResponse {
        val brand = brandRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("Marca não encontrada com ID: $id")

        brand.featured = !brand.featured
        val savedBrand = brandRepository.save(brand)
        return brandMapper.toResponse(savedBrand)
    }

    @Transactional(readOnly = true)
    fun search(query: String, pageable: Pageable): Page<BrandResponse> {
        return brandRepository.findByNameContainingIgnoreCase(query, pageable)
            .map { brandMapper.toResponse(it) }
    }
} 