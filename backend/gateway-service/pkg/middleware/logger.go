package middleware

import (
	"time"

	"github.com/gin-gonic/gin"
	"github.com/sirupsen/logrus"
)

// Logger retorna um middleware para logging de requisições HTTP
func Logger() gin.HandlerFunc {
	return func(c *gin.Context) {
		// Tempo de início
		startTime := time.Now()

		// Processar requisição
		c.Next()

		// Tempo de término
		endTime := time.Now()
		// Tempo de execução em milissegundos
		latency := endTime.Sub(startTime).Milliseconds()

		// Informações de acesso
		statusCode := c.Writer.Status()
		clientIP := c.ClientIP()
		method := c.Request.Method
		path := c.Request.URL.Path
		userAgent := c.Request.UserAgent()

		// Campos para o log
		fields := logrus.Fields{
			"status_code": statusCode,
			"latency_ms":  latency,
			"client_ip":   clientIP,
			"method":      method,
			"path":        path,
			"user_agent":  userAgent,
		}

		entry := logrus.WithFields(fields)

		if c.Errors.Len() > 0 {
			// Registrar erros
			entry.Error(c.Errors.String())
		} else if statusCode >= 500 {
			// Erro do servidor
			entry.Error("Erro do servidor")
		} else if statusCode >= 400 {
			// Erro do cliente
			entry.Warn("Erro do cliente")
		} else {
			// Sucesso
			entry.Info("Requisição concluída")
		}
	}
}
