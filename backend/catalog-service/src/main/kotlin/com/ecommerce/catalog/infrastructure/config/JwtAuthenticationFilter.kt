package com.ecommerce.catalog.infrastructure.config

import com.ecommerce.catalog.infrastructure.service.JwtService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtAuthenticationFilter(private val jwtService: JwtService) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val authHeader = request.getHeader("Authorization")
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                val token = authHeader.substring(7)
                val claims = jwtService.validateTokenAndGetClaims(token)
                
                if (claims != null) {
                    val userId = claims["sub"] as String
                    val username = claims["username"] as String
                    val roles = claims["roles"] as List<*>
                    
                    val authorities = roles.map { SimpleGrantedAuthority("ROLE_$it") }
                    
                    val authentication = UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                    )
                    
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        } catch (e: Exception) {
            logger.error("Não foi possível validar o token JWT", e)
        }
        
        filterChain.doFilter(request, response)
    }
} 