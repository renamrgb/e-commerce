package com.ecommerce.catalog.infrastructure.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.util.*

@Service
class JwtService {

    @Value("\${jwt.secret}")
    private lateinit var secret: String

    @Value("\${jwt.issuer}")
    private lateinit var issuer: String

    fun validateTokenAndGetClaims(token: String): Claims? {
        return try {
            val key = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))
            
            val claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
            
            // Verificar emissor
            if (claims.issuer != issuer) {
                return null
            }
            
            // Verificar expiração
            val now = Date()
            if (claims.expiration.before(now)) {
                return null
            }
            
            claims
        } catch (e: Exception) {
            null
        }
    }
} 