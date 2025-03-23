package service

import (
	"encoding/json"
	"fmt"
	"net/http"
	"time"

	"github.com/ecommerce/gateway-service/pkg/config"
	"github.com/sirupsen/logrus"
)

// Produto representa um produto do catálogo
type Product struct {
	ID          string    `json:"id"`
	Name        string    `json:"name"`
	Description string    `json:"description"`
	Price       float64   `json:"price"`
	ImageURL    string    `json:"imageUrl"`
	Stock       int       `json:"stock"`
	CategoryID  string    `json:"categoryId"`
	Category    *Category `json:"category,omitempty"`
	CreatedAt   time.Time `json:"createdAt"`
	UpdatedAt   time.Time `json:"updatedAt"`
}

// Categoria representa uma categoria de produtos
type Category struct {
	ID          string    `json:"id"`
	Name        string    `json:"name"`
	Description string    `json:"description"`
	CreatedAt   time.Time `json:"createdAt"`
	UpdatedAt   time.Time `json:"updatedAt"`
}

// CatalogService é responsável pela comunicação com o serviço de catálogo
type CatalogService struct {
	baseURL string
	client  *http.Client
}

// NewCatalogService cria uma nova instância do serviço de catálogo
func NewCatalogService(cfg config.ServiceConfig) *CatalogService {
	baseURL := fmt.Sprintf("http://%s:%s", cfg.Host, cfg.Port)

	client := &http.Client{
		Timeout: 10 * time.Second,
	}

	return &CatalogService{
		baseURL: baseURL,
		client:  client,
	}
}

// GetAllProducts retorna todos os produtos do catálogo
func (s *CatalogService) GetAllProducts(page, size int) ([]Product, int, error) {
	url := fmt.Sprintf("%s/api/products?page=%d&size=%d", s.baseURL, page, size)

	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return nil, 0, err
	}

	req.Header.Set("Content-Type", "application/json")

	resp, err := s.client.Do(req)
	if err != nil {
		logrus.WithError(err).Error("Falha ao conectar com o serviço de catálogo")
		return nil, 0, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		logrus.WithField("status", resp.StatusCode).Error("Erro ao obter produtos do catálogo")
		return nil, 0, fmt.Errorf("erro ao obter produtos: código %d", resp.StatusCode)
	}

	var response struct {
		Content       []Product `json:"content"`
		TotalElements int       `json:"totalElements"`
	}

	if err := json.NewDecoder(resp.Body).Decode(&response); err != nil {
		return nil, 0, err
	}

	return response.Content, response.TotalElements, nil
}

// GetProductByID retorna um produto pelo seu ID
func (s *CatalogService) GetProductByID(id string) (*Product, error) {
	url := fmt.Sprintf("%s/api/products/%s", s.baseURL, id)

	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return nil, err
	}

	req.Header.Set("Content-Type", "application/json")

	resp, err := s.client.Do(req)
	if err != nil {
		logrus.WithError(err).Error("Falha ao conectar com o serviço de catálogo")
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode == http.StatusNotFound {
		return nil, fmt.Errorf("produto não encontrado")
	}

	if resp.StatusCode != http.StatusOK {
		logrus.WithField("status", resp.StatusCode).Error("Erro ao obter produto do catálogo")
		return nil, fmt.Errorf("erro ao obter produto: código %d", resp.StatusCode)
	}

	var product Product
	if err := json.NewDecoder(resp.Body).Decode(&product); err != nil {
		return nil, err
	}

	return &product, nil
}

// GetCategories retorna todas as categorias
func (s *CatalogService) GetCategories() ([]Category, error) {
	url := fmt.Sprintf("%s/api/categories", s.baseURL)

	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return nil, err
	}

	req.Header.Set("Content-Type", "application/json")

	resp, err := s.client.Do(req)
	if err != nil {
		logrus.WithError(err).Error("Falha ao conectar com o serviço de catálogo")
		return nil, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		logrus.WithField("status", resp.StatusCode).Error("Erro ao obter categorias do catálogo")
		return nil, fmt.Errorf("erro ao obter categorias: código %d", resp.StatusCode)
	}

	var categories []Category
	if err := json.NewDecoder(resp.Body).Decode(&categories); err != nil {
		return nil, err
	}

	return categories, nil
}

// SearchProducts procura produtos por um termo de busca
func (s *CatalogService) SearchProducts(query string, page, size int) ([]Product, int, error) {
	url := fmt.Sprintf("%s/api/products/search?query=%s&page=%d&size=%d", s.baseURL, query, page, size)

	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return nil, 0, err
	}

	req.Header.Set("Content-Type", "application/json")

	resp, err := s.client.Do(req)
	if err != nil {
		logrus.WithError(err).Error("Falha ao conectar com o serviço de catálogo")
		return nil, 0, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		logrus.WithField("status", resp.StatusCode).Error("Erro ao procurar produtos no catálogo")
		return nil, 0, fmt.Errorf("erro ao procurar produtos: código %d", resp.StatusCode)
	}

	var response struct {
		Content       []Product `json:"content"`
		TotalElements int       `json:"totalElements"`
	}

	if err := json.NewDecoder(resp.Body).Decode(&response); err != nil {
		return nil, 0, err
	}

	return response.Content, response.TotalElements, nil
}
