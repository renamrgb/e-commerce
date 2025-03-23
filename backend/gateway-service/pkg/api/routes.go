package api

import (
	"github.com/ecommerce/gateway-service/pkg/config"
	"github.com/ecommerce/gateway-service/pkg/middleware"
	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
	"github.com/prometheus/client_golang/prometheus/promhttp"
)

// SetupRouter configura todas as rotas do API Gateway
func SetupRouter(cfg *config.Config) *gin.Engine {
	// Configurar o modo do Gin
	if gin.Mode() == gin.DebugMode {
		gin.SetMode(gin.DebugMode)
	} else {
		gin.SetMode(gin.ReleaseMode)
	}

	// Criar o router
	router := gin.New()

	// Configurar CORS
	corsConfig := cors.DefaultConfig()
	corsConfig.AllowOrigins = cfg.Cors.AllowedOrigins
	corsConfig.AllowCredentials = true
	corsConfig.AllowMethods = []string{"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"}
	corsConfig.AllowHeaders = []string{"Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With"}
	router.Use(cors.New(corsConfig))

	// Middlewares globais
	router.Use(middleware.LoggerMiddleware())
	router.Use(middleware.MetricsMiddleware())
	router.Use(gin.Recovery())

	// Métricas e saúde do serviço
	router.GET("/metrics", gin.WrapH(promhttp.Handler()))
	router.GET("/health", healthCheck)

	// Rotas públicas
	public := router.Group("/api")
	{
		// Catálogo (produtos, categorias)
		catalog := public.Group("/catalog")
		{
			catalog.GET("/products", proxyToCatalogService(cfg, "/products"))
			catalog.GET("/products/:id", proxyToCatalogService(cfg, "/products/:id"))
			catalog.GET("/categories", proxyToCatalogService(cfg, "/categories"))
			catalog.GET("/categories/:id", proxyToCatalogService(cfg, "/categories/:id"))
		}

		// Autenticação
		auth := public.Group("/auth")
		{
			auth.POST("/login", proxyToUserService(cfg, "/auth/login"))
			auth.POST("/register", proxyToUserService(cfg, "/auth/register"))
			auth.POST("/forgot-password", proxyToUserService(cfg, "/auth/forgot-password"))
			auth.POST("/reset-password", proxyToUserService(cfg, "/auth/reset-password"))
		}
	}

	// Rotas protegidas (requerem autenticação)
	secured := router.Group("/api")
	secured.Use(middleware.AuthMiddleware(cfg.Auth.JWTSecret))
	{
		// Carrinho de compras
		cart := secured.Group("/cart")
		{
			cart.GET("", proxyToCartService(cfg, ""))
			cart.POST("/items", proxyToCartService(cfg, "/items"))
			cart.PUT("/items/:id", proxyToCartService(cfg, "/items/:id"))
			cart.DELETE("/items/:id", proxyToCartService(cfg, "/items/:id"))
			cart.POST("/checkout", proxyToCartService(cfg, "/checkout"))
		}

		// Pedidos
		orders := secured.Group("/orders")
		{
			orders.GET("", proxyToOrderService(cfg, ""))
			orders.GET("/:id", proxyToOrderService(cfg, "/:id"))
			orders.POST("", proxyToOrderService(cfg, ""))
			orders.PUT("/:id/cancel", proxyToOrderService(cfg, "/:id/cancel"))
		}

		// Usuários (perfil)
		users := secured.Group("/users")
		{
			users.GET("/profile", proxyToUserService(cfg, "/profile"))
			users.PUT("/profile", proxyToUserService(cfg, "/profile"))
			users.GET("/addresses", proxyToUserService(cfg, "/addresses"))
			users.POST("/addresses", proxyToUserService(cfg, "/addresses"))
			users.PUT("/addresses/:id", proxyToUserService(cfg, "/addresses/:id"))
			users.DELETE("/addresses/:id", proxyToUserService(cfg, "/addresses/:id"))
		}

		// Pagamentos
		payments := secured.Group("/payments")
		{
			payments.GET("/methods", proxyToPaymentService(cfg, "/methods"))
			payments.POST("/process", proxyToPaymentService(cfg, "/process"))
			payments.GET("/:id/status", proxyToPaymentService(cfg, "/:id/status"))
		}
	}

	// Rotas administrativas (requerem autenticação e papel de admin)
	admin := router.Group("/api/admin")
	admin.Use(middleware.AuthMiddleware(cfg.Auth.JWTSecret))
	admin.Use(middleware.RoleMiddleware("admin"))
	{
		// Gerenciamento de produtos
		adminCatalog := admin.Group("/catalog")
		{
			adminCatalog.POST("/products", proxyToCatalogService(cfg, "/admin/products"))
			adminCatalog.PUT("/products/:id", proxyToCatalogService(cfg, "/admin/products/:id"))
			adminCatalog.DELETE("/products/:id", proxyToCatalogService(cfg, "/admin/products/:id"))
			adminCatalog.POST("/categories", proxyToCatalogService(cfg, "/admin/categories"))
			adminCatalog.PUT("/categories/:id", proxyToCatalogService(cfg, "/admin/categories/:id"))
			adminCatalog.DELETE("/categories/:id", proxyToCatalogService(cfg, "/admin/categories/:id"))
		}

		// Gerenciamento de pedidos
		adminOrders := admin.Group("/orders")
		{
			adminOrders.GET("", proxyToOrderService(cfg, "/admin"))
			adminOrders.PUT("/:id/status", proxyToOrderService(cfg, "/admin/:id/status"))
		}

		// Gerenciamento de usuários
		adminUsers := admin.Group("/users")
		{
			adminUsers.GET("", proxyToUserService(cfg, "/admin"))
			adminUsers.GET("/:id", proxyToUserService(cfg, "/admin/:id"))
			adminUsers.PUT("/:id", proxyToUserService(cfg, "/admin/:id"))
			adminUsers.DELETE("/:id", proxyToUserService(cfg, "/admin/:id"))
		}

		// Gerenciamento de estoque
		adminInventory := admin.Group("/inventory")
		{
			adminInventory.GET("", proxyToInventoryService(cfg, "/admin"))
			adminInventory.PUT("/products/:id", proxyToInventoryService(cfg, "/admin/products/:id"))
		}
	}

	return router
}
