package com.ecommerce.catalog.application.service

import com.ecommerce.catalog.application.dto.CategoryCreateRequest
import com.ecommerce.catalog.application.dto.CategoryDto
import com.ecommerce.catalog.application.dto.CategoryUpdateRequest
import com.ecommerce.catalog.application.mapper.CategoryMapper
import com.ecommerce.catalog.domain.entity.Category
import com.ecommerce.catalog.domain.exception.EntityNotFoundException
import com.ecommerce.catalog.domain.repository.CategoryRepository
import com.ecommerce.catalog.domain.repository.ProductRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import java.util.*

@ExtendWith(MockitoExtension::class)
class CategoryServiceTest {

    @Mock
    private lateinit var categoryRepository: CategoryRepository

    @Mock
    private lateinit var productRepository: ProductRepository

    @Mock
    private lateinit var categoryMapper: CategoryMapper

    @InjectMocks
    private lateinit var categoryService: CategoryService

    private lateinit var category: Category
    private lateinit var categoryDto: CategoryDto
    private val categoryId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        category = Category(
            id = categoryId,
            name = "Eletrônicos",
            description = "Produtos eletrônicos",
            slug = "eletronicos",
            active = true
        )

        categoryDto = CategoryDto(
            id = categoryId,
            name = "Eletrônicos",
            description = "Produtos eletrônicos",
            slug = "eletronicos",
            active = true
        )
    }

    @Test
    fun `findById should return CategoryDto when category exists`() {
        // Arrange
        `when`(categoryRepository.findByIdOrNull(categoryId)).thenReturn(category)
        `when`(categoryMapper.toDto(category)).thenReturn(categoryDto)

        // Act
        val result = categoryService.findById(categoryId)

        // Assert
        assertEquals(categoryDto, result)
        verify(categoryRepository).findByIdOrNull(categoryId)
        verify(categoryMapper).toDto(category)
    }

    @Test
    fun `findById should throw EntityNotFoundException when category does not exist`() {
        // Arrange
        `when`(categoryRepository.findByIdOrNull(categoryId)).thenReturn(null)

        // Act & Assert
        val exception = assertThrows(EntityNotFoundException::class.java) {
            categoryService.findById(categoryId)
        }
        assertEquals("Categoria não encontrada com ID: $categoryId", exception.message)
        verify(categoryRepository).findByIdOrNull(categoryId)
        verifyNoInteractions(categoryMapper)
    }

    @Test
    fun `findBySlug should return CategoryDto when category exists`() {
        // Arrange
        val slug = "eletronicos"
        `when`(categoryRepository.findBySlug(slug)).thenReturn(category)
        `when`(categoryMapper.toDto(category)).thenReturn(categoryDto)

        // Act
        val result = categoryService.findBySlug(slug)

        // Assert
        assertEquals(categoryDto, result)
        verify(categoryRepository).findBySlug(slug)
        verify(categoryMapper).toDto(category)
    }

    @Test
    fun `findBySlug should throw EntityNotFoundException when category does not exist`() {
        // Arrange
        val slug = "eletronicos"
        `when`(categoryRepository.findBySlug(slug)).thenReturn(null)

        // Act & Assert
        val exception = assertThrows(EntityNotFoundException::class.java) {
            categoryService.findBySlug(slug)
        }
        assertEquals("Categoria não encontrada com slug: $slug", exception.message)
        verify(categoryRepository).findBySlug(slug)
        verifyNoInteractions(categoryMapper)
    }

    @Test
    fun `findAll should return page of CategoryDto`() {
        // Arrange
        val pageable = PageRequest.of(0, 10)
        val categories = listOf(category)
        val page = PageImpl(categories, pageable, categories.size.toLong())
        
        `when`(categoryRepository.findAll(pageable)).thenReturn(page)
        `when`(categoryMapper.toDto(category)).thenReturn(categoryDto)

        // Act
        val result = categoryService.findAll(pageable)

        // Assert
        assertEquals(1, result.totalElements)
        assertEquals(categoryDto, result.content[0])
        verify(categoryRepository).findAll(pageable)
        verify(categoryMapper).toDto(category)
    }

    @Test
    fun `create should return CategoryDto when category is created successfully`() {
        // Arrange
        val request = CategoryCreateRequest(
            name = "Eletrônicos",
            description = "Produtos eletrônicos",
            slug = "eletronicos",
            active = true
        )
        
        `when`(categoryRepository.existsBySlug("eletronicos")).thenReturn(false)
        `when`(categoryMapper.toEntity(request)).thenReturn(category)
        `when`(categoryRepository.save(category)).thenReturn(category)
        `when`(categoryMapper.toDto(category)).thenReturn(categoryDto)

        // Act
        val result = categoryService.create(request)

        // Assert
        assertEquals(categoryDto, result)
        verify(categoryRepository).existsBySlug("eletronicos")
        verify(categoryMapper).toEntity(request)
        verify(categoryRepository).save(category)
        verify(categoryMapper).toDto(category)
    }

    @Test
    fun `create should throw IllegalArgumentException when slug already exists`() {
        // Arrange
        val request = CategoryCreateRequest(
            name = "Eletrônicos",
            description = "Produtos eletrônicos",
            slug = "eletronicos",
            active = true
        )
        
        `when`(categoryRepository.existsBySlug("eletronicos")).thenReturn(true)

        // Act & Assert
        val exception = assertThrows(IllegalArgumentException::class.java) {
            categoryService.create(request)
        }
        assertEquals("Já existe uma categoria com o slug: eletronicos", exception.message)
        verify(categoryRepository).existsBySlug("eletronicos")
        verifyNoMoreInteractions(categoryRepository)
        verifyNoInteractions(categoryMapper)
    }

    @Test
    fun `update should return CategoryDto when category is updated successfully`() {
        // Arrange
        val request = CategoryUpdateRequest(
            name = "Eletrônicos Atualizados",
            description = "Produtos eletrônicos atualizados",
            slug = "eletronicos-atualizados"
        )
        
        val updatedCategory = category.copy(
            name = "Eletrônicos Atualizados",
            description = "Produtos eletrônicos atualizados",
            slug = "eletronicos-atualizados"
        )
        
        val updatedCategoryDto = categoryDto.copy(
            name = "Eletrônicos Atualizados",
            description = "Produtos eletrônicos atualizados",
            slug = "eletronicos-atualizados"
        )
        
        `when`(categoryRepository.findByIdOrNull(categoryId)).thenReturn(category)
        `when`(categoryRepository.existsBySlugAndIdNot("eletronicos-atualizados", categoryId)).thenReturn(false)
        `when`(categoryMapper.updateEntity(category, request)).thenReturn(updatedCategory)
        `when`(categoryRepository.save(updatedCategory)).thenReturn(updatedCategory)
        `when`(categoryMapper.toDto(updatedCategory)).thenReturn(updatedCategoryDto)

        // Act
        val result = categoryService.update(categoryId, request)

        // Assert
        assertEquals(updatedCategoryDto, result)
        verify(categoryRepository).findByIdOrNull(categoryId)
        verify(categoryRepository).existsBySlugAndIdNot("eletronicos-atualizados", categoryId)
        verify(categoryMapper).updateEntity(category, request)
        verify(categoryRepository).save(updatedCategory)
        verify(categoryMapper).toDto(updatedCategory)
    }

    @Test
    fun `update should throw EntityNotFoundException when category does not exist`() {
        // Arrange
        val request = CategoryUpdateRequest(
            name = "Eletrônicos Atualizados"
        )
        
        `when`(categoryRepository.findByIdOrNull(categoryId)).thenReturn(null)

        // Act & Assert
        val exception = assertThrows(EntityNotFoundException::class.java) {
            categoryService.update(categoryId, request)
        }
        assertEquals("Categoria não encontrada com ID: $categoryId", exception.message)
        verify(categoryRepository).findByIdOrNull(categoryId)
        verifyNoMoreInteractions(categoryRepository)
        verifyNoInteractions(categoryMapper)
    }

    @Test
    fun `update should throw IllegalArgumentException when slug already exists`() {
        // Arrange
        val request = CategoryUpdateRequest(
            slug = "eletronicos-atualizados"
        )
        
        `when`(categoryRepository.findByIdOrNull(categoryId)).thenReturn(category)
        `when`(categoryRepository.existsBySlugAndIdNot("eletronicos-atualizados", categoryId)).thenReturn(true)

        // Act & Assert
        val exception = assertThrows(IllegalArgumentException::class.java) {
            categoryService.update(categoryId, request)
        }
        assertEquals("Já existe uma categoria com o slug: eletronicos-atualizados", exception.message)
        verify(categoryRepository).findByIdOrNull(categoryId)
        verify(categoryRepository).existsBySlugAndIdNot("eletronicos-atualizados", categoryId)
        verifyNoMoreInteractions(categoryRepository)
        verifyNoInteractions(categoryMapper)
    }

    @Test
    fun `delete should delete category when it has no products`() {
        // Arrange
        `when`(categoryRepository.findByIdOrNull(categoryId)).thenReturn(category)
        `when`(productRepository.countByCategoriesContaining(category)).thenReturn(0)

        // Act
        categoryService.delete(categoryId)

        // Assert
        verify(categoryRepository).findByIdOrNull(categoryId)
        verify(productRepository).countByCategoriesContaining(category)
        verify(categoryRepository).delete(category)
    }

    @Test
    fun `delete should throw IllegalStateException when category has products`() {
        // Arrange
        `when`(categoryRepository.findByIdOrNull(categoryId)).thenReturn(category)
        `when`(productRepository.countByCategoriesContaining(category)).thenReturn(5)

        // Act & Assert
        val exception = assertThrows(IllegalStateException::class.java) {
            categoryService.delete(categoryId)
        }
        assertEquals("Não é possível excluir a categoria pois existem 5 produtos associados a ela", exception.message)
        verify(categoryRepository).findByIdOrNull(categoryId)
        verify(productRepository).countByCategoriesContaining(category)
        verifyNoMoreInteractions(categoryRepository)
    }

    @Test
    fun `toggleActive should return CategoryDto when category active status is toggled`() {
        // Arrange
        val activeCategory = category.copy(active = true)
        val inactiveCategory = category.copy(active = false)
        
        val activeCategoryDto = categoryDto.copy(active = true)
        val inactiveCategoryDto = categoryDto.copy(active = false)
        
        `when`(categoryRepository.findByIdOrNull(categoryId)).thenReturn(activeCategory)
        `when`(categoryRepository.save(any())).thenAnswer { 
            val saved = it.getArgument<Category>(0)
            saved
        }
        `when`(categoryMapper.toDto(inactiveCategory)).thenReturn(inactiveCategoryDto)

        // Act
        val result = categoryService.toggleActive(categoryId)

        // Assert
        assertEquals(inactiveCategoryDto, result)
        verify(categoryRepository).findByIdOrNull(categoryId)
        verify(categoryRepository).save(any())
        verify(categoryMapper).toDto(any())
    }
} 