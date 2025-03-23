package handler

import (
	"github.com/ecommerce/gateway-service/pkg/config"
	"github.com/ecommerce/gateway-service/pkg/service"
)

// Handlers contém todos os manipuladores de requisições da API
type Handlers struct {
	AuthHandler      *AuthHandler
	ProductHandler   *ProductHandler
	CartHandler      *CartHandler
	OrderHandler     *OrderHandler
	UserHandler      *UserHandler
	HealthHandler    *HealthHandler
	DashboardHandler *DashboardHandler
}

// NewHandlers inicializa todos os handlers com suas dependências
func NewHandlers(cfg *config.Config) *Handlers {
	// Inicializar serviços
	services := service.NewServices(cfg)

	return &Handlers{
		AuthHandler:      NewAuthHandler(services.AuthService),
		ProductHandler:   NewProductHandler(services.CatalogService),
		CartHandler:      NewCartHandler(services.CartService),
		OrderHandler:     NewOrderHandler(services.OrderService),
		UserHandler:      NewUserHandler(services.UserService),
		HealthHandler:    NewHealthHandler(services),
		DashboardHandler: NewDashboardHandler(services),
	}
}
