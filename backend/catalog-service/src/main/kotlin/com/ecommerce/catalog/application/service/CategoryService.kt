package com.ecommerce.catalog.application.service

import com.ecommerce.catalog.application.dto.*
import com.ecommerce.catalog.application.mapper.CategoryMapper
import com.ecommerce.catalog.domain.exception.EntityNotFoundException
import com.ecommerce.catalog.domain.repository.CategoryRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val categoryMapper: CategoryMapper
) {

    @Cacheable(value = ["categories"], key = "#id")
    @Transactional(readOnly = true)
    fun findById(id: UUID): CategoryDto {
        val category = categoryRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("Categoria não encontrada com ID: $id")
        return categoryMapper.toDto(category)
    }

    @Cacheable(value = ["categories"], key = "#slug")
    @Transactional(readOnly = true)
    fun findBySlug(slug: String): CategoryDto {
        val category = categoryRepository.findBySlug(slug)
            ?: throw EntityNotFoundException("Categoria não encontrada com slug: $slug")
        return categoryMapper.toDto(category)
    }

    @Cacheable(value = ["categories"], key = "'all-page:' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<CategoryDto> {
        return categoryRepository.findAll(pageable)
            .map { categoryMapper.toDto(it) }
    }

    @Cacheable(value = ["categories"], key = "'tree'")
    @Transactional(readOnly = true)
    fun findAllAsTree(): List<CategoryTreeResponse> {
        return categoryRepository.findAllByParentIsNull().map { categoryMapper.toTreeResponse(it) }
    }

    @Cacheable(value = ["categories"], key = "'active-tree'")
    @Transactional(readOnly = true)
    fun findAllActiveAsTree(): List<CategoryTreeResponse> {
        return categoryRepository.findAllByParentIsNullAndActiveTrue().map { categoryMapper.toTreeResponse(it) }
    }

    @CacheEvict(value = ["categories"], allEntries = true)
    @Transactional
    fun create(request: CategoryCreateRequest): CategoryDto {
        val parentCategory = request.parentId?.let { 
            categoryRepository.findByIdOrNull(it)
                ?: throw EntityNotFoundException("Categoria pai não encontrada com ID: $it")
        }
        
        val category = categoryMapper.toEntity(request, parentCategory)
        val savedCategory = categoryRepository.save(category)
        return categoryMapper.toDto(savedCategory)
    }

    @CacheEvict(value = ["categories"], allEntries = true)
    @Transactional
    fun update(id: UUID, request: CategoryUpdateRequest): CategoryDto {
        val category = categoryRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("Categoria não encontrada com ID: $id")

        val parentCategory = request.parentId?.let {
            if (it != id) {
                categoryRepository.findByIdOrNull(it)
                    ?: throw EntityNotFoundException("Categoria pai não encontrada com ID: $it")
            } else {
                throw IllegalArgumentException("Uma categoria não pode ser pai dela mesma")
            }
        }

        if (parentCategory != null) {
            category.parent = parentCategory
        } else if (request.parentId == null && request.clearParent == true) {
            category.parent = null
        }

        val updatedCategory = categoryMapper.updateEntity(category, request)
        val savedCategory = categoryRepository.save(updatedCategory)
        return categoryMapper.toDto(savedCategory)
    }

    @CacheEvict(value = ["categories"], allEntries = true)
    @Transactional
    fun delete(id: UUID) {
        val category = categoryRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("Categoria não encontrada com ID: $id")

        // Verificar se a categoria tem filhos
        if (category.children?.isNotEmpty() == true) {
            throw IllegalStateException("Não é possível excluir uma categoria que possui subcategorias")
        }

        // Verificar se a categoria está associada a produtos
        if (category.products?.isNotEmpty() == true) {
            throw IllegalStateException("Não é possível excluir uma categoria que possui produtos associados")
        }

        categoryRepository.delete(category)
    }

    @CacheEvict(value = ["categories"], key = "#id")
    @Transactional
    fun toggleActive(id: UUID): CategoryDto {
        val category = categoryRepository.findByIdOrNull(id)
            ?: throw EntityNotFoundException("Categoria não encontrada com ID: $id")

        category.active = !category.active
        val savedCategory = categoryRepository.save(category)
        return categoryMapper.toDto(savedCategory)
    }

    @Transactional(readOnly = true)
    fun search(query: String, pageable: Pageable): Page<CategoryDto> {
        return categoryRepository.findByNameContainingIgnoreCase(query, pageable)
            .map { categoryMapper.toDto(it) }
    }
} 