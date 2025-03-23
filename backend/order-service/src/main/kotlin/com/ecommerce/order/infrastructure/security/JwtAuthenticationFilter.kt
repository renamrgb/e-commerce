package com.ecommerce.order.infrastructure.security

import com.ecommerce.order.infrastructure.grpc.AuthGrpcClient
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtAuthenticationFilter(private val authGrpcClient: AuthGrpcClient) : OncePerRequestFilter() {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            // Obter o token JWT do cabeçalho Authorization
            val authorizationHeader = request.getHeader("Authorization")
            
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                // Se não houver token, prosseguir sem autenticação
                filterChain.doFilter(request, response)
                return
            }
            
            // Extrair o token
            val token = authorizationHeader.substring(7)
            
            // Validar o token usando o serviço de autenticação gRPC
            val tokenInfo = authGrpcClient.validateToken(token)
            
            if (tokenInfo != null) {
                // Extrair as informações do usuário do token
                val userId = tokenInfo.userId
                val authorities = tokenInfo.roles.map { SimpleGrantedAuthority("ROLE_$it") }
                
                // Criar a autenticação
                val authentication = UsernamePasswordAuthenticationToken(userId, null, authorities)
                
                // Definir a autenticação no contexto de segurança
                SecurityContextHolder.getContext().authentication = authentication
                
                logger.debug("Autenticado usuário: $userId com roles: ${tokenInfo.roles}")
            }
        } catch (e: Exception) {
            logger.error("Não foi possível autenticar o usuário: ${e.message}")
        }
        
        // Prosseguir com a cadeia de filtros
        filterChain.doFilter(request, response)
    }
} 