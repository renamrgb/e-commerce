package auth

import (
	"errors"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
)

// JWT retorna um middleware para autenticação JWT
func JWT(secretKey string) gin.HandlerFunc {
	return func(c *gin.Context) {
		// Obter token do cabeçalho Authorization
		authHeader := c.GetHeader("Authorization")
		if authHeader == "" {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{
				"error": "Token de autenticação não fornecido",
			})
			return
		}

		// O token deve estar no formato "Bearer {token}"
		tokenParts := strings.Split(authHeader, " ")
		if len(tokenParts) != 2 || tokenParts[0] != "Bearer" {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{
				"error": "Formato de token inválido",
			})
			return
		}

		// Extrair o token
		tokenString := tokenParts[1]

		// Validar o token
		token, err := validateToken(tokenString, secretKey)
		if err != nil {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{
				"error": "Token inválido: " + err.Error(),
			})
			return
		}

		// Verificar se o token é válido
		claims, ok := token.Claims.(jwt.MapClaims)
		if !ok || !token.Valid {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{
				"error": "Token inválido",
			})
			return
		}

		// Adicionar claims ao contexto
		c.Set("user_id", claims["user_id"])
		c.Set("email", claims["email"])
		c.Set("roles", claims["roles"])

		c.Next()
	}
}

// validateToken valida um token JWT
func validateToken(tokenString, secretKey string) (*jwt.Token, error) {
	token, err := jwt.Parse(tokenString, func(token *jwt.Token) (interface{}, error) {
		// Verificar o algoritmo de assinatura
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, errors.New("método de assinatura inválido")
		}
		return []byte(secretKey), nil
	})

	if err != nil {
		return nil, err
	}

	return token, nil
}
