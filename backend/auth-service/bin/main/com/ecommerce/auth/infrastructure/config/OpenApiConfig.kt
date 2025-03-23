package com.ecommerce.auth.infrastructure.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
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
                        "Bearer Authentication",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("Informe o token JWT para autenticação")
                    )
            )
            .info(
                Info()
                    .title("E-Commerce - Serviço de Autenticação")
                    .description("API para gerenciamento de autenticação e usuários da aplicação E-Commerce")
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Equipe de Desenvolvimento")
                            .email("dev@ecommerce.com")
                    )
            )
    }
} 