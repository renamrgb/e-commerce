package com.ecommerce.cart.infrastructure.security

import com.ecommerce.cart.infrastructure.grpc.AuthGrpcClient
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtAuthenticationFilter(
    private val authGrpcClient: AuthGrpcClient
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val authHeader = request.getHeader("Authorization")
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response)
                return
            }

            val jwt = authHeader.substring(7)
            val validationResponse = authGrpcClient.validateToken(jwt)

            if (validationResponse != null && validationResponse.valid) {
                val userId = UUID.fromString(validationResponse.userId)
                val authorities = validationResponse.rolesList.map { SimpleGrantedAuthority(it) }
                
                val authentication = UsernamePasswordAuthenticationToken(userId, null, authorities)
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: Exception) {
            logger.error("Não foi possível autenticar o usuário: ${e.message}", e)
        }

        filterChain.doFilter(request, response)
    }
} 