package service

import (
	"github.com/ecommerce/gateway-service/pkg/config"
)

// Services contém todas as instâncias de serviços para comunicação com microserviços
type Services struct {
	AuthService    *AuthService
	CatalogService *CatalogService
	CartService    *CartService
	OrderService   *OrderService
	UserService    *UserService
}

// NewServices inicializa todos os serviços com suas configurações
func NewServices(cfg *config.Config) *Services {
	return &Services{
		AuthService:    NewAuthService(cfg.Services.User),
		CatalogService: NewCatalogService(cfg.Services.Catalog),
		CartService:    NewCartService(cfg.Services.Cart),
		OrderService:   NewOrderService(cfg.Services.Order),
		UserService:    NewUserService(cfg.Services.User),
	}
}
