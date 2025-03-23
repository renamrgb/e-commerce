package com.ecommerce.auth.application.service

import com.ecommerce.auth.application.dto.JwtResponse
import com.ecommerce.auth.application.dto.LoginRequest
import com.ecommerce.auth.application.dto.RegisterRequest
import com.ecommerce.auth.domain.entity.RoleName
import com.ecommerce.auth.domain.entity.User
import com.ecommerce.auth.domain.repository.RoleRepository
import com.ecommerce.auth.domain.repository.UserRepository
import com.ecommerce.auth.infrastructure.security.JwtService
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    @Transactional
    fun register(request: RegisterRequest): JwtResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email já está em uso")
        }

        val customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER.name)
            .orElseThrow { IllegalStateException("Perfil de cliente não encontrado") }

        val user = User(
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            active = true,
            verified = false // Será alterado após verificação por e-mail
        )
        
        user.addRole(customerRole)
        val savedUser = userRepository.save(user)
        
        // Aqui poderia ser implementado o envio de e-mail de verificação
        
        logger.info("Novo usuário registrado: {}", savedUser.email)
        
        return authenticateUser(LoginRequest(request.email, request.password))
    }

    fun authenticateUser(loginRequest: LoginRequest): JwtResponse {
        val authentication: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(loginRequest.email, loginRequest.password)
        )
        
        SecurityContextHolder.getContext().authentication = authentication
        
        val user = userRepository.findByEmail(loginRequest.email)
            .orElseThrow { IllegalArgumentException("Usuário não encontrado") }
        
        val userDetails = authentication.principal as org.springframework.security.core.userdetails.User
        val jwt = jwtService.generateToken(userDetails, user.id!!)
        
        return JwtResponse(
            token = jwt,
            expiresIn = jwtService.getExpirationTime(),
            userId = user.id,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            roles = user.roles.map { it.name }
        )
    }
} 