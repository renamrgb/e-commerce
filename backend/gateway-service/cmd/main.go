package main

import (
	"context"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/ecommerce/gateway-service/pkg/config"
	"github.com/ecommerce/gateway-service/pkg/handler"
	"github.com/ecommerce/gateway-service/pkg/middleware"
	"github.com/ecommerce/gateway-service/pkg/router"
	"github.com/gin-gonic/gin"
	"github.com/sirupsen/logrus"
)

func main() {
	// Configurar logger
	logrus.SetFormatter(&logrus.JSONFormatter{})
	logrus.SetOutput(os.Stdout)
	logrus.SetLevel(logrus.InfoLevel)

	// Carregar configurações
	cfg, err := config.LoadConfig()
	if err != nil {
		logrus.Fatalf("Falha ao carregar configurações: %v", err)
	}

	// Inicializar o router Gin
	engine := gin.New()
	engine.Use(gin.Recovery())
	engine.Use(middleware.Logger())
	engine.Use(middleware.Cors(cfg))
	engine.Use(middleware.Metrics())

	// Configurar handlers
	handlers := handler.NewHandlers(cfg)

	// Configurar rotas
	router.SetupRoutes(engine, handlers, cfg)

	// Configurar servidor HTTP
	srv := &http.Server{
		Addr:         ":" + cfg.Server.Port,
		Handler:      engine,
		ReadTimeout:  15 * time.Second,
		WriteTimeout: 15 * time.Second,
		IdleTimeout:  60 * time.Second,
	}

	// Iniciar o servidor em uma goroutine
	go func() {
		logrus.Infof("Servidor iniciado na porta %s", cfg.Server.Port)
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			logrus.Fatalf("Falha ao iniciar servidor: %v", err)
		}
	}()

	// Configurar canal para capturar sinais de interrupção
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit

	// Desligamento gracioso
	logrus.Info("Desligando servidor...")
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	if err := srv.Shutdown(ctx); err != nil {
		logrus.Fatalf("Falha ao desligar servidor: %v", err)
	}

	logrus.Info("Servidor encerrado com sucesso")
}
