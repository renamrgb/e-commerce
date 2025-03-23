package com.ecommerce.cart.infrastructure.grpc

import com.ecommerce.catalog.ProductRequest
import com.ecommerce.catalog.ProductResponse
import com.ecommerce.catalog.ProductServiceGrpc
import com.ecommerce.catalog.VariantRequest
import com.ecommerce.catalog.VariantResponse
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class CatalogGrpcClient(private val catalogChannel: ManagedChannel) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    private val productStub = ProductServiceGrpc.newBlockingStub(catalogChannel)
    
    /**
     * Obtém um produto pelo ID
     */
    fun getProduct(productId: UUID): ProductInfo? {
        try {
            val request = com.ecommerce.catalog.ProductRequest.newBuilder()
                .setId(productId.toString())
                .build()
            
            val response = productStub.withDeadlineAfter(3, TimeUnit.SECONDS)
                .getProduct(request)
            
            return if (response != null) mapToProductInfo(response) else null
        } catch (e: Exception) {
            logger.error("Erro ao obter produto via gRPC: ${e.message}")
            return null
        }
    }
    
    /**
     * Converte a resposta gRPC para um objeto de domínio
     */
    private fun mapToProductInfo(response: ProductResponse): ProductInfo {
        return ProductInfo(
            id = UUID.fromString(response.id),
            name = response.name,
            slug = response.slug,
            description = response.description,
            price = response.price,
            mainImage = response.mainImage,
            active = response.active
        )
    }
    
    /**
     * Classe de domínio para informações do produto
     */
    data class ProductInfo(
        val id: UUID,
        val name: String,
        val slug: String,
        val description: String,
        val price: String,
        val mainImage: String,
        val active: Boolean
    )
    
    /**
     * Obtém uma variante de produto pelo ID via gRPC
     */
    fun getProductVariant(variantId: java.util.UUID): VariantResponse? {
        try {
            val request = VariantRequest.newBuilder()
                .setId(variantId.toString())
                .build()
            
            return productStub.getVariant(request)
        } catch (e: StatusRuntimeException) {
            if (e.status.code == Status.Code.NOT_FOUND) {
                return null
            }
            logger.error("Erro ao buscar variante de produto via gRPC: ${e.message}")
            throw e
        } catch (e: Exception) {
            logger.error("Erro inesperado ao buscar variante de produto via gRPC: ${e.message}")
            throw e
        }
    }
} 