package com.ecommerce.payment.infrastructure.config

import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.consumer.group-id}")
    private lateinit var groupId: String

    @Value("\${spring.application.name}")
    private lateinit var applicationName: String

    /**
     * Configuração do produtor Kafka
     */
    @Bean
    fun producerFactory(): ProducerFactory<String, String> {
        val configProps = HashMap<String, Any>()
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[ProducerConfig.ACKS_CONFIG] = "all"
        configProps[ProducerConfig.RETRIES_CONFIG] = 3
        configProps[ProducerConfig.CLIENT_ID_CONFIG] = "$applicationName-producer"
        return DefaultKafkaProducerFactory(configProps)
    }

    /**
     * Template para envio de mensagens Kafka
     */
    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, String> {
        return KafkaTemplate(producerFactory())
    }

    /**
     * Configuração do consumidor Kafka
     */
    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        val configProps = HashMap<String, Any>()
        configProps[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configProps[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        configProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        configProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        configProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        configProps[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        configProps[ConsumerConfig.CLIENT_ID_CONFIG] = "$applicationName-consumer"
        return DefaultKafkaConsumerFactory(configProps)
    }

    /**
     * Factory para configuração de listeners Kafka
     */
    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = consumerFactory()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        factory.containerProperties.isSyncCommits = true
        return factory
    }

    /**
     * Configuração do administrador Kafka
     */
    @Bean
    fun kafkaAdmin(): KafkaAdmin {
        val configs = HashMap<String, Any>()
        configs[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        return KafkaAdmin(configs)
    }

    /**
     * Criar tópicos do Kafka automaticamente na inicialização
     */
    @Bean
    fun paymentCompletedTopic(): NewTopic {
        return NewTopic("payment-service.payment.completed", 3, 1.toShort())
    }

    @Bean
    fun paymentFailedTopic(): NewTopic {
        return NewTopic("payment-service.payment.failed", 3, 1.toShort())
    }

    @Bean
    fun paymentCanceledTopic(): NewTopic {
        return NewTopic("payment-service.payment.canceled", 3, 1.toShort())
    }

    @Bean
    fun paymentRefundedTopic(): NewTopic {
        return NewTopic("payment-service.payment.refunded", 3, 1.toShort())
    }
} 