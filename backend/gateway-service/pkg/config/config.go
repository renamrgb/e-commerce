package config

import (
	"github.com/sirupsen/logrus"
	"github.com/spf13/viper"
)

// ServiceConfig armazena as configurações para serviços remotos
type ServiceConfig struct {
	Host string
	Port string
}

// Config armazena todas as configurações da aplicação
type Config struct {
	Server struct {
		Port string
	}
	Services struct {
		Catalog      ServiceConfig
		Order        ServiceConfig
		Cart         ServiceConfig
		User         ServiceConfig
		Payment      ServiceConfig
		Inventory    ServiceConfig
		Notification ServiceConfig
	}
	Auth struct {
		JWTSecret   string
		TokenExpiry int // em minutos
	}
	Cors struct {
		AllowedOrigins []string
	}
}

// LoadConfig carrega a configuração do arquivo config.yaml
func LoadConfig() (*Config, error) {
	viper.SetConfigName("config")
	viper.SetConfigType("yaml")
	viper.AddConfigPath(".")
	viper.AddConfigPath("./config")
	viper.AutomaticEnv()

	if err := viper.ReadInConfig(); err != nil {
		logrus.Warnf("Config file not found: %v", err)
	}

	// Configurações padrão
	setDefaults()

	var config Config
	if err := viper.Unmarshal(&config); err != nil {
		return nil, err
	}

	return &config, nil
}

// setDefaults define valores padrão para configurações
func setDefaults() {
	viper.SetDefault("server.port", "8080")

	// Configurações padrão para serviços
	viper.SetDefault("services.catalog.host", "catalog")
	viper.SetDefault("services.catalog.port", "8081")

	viper.SetDefault("services.order.host", "order")
	viper.SetDefault("services.order.port", "8082")

	viper.SetDefault("services.cart.host", "cart")
	viper.SetDefault("services.cart.port", "8083")

	viper.SetDefault("services.user.host", "user")
	viper.SetDefault("services.user.port", "8084")

	viper.SetDefault("services.payment.host", "payment")
	viper.SetDefault("services.payment.port", "8085")

	viper.SetDefault("services.inventory.host", "inventory")
	viper.SetDefault("services.inventory.port", "8086")

	viper.SetDefault("services.notification.host", "notification")
	viper.SetDefault("services.notification.port", "8087")

	// Configurações de autenticação
	viper.SetDefault("auth.jwtsecret", "your-secret-key")
	viper.SetDefault("auth.tokenExpiry", 60) // 60 minutos

	// Configurações CORS
	viper.SetDefault("cors.allowedOrigins", []string{"*"})
} 