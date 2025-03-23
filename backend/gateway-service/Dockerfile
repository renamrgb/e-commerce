FROM golang:1.21-alpine AS builder

WORKDIR /app

# Copiar arquivos de dependências
COPY go.mod go.sum ./

# Baixar dependências
RUN go mod download

# Copiar o código fonte
COPY . .

# Compilar o aplicativo
RUN CGO_ENABLED=0 GOOS=linux go build -o gateway ./cmd/main.go

# Criar uma imagem menor para execução
FROM alpine:3.18

WORKDIR /app

# Instalar certificados para comunicações HTTPS
RUN apk --no-cache add ca-certificates

# Copiar o executável compilado
COPY --from=builder /app/gateway .
COPY --from=builder /app/config.yaml .

# Definir usuário não-root para execução
RUN adduser -D -g '' appuser
USER appuser

EXPOSE 8080

CMD ["./gateway"] 