package router

import (
	"github.com/ecommerce/gateway-service/pkg/config"
	"github.com/ecommerce/gateway-service/pkg/handler"
	"github.com/ecommerce/gateway-service/pkg/middleware/auth"
	"github.com/gin-gonic/gin"
	"github.com/prometheus/client_golang/prometheus/promhttp"
)

// SetupRoutes configura todas as rotas da API
func SetupRoutes(router *gin.Engine, handlers *handler.Handlers, cfg *config.Config) {
	// Rota de healthcheck
	router.GET("/health", handlers.HealthHandler.Check)

	// Rota para métricas do Prometheus
	router.GET("/metrics", gin.WrapH(promhttp.Handler()))

	// Grupo principal da API
	api := router.Group("/api/v1")

	// Rotas públicas (sem autenticação)
	setupPublicRoutes(api, handlers)

	// Rotas protegidas (requerem autenticação)
	protected := api.Group("")
	protected.Use(auth.JWT(cfg.Auth.JWTSecret))
	setupProtectedRoutes(protected, handlers)
}

// setupPublicRoutes configura rotas que não exigem autenticação
func setupPublicRoutes(router *gin.RouterGroup, handlers *handler.Handlers) {
	// Autenticação
	auth := router.Group("/auth")
	{
		auth.POST("/login", handlers.AuthHandler.Login)
		auth.POST("/register", handlers.AuthHandler.Register)
		auth.POST("/refresh", handlers.AuthHandler.RefreshToken)
	}

	// Produtos (apenas leitura)
	products := router.Group("/products")
	{
		products.GET("", handlers.ProductHandler.GetAll)
		products.GET("/:id", handlers.ProductHandler.GetByID)
		products.GET("/categories", handlers.ProductHandler.GetCategories)
		products.GET("/search", handlers.ProductHandler.Search)
	}
}

// setupProtectedRoutes configura rotas que exigem autenticação
func setupProtectedRoutes(router *gin.RouterGroup, handlers *handler.Handlers) {
	// Usuários
	users := router.Group("/users")
	{
		users.GET("/me", handlers.UserHandler.GetProfile)
		users.PUT("/me", handlers.UserHandler.UpdateProfile)
		users.GET("/me/addresses", handlers.UserHandler.GetAddresses)
		users.POST("/me/addresses", handlers.UserHandler.AddAddress)
		users.PUT("/me/addresses/:id", handlers.UserHandler.UpdateAddress)
		users.DELETE("/me/addresses/:id", handlers.UserHandler.DeleteAddress)
	}

	// Carrinho
	cart := router.Group("/cart")
	{
		cart.GET("", handlers.CartHandler.GetCart)
		cart.POST("/items", handlers.CartHandler.AddItem)
		cart.PUT("/items/:id", handlers.CartHandler.UpdateItem)
		cart.DELETE("/items/:id", handlers.CartHandler.RemoveItem)
		cart.POST("/checkout", handlers.CartHandler.Checkout)
	}

	// Pedidos
	orders := router.Group("/orders")
	{
		orders.GET("", handlers.OrderHandler.GetAll)
		orders.GET("/:id", handlers.OrderHandler.GetByID)
		orders.POST("", handlers.OrderHandler.Create)
		orders.PUT("/:id/cancel", handlers.OrderHandler.Cancel)
	}

	// Dashboard
	dashboard := router.Group("/dashboard")
	{
		dashboard.GET("/stats", handlers.DashboardHandler.GetStats)
		dashboard.GET("/recent-orders", handlers.DashboardHandler.GetRecentOrders)
	}
}
