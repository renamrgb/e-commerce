package com.ecommerce.catalog.infrastructure.messaging.producer

import com.ecommerce.catalog.application.dto.ProductResponse
import com.ecommerce.catalog.infrastructure.messaging.event.ProductEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class ProductEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${kafka.topic.product-created}")
    private lateinit var productCreatedTopic: String

    @Value("\${kafka.topic.product-updated}")
    private lateinit var productUpdatedTopic: String

    @Value("\${kafka.topic.product-deleted}")
    private lateinit var productDeletedTopic: String

    fun publishProductCreatedEvent(product: ProductResponse) {
        val event = ProductEvent(
            id = UUID.randomUUID().toString(),
            productId = product.id.toString(),
            type = "PRODUCT_CREATED",
            data = product,
            timestamp = System.currentTimeMillis()
        )

        kafkaTemplate.send(productCreatedTopic, product.id.toString(), event)
            .addCallback(
                { success -> logger.info("Evento PRODUCT_CREATED enviado para o tópico $productCreatedTopic: ${success?.recordMetadata}") },
                { failure -> logger.error("Erro ao enviar evento PRODUCT_CREATED para o tópico $productCreatedTopic", failure) }
            )
    }

    fun publishProductUpdatedEvent(product: ProductResponse) {
        val event = ProductEvent(
            id = UUID.randomUUID().toString(),
            productId = product.id.toString(),
            type = "PRODUCT_UPDATED",
            data = product,
            timestamp = System.currentTimeMillis()
        )

        kafkaTemplate.send(productUpdatedTopic, product.id.toString(), event)
            .addCallback(
                { success -> logger.info("Evento PRODUCT_UPDATED enviado para o tópico $productUpdatedTopic: ${success?.recordMetadata}") },
                { failure -> logger.error("Erro ao enviar evento PRODUCT_UPDATED para o tópico $productUpdatedTopic", failure) }
            )
    }

    fun publishProductDeletedEvent(productId: UUID) {
        val event = ProductEvent(
            id = UUID.randomUUID().toString(),
            productId = productId.toString(),
            type = "PRODUCT_DELETED",
            data = mapOf("productId" to productId.toString()),
            timestamp = System.currentTimeMillis()
        )

        kafkaTemplate.send(productDeletedTopic, productId.toString(), event)
            .addCallback(
                { success -> logger.info("Evento PRODUCT_DELETED enviado para o tópico $productDeletedTopic: ${success?.recordMetadata}") },
                { failure -> logger.error("Erro ao enviar evento PRODUCT_DELETED para o tópico $productDeletedTopic", failure) }
            )
    }
} 