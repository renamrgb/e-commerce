package com.ecommerce.auth.infrastructure.rest

import com.ecommerce.auth.application.dto.UserCreateRequest
import com.ecommerce.auth.application.dto.UserDto
import com.ecommerce.auth.application.dto.UserUpdateRequest
import com.ecommerce.auth.application.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Usuários", description = "API para gerenciamento de usuários")
@SecurityRequirement(name = "Bearer Authentication")
class UserController(private val userService: UserService) {

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    @PreAuthorize("hasRole('ADMIN')")
    fun findById(@PathVariable id: UUID): ResponseEntity<UserDto> {
        val user = userService.findById(id)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Buscar usuário por email")
    @PreAuthorize("hasRole('ADMIN')")
    fun findByEmail(@PathVariable email: String): ResponseEntity<UserDto> {
        val user = userService.findByEmail(email)
        return ResponseEntity.ok(user)
    }

    @GetMapping
    @Operation(summary = "Listar todos os usuários")
    @PreAuthorize("hasRole('ADMIN')")
    fun findAll(@PageableDefault pageable: Pageable): ResponseEntity<Page<UserDto>> {
        val users = userService.findAll(pageable)
        return ResponseEntity.ok(users)
    }

    @PostMapping
    @Operation(summary = "Criar um novo usuário")
    @PreAuthorize("hasRole('ADMIN')")
    fun create(@RequestBody @Valid request: UserCreateRequest): ResponseEntity<UserDto> {
        val user = userService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(user)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar um usuário existente")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(
        @PathVariable id: UUID,
        @RequestBody @Valid request: UserUpdateRequest
    ): ResponseEntity<UserDto> {
        val user = userService.update(id, request)
        return ResponseEntity.ok(user)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir um usuário")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        userService.delete(id)
        return ResponseEntity.noContent().build()
    }
} 