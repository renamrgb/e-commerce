package com.ecommerce.payment.infrastructure.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuração do OpenAPI para documentação da API
 */
@Configuration
class OpenApiConfig(
    @Value("\${spring.application.name:payment-service}") private val applicationName: String,
    @Value("\${springdoc.server.url:http://localhost:8085}") private val serverUrl: String
) {

    @Bean
    fun customOpenAPI(): OpenAPI {
        // Adiciona o esquema de segurança JWT
        val securitySchemeName = "bearerAuth"
        val securityScheme = SecurityScheme()
            .name(securitySchemeName)
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("Insira o token JWT com o prefixo Bearer.")
        
        // Adiciona o requisito de segurança global
        val securityRequirement = SecurityRequirement().addList(securitySchemeName)
        
        // Configuração de servidor
        val server = Server()
            .url(serverUrl)
            .description("Servidor da API de Pagamentos")
        
        // Tags para agrupar endpoints
        val paymentTag = Tag()
            .name("Pagamentos")
            .description("Operações relacionadas a pagamentos")
        
        val webhookTag = Tag()
            .name("Webhooks")
            .description("Endpoints para receber eventos de provedores de pagamento")
        
        val adminTag = Tag()
            .name("Administração")
            .description("Endpoints administrativos para gestão de pagamentos")
        
        return OpenAPI()
            .info(getApiInfo())
            .addServersItem(server)
            .addTagsItem(paymentTag)
            .addTagsItem(webhookTag)
            .addTagsItem(adminTag)
            .components(Components().addSecuritySchemes(securitySchemeName, securityScheme))
            .addSecurityItem(securityRequirement)
    }
    
    private fun getApiInfo(): Info {
        return Info()
            .title("API de Serviço de Pagamentos")
            .description(
                "API para processamento de pagamentos, integração com Stripe e gestão de transações financeiras. " +
                "Fornece funcionalidades para processamento de pagamentos, callbacks de webhooks e monitoramento de transações."
            )
            .version("1.0.0")
            .contact(
                Contact()
                    .name("Equipe de E-commerce")
                    .email("ecommerce@example.com")
                    .url("https://github.com/renamrgb/e-commerce")
            )
            .license(
                License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")
            )
            .termsOfService("https://example.com/terms-of-service")
    }
} 