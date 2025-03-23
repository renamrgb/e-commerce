package middleware

import (
	"github.com/ecommerce/gateway-service/pkg/config"
	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
)

// Cors retorna um middleware para configuração de CORS
func Cors(cfg *config.Config) gin.HandlerFunc {
	corsConfig := cors.Config{
		AllowMethods:     []string{"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"},
		AllowHeaders:     []string{"Origin", "Content-Length", "Content-Type", "Authorization"},
		AllowCredentials: true,
		MaxAge:           86400, // 24 horas
	}

	// Configurar origens permitidas
	if len(cfg.Cors.AllowedOrigins) > 0 {
		corsConfig.AllowOrigins = cfg.Cors.AllowedOrigins
	} else {
		corsConfig.AllowOrigins = []string{"*"}
	}

	return cors.New(corsConfig)
}
