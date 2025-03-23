package com.ecommerce.payment.infrastructure.config

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.timelimiter.TimeLimiterConfig
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder
import org.springframework.cloud.client.circuitbreaker.Customizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class Resilience4jConfig {

    /**
     * Configura os circuit breakers padrão com Resilience4j
     */
    @Bean
    fun defaultCustomizer(): Customizer<Resilience4JCircuitBreakerFactory> {
        return Customizer { factory ->
            factory.configureDefault { id ->
                Resilience4JConfigBuilder(id)
                    .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(4))
                        .build())
                    .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .failureRateThreshold(50f)
                        .waitDurationInOpenState(Duration.ofMillis(1000))
                        .slidingWindowSize(10)
                        .minimumNumberOfCalls(5)
                        .permittedNumberOfCallsInHalfOpenState(3)
                        .build())
                    .build()
            }
        }
    }
    
    /**
     * Configuração específica para o serviço do Stripe
     */
    @Bean
    fun stripeServiceCustomizer(): Customizer<Resilience4JCircuitBreakerFactory> {
        return Customizer { factory ->
            factory.configure({ builder ->
                builder
                    .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(5))
                        .build())
                    .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .failureRateThreshold(40f)
                        .waitDurationInOpenState(Duration.ofSeconds(10))
                        .slidingWindowSize(10)
                        .build())
            }, "stripeService")
        }
    }
} 