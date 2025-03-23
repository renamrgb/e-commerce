package com.ecommerce.payment.infrastructure.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.util.*

/**
 * Componente responsável por manipular tokens JWT
 * Provê métodos para validar, gerar e extrair informações de tokens
 */
@Component
class JwtTokenProvider(
    @Value("\${jwt.secret:defaultSecretKeyForDevelopmentEnvironmentOnly}") private val jwtSecret: String,
    @Value("\${jwt.expiration:86400000}") private val jwtExpirationMs: Long // 1 dia em milissegundos
) {

    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)
    private val key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    
    /**
     * Valida um token JWT
     * @param token Token JWT a ser validado
     * @return true se o token for válido, false caso contrário
     */
    fun validateToken(token: String): Boolean {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            return true
        } catch (ex: Exception) {
            logger.error("Token JWT inválido: {}", ex.message)
            return false
        }
    }
    
    /**
     * Extrai o ID de usuário do token JWT
     * @param token Token JWT
     * @return ID do usuário
     */
    fun getUserIdFromToken(token: String): String {
        return getClaimsFromToken(token).subject
    }
    
    /**
     * Extrai as permissões do usuário do token JWT
     * @param token Token JWT
     * @return Lista de autoridades do usuário
     */
    fun getAuthoritiesFromToken(token: String): Collection<GrantedAuthority> {
        val claims = getClaimsFromToken(token)
        val roles = claims["roles"] as? List<String> ?: emptyList()
        
        return roles.map { SimpleGrantedAuthority(it) }
    }
    
    /**
     * Extrai todas as claims do token JWT
     * @param token Token JWT
     * @return Claims do token
     */
    private fun getClaimsFromToken(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
    }
    
    /**
     * Gera um token JWT para o usuário
     * @param userId ID do usuário
     * @param roles Permissões do usuário
     * @return Token JWT gerado
     */
    fun generateToken(userId: String, roles: List<String>): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationMs)
        
        return Jwts.builder()
            .setSubject(userId)
            .claim("roles", roles)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key)
            .compact()
    }
} 