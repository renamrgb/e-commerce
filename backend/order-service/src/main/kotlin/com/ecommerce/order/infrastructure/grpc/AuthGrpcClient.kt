package com.ecommerce.order.infrastructure.grpc

import com.ecommerce.auth.AuthRequest
import com.ecommerce.auth.AuthServiceGrpc
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class AuthGrpcClient(private val authChannel: ManagedChannel) {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    private val authStub = AuthServiceGrpc.newBlockingStub(authChannel)
    
    /**
     * Valida um token JWT usando o serviço de autenticação via gRPC
     */
    fun validateToken(token: String): TokenInfo? {
        try {
            val request = AuthRequest.newBuilder()
                .setToken(token)
                .build()
            
            val response = authStub.withDeadlineAfter(3, TimeUnit.SECONDS)
                .validateToken(request)
            
            if (response.valid) {
                return TokenInfo(
                    userId = UUID.fromString(response.userId),
                    email = response.email,
                    roles = response.rolesList.toList()
                )
            }
            
            return null
        } catch (e: Exception) {
            logger.error("Erro ao validar token via gRPC: ${e.message}")
            return null
        }
    }
    
    /**
     * Classe de domínio para informações do token
     */
    data class TokenInfo(
        val userId: UUID,
        val email: String,
        val roles: List<String>
    )
} 