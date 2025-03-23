package com.ecommerce.order.infrastructure.grpc

import com.ecommerce.catalog.ProductDetailRequest
import com.ecommerce.catalog.ProductDetailResponse
import com.ecommerce.catalog.ProductServiceGrpc
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class CatalogGrpcClient(private val catalogChannel: ManagedChannel) {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    private val productStub = ProductServiceGrpc.newBlockingStub(catalogChannel)
    
    /**
     * Obtém detalhes de um produto pelo ID
     */
    fun getProductDetail(productId: UUID): ProductInfo? {
        try {
            val request = ProductDetailRequest.newBuilder()
                .setId(productId.toString())
                .build()
            
            val response = productStub.withDeadlineAfter(3, TimeUnit.SECONDS)
                .getProductDetail(request)
            
            return if (response != null) mapToProductInfo(response) else null
        } catch (e: Exception) {
            logger.error("Erro ao obter detalhes do produto via gRPC: ${e.message}")
            return null
        }
    }
    
    /**
     * Converte a resposta gRPC para um objeto de domínio
     */
    private fun mapToProductInfo(response: ProductDetailResponse): ProductInfo {
        return ProductInfo(
            id = UUID.fromString(response.id),
            name = response.name,
            slug = response.slug,
            description = response.description,
            price = BigDecimal(response.price),
            stock = response.stock,
            available = response.available,
            imageUrl = if (response.hasImageUrl()) response.imageUrl else null,
            categoryId = if (response.hasCategoryId()) UUID.fromString(response.categoryId) else null,
            categoryName = if (response.hasCategoryName()) response.categoryName else null,
            variants = response.variantsList.map { variant ->
                VariantInfo(
                    id = UUID.fromString(variant.id),
                    name = variant.name,
                    price = BigDecimal(variant.price),
                    stock = variant.stock,
                    available = variant.available
                )
            }
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
        val price: BigDecimal,
        val stock: Int,
        val available: Boolean,
        val imageUrl: String?,
        val categoryId: UUID?,
        val categoryName: String?,
        val variants: List<VariantInfo>
    )
    
    /**
     * Classe de domínio para informações da variante
     */
    data class VariantInfo(
        val id: UUID,
        val name: String,
        val price: BigDecimal,
        val stock: Int,
        val available: Boolean
    )
} 