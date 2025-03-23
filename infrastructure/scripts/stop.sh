#!/bin/bash

# Definir cores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}┌──────────────────────────────────────────┐${NC}"
echo -e "${YELLOW}│    Parando E-Commerce Platform           │${NC}"
echo -e "${YELLOW}└──────────────────────────────────────────┘${NC}"

# Definir diretório raiz do projeto
PROJECT_ROOT=$(cd "$(dirname "$0")/../.." && pwd)
cd "$PROJECT_ROOT"

echo -e "${YELLOW}Parando os serviços do Docker Compose...${NC}"
docker-compose down

# Verificar se todos os serviços foram parados corretamente
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Todos os serviços foram parados com sucesso!${NC}"
else
    echo -e "${YELLOW}⚠️ Houve um problema ao parar os serviços. Verifique os logs para mais detalhes.${NC}"
fi

echo -e "${GREEN}Para iniciar novamente todos os serviços, execute:${NC}"
echo -e "  ./infrastructure/scripts/start.sh" 