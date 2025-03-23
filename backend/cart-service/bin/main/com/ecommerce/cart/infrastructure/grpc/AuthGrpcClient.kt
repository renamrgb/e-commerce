package com.ecommerce.cart.infrastructure.grpc

import com.ecommerce.auth.grpc.AuthServiceGrpc
import com.ecommerce.auth.grpc.TokenValidationRequest
import com.ecommerce.auth.grpc.TokenValidationResponse
import com.ecommerce.auth.grpc.UserRequest
import com.ecommerce.auth.grpc.UserResponse
import net.devh.boot.grpc.client.inject.GrpcClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AuthGrpcClient {
    private val logger = LoggerFactory.getLogger(AuthGrpcClient::class.java)

    @GrpcClient("auth-service")
    private lateinit var authServiceStub: AuthServiceGrpc.AuthServiceBlockingStub

    fun validateToken(token: String): TokenValidationResponse? {
        return try {
            val request = TokenValidationRequest.newBuilder()
                .setToken(token)
                .build()
            
            authServiceStub.validateToken(request)
        } catch (e: Exception) {
            logger.error("Erro ao validar token via gRPC: ${e.message}", e)
            null
        }
    }

    fun getUserDetails(userId: UUID): UserResponse? {
        return try {
            val request = UserRequest.newBuilder()
                .setUserId(userId.toString())
                .build()
            
            authServiceStub.getUserDetails(request)
        } catch (e: Exception) {
            logger.error("Erro ao obter detalhes do usuário via gRPC: ${e.message}", e)
            null
        }
    }
} 