package handler

import (
	"net/http"
	"time"

	"github.com/ecommerce/gateway-service/pkg/service"
	"github.com/gin-gonic/gin"
)

// HealthHandler gerencia as requisições relacionadas à saúde do sistema
type HealthHandler struct {
	services *service.Services
}

// NewHealthHandler cria uma nova instância do handler de saúde
func NewHealthHandler(services *service.Services) *HealthHandler {
	return &HealthHandler{
		services: services,
	}
}

// ServiceStatus representa o status de um serviço
type ServiceStatus struct {
	Name    string `json:"name"`
	Status  string `json:"status"`
	Message string `json:"message,omitempty"`
}

// Check verifica o status de todos os serviços e retorna o resultado
func (h *HealthHandler) Check(c *gin.Context) {
	startTime := time.Now()

	// Verifique o status de cada serviço (simulado por enquanto)
	services := []ServiceStatus{
		{Name: "gateway", Status: "UP"},
		{Name: "auth", Status: "UP"},
		{Name: "catalog", Status: "UP"},
		{Name: "cart", Status: "UP"},
		{Name: "order", Status: "UP"},
		{Name: "user", Status: "UP"},
		// Adicione mais serviços conforme necessário
	}

	// Calcular tempo de resposta
	responseTime := time.Since(startTime).Milliseconds()

	// Construir resposta
	c.JSON(http.StatusOK, gin.H{
		"status":       "UP",
		"services":     services,
		"responseTime": responseTime,
		"timestamp":    time.Now().Format(time.RFC3339),
		"version":      "1.0.0", // Versão da API
	})
}
