#!/bin/bash

# Definir cores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}┌──────────────────────────────────────────┐${NC}"
echo -e "${GREEN}│    Iniciando E-Commerce Platform         │${NC}"
echo -e "${GREEN}└──────────────────────────────────────────┘${NC}"

# Verificar se o Docker está em execução
if ! docker info > /dev/null 2>&1; then
    echo -e "${YELLOW}Docker não está em execução. Por favor, inicie o Docker e tente novamente.${NC}"
    exit 1
fi

# Definir diretório raiz do projeto
PROJECT_ROOT=$(cd "$(dirname "$0")/../.." && pwd)
cd "$PROJECT_ROOT"

echo -e "${GREEN}Iniciando os serviços com Docker Compose...${NC}"
docker-compose up -d --build

# Verificar se todos os serviços foram iniciados corretamente
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Todos os serviços foram iniciados com sucesso!${NC}"
    echo -e "${GREEN}📊 Serviços disponíveis:${NC}"
    echo -e "  - Frontend: http://localhost:4200"
    echo -e "  - API Gateway: http://localhost:8080"
    echo -e "  - Serviços Backend:"
    echo -e "    - Catalog: http://localhost:8081"
    echo -e "    - Order: http://localhost:8082"
    echo -e "    - Cart: http://localhost:8083"
    echo -e "    - User: http://localhost:8084"
    echo -e "    - Payment: http://localhost:8085"
    echo -e "    - Inventory: http://localhost:8086"
    echo -e "    - Notification: http://localhost:8087"
else
    echo -e "${YELLOW}⚠️ Houve um problema ao iniciar os serviços. Verifique os logs para mais detalhes.${NC}"
fi

echo -e "${GREEN}Para visualizar os logs dos serviços, execute:${NC}"
echo -e "  docker-compose logs -f [service_name]"

echo -e "${GREEN}Para parar todos os serviços, execute:${NC}"
echo -e "  ./infrastructure/scripts/stop.sh" 