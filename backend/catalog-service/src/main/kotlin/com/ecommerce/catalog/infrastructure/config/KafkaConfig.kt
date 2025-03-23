package com.ecommerce.catalog.infrastructure.config

import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaAdmin

@Configuration
class KafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${kafka.topic.product-created}")
    private lateinit var productCreatedTopic: String

    @Value("\${kafka.topic.product-updated}")
    private lateinit var productUpdatedTopic: String

    @Value("\${kafka.topic.product-deleted}")
    private lateinit var productDeletedTopic: String

    @Bean
    fun kafkaAdmin(): KafkaAdmin {
        val configs = mapOf(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers
        )
        return KafkaAdmin(configs)
    }

    @Bean
    fun productCreatedTopic(): NewTopic {
        return TopicBuilder.name(productCreatedTopic)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun productUpdatedTopic(): NewTopic {
        return TopicBuilder.name(productUpdatedTopic)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun productDeletedTopic(): NewTopic {
        return TopicBuilder.name(productDeletedTopic)
            .partitions(3)
            .replicas(1)
            .build()
    }
} 