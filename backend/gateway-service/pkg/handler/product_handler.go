package handler

import (
	"net/http"
	"strconv"

	"github.com/ecommerce/gateway-service/pkg/service"
	"github.com/gin-gonic/gin"
	"github.com/sirupsen/logrus"
)

// ProductHandler gerencia as requisições relacionadas a produtos
type ProductHandler struct {
	catalogService *service.CatalogService
}

// NewProductHandler cria uma nova instância do handler de produtos
func NewProductHandler(catalogService *service.CatalogService) *ProductHandler {
	return &ProductHandler{
		catalogService: catalogService,
	}
}

// GetAll retorna todos os produtos paginados
func (h *ProductHandler) GetAll(c *gin.Context) {
	// Parâmetros de paginação
	page, err := strconv.Atoi(c.DefaultQuery("page", "0"))
	if err != nil {
		page = 0
	}

	size, err := strconv.Atoi(c.DefaultQuery("size", "10"))
	if err != nil {
		size = 10
	}

	// Limitar o tamanho da página
	if size > 50 {
		size = 50
	}

	// Buscar produtos do serviço de catálogo
	products, total, err := h.catalogService.GetAllProducts(page, size)
	if err != nil {
		logrus.WithError(err).Error("Erro ao obter produtos")
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": "Erro ao obter produtos",
		})
		return
	}

	// Responder com os produtos e informações de paginação
	c.JSON(http.StatusOK, gin.H{
		"content":       products,
		"totalElements": total,
		"page":          page,
		"size":          size,
		"totalPages":    (total + size - 1) / size,
	})
}

// GetByID retorna um produto pelo seu ID
func (h *ProductHandler) GetByID(c *gin.Context) {
	id := c.Param("id")
	if id == "" {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": "ID do produto não fornecido",
		})
		return
	}

	// Buscar produto do serviço de catálogo
	product, err := h.catalogService.GetProductByID(id)
	if err != nil {
		logrus.WithError(err).Error("Erro ao obter produto")

		// Verificar se o produto não foi encontrado
		if err.Error() == "produto não encontrado" {
			c.JSON(http.StatusNotFound, gin.H{
				"error": "Produto não encontrado",
			})
			return
		}

		c.JSON(http.StatusInternalServerError, gin.H{
			"error": "Erro ao obter produto",
		})
		return
	}

	c.JSON(http.StatusOK, product)
}

// GetCategories retorna todas as categorias
func (h *ProductHandler) GetCategories(c *gin.Context) {
	// Buscar categorias do serviço de catálogo
	categories, err := h.catalogService.GetCategories()
	if err != nil {
		logrus.WithError(err).Error("Erro ao obter categorias")
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": "Erro ao obter categorias",
		})
		return
	}

	c.JSON(http.StatusOK, categories)
}

// Search procura produtos por um termo de busca
func (h *ProductHandler) Search(c *gin.Context) {
	query := c.Query("q")
	if query == "" {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": "Termo de busca não fornecido",
		})
		return
	}

	// Parâmetros de paginação
	page, err := strconv.Atoi(c.DefaultQuery("page", "0"))
	if err != nil {
		page = 0
	}

	size, err := strconv.Atoi(c.DefaultQuery("size", "10"))
	if err != nil {
		size = 10
	}

	// Limitar o tamanho da página
	if size > 50 {
		size = 50
	}

	// Buscar produtos do serviço de catálogo
	products, total, err := h.catalogService.SearchProducts(query, page, size)
	if err != nil {
		logrus.WithError(err).Error("Erro ao buscar produtos")
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": "Erro ao buscar produtos",
		})
		return
	}

	// Responder com os produtos e informações de paginação
	c.JSON(http.StatusOK, gin.H{
		"content":       products,
		"totalElements": total,
		"page":          page,
		"size":          size,
		"totalPages":    (total + size - 1) / size,
	})
}
