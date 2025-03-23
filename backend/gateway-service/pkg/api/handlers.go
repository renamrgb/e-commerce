package api

import (
	"fmt"
	"io"
	"net/http"
	"net/http/httputil"
	"net/url"

	"github.com/ecommerce/gateway-service/pkg/config"
	"github.com/gin-gonic/gin"
	"github.com/sirupsen/logrus"
)

// healthCheck verifica o status de saúde do API Gateway
func healthCheck(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{
		"status":  "up",
		"service": "api-gateway",
	})
}

// createProxyHandler cria um handler de proxy para encaminhar requisições para os microserviços
func createProxyHandler(targetURL string) gin.HandlerFunc {
	target, err := url.Parse(targetURL)
	if err != nil {
		logrus.WithError(err).Errorf("Failed to parse target URL: %s", targetURL)
		return func(c *gin.Context) {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to create proxy"})
		}
	}

	proxy := httputil.NewSingleHostReverseProxy(target)

	// Configurar o handler de erro do proxy
	proxy.ErrorHandler = func(rw http.ResponseWriter, req *http.Request, err error) {
		logrus.WithError(err).Errorf("Proxy error: %s", target.String())
		rw.WriteHeader(http.StatusBadGateway)
		io.WriteString(rw, fmt.Sprintf("Service unavailable: %s", err.Error()))
	}

	return func(c *gin.Context) {
		// Preservar o contexto original
		originalPath := c.Request.URL.Path
		originalHost := c.Request.Host

		// Obter informações do usuário do contexto, se existirem
		userId, exists := c.Get("user_id")
		if exists {
			c.Request.Header.Set("X-User-ID", userId.(string))
		}

		// Modificar o caminho da requisição para o serviço de destino
		c.Request.URL.Path = originalPath
		c.Request.Host = target.Host
		c.Request.Header.Set("X-Forwarded-Host", originalHost)
		c.Request.Header.Set("X-Real-IP", c.ClientIP())

		// Encaminhar a requisição
		proxy.ServeHTTP(c.Writer, c.Request)
	}
}

// proxyToService cria um handler para encaminhar requisições para um serviço específico
func proxyToService(cfg *config.Config, serviceName, host, port, path string) gin.HandlerFunc {
	// Montar URL de destino
	targetURL := fmt.Sprintf("http://%s:%s%s", host, port, path)
	logrus.Infof("Creating proxy to %s: %s", serviceName, targetURL)
	return createProxyHandler(targetURL)
}

// proxyToCatalogService encaminha requisições para o serviço de catálogo
func proxyToCatalogService(cfg *config.Config, path string) gin.HandlerFunc {
	return proxyToService(cfg, "catalog", cfg.Services.Catalog.Host, cfg.Services.Catalog.Port, path)
}

// proxyToOrderService encaminha requisições para o serviço de pedidos
func proxyToOrderService(cfg *config.Config, path string) gin.HandlerFunc {
	return proxyToService(cfg, "order", cfg.Services.Order.Host, cfg.Services.Order.Port, path)
}

// proxyToCartService encaminha requisições para o serviço de carrinho
func proxyToCartService(cfg *config.Config, path string) gin.HandlerFunc {
	return proxyToService(cfg, "cart", cfg.Services.Cart.Host, cfg.Services.Cart.Port, path)
}

// proxyToUserService encaminha requisições para o serviço de usuários
func proxyToUserService(cfg *config.Config, path string) gin.HandlerFunc {
	return proxyToService(cfg, "user", cfg.Services.User.Host, cfg.Services.User.Port, path)
}

// proxyToPaymentService encaminha requisições para o serviço de pagamentos
func proxyToPaymentService(cfg *config.Config, path string) gin.HandlerFunc {
	return proxyToService(cfg, "payment", cfg.Services.Payment.Host, cfg.Services.Payment.Port, path)
}

// proxyToInventoryService encaminha requisições para o serviço de inventário
func proxyToInventoryService(cfg *config.Config, path string) gin.HandlerFunc {
	return proxyToService(cfg, "inventory", cfg.Services.Inventory.Host, cfg.Services.Inventory.Port, path)
}

// proxyToNotificationService encaminha requisições para o serviço de notificações
func proxyToNotificationService(cfg *config.Config, path string) gin.HandlerFunc {
	return proxyToService(cfg, "notification", cfg.Services.Notification.Host, cfg.Services.Notification.Port, path)
}
