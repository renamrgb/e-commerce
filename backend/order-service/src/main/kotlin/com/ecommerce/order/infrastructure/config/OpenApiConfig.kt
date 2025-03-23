package com.ecommerce.order.infrastructure.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    
    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .components(
                Components()
                    .addSecuritySchemes(
                        "bearerAuth",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("Utilize o token JWT obtido na API de autenticação")
                    )
            )
            .addSecurityItem(SecurityRequirement().addList("bearerAuth"))
            .info(
                Info()
                    .title("E-Commerce - Serviço de Pedidos")
                    .description("API para gerenciamento de pedidos do e-commerce")
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Time de Desenvolvimento")
                            .email("dev@ecommerce.com")
                    )
            )
    }
} 