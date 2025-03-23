package com.ecommerce.payment.infrastructure.config

import com.stripe.Stripe
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@Configuration
class StripeConfig {

    @Value("\${stripe.api.key}")
    private lateinit var stripeApiKey: String

    @Value("\${stripe.webhook.secret}")
    lateinit var webhookSecret: String

    @PostConstruct
    fun init() {
        Stripe.apiKey = stripeApiKey
    }
}