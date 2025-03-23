package com.ecommerce.catalog.infrastructure.rest

import com.ecommerce.catalog.application.dto.CategoryCreateRequest
import com.ecommerce.catalog.application.dto.CategoryDto
import com.ecommerce.catalog.application.dto.CategoryUpdateRequest
import com.ecommerce.catalog.application.service.CategoryService
import com.ecommerce.catalog.domain.exception.EntityNotFoundException
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*

@WebMvcTest(CategoryController::class)
class CategoryControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var categoryService: CategoryService

    private val categoryId = UUID.randomUUID()

    @Test
    fun `findById should return 200 with CategoryDto when category exists`() {
        // Arrange
        val categoryDto = CategoryDto(
            id = categoryId,
            name = "Eletrônicos",
            description = "Produtos eletrônicos",
            slug = "eletronicos",
            active = true
        )
        
        `when`(categoryService.findById(categoryId)).thenReturn(categoryDto)

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/$categoryId"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(categoryId.toString()))
            .andExpect(jsonPath("$.name").value("Eletrônicos"))
            .andExpect(jsonPath("$.description").value("Produtos eletrônicos"))
            .andExpect(jsonPath("$.slug").value("eletronicos"))
            .andExpect(jsonPath("$.active").value(true))
    }

    @Test
    fun `findById should return 404 when category does not exist`() {
        // Arrange
        `when`(categoryService.findById(categoryId)).thenThrow(EntityNotFoundException("Categoria não encontrada com ID: $categoryId"))

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/$categoryId"))
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Recurso não encontrado"))
            .andExpect(jsonPath("$.message").value("Categoria não encontrada com ID: $categoryId"))
    }

    @Test
    fun `findBySlug should return 200 with CategoryDto when category exists`() {
        // Arrange
        val slug = "eletronicos"
        val categoryDto = CategoryDto(
            id = categoryId,
            name = "Eletrônicos",
            description = "Produtos eletrônicos",
            slug = slug,
            active = true
        )
        
        `when`(categoryService.findBySlug(slug)).thenReturn(categoryDto)

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/slug/$slug"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(categoryId.toString()))
            .andExpect(jsonPath("$.name").value("Eletrônicos"))
            .andExpect(jsonPath("$.slug").value(slug))
    }

    @Test
    fun `findAll should return 200 with page of CategoryDto`() {
        // Arrange
        val categoryDto = CategoryDto(
            id = categoryId,
            name = "Eletrônicos",
            description = "Produtos eletrônicos",
            slug = "eletronicos",
            active = true
        )
        
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(categoryDto), pageable, 1)
        
        `when`(categoryService.findAll(any())).thenReturn(page)

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content[0].id").value(categoryId.toString()))
            .andExpect(jsonPath("$.content[0].name").value("Eletrônicos"))
            .andExpect(jsonPath("$.totalElements").value(1))
    }

    @Test
    fun `create should return 201 with CategoryDto when category is created successfully`() {
        // Arrange
        val request = CategoryCreateRequest(
            name = "Eletrônicos",
            description = "Produtos eletrônicos",
            slug = "eletronicos",
            active = true
        )
        
        val categoryDto = CategoryDto(
            id = categoryId,
            name = "Eletrônicos",
            description = "Produtos eletrônicos",
            slug = "eletronicos",
            active = true
        )
        
        `when`(categoryService.create(request)).thenReturn(categoryDto)

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(categoryId.toString()))
            .andExpect(jsonPath("$.name").value("Eletrônicos"))
    }

    @Test
    fun `create should return 400 when request is invalid`() {
        // Arrange
        val request = CategoryCreateRequest(
            name = "", // Nome vazio (inválido)
            description = "Produtos eletrônicos",
            slug = "eletronicos",
            active = true
        )

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `update should return 200 with CategoryDto when category is updated successfully`() {
        // Arrange
        val request = CategoryUpdateRequest(
            name = "Eletrônicos Atualizados",
            description = "Produtos eletrônicos atualizados",
            slug = "eletronicos-atualizados"
        )
        
        val categoryDto = CategoryDto(
            id = categoryId,
            name = "Eletrônicos Atualizados",
            description = "Produtos eletrônicos atualizados",
            slug = "eletronicos-atualizados",
            active = true
        )
        
        `when`(categoryService.update(categoryId, request)).thenReturn(categoryDto)

        // Act & Assert
        mockMvc.perform(put("/api/v1/categories/$categoryId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(categoryId.toString()))
            .andExpect(jsonPath("$.name").value("Eletrônicos Atualizados"))
            .andExpect(jsonPath("$.slug").value("eletronicos-atualizados"))
    }

    @Test
    fun `delete should return 204 when category is deleted successfully`() {
        // Arrange
        doNothing().`when`(categoryService).delete(categoryId)

        // Act & Assert
        mockMvc.perform(delete("/api/v1/categories/$categoryId"))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `toggleActive should return 200 with CategoryDto when category active status is toggled`() {
        // Arrange
        val categoryDto = CategoryDto(
            id = categoryId,
            name = "Eletrônicos",
            description = "Produtos eletrônicos",
            slug = "eletronicos",
            active = false // Agora inativo após o toggle
        )
        
        `when`(categoryService.toggleActive(categoryId)).thenReturn(categoryDto)

        // Act & Assert
        mockMvc.perform(patch("/api/v1/categories/$categoryId/toggle-active"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(categoryId.toString()))
            .andExpect(jsonPath("$.active").value(false))
    }

    @Test
    fun `search should return 200 with page of CategoryDto`() {
        // Arrange
        val query = "eletronicos"
        val categoryDto = CategoryDto(
            id = categoryId,
            name = "Eletrônicos",
            description = "Produtos eletrônicos",
            slug = "eletronicos",
            active = true
        )
        
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(categoryDto), pageable, 1)
        
        `when`(categoryService.search(eq(query), any())).thenReturn(page)

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/search?query=$query"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content[0].id").value(categoryId.toString()))
            .andExpect(jsonPath("$.content[0].name").value("Eletrônicos"))
            .andExpect(jsonPath("$.totalElements").value(1))
    }
} 