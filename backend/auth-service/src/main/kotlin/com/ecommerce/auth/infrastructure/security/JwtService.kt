package com.ecommerce.auth.infrastructure.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.security.Key
import java.util.Date
import java.util.UUID
import java.util.function.Function

@Service
class JwtService {
    private val logger = LoggerFactory.getLogger(JwtService::class.java)

    @Value("\${jwt.secret}")
    private val secretKey: String = ""

    @Value("\${jwt.expiration}")
    private val jwtExpiration: Long = 0L

    @Value("\${jwt.issuer}")
    private val issuer: String = ""

    private fun getSigningKey(): Key {
        return Keys.hmacShaKeyFor(secretKey.toByteArray())
    }

    fun extractUsername(token: String): String {
        return extractClaim(token, Claims::getSubject)
    }

    fun extractUserId(token: String): UUID {
        val subject = extractAllClaims(token).get("userId", String::class.java)
        return UUID.fromString(subject)
    }

    fun extractExpiration(token: String): Date {
        return extractClaim(token, Claims::getExpiration)
    }

    fun <T> extractClaim(token: String, claimsResolver: Function<Claims, T>): T {
        val claims = extractAllClaims(token)
        return claimsResolver.apply(claims)
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .body
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }

    fun generateToken(userDetails: UserDetails, userId: UUID): String {
        val authorities = userDetails.authorities.map { it.authority }
        
        return Jwts.builder()
            .setClaims(mapOf("userId" to userId.toString(), "roles" to authorities))
            .setSubject(userDetails.username)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + jwtExpiration))
            .setIssuer(issuer)
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact()
    }

    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        try {
            val username = extractUsername(token)
            return (username == userDetails.username && !isTokenExpired(token))
        } catch (e: Exception) {
            when (e) {
                is ExpiredJwtException -> logger.error("JWT token expired: {}", e.message)
                is UnsupportedJwtException -> logger.error("JWT token is unsupported: {}", e.message)
                is MalformedJwtException -> logger.error("Invalid JWT token: {}", e.message)
                is IllegalArgumentException -> logger.error("JWT claims string is empty: {}", e.message)
                else -> logger.error("JWT validation error: {}", e.message)
            }
            return false
        }
    }

    fun getExpirationTime(): Long {
        return jwtExpiration
    }
} 