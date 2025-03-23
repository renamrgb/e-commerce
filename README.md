# E-Commerce Microservices

Plataforma de e-commerce baseada em microserviços desenvolvida com Spring Boot, Kotlin e gRPC.

## Estrutura do Projeto

O projeto está organizado nos seguintes componentes:

### Backend

- **auth-service**: Serviço de autenticação e gerenciamento de usuários
- **catalog-service**: Serviço de catálogo de produtos
- **cart-service**: Serviço de carrinho de compras
- **order-service**: Serviço de pedidos
- **payment-service**: Serviço de pagamentos (em desenvolvimento)

### Frontend

- **web-app**: Aplicação web para clientes (em desenvolvimento)
- **admin-panel**: Painel administrativo (em desenvolvimento)

### Infraestrutura

- **docker-compose.yml**: Configuração para execução dos serviços em containers Docker
- **infrastructure/**: Scripts e configurações para a infraestrutura

## Tecnologias Utilizadas

- **Linguagem**: Kotlin
- **Framework**: Spring Boot
- **Comunicação entre serviços**: gRPC
- **Banco de Dados**: PostgreSQL
- **Cache**: Redis
- **Containerização**: Docker e Docker Compose

## Como Executar

### Requisitos

- JDK 17+
- Docker e Docker Compose
- Maven

### Passos para Execução

1. Clone o repositório:
```
git clone https://github.com/seu-usuario/e-commerce.git
cd e-commerce
```

2. Inicie os serviços de infraestrutura:
```
docker-compose up -d
```

3. Compile e execute cada serviço:
```
cd backend/auth-service
./mvnw spring-boot:run
```

Repita o passo 3 para cada um dos outros serviços.

## Documentação da API

Após iniciar os serviços, a documentação OpenAPI está disponível nos seguintes endpoints:

- Auth Service: http://localhost:8082/swagger-ui.html
- Catalog Service: http://localhost:8081/swagger-ui.html
- Cart Service: http://localhost:8083/swagger-ui.html
- Order Service: http://localhost:8084/swagger-ui.html

## Licença

Este projeto está licenciado sob a [Licença MIT](LICENSE). 