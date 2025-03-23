package com.ecommerce.auth.application.service

import com.ecommerce.auth.application.dto.RoleDto
import com.ecommerce.auth.application.dto.UserCreateRequest
import com.ecommerce.auth.application.dto.UserDto
import com.ecommerce.auth.application.dto.UserUpdateRequest
import com.ecommerce.auth.domain.entity.User
import com.ecommerce.auth.domain.repository.RoleRepository
import com.ecommerce.auth.domain.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import javax.persistence.EntityNotFoundException

@Service
class UserService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional(readOnly = true)
    fun findById(id: UUID): UserDto {
        val user = userRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Usuário não encontrado com o ID: $id") }
        
        return mapToDto(user)
    }

    @Transactional(readOnly = true)
    fun findByEmail(email: String): UserDto {
        val user = userRepository.findByEmail(email)
            .orElseThrow { EntityNotFoundException("Usuário não encontrado com o email: $email") }
        
        return mapToDto(user)
    }

    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<UserDto> {
        return userRepository.findAll(pageable).map { mapToDto(it) }
    }

    @Transactional
    fun create(request: UserCreateRequest): UserDto {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email já está em uso")
        }

        val roles = request.roles.mapNotNull { roleName ->
            roleRepository.findByName(roleName).orElse(null)
        }.toMutableSet()

        if (roles.isEmpty()) {
            throw IllegalArgumentException("Pelo menos um perfil válido deve ser informado")
        }

        val user = User(
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            active = request.active,
            verified = request.verified
        )

        roles.forEach { user.addRole(it) }

        val savedUser = userRepository.save(user)
        return mapToDto(savedUser)
    }

    @Transactional
    fun update(id: UUID, request: UserUpdateRequest): UserDto {
        val user = userRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Usuário não encontrado com o ID: $id") }

        request.firstName?.let { user.firstName = it }
        request.lastName?.let { user.lastName = it }
        request.active?.let { user.active = it }
        request.verified?.let { user.verified = it }

        request.roles?.let { roleNames ->
            val roles = roleNames.mapNotNull { roleName ->
                roleRepository.findByName(roleName).orElse(null)
            }.toMutableSet()

            if (roles.isNotEmpty()) {
                // Remover todas as funções atuais
                val currentRoles = user.roles.toList()
                currentRoles.forEach { user.removeRole(it) }
                
                // Adicionar as novas funções
                roles.forEach { user.addRole(it) }
            }
        }

        val updatedUser = userRepository.save(user)
        return mapToDto(updatedUser)
    }

    @Transactional
    fun delete(id: UUID) {
        if (!userRepository.existsById(id)) {
            throw EntityNotFoundException("Usuário não encontrado com o ID: $id")
        }
        userRepository.deleteById(id)
    }

    private fun mapToDto(user: User): UserDto {
        val roleDtos = user.roles.map {
            RoleDto(
                id = it.id!!,
                name = it.name,
                description = it.description
            )
        }.toSet()

        return UserDto(
            id = user.id!!,
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            active = user.active,
            verified = user.verified,
            roles = roleDtos,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }
} 