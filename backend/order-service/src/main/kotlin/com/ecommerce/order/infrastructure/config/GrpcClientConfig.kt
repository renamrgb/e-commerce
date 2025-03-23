package com.ecommerce.order.infrastructure.config

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GrpcClientConfig {

    @Value("\${service.cart.grpc.host:localhost}")
    private lateinit var cartHost: String

    @Value("\${service.cart.grpc.port:9083}")
    private var cartPort: Int = 9083

    @Value("\${service.catalog.grpc.host:localhost}")
    private lateinit var catalogHost: String

    @Value("\${service.catalog.grpc.port:9081}")
    private var catalogPort: Int = 9081
    
    @Value("\${service.user.grpc.host:localhost}")
    private lateinit var authHost: String

    @Value("\${service.user.grpc.port:9082}")
    private var authPort: Int = 9082

    @Bean(destroyMethod = "shutdown")
    fun cartChannel(): ManagedChannel {
        return ManagedChannelBuilder
            .forAddress(cartHost, cartPort)
            .usePlaintext() // Apenas para desenvolvimento. Use TLS em produção
            .build()
    }

    @Bean(destroyMethod = "shutdown")
    fun catalogChannel(): ManagedChannel {
        return ManagedChannelBuilder
            .forAddress(catalogHost, catalogPort)
            .usePlaintext() // Apenas para desenvolvimento. Use TLS em produção
            .build()
    }
    
    @Bean(destroyMethod = "shutdown")
    fun authChannel(): ManagedChannel {
        return ManagedChannelBuilder
            .forAddress(authHost, authPort)
            .usePlaintext() // Apenas para desenvolvimento. Use TLS em produção
            .build()
    }
} 