package com.ecommerce.payment.infrastructure.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

/**
 * Configuração do Redis para cache
 */
@Configuration
@EnableCaching
class RedisConfig(
    @Value("\${spring.redis.host:localhost}") private val redisHost: String,
    @Value("\${spring.redis.port:6379}") private val redisPort: Int,
    @Value("\${spring.redis.password:}") private val redisPassword: String,
    @Value("\${spring.redis.database:0}") private val redisDatabase: Int,
    @Value("\${spring.cache.redis.time-to-live:3600}") private val timeToLive: Long
) {

    /**
     * Configuração da conexão com o Redis
     */
    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val redisConfig = RedisStandaloneConfiguration(redisHost, redisPort)
        
        if (redisPassword.isNotEmpty()) {
            redisConfig.setPassword(redisPassword)
        }
        
        redisConfig.database = redisDatabase
        
        return LettuceConnectionFactory(redisConfig)
    }
    
    /**
     * Template para operações com Redis
     */
    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = redisConnectionFactory
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = GenericJackson2JsonRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = GenericJackson2JsonRedisSerializer()
        
        return template
    }
    
    /**
     * Gerenciador de cache Redis
     */
    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): CacheManager {
        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(timeToLive))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer())
            )
            .disableCachingNullValues()
        
        // Configurações específicas por cache
        val cacheConfigurations = mapOf(
            "payments" to defaultConfig.entryTtl(Duration.ofMinutes(30)),
            "reports" to defaultConfig.entryTtl(Duration.ofHours(6)),
            "statistics" to defaultConfig.entryTtl(Duration.ofMinutes(15))
        )
        
        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }
} 