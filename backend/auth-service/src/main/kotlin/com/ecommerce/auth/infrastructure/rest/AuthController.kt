package com.ecommerce.auth.infrastructure.rest

import com.ecommerce.auth.application.dto.JwtResponse
import com.ecommerce.auth.application.dto.LoginRequest
import com.ecommerce.auth.application.dto.RegisterRequest
import com.ecommerce.auth.application.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "API para autenticação de usuários")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário")
    fun login(@RequestBody @Valid loginRequest: LoginRequest): ResponseEntity<JwtResponse> {
        val jwtResponse = authService.authenticateUser(loginRequest)
        return ResponseEntity.ok(jwtResponse)
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário")
    fun register(@RequestBody @Valid registerRequest: RegisterRequest): ResponseEntity<JwtResponse> {
        val jwtResponse = authService.register(registerRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(jwtResponse)
    }
} 